
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: vector; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS vector WITH SCHEMA public;

--
-- Name: EXTENSION vector; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION vector IS 'vector data type and ivfflat and hnsw access methods';

--
-- Name: bot_persona_style; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.bot_persona_style AS ENUM (
    'PROFESSIONAL',
    'FRIENDLY',
    'ENTHUSIASTIC',
    'HUMOROUS',
    'FORMAL'
);

--
-- Name: llm_provider_type; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.llm_provider_type AS ENUM (
    'OPENAI',
    'CLAUDE',
    'GEMINI',
    'OLLAMA'
);

--
-- Name: archive_old_messages(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.archive_old_messages(days_to_keep integer DEFAULT 90) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
    archived_count INTEGER;
BEGIN
    -- Move messages older than days_to_keep to archive table
    INSERT INTO messages_archive (id, tenant_id, conversation_id, sender, content, raw_payload, 
                                   message_type, external_message_id, is_read, sent_time, created_at, archived_at)
    SELECT id, tenant_id, conversation_id, sender, content, raw_payload, 
           message_type, external_message_id, is_read, sent_time, created_at, CURRENT_TIMESTAMP
    FROM messages
    WHERE created_at < CURRENT_DATE - (days_to_keep || ' days')::INTERVAL;
    
    GET DIAGNOSTICS archived_count = ROW_COUNT;
    
    -- Delete archived messages from main table
    DELETE FROM messages
    WHERE created_at < CURRENT_DATE - (days_to_keep || ' days')::INTERVAL;
    
    RETURN archived_count;
END;
$$;

--
-- Name: create_monthly_partitions(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.create_monthly_partitions() RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
    start_date DATE;
    end_date DATE;
    partition_name TEXT;
    i INTEGER;
BEGIN
    -- Create partitions for last 3 months
    FOR i IN -3..12 LOOP
        start_date := date_trunc('month', CURRENT_DATE + (i || ' months')::INTERVAL);
        end_date := start_date + INTERVAL '1 month';
        partition_name := 'messages_' || to_char(start_date, 'YYYY_MM');
        
        EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF messages FOR VALUES FROM (%L) TO (%L)',
                      partition_name, start_date, end_date);
        
        -- Create indexes on partition
        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_tenant ON %I(tenant_id)', 
                      partition_name, partition_name);
        EXECUTE format('CREATE INDEX IF NOT EXISTS idx_%s_conversation ON %I(conversation_id)', 
                      partition_name, partition_name);
    END LOOP;
END;
$$;

--
-- Name: ensure_future_partitions(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.ensure_future_partitions() RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Ensure partitions exist for next 3 months
    PERFORM create_monthly_partitions();
END;
$$;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: agent_workload; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agent_workload (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    agent_id bigint NOT NULL,
    team_id bigint,
    active_conversations integer DEFAULT 0 NOT NULL,
    max_concurrent_conversations integer DEFAULT 10 NOT NULL,
    total_conversations_today integer DEFAULT 0 NOT NULL,
    total_conversations_this_week integer DEFAULT 0 NOT NULL,
    average_response_time_seconds integer,
    last_activity_at timestamp without time zone,
    status character varying(20) DEFAULT 'AVAILABLE'::character varying NOT NULL,
    priority_score integer DEFAULT 50 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_agent_workload_conversations CHECK ((active_conversations >= 0)),
    CONSTRAINT chk_agent_workload_max_concurrent CHECK ((max_concurrent_conversations > 0)),
    CONSTRAINT chk_agent_workload_status CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'BUSY'::character varying, 'OFFLINE'::character varying, 'AWAY'::character varying])::text[])))
);

--
-- Name: TABLE agent_workload; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.agent_workload IS 'Tracks agent workload, availability, and capacity for intelligent routing';

--
-- Name: agent_workload_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.agent_workload_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: agent_workload_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.agent_workload_id_seq OWNED BY public.agent_workload.id;

--
-- Name: agents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agents (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    user_id bigint,
    name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    role character varying(50) DEFAULT 'AGENT'::character varying NOT NULL,
    status character varying(50) DEFAULT 'OFFLINE'::character varying NOT NULL,
    current_load integer DEFAULT 0 NOT NULL,
    max_concurrent_conversations integer DEFAULT 10 NOT NULL,
    skills jsonb,
    assignment_preferences jsonb,
    active boolean DEFAULT true NOT NULL,
    bio text,
    phone_number character varying(50),
    avatar_url text,
    last_activity_at timestamp without time zone,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: agents_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.agents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: agents_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.agents_id_seq OWNED BY public.agents.id;

--
-- Name: ai_suggestions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ai_suggestions (
    id bigint NOT NULL,
    tenant_id bigint,
    tenant_key character varying(255),
    conversation_id bigint NOT NULL,
    agent_id bigint,
    suggestion_text text NOT NULL,
    confidence double precision,
    provider character varying(50),
    model character varying(100),
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    response_note text,
    responded_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL,
    CONSTRAINT chk_ai_suggestion_confidence CHECK (((confidence IS NULL) OR ((confidence >= (0)::double precision) AND (confidence <= (1)::double precision)))),
    CONSTRAINT chk_ai_suggestion_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'EXPIRED'::character varying])::text[])))
);

--
-- Name: ai_suggestions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ai_suggestions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: ai_suggestions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ai_suggestions_id_seq OWNED BY public.ai_suggestions.id;

--
-- Name: analytics_daily_summary; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.analytics_daily_summary (
    id bigint NOT NULL,
    tenant_id bigint,
    tenant_key character varying(255),
    summary_date date NOT NULL,
    total_conversations integer DEFAULT 0 NOT NULL,
    new_conversations integer DEFAULT 0 NOT NULL,
    closed_conversations integer DEFAULT 0 NOT NULL,
    total_messages integer DEFAULT 0 NOT NULL,
    user_messages integer DEFAULT 0 NOT NULL,
    bot_messages integer DEFAULT 0 NOT NULL,
    agent_messages integer DEFAULT 0 NOT NULL,
    escalations_count integer DEFAULT 0 NOT NULL,
    avg_first_response_minutes double precision,
    avg_resolution_minutes double precision,
    avg_sentiment_score double precision,
    negative_sentiment_count integer DEFAULT 0 NOT NULL,
    ai_suggestions_count integer DEFAULT 0 NOT NULL,
    ai_suggestions_accepted integer DEFAULT 0 NOT NULL,
    ai_suggestions_rejected integer DEFAULT 0 NOT NULL,
    metrics jsonb,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: analytics_daily_summary_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.analytics_daily_summary_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: analytics_daily_summary_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.analytics_daily_summary_id_seq OWNED BY public.analytics_daily_summary.id;

--
-- Name: auto_assign_configs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.auto_assign_configs (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    interval_seconds integer DEFAULT 30 NOT NULL,
    max_concurrent_per_agent integer DEFAULT 5 NOT NULL,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: TABLE auto_assign_configs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.auto_assign_configs IS 'Auto-assignment configuration for tenant-specific agent assignment';

--
-- Name: COLUMN auto_assign_configs.enabled; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.auto_assign_configs.enabled IS 'Whether auto-assignment is enabled';

--
-- Name: COLUMN auto_assign_configs.interval_seconds; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.auto_assign_configs.interval_seconds IS 'Auto-assignment interval in seconds';

--
-- Name: COLUMN auto_assign_configs.max_concurrent_per_agent; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.auto_assign_configs.max_concurrent_per_agent IS 'Maximum concurrent conversations per agent';

--
-- Name: auto_assign_configs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.auto_assign_configs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: auto_assign_configs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.auto_assign_configs_id_seq OWNED BY public.auto_assign_configs.id;

--
-- Name: bot_webhook_dead_letters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bot_webhook_dead_letters (
    id bigint NOT NULL,
    rule_id uuid NOT NULL,
    bot_id uuid NOT NULL,
    rule_name character varying(200),
    webhook_url character varying(500) NOT NULL,
    payload text,
    attempts integer NOT NULL,
    last_error text,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: bot_webhook_dead_letters_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.bot_webhook_dead_letters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: bot_webhook_dead_letters_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.bot_webhook_dead_letters_id_seq OWNED BY public.bot_webhook_dead_letters.id;

--
-- Name: conversation_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversation_events (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    tenant_key character varying(255),
    conversation_id bigint NOT NULL,
    event_type character varying(64) NOT NULL,
    actor_type character varying(16),
    actor_id character varying(128),
    from_value character varying(255),
    to_value character varying(255),
    metadata jsonb,
    occurred_at timestamp without time zone NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255)
);

--
-- Name: conversation_events_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.conversation_events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: conversation_events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.conversation_events_id_seq OWNED BY public.conversation_events.id;

--
-- Name: conversation_sentiment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversation_sentiment (
    id bigint NOT NULL,
    tenant_id bigint,
    tenant_key character varying(255),
    conversation_id bigint NOT NULL,
    message_id bigint,
    score double precision NOT NULL,
    label character varying(20) NOT NULL,
    source character varying(20) DEFAULT 'LEXICON'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL,
    CONSTRAINT chk_sentiment_label CHECK (((label)::text = ANY ((ARRAY['POSITIVE'::character varying, 'NEUTRAL'::character varying, 'NEGATIVE'::character varying])::text[]))),
    CONSTRAINT chk_sentiment_score CHECK (((score >= ('-1'::integer)::double precision) AND (score <= (1)::double precision))),
    CONSTRAINT chk_sentiment_source CHECK (((source)::text = ANY ((ARRAY['AI'::character varying, 'LEXICON'::character varying])::text[])))
);

--
-- Name: conversation_sentiment_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.conversation_sentiment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: conversation_sentiment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.conversation_sentiment_id_seq OWNED BY public.conversation_sentiment.id;

--
-- Name: conversation_transfers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversation_transfers (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    from_agent_id bigint NOT NULL,
    to_agent_id bigint NOT NULL,
    transfer_reason text,
    transfer_type character varying(50) NOT NULL,
    status character varying(50) NOT NULL,
    notes text,
    transferred_by bigint,
    accepted_at timestamp without time zone,
    rejected_at timestamp without time zone,
    completed_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_transfer_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[]))),
    CONSTRAINT chk_transfer_type CHECK (((transfer_type)::text = ANY ((ARRAY['MANUAL'::character varying, 'AUTO_ESCALATION'::character varying, 'WORKLOAD_BALANCE'::character varying, 'SKILL_MISMATCH'::character varying, 'SUPERVISOR_REQUEST'::character varying])::text[])))
);

--
-- Name: TABLE conversation_transfers; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.conversation_transfers IS 'History of conversation transfers between agents';

--
-- Name: conversation_transfers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.conversation_transfers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: conversation_transfers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.conversation_transfers_id_seq OWNED BY public.conversation_transfers.id;

--
-- Name: conversations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversations (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    connection_id uuid,
    owner_id character varying(255),
    external_user_id character varying(255),
    user_name character varying(255),
    user_avatar text,
    status character varying(50) DEFAULT 'open'::character varying NOT NULL,
    channel character varying(50),
    last_message_id bigint,
    agent_assigned_id bigint,
    is_closed_by_agent boolean DEFAULT false NOT NULL,
    is_taken_over_by_agent boolean DEFAULT false NOT NULL,
    tags jsonb,
    customer_tier character varying(50),
    language character varying(10),
    custom_attributes jsonb,
    queue_name character varying(100),
    custom_action character varying(100),
    custom_action_data jsonb,
    first_agent_response_time timestamp without time zone,
    first_bot_response_time timestamp without time zone,
    sla_breach_count integer DEFAULT 0 NOT NULL,
    expected_response_time bigint,
    required_skills jsonb,
    current_escalation_tier integer,
    last_escalated_at timestamp without time zone,
    summary text,
    user_satisfaction_rating integer,
    resolution_status character varying(50),
    resolution_time timestamp without time zone,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: COLUMN conversations.user_satisfaction_rating; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.conversations.user_satisfaction_rating IS 'User satisfaction rating from 1-5 based on feedback';

--
-- Name: COLUMN conversations.resolution_status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.conversations.resolution_status IS 'Resolution status: resolved, unresolved, pending';

--
-- Name: COLUMN conversations.resolution_time; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.conversations.resolution_time IS 'Timestamp when conversation was resolved';

--
-- Name: conversations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.conversations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: conversations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.conversations_id_seq OWNED BY public.conversations.id;

--
-- Name: custom_report; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.custom_report (
    id bigint NOT NULL,
    tenant_id bigint,
    tenant_key character varying(255),
    name character varying(150) NOT NULL,
    description character varying(500),
    report_type character varying(20) NOT NULL,
    date_range_days integer DEFAULT 30 NOT NULL,
    filters jsonb,
    last_generated_at timestamp without time zone,
    last_result jsonb,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: custom_report_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.custom_report_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: custom_report_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.custom_report_id_seq OWNED BY public.custom_report.id;

--
-- Name: escalation_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.escalation_history (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    escalated_to_agent_id bigint,
    escalated_to_team_id bigint,
    escalation_rule_id bigint,
    escalation_trigger_type character varying(50) NOT NULL,
    escalation_action character varying(50) NOT NULL,
    escalated_to_supervisor boolean,
    supervisor_notified boolean,
    escalated boolean NOT NULL,
    reason text,
    metadata jsonb,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_escalation_action CHECK (((escalation_action)::text = ANY ((ARRAY['ASSIGN_TO_AGENT'::character varying, 'ASSIGN_TO_TEAM'::character varying, 'ASSIGN_TO_SUPERVISOR'::character varying, 'MOVE_TO_QUEUE'::character varying, 'SEND_NOTIFICATION'::character varying])::text[]))),
    CONSTRAINT chk_escalation_trigger_type CHECK (((escalation_trigger_type)::text = ANY ((ARRAY['WAIT_TIME_EXCEEDED'::character varying, 'NO_RESPONSE'::character varying, 'LOW_CUSTOMER_SATISFACTION'::character varying, 'HIGH_PRIORITY_CUSTOMER'::character varying, 'CUSTOMER_TIER'::character varying, 'CUSTOM_CONDITION'::character varying])::text[])))
);

--
-- Name: TABLE escalation_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.escalation_history IS 'History of escalation events for audit and analysis';

--
-- Name: escalation_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.escalation_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: escalation_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.escalation_history_id_seq OWNED BY public.escalation_history.id;

--
-- Name: escalation_tiers; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.escalation_tiers (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    level integer NOT NULL,
    name character varying(255) NOT NULL,
    timeout_seconds bigint NOT NULL,
    active boolean DEFAULT true NOT NULL,
    description text,
    required_role character varying(50),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: escalation_tiers_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.escalation_tiers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: escalation_tiers_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.escalation_tiers_id_seq OWNED BY public.escalation_tiers.id;

--
-- Name: funnel_stage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.funnel_stage (
    id bigint NOT NULL,
    tenant_id bigint,
    tenant_key character varying(255),
    name character varying(100) NOT NULL,
    stage_order integer NOT NULL,
    match_type character varying(20) NOT NULL,
    match_value character varying(50),
    active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: funnel_stage_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.funnel_stage_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: funnel_stage_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.funnel_stage_id_seq OWNED BY public.funnel_stage.id;

--
-- Name: messages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
)
PARTITION BY RANGE (created_at);

--
-- Name: COLUMN messages.is_read; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.messages.is_read IS 'Indicates whether the message has been read by the recipient';

--
-- Name: messages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: messages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.messages_id_seq OWNED BY public.messages.id;

--
-- Name: messages_2026_08; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2026_08 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2026_09; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2026_09 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2026_10; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2026_10 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2026_11; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2026_11 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2026_12; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2026_12 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_01; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_01 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_02; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_02 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_03; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_03 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_04; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_04 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_05; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_05 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_06; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_06 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_07; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_07 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_08; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_08 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_09; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_09 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_10; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_10 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_2027_11; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_2027_11 (
    id bigint DEFAULT nextval('public.messages_id_seq'::regclass) NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_key character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: messages_archive; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.messages_archive (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    sender character varying(50) NOT NULL,
    content text NOT NULL,
    raw_payload jsonb,
    message_type character varying(50) NOT NULL,
    external_message_id character varying(255),
    is_read boolean DEFAULT false NOT NULL,
    sent_time timestamp without time zone,
    created_at timestamp without time zone NOT NULL,
    archived_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    archive_reason character varying(100) DEFAULT 'retention_policy'::character varying
);

--
-- Name: messages_archive_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.messages_archive_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: messages_archive_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.messages_archive_id_seq OWNED BY public.messages_archive.id;

--
-- Name: penny_analytics_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_analytics_events (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bot_id uuid,
    event_type character varying(100) NOT NULL,
    "timestamp" timestamp with time zone DEFAULT now() NOT NULL,
    data jsonb,
    user_id character varying(255),
    platform character varying(100),
    intent character varying(255),
    provider_used character varying(100),
    processing_time_ms bigint,
    has_error boolean,
    error_type character varying(255),
    created_at timestamp with time zone DEFAULT now() NOT NULL
);

--
-- Name: penny_bot_api_keys; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_bot_api_keys (
    id uuid NOT NULL,
    bot_id uuid NOT NULL,
    tenant_id bigint NOT NULL,
    key_hash character varying(64) NOT NULL,
    key_prefix character varying(16) NOT NULL,
    name character varying(100),
    is_active boolean DEFAULT true NOT NULL,
    expires_at timestamp without time zone,
    last_used_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255)
);

--
-- Name: penny_bot_rules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_bot_rules (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bot_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    condition text NOT NULL,
    action text NOT NULL,
    priority integer DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    rule_type character varying(50) DEFAULT 'RESPONSE'::character varying NOT NULL,
    trigger_type character varying(50) DEFAULT 'INTENT'::character varying NOT NULL,
    trigger_value character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255) NOT NULL,
    updated_by character varying(255),
    execution_count bigint DEFAULT 0,
    last_executed_at timestamp without time zone,
    metadata text,
    deleted_at timestamp without time zone,
    deleted_by character varying(255)
);

--
-- Name: COLUMN penny_bot_rules.deleted_at; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.penny_bot_rules.deleted_at IS 'Soft-delete timestamp; NULL means the rule is not deleted';

--
-- Name: COLUMN penny_bot_rules.deleted_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.penny_bot_rules.deleted_by IS 'Email/ID of the user who performed the soft delete';

--
-- Name: penny_bots; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_bots (
    id uuid NOT NULL,
    tenant_key character varying(255),
    bot_name character varying(255) NOT NULL,
    bot_type character varying(50) NOT NULL,
    tenant_id bigint NOT NULL,
    owner_id character varying(255) NOT NULL,
    penny_bot_id character varying(255) NOT NULL,
    description text,
    is_active boolean DEFAULT true NOT NULL,
    is_enabled boolean DEFAULT true NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_used_at timestamp without time zone,
    configuration text,
    system_prompt text,
    business_name character varying(255),
    business_description text,
    confidence_threshold double precision DEFAULT 0.6,
    is_deleted boolean DEFAULT false NOT NULL,
    provider_type character varying(50) DEFAULT 'OPENAI'::character varying,
    model_name character varying(100) DEFAULT 'gpt-4o-mini'::character varying,
    temperature double precision DEFAULT 0.7,
    persona_style character varying(50) DEFAULT 'PROFESSIONAL'::character varying,
    custom_instructions text,
    greeting_message text,
    fallback_message text,
    fallback_provider_type character varying(20),
    CONSTRAINT chk_bot_type CHECK (((bot_type)::text = ANY ((ARRAY['GENERAL'::character varying, 'SUPPORT'::character varying, 'BUSINESS'::character varying, 'BOTPRESS'::character varying])::text[])))
);

--
-- Name: COLUMN penny_bots.fallback_provider_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.penny_bots.fallback_provider_type IS 'Optional fallback LLM provider (OPENAI, CLAUDE, GEMINI, OLLAMA). NULL = use default fallback order.';

--
-- Name: penny_conversation_contexts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_conversation_contexts (
    id uuid NOT NULL,
    tenant_id bigint NOT NULL,
    bot_id character varying(64) NOT NULL,
    user_id character varying(255),
    platform character varying(64),
    context_key character varying(512) NOT NULL,
    status character varying(32),
    context_data text,
    message_count integer,
    last_intent character varying(255),
    created_at timestamp without time zone NOT NULL,
    last_updated timestamp without time zone NOT NULL,
    expires_at timestamp without time zone
);

--
-- Name: penny_escalation_tickets; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_escalation_tickets (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bot_id uuid,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    user_id character varying(255) NOT NULL,
    conversation_id character varying(255),
    reason text,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    assigned_agent_id character varying(255),
    priority character varying(20) DEFAULT 'NORMAL'::character varying,
    confidence_score double precision,
    metadata jsonb,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now(),
    resolved_at timestamp without time zone,
    resolution_notes text,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: penny_knowledge_articles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_knowledge_articles (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bot_id uuid NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    title character varying(500) NOT NULL,
    content text NOT NULL,
    category character varying(100),
    tags character varying(500),
    embedding public.vector(1536),
    embedding_model character varying(100),
    source_url character varying(1000),
    is_active boolean DEFAULT true,
    priority integer DEFAULT 0,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: penny_knowledge_chunks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_knowledge_chunks (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    document_id uuid NOT NULL,
    bot_id uuid NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    chunk_index integer NOT NULL,
    chunk_text text NOT NULL,
    chunk_tokens integer DEFAULT 0,
    embedding public.vector(1536),
    embedding_model character varying(100),
    page_number integer,
    sheet_name character varying(255),
    row_index integer,
    metadata jsonb,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now(),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: penny_knowledge_documents; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_knowledge_documents (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bot_id uuid NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    document_name character varying(500) NOT NULL,
    file_name character varying(500) NOT NULL,
    file_type character varying(50) NOT NULL,
    file_size bigint NOT NULL,
    file_path character varying(1000),
    file_url character varying(1000),
    status character varying(50) DEFAULT 'PROCESSING'::character varying,
    total_pages integer DEFAULT 0,
    total_chunks integer DEFAULT 0,
    metadata jsonb,
    uploaded_by character varying(255),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now(),
    updated_at timestamp without time zone DEFAULT now(),
    processed_at timestamp without time zone,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: penny_response_templates; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.penny_response_templates (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    bot_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    intent character varying(255) NOT NULL,
    template_text text NOT NULL,
    template_type character varying(50) DEFAULT 'TEXT'::character varying NOT NULL,
    language character varying(10) DEFAULT 'vi'::character varying NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    priority integer DEFAULT 0 NOT NULL,
    variables text,
    quick_replies text,
    attachments text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255) NOT NULL,
    updated_by character varying(255),
    usage_count bigint DEFAULT 0,
    last_used_at timestamp without time zone
);

--
-- Name: report_schedule; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.report_schedule (
    id bigint NOT NULL,
    tenant_id bigint,
    tenant_key character varying(255),
    report_id bigint NOT NULL,
    cron_expression character varying(50) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    next_run_at timestamp without time zone,
    last_run_at timestamp without time zone,
    last_run_status character varying(20),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: report_schedule_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.report_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: report_schedule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.report_schedule_id_seq OWNED BY public.report_schedule.id;

--
-- Name: routing_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.routing_history (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    assigned_agent_id bigint,
    assigned_team_id bigint,
    routing_rule_id bigint,
    routing_strategy character varying(100) NOT NULL,
    conditions text,
    assigned boolean DEFAULT false NOT NULL,
    reason text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: TABLE routing_history; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.routing_history IS 'History of routing decisions for audit and analysis';

--
-- Name: routing_history_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.routing_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: routing_history_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.routing_history_id_seq OWNED BY public.routing_history.id;

--
-- Name: skills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.skills (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    category character varying(50) DEFAULT 'SOFT_SKILL'::character varying NOT NULL,
    description text,
    active boolean DEFAULT true NOT NULL,
    level character varying(50),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: skills_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.skills_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: skills_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.skills_id_seq OWNED BY public.skills.id;

--
-- Name: sla_configurations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sla_configurations (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    customer_tier character varying(100) NOT NULL,
    expected_response_time bigint NOT NULL,
    max_breach_count integer DEFAULT 3 NOT NULL,
    active boolean DEFAULT true NOT NULL,
    description text,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: sla_configurations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.sla_configurations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: sla_configurations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.sla_configurations_id_seq OWNED BY public.sla_configurations.id;

--
-- Name: team_workload; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.team_workload (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    team_id bigint NOT NULL,
    active_conversations integer DEFAULT 0 NOT NULL,
    max_concurrent_conversations integer DEFAULT 50 NOT NULL,
    total_conversations_today integer DEFAULT 0 NOT NULL,
    total_conversations_this_week integer DEFAULT 0 NOT NULL,
    average_response_time_seconds integer,
    last_activity_at timestamp without time zone,
    status character varying(20) DEFAULT 'AVAILABLE'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_team_workload_conversations CHECK ((active_conversations >= 0)),
    CONSTRAINT chk_team_workload_max_concurrent CHECK ((max_concurrent_conversations > 0)),
    CONSTRAINT chk_team_workload_status CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'BUSY'::character varying, 'OFFLINE'::character varying, 'AWAY'::character varying])::text[])))
);

--
-- Name: TABLE team_workload; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.team_workload IS 'Tracks team workload and capacity for intelligent routing';

--
-- Name: team_workload_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.team_workload_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: team_workload_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.team_workload_id_seq OWNED BY public.team_workload.id;

--
-- Name: messages_2026_08; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2026_08 FOR VALUES FROM ('2026-08-01 00:00:00') TO ('2026-09-01 00:00:00');

--
-- Name: messages_2026_09; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2026_09 FOR VALUES FROM ('2026-09-01 00:00:00') TO ('2026-10-01 00:00:00');

--
-- Name: messages_2026_10; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2026_10 FOR VALUES FROM ('2026-10-01 00:00:00') TO ('2026-11-01 00:00:00');

--
-- Name: messages_2026_11; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2026_11 FOR VALUES FROM ('2026-11-01 00:00:00') TO ('2026-12-01 00:00:00');

--
-- Name: messages_2026_12; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2026_12 FOR VALUES FROM ('2026-12-01 00:00:00') TO ('2027-01-01 00:00:00');

--
-- Name: messages_2027_01; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_01 FOR VALUES FROM ('2027-01-01 00:00:00') TO ('2027-02-01 00:00:00');

--
-- Name: messages_2027_02; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_02 FOR VALUES FROM ('2027-02-01 00:00:00') TO ('2027-03-01 00:00:00');

--
-- Name: messages_2027_03; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_03 FOR VALUES FROM ('2027-03-01 00:00:00') TO ('2027-04-01 00:00:00');

--
-- Name: messages_2027_04; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_04 FOR VALUES FROM ('2027-04-01 00:00:00') TO ('2027-05-01 00:00:00');

--
-- Name: messages_2027_05; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_05 FOR VALUES FROM ('2027-05-01 00:00:00') TO ('2027-06-01 00:00:00');

--
-- Name: messages_2027_06; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_06 FOR VALUES FROM ('2027-06-01 00:00:00') TO ('2027-07-01 00:00:00');

--
-- Name: messages_2027_07; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_07 FOR VALUES FROM ('2027-07-01 00:00:00') TO ('2027-08-01 00:00:00');

--
-- Name: messages_2027_08; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_08 FOR VALUES FROM ('2027-08-01 00:00:00') TO ('2027-09-01 00:00:00');

--
-- Name: messages_2027_09; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_09 FOR VALUES FROM ('2027-09-01 00:00:00') TO ('2027-10-01 00:00:00');

--
-- Name: messages_2027_10; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_10 FOR VALUES FROM ('2027-10-01 00:00:00') TO ('2027-11-01 00:00:00');

--
-- Name: messages_2027_11; Type: TABLE ATTACH; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ATTACH PARTITION public.messages_2027_11 FOR VALUES FROM ('2027-11-01 00:00:00') TO ('2027-12-01 00:00:00');

--
-- Name: agent_workload id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_workload ALTER COLUMN id SET DEFAULT nextval('public.agent_workload_id_seq'::regclass);

--
-- Name: agents id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agents ALTER COLUMN id SET DEFAULT nextval('public.agents_id_seq'::regclass);

--
-- Name: ai_suggestions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_suggestions ALTER COLUMN id SET DEFAULT nextval('public.ai_suggestions_id_seq'::regclass);

--
-- Name: analytics_daily_summary id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.analytics_daily_summary ALTER COLUMN id SET DEFAULT nextval('public.analytics_daily_summary_id_seq'::regclass);

--
-- Name: auto_assign_configs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auto_assign_configs ALTER COLUMN id SET DEFAULT nextval('public.auto_assign_configs_id_seq'::regclass);

--
-- Name: bot_webhook_dead_letters id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bot_webhook_dead_letters ALTER COLUMN id SET DEFAULT nextval('public.bot_webhook_dead_letters_id_seq'::regclass);

--
-- Name: conversation_events id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_events ALTER COLUMN id SET DEFAULT nextval('public.conversation_events_id_seq'::regclass);

--
-- Name: conversation_sentiment id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_sentiment ALTER COLUMN id SET DEFAULT nextval('public.conversation_sentiment_id_seq'::regclass);

--
-- Name: conversation_transfers id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_transfers ALTER COLUMN id SET DEFAULT nextval('public.conversation_transfers_id_seq'::regclass);

--
-- Name: conversations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations ALTER COLUMN id SET DEFAULT nextval('public.conversations_id_seq'::regclass);

--
-- Name: custom_report id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_report ALTER COLUMN id SET DEFAULT nextval('public.custom_report_id_seq'::regclass);

--
-- Name: escalation_history id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escalation_history ALTER COLUMN id SET DEFAULT nextval('public.escalation_history_id_seq'::regclass);

--
-- Name: escalation_tiers id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escalation_tiers ALTER COLUMN id SET DEFAULT nextval('public.escalation_tiers_id_seq'::regclass);

--
-- Name: funnel_stage id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funnel_stage ALTER COLUMN id SET DEFAULT nextval('public.funnel_stage_id_seq'::regclass);

--
-- Name: messages id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages ALTER COLUMN id SET DEFAULT nextval('public.messages_id_seq'::regclass);

--
-- Name: messages_archive id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_archive ALTER COLUMN id SET DEFAULT nextval('public.messages_archive_id_seq'::regclass);

--
-- Name: report_schedule id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_schedule ALTER COLUMN id SET DEFAULT nextval('public.report_schedule_id_seq'::regclass);

--
-- Name: routing_history id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routing_history ALTER COLUMN id SET DEFAULT nextval('public.routing_history_id_seq'::regclass);

--
-- Name: skills id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills ALTER COLUMN id SET DEFAULT nextval('public.skills_id_seq'::regclass);

--
-- Name: sla_configurations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sla_configurations ALTER COLUMN id SET DEFAULT nextval('public.sla_configurations_id_seq'::regclass);

--
-- Name: team_workload id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team_workload ALTER COLUMN id SET DEFAULT nextval('public.team_workload_id_seq'::regclass);

--
-- Name: agent_workload agent_workload_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_workload
    ADD CONSTRAINT agent_workload_pkey PRIMARY KEY (id);

--
-- Name: agents agents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agents
    ADD CONSTRAINT agents_pkey PRIMARY KEY (id);

--
-- Name: ai_suggestions ai_suggestions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ai_suggestions
    ADD CONSTRAINT ai_suggestions_pkey PRIMARY KEY (id);

--
-- Name: analytics_daily_summary analytics_daily_summary_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.analytics_daily_summary
    ADD CONSTRAINT analytics_daily_summary_pkey PRIMARY KEY (id);

--
-- Name: auto_assign_configs auto_assign_configs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.auto_assign_configs
    ADD CONSTRAINT auto_assign_configs_pkey PRIMARY KEY (id);

--
-- Name: bot_webhook_dead_letters bot_webhook_dead_letters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bot_webhook_dead_letters
    ADD CONSTRAINT bot_webhook_dead_letters_pkey PRIMARY KEY (id);

--
-- Name: conversation_events conversation_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_events
    ADD CONSTRAINT conversation_events_pkey PRIMARY KEY (id);

--
-- Name: conversation_sentiment conversation_sentiment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_sentiment
    ADD CONSTRAINT conversation_sentiment_pkey PRIMARY KEY (id);

--
-- Name: conversation_transfers conversation_transfers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_transfers
    ADD CONSTRAINT conversation_transfers_pkey PRIMARY KEY (id);

--
-- Name: conversations conversations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversations
    ADD CONSTRAINT conversations_pkey PRIMARY KEY (id);

--
-- Name: custom_report custom_report_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.custom_report
    ADD CONSTRAINT custom_report_pkey PRIMARY KEY (id);

--
-- Name: escalation_history escalation_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escalation_history
    ADD CONSTRAINT escalation_history_pkey PRIMARY KEY (id);

--
-- Name: escalation_tiers escalation_tiers_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escalation_tiers
    ADD CONSTRAINT escalation_tiers_pkey PRIMARY KEY (id);

--
-- Name: funnel_stage funnel_stage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.funnel_stage
    ADD CONSTRAINT funnel_stage_pkey PRIMARY KEY (id);

--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2026_08 messages_2026_08_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2026_08
    ADD CONSTRAINT messages_2026_08_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2026_09 messages_2026_09_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2026_09
    ADD CONSTRAINT messages_2026_09_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2026_10 messages_2026_10_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2026_10
    ADD CONSTRAINT messages_2026_10_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2026_11 messages_2026_11_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2026_11
    ADD CONSTRAINT messages_2026_11_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2026_12 messages_2026_12_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2026_12
    ADD CONSTRAINT messages_2026_12_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_01 messages_2027_01_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_01
    ADD CONSTRAINT messages_2027_01_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_02 messages_2027_02_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_02
    ADD CONSTRAINT messages_2027_02_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_03 messages_2027_03_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_03
    ADD CONSTRAINT messages_2027_03_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_04 messages_2027_04_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_04
    ADD CONSTRAINT messages_2027_04_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_05 messages_2027_05_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_05
    ADD CONSTRAINT messages_2027_05_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_06 messages_2027_06_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_06
    ADD CONSTRAINT messages_2027_06_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_07 messages_2027_07_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_07
    ADD CONSTRAINT messages_2027_07_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_08 messages_2027_08_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_08
    ADD CONSTRAINT messages_2027_08_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_09 messages_2027_09_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_09
    ADD CONSTRAINT messages_2027_09_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_10 messages_2027_10_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_10
    ADD CONSTRAINT messages_2027_10_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_2027_11 messages_2027_11_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_2027_11
    ADD CONSTRAINT messages_2027_11_pkey PRIMARY KEY (id, created_at);

--
-- Name: messages_archive messages_archive_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.messages_archive
    ADD CONSTRAINT messages_archive_pkey PRIMARY KEY (id);

--
-- Name: penny_analytics_events penny_analytics_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_analytics_events
    ADD CONSTRAINT penny_analytics_events_pkey PRIMARY KEY (id);

--
-- Name: penny_bot_api_keys penny_bot_api_keys_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_bot_api_keys
    ADD CONSTRAINT penny_bot_api_keys_pkey PRIMARY KEY (id);

--
-- Name: penny_bot_rules penny_bot_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_bot_rules
    ADD CONSTRAINT penny_bot_rules_pkey PRIMARY KEY (id);

--
-- Name: penny_bots penny_bots_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_bots
    ADD CONSTRAINT penny_bots_pkey PRIMARY KEY (id);

--
-- Name: penny_conversation_contexts penny_conversation_contexts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_conversation_contexts
    ADD CONSTRAINT penny_conversation_contexts_pkey PRIMARY KEY (id);

--
-- Name: penny_escalation_tickets penny_escalation_tickets_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_escalation_tickets
    ADD CONSTRAINT penny_escalation_tickets_pkey PRIMARY KEY (id);

--
-- Name: penny_knowledge_articles penny_knowledge_articles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_knowledge_articles
    ADD CONSTRAINT penny_knowledge_articles_pkey PRIMARY KEY (id);

--
-- Name: penny_knowledge_chunks penny_knowledge_chunks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_knowledge_chunks
    ADD CONSTRAINT penny_knowledge_chunks_pkey PRIMARY KEY (id);

--
-- Name: penny_knowledge_documents penny_knowledge_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_knowledge_documents
    ADD CONSTRAINT penny_knowledge_documents_pkey PRIMARY KEY (id);

--
-- Name: penny_response_templates penny_response_templates_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_response_templates
    ADD CONSTRAINT penny_response_templates_pkey PRIMARY KEY (id);

--
-- Name: report_schedule report_schedule_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.report_schedule
    ADD CONSTRAINT report_schedule_pkey PRIMARY KEY (id);

--
-- Name: routing_history routing_history_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routing_history
    ADD CONSTRAINT routing_history_pkey PRIMARY KEY (id);

--
-- Name: skills skills_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.skills
    ADD CONSTRAINT skills_pkey PRIMARY KEY (id);

--
-- Name: sla_configurations sla_configurations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sla_configurations
    ADD CONSTRAINT sla_configurations_pkey PRIMARY KEY (id);

--
-- Name: team_workload team_workload_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team_workload
    ADD CONSTRAINT team_workload_pkey PRIMARY KEY (id);

--
-- Name: agents uq_agent_email_tenant; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agents
    ADD CONSTRAINT uq_agent_email_tenant UNIQUE (email, tenant_id);

--
-- Name: agents uq_agents_user_tenant; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agents
    ADD CONSTRAINT uq_agents_user_tenant UNIQUE (user_id, tenant_id);

--
-- Name: analytics_daily_summary uq_analytics_daily_tenant_date; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.analytics_daily_summary
    ADD CONSTRAINT uq_analytics_daily_tenant_date UNIQUE (tenant_id, summary_date);

--
-- Name: idx_agent_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_active ON public.agents USING btree (tenant_id, active, status);

--
-- Name: idx_agent_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_status ON public.agents USING btree (status);

--
-- Name: idx_agent_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_tenant ON public.agents USING btree (tenant_id);

--
-- Name: idx_agent_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_user ON public.agents USING btree (user_id);

--
-- Name: idx_agent_workload_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_workload_agent ON public.agent_workload USING btree (agent_id);

--
-- Name: idx_agent_workload_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_workload_status ON public.agent_workload USING btree (status);

--
-- Name: idx_agent_workload_team; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_workload_team ON public.agent_workload USING btree (team_id);

--
-- Name: idx_agent_workload_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_workload_tenant ON public.agent_workload USING btree (tenant_id);

--
-- Name: idx_agent_workload_tenant_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_workload_tenant_status ON public.agent_workload USING btree (tenant_id, status);

--
-- Name: idx_ai_suggestion_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_suggestion_conversation ON public.ai_suggestions USING btree (conversation_id);

--
-- Name: idx_ai_suggestion_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_suggestion_created ON public.ai_suggestions USING btree (created_at);

--
-- Name: idx_ai_suggestion_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_suggestion_tenant ON public.ai_suggestions USING btree (tenant_id);

--
-- Name: idx_ai_suggestion_tenant_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ai_suggestion_tenant_status ON public.ai_suggestions USING btree (tenant_id, status);

--
-- Name: idx_analytics_bot; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_analytics_bot ON public.penny_analytics_events USING btree (bot_id);

--
-- Name: idx_analytics_bot_timestamp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_analytics_bot_timestamp ON public.penny_analytics_events USING btree (bot_id, "timestamp");

--
-- Name: idx_analytics_daily_tenant_date; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_analytics_daily_tenant_date ON public.analytics_daily_summary USING btree (tenant_id, summary_date);

--
-- Name: idx_analytics_event_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_analytics_event_type ON public.penny_analytics_events USING btree (event_type);

--
-- Name: idx_analytics_timestamp; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_analytics_timestamp ON public.penny_analytics_events USING btree ("timestamp");

--
-- Name: idx_auto_assign_enabled; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_auto_assign_enabled ON public.auto_assign_configs USING btree (enabled);

--
-- Name: idx_auto_assign_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_auto_assign_tenant ON public.auto_assign_configs USING btree (tenant_id);

--
-- Name: idx_bot_rules_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bot_rules_active ON public.penny_bot_rules USING btree (bot_id, is_active) WHERE (deleted_at IS NULL);

--
-- Name: idx_bot_rules_bot; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bot_rules_bot ON public.penny_bot_rules USING btree (bot_id);

--
-- Name: idx_bot_rules_trigger; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bot_rules_trigger ON public.penny_bot_rules USING btree (bot_id, trigger_type, trigger_value);

--
-- Name: idx_bot_webhook_dlq_bot; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bot_webhook_dlq_bot ON public.bot_webhook_dead_letters USING btree (bot_id);

--
-- Name: idx_bot_webhook_dlq_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_bot_webhook_dlq_status ON public.bot_webhook_dead_letters USING btree (status);

--
-- Name: idx_chunk_bot_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chunk_bot_tenant ON public.penny_knowledge_chunks USING btree (bot_id, tenant_id);

--
-- Name: idx_chunk_document; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chunk_document ON public.penny_knowledge_chunks USING btree (document_id);

--
-- Name: idx_chunk_embedding_hnsw; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chunk_embedding_hnsw ON public.penny_knowledge_chunks USING hnsw (embedding public.vector_cosine_ops) WITH (m='16', ef_construction='64');

--
-- Name: idx_chunk_index; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chunk_index ON public.penny_knowledge_chunks USING btree (document_id, chunk_index);

--
-- Name: idx_conv_event_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conv_event_conversation ON public.conversation_events USING btree (conversation_id);

--
-- Name: idx_conv_event_occurred; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conv_event_occurred ON public.conversation_events USING btree (occurred_at);

--
-- Name: idx_conv_event_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conv_event_tenant ON public.conversation_events USING btree (tenant_id);

--
-- Name: idx_conv_event_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conv_event_type ON public.conversation_events USING btree (event_type);

--
-- Name: idx_conversation_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_agent ON public.conversations USING btree (agent_assigned_id);

--
-- Name: idx_conversation_escalation_tier; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_escalation_tier ON public.conversations USING btree (tenant_id, current_escalation_tier) WHERE (current_escalation_tier IS NOT NULL);

--
-- Name: idx_conversation_external_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_external_user ON public.conversations USING btree (external_user_id);

--
-- Name: idx_conversation_resolution_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_resolution_status ON public.conversations USING btree (resolution_status);

--
-- Name: idx_conversation_satisfaction_rating; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_satisfaction_rating ON public.conversations USING btree (user_satisfaction_rating);

--
-- Name: idx_conversation_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_status ON public.conversations USING btree (status);

--
-- Name: idx_conversation_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_tenant ON public.conversations USING btree (tenant_id);

--
-- Name: idx_conversation_tenant_connection; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_tenant_connection ON public.conversations USING btree (tenant_id, connection_id);

--
-- Name: idx_conversation_tenant_external_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_tenant_external_user ON public.conversations USING btree (tenant_id, external_user_id);

--
-- Name: idx_custom_report_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_custom_report_tenant ON public.custom_report USING btree (tenant_id, is_deleted);

--
-- Name: idx_document_bot_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_document_bot_tenant ON public.penny_knowledge_documents USING btree (bot_id, tenant_id);

--
-- Name: idx_document_file_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_document_file_type ON public.penny_knowledge_documents USING btree (file_type);

--
-- Name: idx_document_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_document_status ON public.penny_knowledge_documents USING btree (status);

--
-- Name: idx_document_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_document_tenant ON public.penny_knowledge_documents USING btree (tenant_id);

--
-- Name: idx_escalation_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_agent ON public.penny_escalation_tickets USING btree (assigned_agent_id) WHERE (assigned_agent_id IS NOT NULL);

--
-- Name: idx_escalation_bot_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_bot_tenant ON public.penny_escalation_tickets USING btree (bot_id, tenant_id);

--
-- Name: idx_escalation_history_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_history_agent ON public.escalation_history USING btree (escalated_to_agent_id);

--
-- Name: idx_escalation_history_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_history_conversation ON public.escalation_history USING btree (conversation_id);

--
-- Name: idx_escalation_history_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_history_created_at ON public.escalation_history USING btree (created_at DESC);

--
-- Name: idx_escalation_history_rule; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_history_rule ON public.escalation_history USING btree (escalation_rule_id);

--
-- Name: idx_escalation_history_team; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_history_team ON public.escalation_history USING btree (escalated_to_team_id);

--
-- Name: idx_escalation_history_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_history_tenant ON public.escalation_history USING btree (tenant_id);

--
-- Name: idx_escalation_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_status ON public.penny_escalation_tickets USING btree (status, created_at);

--
-- Name: idx_escalation_tier_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_tier_active ON public.escalation_tiers USING btree (tenant_id, active);

--
-- Name: idx_escalation_tier_level; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_tier_level ON public.escalation_tiers USING btree (level);

--
-- Name: idx_escalation_tier_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_tier_tenant ON public.escalation_tiers USING btree (tenant_id);

--
-- Name: idx_escalation_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_user ON public.penny_escalation_tickets USING btree (user_id);

--
-- Name: idx_funnel_stage_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_funnel_stage_tenant ON public.funnel_stage USING btree (tenant_id, active);

--
-- Name: idx_knowledge_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_knowledge_active ON public.penny_knowledge_articles USING btree (bot_id, tenant_id, is_active);

--
-- Name: idx_knowledge_bot_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_knowledge_bot_tenant ON public.penny_knowledge_articles USING btree (bot_id, tenant_id);

--
-- Name: idx_knowledge_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_knowledge_category ON public.penny_knowledge_articles USING btree (bot_id, category);

--
-- Name: idx_knowledge_embedding; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_knowledge_embedding ON public.penny_knowledge_articles USING ivfflat (embedding public.vector_cosine_ops) WITH (lists='100');

--
-- Name: idx_messages_2026_08_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_08_conversation ON public.messages_2026_08 USING btree (conversation_id);

--
-- Name: idx_messages_2026_08_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_08_tenant ON public.messages_2026_08 USING btree (tenant_id);

--
-- Name: idx_messages_2026_09_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_09_conversation ON public.messages_2026_09 USING btree (conversation_id);

--
-- Name: idx_messages_2026_09_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_09_tenant ON public.messages_2026_09 USING btree (tenant_id);

--
-- Name: idx_messages_2026_10_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_10_conversation ON public.messages_2026_10 USING btree (conversation_id);

--
-- Name: idx_messages_2026_10_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_10_tenant ON public.messages_2026_10 USING btree (tenant_id);

--
-- Name: idx_messages_2026_11_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_11_conversation ON public.messages_2026_11 USING btree (conversation_id);

--
-- Name: idx_messages_2026_11_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_11_tenant ON public.messages_2026_11 USING btree (tenant_id);

--
-- Name: idx_messages_2026_12_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_12_conversation ON public.messages_2026_12 USING btree (conversation_id);

--
-- Name: idx_messages_2026_12_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2026_12_tenant ON public.messages_2026_12 USING btree (tenant_id);

--
-- Name: idx_messages_2027_01_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_01_conversation ON public.messages_2027_01 USING btree (conversation_id);

--
-- Name: idx_messages_2027_01_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_01_tenant ON public.messages_2027_01 USING btree (tenant_id);

--
-- Name: idx_messages_2027_02_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_02_conversation ON public.messages_2027_02 USING btree (conversation_id);

--
-- Name: idx_messages_2027_02_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_02_tenant ON public.messages_2027_02 USING btree (tenant_id);

--
-- Name: idx_messages_2027_03_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_03_conversation ON public.messages_2027_03 USING btree (conversation_id);

--
-- Name: idx_messages_2027_03_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_03_tenant ON public.messages_2027_03 USING btree (tenant_id);

--
-- Name: idx_messages_2027_04_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_04_conversation ON public.messages_2027_04 USING btree (conversation_id);

--
-- Name: idx_messages_2027_04_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_04_tenant ON public.messages_2027_04 USING btree (tenant_id);

--
-- Name: idx_messages_2027_05_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_05_conversation ON public.messages_2027_05 USING btree (conversation_id);

--
-- Name: idx_messages_2027_05_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_05_tenant ON public.messages_2027_05 USING btree (tenant_id);

--
-- Name: idx_messages_2027_06_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_06_conversation ON public.messages_2027_06 USING btree (conversation_id);

--
-- Name: idx_messages_2027_06_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_06_tenant ON public.messages_2027_06 USING btree (tenant_id);

--
-- Name: idx_messages_2027_07_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_07_conversation ON public.messages_2027_07 USING btree (conversation_id);

--
-- Name: idx_messages_2027_07_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_07_tenant ON public.messages_2027_07 USING btree (tenant_id);

--
-- Name: idx_messages_2027_08_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_08_conversation ON public.messages_2027_08 USING btree (conversation_id);

--
-- Name: idx_messages_2027_08_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_08_tenant ON public.messages_2027_08 USING btree (tenant_id);

--
-- Name: idx_messages_2027_09_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_09_conversation ON public.messages_2027_09 USING btree (conversation_id);

--
-- Name: idx_messages_2027_09_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_09_tenant ON public.messages_2027_09 USING btree (tenant_id);

--
-- Name: idx_messages_2027_10_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_10_conversation ON public.messages_2027_10 USING btree (conversation_id);

--
-- Name: idx_messages_2027_10_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_10_tenant ON public.messages_2027_10 USING btree (tenant_id);

--
-- Name: idx_messages_2027_11_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_11_conversation ON public.messages_2027_11 USING btree (conversation_id);

--
-- Name: idx_messages_2027_11_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_2027_11_tenant ON public.messages_2027_11 USING btree (tenant_id);

--
-- Name: idx_messages_archive_archived_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_archive_archived_at ON public.messages_archive USING btree (archived_at);

--
-- Name: idx_messages_archive_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_archive_conversation ON public.messages_archive USING btree (conversation_id);

--
-- Name: idx_messages_archive_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_archive_created_at ON public.messages_archive USING btree (created_at);

--
-- Name: idx_messages_archive_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_archive_tenant ON public.messages_archive USING btree (tenant_id);

--
-- Name: idx_messages_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_conversation ON ONLY public.messages USING btree (conversation_id);

--
-- Name: idx_messages_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_created_at ON ONLY public.messages USING btree (created_at);

--
-- Name: idx_messages_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_tenant ON ONLY public.messages USING btree (tenant_id);

--
-- Name: idx_messages_tenant_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_tenant_conversation ON ONLY public.messages USING btree (tenant_id, conversation_id);

--
-- Name: idx_messages_tenant_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_messages_tenant_key ON ONLY public.messages USING btree (tenant_key);

--
-- Name: idx_penny_bot_api_keys_bot; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_bot_api_keys_bot ON public.penny_bot_api_keys USING btree (bot_id);

--
-- Name: idx_penny_bot_api_keys_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_bot_api_keys_hash ON public.penny_bot_api_keys USING btree (key_hash);

--
-- Name: idx_penny_bot_api_keys_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_bot_api_keys_tenant ON public.penny_bot_api_keys USING btree (tenant_id);

--
-- Name: idx_penny_bots_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_bots_active ON public.penny_bots USING btree (is_active, is_enabled);

--
-- Name: idx_penny_bots_owner; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_bots_owner ON public.penny_bots USING btree (owner_id);

--
-- Name: idx_penny_bots_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_bots_tenant ON public.penny_bots USING btree (tenant_id);

--
-- Name: idx_penny_ctx_expires_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_ctx_expires_at ON public.penny_conversation_contexts USING btree (expires_at);

--
-- Name: idx_penny_ctx_tenant_bot; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_ctx_tenant_bot ON public.penny_conversation_contexts USING btree (tenant_id, bot_id);

--
-- Name: idx_penny_ctx_tenant_user_platform; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_penny_ctx_tenant_user_platform ON public.penny_conversation_contexts USING btree (tenant_id, user_id, platform);

--
-- Name: idx_report_schedule_due; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_report_schedule_due ON public.report_schedule USING btree (enabled, next_run_at);

--
-- Name: idx_response_templates_bot; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_response_templates_bot ON public.penny_response_templates USING btree (bot_id);

--
-- Name: idx_response_templates_intent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_response_templates_intent ON public.penny_response_templates USING btree (bot_id, intent);

--
-- Name: idx_routing_history_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_history_agent ON public.routing_history USING btree (assigned_agent_id);

--
-- Name: idx_routing_history_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_history_conversation ON public.routing_history USING btree (conversation_id);

--
-- Name: idx_routing_history_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_history_created_at ON public.routing_history USING btree (created_at DESC);

--
-- Name: idx_routing_history_rule; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_history_rule ON public.routing_history USING btree (routing_rule_id);

--
-- Name: idx_routing_history_team; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_history_team ON public.routing_history USING btree (assigned_team_id);

--
-- Name: idx_routing_history_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_history_tenant ON public.routing_history USING btree (tenant_id);

--
-- Name: idx_routing_history_tenant_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_history_tenant_conversation ON public.routing_history USING btree (tenant_id, conversation_id);

--
-- Name: idx_sentiment_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sentiment_conversation ON public.conversation_sentiment USING btree (conversation_id);

--
-- Name: idx_sentiment_conversation_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sentiment_conversation_created ON public.conversation_sentiment USING btree (conversation_id, created_at);

--
-- Name: idx_sentiment_label; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sentiment_label ON public.conversation_sentiment USING btree (tenant_id, label);

--
-- Name: idx_sentiment_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sentiment_tenant ON public.conversation_sentiment USING btree (tenant_id);

--
-- Name: idx_skill_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_skill_active ON public.skills USING btree (tenant_id, active);

--
-- Name: idx_skill_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_skill_category ON public.skills USING btree (category);

--
-- Name: idx_skill_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_skill_tenant ON public.skills USING btree (tenant_id);

--
-- Name: idx_sla_config_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sla_config_active ON public.sla_configurations USING btree (tenant_id, active);

--
-- Name: idx_sla_config_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sla_config_tenant ON public.sla_configurations USING btree (tenant_id);

--
-- Name: idx_sla_config_tier; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_sla_config_tier ON public.sla_configurations USING btree (customer_tier);

--
-- Name: idx_team_workload_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_team_workload_status ON public.team_workload USING btree (status);

--
-- Name: idx_team_workload_team; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_team_workload_team ON public.team_workload USING btree (team_id);

--
-- Name: idx_team_workload_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_team_workload_tenant ON public.team_workload USING btree (tenant_id);

--
-- Name: idx_team_workload_tenant_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_team_workload_tenant_status ON public.team_workload USING btree (tenant_id, status);

--
-- Name: idx_transfer_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_conversation ON public.conversation_transfers USING btree (conversation_id);

--
-- Name: idx_transfer_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_created_at ON public.conversation_transfers USING btree (created_at DESC);

--
-- Name: idx_transfer_from_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_from_agent ON public.conversation_transfers USING btree (from_agent_id);

--
-- Name: idx_transfer_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_status ON public.conversation_transfers USING btree (status);

--
-- Name: idx_transfer_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_tenant ON public.conversation_transfers USING btree (tenant_id);

--
-- Name: idx_transfer_to_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_transfer_to_agent ON public.conversation_transfers USING btree (to_agent_id);

--
-- Name: messages_2026_08_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_08_conversation_id_idx ON public.messages_2026_08 USING btree (conversation_id);

--
-- Name: messages_2026_08_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_08_created_at_idx ON public.messages_2026_08 USING btree (created_at);

--
-- Name: messages_2026_08_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_08_tenant_id_conversation_id_idx ON public.messages_2026_08 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2026_08_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_08_tenant_id_idx ON public.messages_2026_08 USING btree (tenant_id);

--
-- Name: messages_2026_08_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_08_tenant_key_idx ON public.messages_2026_08 USING btree (tenant_key);

--
-- Name: messages_2026_09_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_09_conversation_id_idx ON public.messages_2026_09 USING btree (conversation_id);

--
-- Name: messages_2026_09_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_09_created_at_idx ON public.messages_2026_09 USING btree (created_at);

--
-- Name: messages_2026_09_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_09_tenant_id_conversation_id_idx ON public.messages_2026_09 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2026_09_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_09_tenant_id_idx ON public.messages_2026_09 USING btree (tenant_id);

--
-- Name: messages_2026_09_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_09_tenant_key_idx ON public.messages_2026_09 USING btree (tenant_key);

--
-- Name: messages_2026_10_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_10_conversation_id_idx ON public.messages_2026_10 USING btree (conversation_id);

--
-- Name: messages_2026_10_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_10_created_at_idx ON public.messages_2026_10 USING btree (created_at);

--
-- Name: messages_2026_10_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_10_tenant_id_conversation_id_idx ON public.messages_2026_10 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2026_10_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_10_tenant_id_idx ON public.messages_2026_10 USING btree (tenant_id);

--
-- Name: messages_2026_10_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_10_tenant_key_idx ON public.messages_2026_10 USING btree (tenant_key);

--
-- Name: messages_2026_11_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_11_conversation_id_idx ON public.messages_2026_11 USING btree (conversation_id);

--
-- Name: messages_2026_11_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_11_created_at_idx ON public.messages_2026_11 USING btree (created_at);

--
-- Name: messages_2026_11_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_11_tenant_id_conversation_id_idx ON public.messages_2026_11 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2026_11_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_11_tenant_id_idx ON public.messages_2026_11 USING btree (tenant_id);

--
-- Name: messages_2026_11_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_11_tenant_key_idx ON public.messages_2026_11 USING btree (tenant_key);

--
-- Name: messages_2026_12_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_12_conversation_id_idx ON public.messages_2026_12 USING btree (conversation_id);

--
-- Name: messages_2026_12_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_12_created_at_idx ON public.messages_2026_12 USING btree (created_at);

--
-- Name: messages_2026_12_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_12_tenant_id_conversation_id_idx ON public.messages_2026_12 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2026_12_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_12_tenant_id_idx ON public.messages_2026_12 USING btree (tenant_id);

--
-- Name: messages_2026_12_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2026_12_tenant_key_idx ON public.messages_2026_12 USING btree (tenant_key);

--
-- Name: messages_2027_01_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_01_conversation_id_idx ON public.messages_2027_01 USING btree (conversation_id);

--
-- Name: messages_2027_01_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_01_created_at_idx ON public.messages_2027_01 USING btree (created_at);

--
-- Name: messages_2027_01_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_01_tenant_id_conversation_id_idx ON public.messages_2027_01 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_01_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_01_tenant_id_idx ON public.messages_2027_01 USING btree (tenant_id);

--
-- Name: messages_2027_01_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_01_tenant_key_idx ON public.messages_2027_01 USING btree (tenant_key);

--
-- Name: messages_2027_02_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_02_conversation_id_idx ON public.messages_2027_02 USING btree (conversation_id);

--
-- Name: messages_2027_02_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_02_created_at_idx ON public.messages_2027_02 USING btree (created_at);

--
-- Name: messages_2027_02_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_02_tenant_id_conversation_id_idx ON public.messages_2027_02 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_02_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_02_tenant_id_idx ON public.messages_2027_02 USING btree (tenant_id);

--
-- Name: messages_2027_02_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_02_tenant_key_idx ON public.messages_2027_02 USING btree (tenant_key);

--
-- Name: messages_2027_03_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_03_conversation_id_idx ON public.messages_2027_03 USING btree (conversation_id);

--
-- Name: messages_2027_03_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_03_created_at_idx ON public.messages_2027_03 USING btree (created_at);

--
-- Name: messages_2027_03_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_03_tenant_id_conversation_id_idx ON public.messages_2027_03 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_03_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_03_tenant_id_idx ON public.messages_2027_03 USING btree (tenant_id);

--
-- Name: messages_2027_03_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_03_tenant_key_idx ON public.messages_2027_03 USING btree (tenant_key);

--
-- Name: messages_2027_04_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_04_conversation_id_idx ON public.messages_2027_04 USING btree (conversation_id);

--
-- Name: messages_2027_04_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_04_created_at_idx ON public.messages_2027_04 USING btree (created_at);

--
-- Name: messages_2027_04_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_04_tenant_id_conversation_id_idx ON public.messages_2027_04 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_04_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_04_tenant_id_idx ON public.messages_2027_04 USING btree (tenant_id);

--
-- Name: messages_2027_04_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_04_tenant_key_idx ON public.messages_2027_04 USING btree (tenant_key);

--
-- Name: messages_2027_05_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_05_conversation_id_idx ON public.messages_2027_05 USING btree (conversation_id);

--
-- Name: messages_2027_05_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_05_created_at_idx ON public.messages_2027_05 USING btree (created_at);

--
-- Name: messages_2027_05_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_05_tenant_id_conversation_id_idx ON public.messages_2027_05 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_05_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_05_tenant_id_idx ON public.messages_2027_05 USING btree (tenant_id);

--
-- Name: messages_2027_05_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_05_tenant_key_idx ON public.messages_2027_05 USING btree (tenant_key);

--
-- Name: messages_2027_06_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_06_conversation_id_idx ON public.messages_2027_06 USING btree (conversation_id);

--
-- Name: messages_2027_06_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_06_created_at_idx ON public.messages_2027_06 USING btree (created_at);

--
-- Name: messages_2027_06_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_06_tenant_id_conversation_id_idx ON public.messages_2027_06 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_06_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_06_tenant_id_idx ON public.messages_2027_06 USING btree (tenant_id);

--
-- Name: messages_2027_06_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_06_tenant_key_idx ON public.messages_2027_06 USING btree (tenant_key);

--
-- Name: messages_2027_07_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_07_conversation_id_idx ON public.messages_2027_07 USING btree (conversation_id);

--
-- Name: messages_2027_07_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_07_created_at_idx ON public.messages_2027_07 USING btree (created_at);

--
-- Name: messages_2027_07_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_07_tenant_id_conversation_id_idx ON public.messages_2027_07 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_07_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_07_tenant_id_idx ON public.messages_2027_07 USING btree (tenant_id);

--
-- Name: messages_2027_07_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_07_tenant_key_idx ON public.messages_2027_07 USING btree (tenant_key);

--
-- Name: messages_2027_08_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_08_conversation_id_idx ON public.messages_2027_08 USING btree (conversation_id);

--
-- Name: messages_2027_08_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_08_created_at_idx ON public.messages_2027_08 USING btree (created_at);

--
-- Name: messages_2027_08_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_08_tenant_id_conversation_id_idx ON public.messages_2027_08 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_08_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_08_tenant_id_idx ON public.messages_2027_08 USING btree (tenant_id);

--
-- Name: messages_2027_08_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_08_tenant_key_idx ON public.messages_2027_08 USING btree (tenant_key);

--
-- Name: messages_2027_09_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_09_conversation_id_idx ON public.messages_2027_09 USING btree (conversation_id);

--
-- Name: messages_2027_09_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_09_created_at_idx ON public.messages_2027_09 USING btree (created_at);

--
-- Name: messages_2027_09_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_09_tenant_id_conversation_id_idx ON public.messages_2027_09 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_09_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_09_tenant_id_idx ON public.messages_2027_09 USING btree (tenant_id);

--
-- Name: messages_2027_09_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_09_tenant_key_idx ON public.messages_2027_09 USING btree (tenant_key);

--
-- Name: messages_2027_10_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_10_conversation_id_idx ON public.messages_2027_10 USING btree (conversation_id);

--
-- Name: messages_2027_10_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_10_created_at_idx ON public.messages_2027_10 USING btree (created_at);

--
-- Name: messages_2027_10_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_10_tenant_id_conversation_id_idx ON public.messages_2027_10 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_10_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_10_tenant_id_idx ON public.messages_2027_10 USING btree (tenant_id);

--
-- Name: messages_2027_10_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_10_tenant_key_idx ON public.messages_2027_10 USING btree (tenant_key);

--
-- Name: messages_2027_11_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_11_conversation_id_idx ON public.messages_2027_11 USING btree (conversation_id);

--
-- Name: messages_2027_11_created_at_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_11_created_at_idx ON public.messages_2027_11 USING btree (created_at);

--
-- Name: messages_2027_11_tenant_id_conversation_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_11_tenant_id_conversation_id_idx ON public.messages_2027_11 USING btree (tenant_id, conversation_id);

--
-- Name: messages_2027_11_tenant_id_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_11_tenant_id_idx ON public.messages_2027_11 USING btree (tenant_id);

--
-- Name: messages_2027_11_tenant_key_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX messages_2027_11_tenant_key_idx ON public.messages_2027_11 USING btree (tenant_key);

--
-- Name: uq_penny_ctx_context_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_penny_ctx_context_key ON public.penny_conversation_contexts USING btree (context_key);

--
-- Name: messages_2026_08_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2026_08_conversation_id_idx;

--
-- Name: messages_2026_08_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2026_08_created_at_idx;

--
-- Name: messages_2026_08_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2026_08_pkey;

--
-- Name: messages_2026_08_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2026_08_tenant_id_conversation_id_idx;

--
-- Name: messages_2026_08_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2026_08_tenant_id_idx;

--
-- Name: messages_2026_08_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2026_08_tenant_key_idx;

--
-- Name: messages_2026_09_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2026_09_conversation_id_idx;

--
-- Name: messages_2026_09_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2026_09_created_at_idx;

--
-- Name: messages_2026_09_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2026_09_pkey;

--
-- Name: messages_2026_09_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2026_09_tenant_id_conversation_id_idx;

--
-- Name: messages_2026_09_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2026_09_tenant_id_idx;

--
-- Name: messages_2026_09_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2026_09_tenant_key_idx;

--
-- Name: messages_2026_10_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2026_10_conversation_id_idx;

--
-- Name: messages_2026_10_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2026_10_created_at_idx;

--
-- Name: messages_2026_10_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2026_10_pkey;

--
-- Name: messages_2026_10_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2026_10_tenant_id_conversation_id_idx;

--
-- Name: messages_2026_10_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2026_10_tenant_id_idx;

--
-- Name: messages_2026_10_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2026_10_tenant_key_idx;

--
-- Name: messages_2026_11_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2026_11_conversation_id_idx;

--
-- Name: messages_2026_11_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2026_11_created_at_idx;

--
-- Name: messages_2026_11_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2026_11_pkey;

--
-- Name: messages_2026_11_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2026_11_tenant_id_conversation_id_idx;

--
-- Name: messages_2026_11_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2026_11_tenant_id_idx;

--
-- Name: messages_2026_11_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2026_11_tenant_key_idx;

--
-- Name: messages_2026_12_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2026_12_conversation_id_idx;

--
-- Name: messages_2026_12_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2026_12_created_at_idx;

--
-- Name: messages_2026_12_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2026_12_pkey;

--
-- Name: messages_2026_12_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2026_12_tenant_id_conversation_id_idx;

--
-- Name: messages_2026_12_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2026_12_tenant_id_idx;

--
-- Name: messages_2026_12_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2026_12_tenant_key_idx;

--
-- Name: messages_2027_01_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_01_conversation_id_idx;

--
-- Name: messages_2027_01_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_01_created_at_idx;

--
-- Name: messages_2027_01_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_01_pkey;

--
-- Name: messages_2027_01_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_01_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_01_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_01_tenant_id_idx;

--
-- Name: messages_2027_01_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_01_tenant_key_idx;

--
-- Name: messages_2027_02_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_02_conversation_id_idx;

--
-- Name: messages_2027_02_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_02_created_at_idx;

--
-- Name: messages_2027_02_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_02_pkey;

--
-- Name: messages_2027_02_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_02_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_02_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_02_tenant_id_idx;

--
-- Name: messages_2027_02_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_02_tenant_key_idx;

--
-- Name: messages_2027_03_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_03_conversation_id_idx;

--
-- Name: messages_2027_03_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_03_created_at_idx;

--
-- Name: messages_2027_03_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_03_pkey;

--
-- Name: messages_2027_03_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_03_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_03_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_03_tenant_id_idx;

--
-- Name: messages_2027_03_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_03_tenant_key_idx;

--
-- Name: messages_2027_04_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_04_conversation_id_idx;

--
-- Name: messages_2027_04_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_04_created_at_idx;

--
-- Name: messages_2027_04_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_04_pkey;

--
-- Name: messages_2027_04_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_04_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_04_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_04_tenant_id_idx;

--
-- Name: messages_2027_04_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_04_tenant_key_idx;

--
-- Name: messages_2027_05_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_05_conversation_id_idx;

--
-- Name: messages_2027_05_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_05_created_at_idx;

--
-- Name: messages_2027_05_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_05_pkey;

--
-- Name: messages_2027_05_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_05_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_05_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_05_tenant_id_idx;

--
-- Name: messages_2027_05_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_05_tenant_key_idx;

--
-- Name: messages_2027_06_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_06_conversation_id_idx;

--
-- Name: messages_2027_06_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_06_created_at_idx;

--
-- Name: messages_2027_06_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_06_pkey;

--
-- Name: messages_2027_06_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_06_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_06_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_06_tenant_id_idx;

--
-- Name: messages_2027_06_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_06_tenant_key_idx;

--
-- Name: messages_2027_07_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_07_conversation_id_idx;

--
-- Name: messages_2027_07_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_07_created_at_idx;

--
-- Name: messages_2027_07_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_07_pkey;

--
-- Name: messages_2027_07_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_07_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_07_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_07_tenant_id_idx;

--
-- Name: messages_2027_07_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_07_tenant_key_idx;

--
-- Name: messages_2027_08_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_08_conversation_id_idx;

--
-- Name: messages_2027_08_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_08_created_at_idx;

--
-- Name: messages_2027_08_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_08_pkey;

--
-- Name: messages_2027_08_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_08_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_08_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_08_tenant_id_idx;

--
-- Name: messages_2027_08_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_08_tenant_key_idx;

--
-- Name: messages_2027_09_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_09_conversation_id_idx;

--
-- Name: messages_2027_09_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_09_created_at_idx;

--
-- Name: messages_2027_09_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_09_pkey;

--
-- Name: messages_2027_09_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_09_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_09_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_09_tenant_id_idx;

--
-- Name: messages_2027_09_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_09_tenant_key_idx;

--
-- Name: messages_2027_10_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_10_conversation_id_idx;

--
-- Name: messages_2027_10_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_10_created_at_idx;

--
-- Name: messages_2027_10_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_10_pkey;

--
-- Name: messages_2027_10_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_10_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_10_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_10_tenant_id_idx;

--
-- Name: messages_2027_10_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_10_tenant_key_idx;

--
-- Name: messages_2027_11_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_conversation ATTACH PARTITION public.messages_2027_11_conversation_id_idx;

--
-- Name: messages_2027_11_created_at_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_created_at ATTACH PARTITION public.messages_2027_11_created_at_idx;

--
-- Name: messages_2027_11_pkey; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.messages_pkey ATTACH PARTITION public.messages_2027_11_pkey;

--
-- Name: messages_2027_11_tenant_id_conversation_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_conversation ATTACH PARTITION public.messages_2027_11_tenant_id_conversation_id_idx;

--
-- Name: messages_2027_11_tenant_id_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant ATTACH PARTITION public.messages_2027_11_tenant_id_idx;

--
-- Name: messages_2027_11_tenant_key_idx; Type: INDEX ATTACH; Schema: public; Owner: -
--

ALTER INDEX public.idx_messages_tenant_key ATTACH PARTITION public.messages_2027_11_tenant_key_idx;

--
-- Name: penny_knowledge_chunks fk_chunk_bot; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_knowledge_chunks
    ADD CONSTRAINT fk_chunk_bot FOREIGN KEY (bot_id) REFERENCES public.penny_bots(id) ON DELETE CASCADE;

--
-- Name: penny_knowledge_chunks fk_chunk_document; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_knowledge_chunks
    ADD CONSTRAINT fk_chunk_document FOREIGN KEY (document_id) REFERENCES public.penny_knowledge_documents(id) ON DELETE CASCADE;

--
-- Name: penny_knowledge_documents fk_document_bot; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_knowledge_documents
    ADD CONSTRAINT fk_document_bot FOREIGN KEY (bot_id) REFERENCES public.penny_bots(id) ON DELETE CASCADE;

--
-- Name: penny_escalation_tickets fk_escalation_bot; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_escalation_tickets
    ADD CONSTRAINT fk_escalation_bot FOREIGN KEY (bot_id) REFERENCES public.penny_bots(id) ON DELETE SET NULL;

--
-- Name: penny_knowledge_articles fk_knowledge_bot; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_knowledge_articles
    ADD CONSTRAINT fk_knowledge_bot FOREIGN KEY (bot_id) REFERENCES public.penny_bots(id) ON DELETE CASCADE;

--
-- Name: penny_bot_rules fk_rule_bot; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_bot_rules
    ADD CONSTRAINT fk_rule_bot FOREIGN KEY (bot_id) REFERENCES public.penny_bots(id) ON DELETE CASCADE;

--
-- Name: penny_response_templates fk_template_bot; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_response_templates
    ADD CONSTRAINT fk_template_bot FOREIGN KEY (bot_id) REFERENCES public.penny_bots(id) ON DELETE CASCADE;

--
-- Name: penny_bot_api_keys penny_bot_api_keys_bot_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.penny_bot_api_keys
    ADD CONSTRAINT penny_bot_api_keys_bot_id_fkey FOREIGN KEY (bot_id) REFERENCES public.penny_bots(id) ON DELETE CASCADE;

--
-- PostgreSQL database dump complete
--
