
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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: agent_certifications; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agent_certifications (
    id bigint NOT NULL,
    agent_id bigint NOT NULL,
    certification_level_id bigint NOT NULL,
    status character varying(50),
    issued_at character varying(255),
    expires_at character varying(255),
    certificate_number character varying(255),
    issued_by character varying(255),
    revoked_at character varying(255),
    revocation_reason text,
    revoked_by character varying(255),
    notes text
);

--
-- Name: agent_certifications_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.agent_certifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: agent_certifications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.agent_certifications_id_seq OWNED BY public.agent_certifications.id;

--
-- Name: agent_onboarding_progress; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agent_onboarding_progress (
    id bigint NOT NULL,
    agent_id bigint NOT NULL,
    task_id bigint NOT NULL,
    status character varying(50),
    started_at character varying(255),
    completed_at character varying(255),
    time_spent_seconds integer,
    notes text,
    supervisor_approval character varying(50),
    supervisor_id bigint,
    supervisor_comments text,
    approved_at character varying(255)
);

--
-- Name: agent_onboarding_progress_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.agent_onboarding_progress_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: agent_onboarding_progress_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.agent_onboarding_progress_id_seq OWNED BY public.agent_onboarding_progress.id;

--
-- Name: agent_onboarding_tasks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agent_onboarding_tasks (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    description text,
    type character varying(50),
    training_id bigint,
    required boolean NOT NULL,
    display_order integer,
    status character varying(50),
    estimated_duration_minutes integer,
    instructions text,
    external_url character varying(255)
);

--
-- Name: agent_onboarding_tasks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.agent_onboarding_tasks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: agent_onboarding_tasks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.agent_onboarding_tasks_id_seq OWNED BY public.agent_onboarding_tasks.id;

--
-- Name: agent_skills; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agent_skills (
    id bigint NOT NULL,
    agent_id bigint NOT NULL,
    skill_category character varying(50) NOT NULL,
    score integer NOT NULL,
    proficiency_level character varying(50),
    last_assessed_at character varying(255),
    assessment_method character varying(50),
    training_id bigint,
    previous_score integer,
    assessment_count integer,
    notes text
);

--
-- Name: agent_skills_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.agent_skills_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: agent_skills_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.agent_skills_id_seq OWNED BY public.agent_skills.id;

--
-- Name: agent_training_progress; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agent_training_progress (
    id bigint NOT NULL,
    agent_id bigint NOT NULL,
    training_id bigint NOT NULL,
    status character varying(50) NOT NULL,
    progress integer NOT NULL,
    quiz_score integer,
    started_at character varying(255),
    completed_at character varying(255),
    time_spent_seconds integer,
    notes text
);

--
-- Name: agent_training_progress_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.agent_training_progress_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: agent_training_progress_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.agent_training_progress_id_seq OWNED BY public.agent_training_progress.id;

--
-- Name: app_configurations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_configurations (
    id bigint NOT NULL,
    tenant_key character varying(255),
    app_id bigint NOT NULL,
    config_key character varying(255) NOT NULL,
    config_value text,
    config_type character varying(50) NOT NULL,
    is_required boolean DEFAULT false NOT NULL,
    is_encrypted boolean DEFAULT false NOT NULL,
    description text,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: app_configurations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_configurations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: app_configurations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_configurations_id_seq OWNED BY public.app_configurations.id;

--
-- Name: app_guards; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_guards (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    app_id bigint NOT NULL,
    guard_name character varying(255) NOT NULL,
    guard_type character varying(50) NOT NULL,
    description text,
    is_active boolean DEFAULT true NOT NULL,
    priority integer DEFAULT 0 NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: app_guards_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_guards_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: app_guards_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_guards_id_seq OWNED BY public.app_guards.id;

--
-- Name: app_registry; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_registry (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    display_name character varying(255) NOT NULL,
    description text NOT NULL,
    app_type character varying(50) NOT NULL,
    status character varying(50) NOT NULL,
    version character varying(50),
    api_endpoint character varying(500),
    webhook_url character varying(500),
    config_schema text,
    default_config text,
    is_active boolean DEFAULT true NOT NULL,
    is_public boolean DEFAULT false NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: app_registry_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_registry_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: app_registry_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_registry_id_seq OWNED BY public.app_registry.id;

--
-- Name: app_subscriptions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_subscriptions (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    app_id bigint NOT NULL,
    user_id bigint NOT NULL,
    subscription_plan character varying(50) NOT NULL,
    subscription_status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    subscription_start timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    subscription_end timestamp without time zone,
    auto_renew boolean DEFAULT false NOT NULL,
    trial_end timestamp without time zone,
    config_data text,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: app_subscriptions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_subscriptions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: app_subscriptions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_subscriptions_id_seq OWNED BY public.app_subscriptions.id;

--
-- Name: certification_levels; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.certification_levels (
    id bigint NOT NULL,
    level_name character varying(255) NOT NULL,
    description text,
    required_training_ids character varying(255),
    required_skill_scores character varying(255),
    required_stats character varying(255),
    badge_icon character varying(255),
    badge_color character varying(255),
    display_order integer,
    active boolean DEFAULT true NOT NULL,
    validity_days integer
);

--
-- Name: certification_levels_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.certification_levels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: certification_levels_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.certification_levels_id_seq OWNED BY public.certification_levels.id;

--
-- Name: conversation_routing; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.conversation_routing (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    routing_status character varying(50) NOT NULL,
    assigned_agent_id bigint,
    assigned_team_id bigint,
    routing_rule_id bigint,
    priority integer DEFAULT 5 NOT NULL,
    queue_position integer,
    assigned_at timestamp without time zone,
    escalated_at timestamp without time zone,
    escalation_rule_id bigint,
    escalated_from_agent_id bigint,
    escalated_to_agent_id bigint,
    escalation_reason text,
    wait_time_seconds integer,
    handling_time_seconds integer,
    is_escalated boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_conversation_routing_status CHECK (((routing_status)::text = ANY ((ARRAY['QUEUED'::character varying, 'ASSIGNED'::character varying, 'IN_PROGRESS'::character varying, 'ESCALATED'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])))
);

--
-- Name: TABLE conversation_routing; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.conversation_routing IS 'Tracks conversation routing status and assignment';

--
-- Name: conversation_routing_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.conversation_routing_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: conversation_routing_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.conversation_routing_id_seq OWNED BY public.conversation_routing.id;

--
-- Name: customer_notes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.customer_notes (
    id bigint NOT NULL,
    tenant_id bigint,
    tenant_key character varying(255),
    customer_profile_id bigint NOT NULL,
    author_id character varying(255),
    author_name character varying(255),
    content text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: customer_notes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.customer_notes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: customer_notes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.customer_notes_id_seq OWNED BY public.customer_notes.id;

--
-- Name: device_bindings; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.device_bindings (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    device_id character varying(255) NOT NULL,
    device_name character varying(255),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    activated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    last_seen_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    created_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: TABLE device_bindings; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.device_bindings IS 'Stores device binding and active session tracking for license enforcement';

--
-- Name: device_bindings_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.device_bindings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: device_bindings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.device_bindings_id_seq OWNED BY public.device_bindings.id;

--
-- Name: escalation_rules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.escalation_rules (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    trigger_type character varying(50) NOT NULL,
    trigger_value character varying(255),
    threshold_value integer,
    time_threshold_minutes integer,
    conditions jsonb,
    escalation_action character varying(50) NOT NULL,
    escalate_to_agent_id bigint,
    escalate_to_team_id bigint,
    escalate_to_supervisor boolean DEFAULT false NOT NULL,
    notify_supervisor boolean DEFAULT true NOT NULL,
    priority integer NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_escalation_action CHECK (((escalation_action)::text = ANY ((ARRAY['ASSIGN_TO_AGENT'::character varying, 'ASSIGN_TO_TEAM'::character varying, 'ASSIGN_TO_SUPERVISOR'::character varying, 'MOVE_TO_QUEUE'::character varying, 'SEND_NOTIFICATION'::character varying])::text[]))),
    CONSTRAINT chk_escalation_trigger_type CHECK (((trigger_type)::text = ANY ((ARRAY['WAIT_TIME_EXCEEDED'::character varying, 'NO_RESPONSE'::character varying, 'LOW_CUSTOMER_SATISFACTION'::character varying, 'HIGH_PRIORITY_CUSTOMER'::character varying, 'CUSTOMER_TIER'::character varying, 'CUSTOM_CONDITION'::character varying])::text[])))
);

--
-- Name: TABLE escalation_rules; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.escalation_rules IS 'Rules for automatic conversation escalation';

--
-- Name: COLUMN escalation_rules.conditions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.escalation_rules.conditions IS 'JSONB conditions for trigger matching';

--
-- Name: escalation_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.escalation_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: escalation_rules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.escalation_rules_id_seq OWNED BY public.escalation_rules.id;

--
-- Name: guard_rules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.guard_rules (
    id bigint NOT NULL,
    tenant_key character varying(255),
    app_guard_id bigint NOT NULL,
    rule_name character varying(255) NOT NULL,
    rule_condition text NOT NULL,
    rule_action character varying(255) NOT NULL,
    rule_parameters text,
    is_active boolean DEFAULT true NOT NULL,
    priority integer DEFAULT 0 NOT NULL,
    description text,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: guard_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.guard_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: guard_rules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.guard_rules_id_seq OWNED BY public.guard_rules.id;

--
-- Name: license_features; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.license_features (
    id bigint NOT NULL,
    tenant_key character varying(255),
    license_id bigint NOT NULL,
    feature character varying(255) NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: license_features_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.license_features_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: license_features_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.license_features_id_seq OWNED BY public.license_features.id;

--
-- Name: license_limits; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.license_limits (
    id bigint NOT NULL,
    tenant_key character varying(255),
    license_id bigint NOT NULL,
    limit_key character varying(255) NOT NULL,
    limit_value integer NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: license_limits_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.license_limits_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: license_limits_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.license_limits_id_seq OWNED BY public.license_limits.id;

--
-- Name: license_modules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.license_modules (
    id bigint NOT NULL,
    tenant_key character varying(255),
    license_id bigint NOT NULL,
    module character varying(255) NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: license_modules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.license_modules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: license_modules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.license_modules_id_seq OWNED BY public.license_modules.id;

--
-- Name: licenses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.licenses (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    user_id bigint NOT NULL,
    plan_name character varying(255) NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    expires_at timestamp without time zone,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: licenses_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.licenses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: licenses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.licenses_id_seq OWNED BY public.licenses.id;

--
-- Name: routing_rules; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.routing_rules (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    rule_type character varying(50) NOT NULL,
    priority integer NOT NULL,
    target_type character varying(50) NOT NULL,
    target_agent_id bigint,
    target_team_id bigint,
    target_skill_id bigint,
    conditions jsonb,
    max_concurrent_conversations integer,
    is_active boolean DEFAULT true NOT NULL,
    is_default boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_routing_rule_type CHECK (((rule_type)::text = ANY ((ARRAY['SKILL_BASED'::character varying, 'PRIORITY_BASED'::character varying, 'ROUND_ROBIN'::character varying, 'LEAST_BUSY'::character varying, 'CUSTOM'::character varying])::text[]))),
    CONSTRAINT chk_routing_target_type CHECK (((target_type)::text = ANY ((ARRAY['AGENT'::character varying, 'TEAM'::character varying, 'SKILL'::character varying, 'QUEUE'::character varying])::text[])))
);

--
-- Name: TABLE routing_rules; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.routing_rules IS 'Rules for intelligent conversation routing';

--
-- Name: COLUMN routing_rules.conditions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.routing_rules.conditions IS 'JSONB conditions for rule matching';

--
-- Name: routing_rules_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.routing_rules_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: routing_rules_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.routing_rules_id_seq OWNED BY public.routing_rules.id;

--
-- Name: supervisor_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.supervisor_sessions (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    supervisor_id bigint NOT NULL,
    conversation_id bigint NOT NULL,
    session_type character varying(50) NOT NULL,
    session_status character varying(50) NOT NULL,
    monitored_agent_id bigint,
    joined_at timestamp without time zone,
    left_at timestamp without time zone,
    duration_seconds integer,
    is_silent boolean DEFAULT true NOT NULL,
    can_intervene boolean DEFAULT false NOT NULL,
    intervention_count integer DEFAULT 0 NOT NULL,
    notes text,
    rating integer,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_supervisor_rating CHECK (((rating IS NULL) OR ((rating >= 1) AND (rating <= 5)))),
    CONSTRAINT chk_supervisor_session_status CHECK (((session_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'ENDED'::character varying, 'INTERRUPTED'::character varying])::text[]))),
    CONSTRAINT chk_supervisor_session_type CHECK (((session_type)::text = ANY ((ARRAY['MONITORING'::character varying, 'COACHING'::character varying, 'TAKEOVER'::character varying, 'ASSISTING'::character varying])::text[])))
);

--
-- Name: TABLE supervisor_sessions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.supervisor_sessions IS 'Sessions for supervisor monitoring and takeover of conversations';

--
-- Name: supervisor_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.supervisor_sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: supervisor_sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.supervisor_sessions_id_seq OWNED BY public.supervisor_sessions.id;

--
-- Name: training_categories; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.training_categories (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text
);

--
-- Name: training_categories_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.training_categories_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: training_categories_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.training_categories_id_seq OWNED BY public.training_categories.id;

--
-- Name: training_materials; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.training_materials (
    id bigint NOT NULL,
    title character varying(255) NOT NULL,
    description text,
    content text,
    type character varying(50) NOT NULL,
    category_id bigint,
    status character varying(50) NOT NULL,
    estimated_duration_minutes integer,
    required_skills jsonb,
    difficulty_level character varying(50),
    is_mandatory boolean,
    sequence_order integer,
    pass_score_threshold integer,
    quiz_questions jsonb,
    external_url character varying(255),
    file_url character varying(255)
);

--
-- Name: training_materials_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.training_materials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: training_materials_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.training_materials_id_seq OWNED BY public.training_materials.id;

--
-- Name: vip_configs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vip_configs (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    tenant_key character varying(255),
    basic_min_conversations integer DEFAULT 3,
    premium_min_conversations integer DEFAULT 10,
    premium_min_satisfaction double precision DEFAULT 4.0,
    enterprise_min_conversations integer DEFAULT 50,
    auto_recalculate_enabled boolean DEFAULT true NOT NULL,
    description text,
    is_deleted boolean DEFAULT false NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    created_by character varying(255),
    updated_by character varying(255)
);

--
-- Name: vip_configs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.vip_configs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: vip_configs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.vip_configs_id_seq OWNED BY public.vip_configs.id;

--
-- Name: agent_certifications id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_certifications ALTER COLUMN id SET DEFAULT nextval('public.agent_certifications_id_seq'::regclass);

--
-- Name: agent_onboarding_progress id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_onboarding_progress ALTER COLUMN id SET DEFAULT nextval('public.agent_onboarding_progress_id_seq'::regclass);

--
-- Name: agent_onboarding_tasks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_onboarding_tasks ALTER COLUMN id SET DEFAULT nextval('public.agent_onboarding_tasks_id_seq'::regclass);

--
-- Name: agent_skills id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_skills ALTER COLUMN id SET DEFAULT nextval('public.agent_skills_id_seq'::regclass);

--
-- Name: agent_training_progress id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_training_progress ALTER COLUMN id SET DEFAULT nextval('public.agent_training_progress_id_seq'::regclass);

--
-- Name: app_configurations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_configurations ALTER COLUMN id SET DEFAULT nextval('public.app_configurations_id_seq'::regclass);

--
-- Name: app_guards id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_guards ALTER COLUMN id SET DEFAULT nextval('public.app_guards_id_seq'::regclass);

--
-- Name: app_registry id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_registry ALTER COLUMN id SET DEFAULT nextval('public.app_registry_id_seq'::regclass);

--
-- Name: app_subscriptions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_subscriptions ALTER COLUMN id SET DEFAULT nextval('public.app_subscriptions_id_seq'::regclass);

--
-- Name: certification_levels id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certification_levels ALTER COLUMN id SET DEFAULT nextval('public.certification_levels_id_seq'::regclass);

--
-- Name: conversation_routing id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_routing ALTER COLUMN id SET DEFAULT nextval('public.conversation_routing_id_seq'::regclass);

--
-- Name: customer_notes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_notes ALTER COLUMN id SET DEFAULT nextval('public.customer_notes_id_seq'::regclass);

--
-- Name: device_bindings id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.device_bindings ALTER COLUMN id SET DEFAULT nextval('public.device_bindings_id_seq'::regclass);

--
-- Name: escalation_rules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escalation_rules ALTER COLUMN id SET DEFAULT nextval('public.escalation_rules_id_seq'::regclass);

--
-- Name: guard_rules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.guard_rules ALTER COLUMN id SET DEFAULT nextval('public.guard_rules_id_seq'::regclass);

--
-- Name: license_features id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_features ALTER COLUMN id SET DEFAULT nextval('public.license_features_id_seq'::regclass);

--
-- Name: license_limits id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_limits ALTER COLUMN id SET DEFAULT nextval('public.license_limits_id_seq'::regclass);

--
-- Name: license_modules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_modules ALTER COLUMN id SET DEFAULT nextval('public.license_modules_id_seq'::regclass);

--
-- Name: licenses id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.licenses ALTER COLUMN id SET DEFAULT nextval('public.licenses_id_seq'::regclass);

--
-- Name: routing_rules id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routing_rules ALTER COLUMN id SET DEFAULT nextval('public.routing_rules_id_seq'::regclass);

--
-- Name: supervisor_sessions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supervisor_sessions ALTER COLUMN id SET DEFAULT nextval('public.supervisor_sessions_id_seq'::regclass);

--
-- Name: training_categories id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.training_categories ALTER COLUMN id SET DEFAULT nextval('public.training_categories_id_seq'::regclass);

--
-- Name: training_materials id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.training_materials ALTER COLUMN id SET DEFAULT nextval('public.training_materials_id_seq'::regclass);

--
-- Name: vip_configs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vip_configs ALTER COLUMN id SET DEFAULT nextval('public.vip_configs_id_seq'::regclass);

--
-- Name: agent_certifications agent_certifications_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_certifications
    ADD CONSTRAINT agent_certifications_pkey PRIMARY KEY (id);

--
-- Name: agent_onboarding_progress agent_onboarding_progress_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_onboarding_progress
    ADD CONSTRAINT agent_onboarding_progress_pkey PRIMARY KEY (id);

--
-- Name: agent_onboarding_tasks agent_onboarding_tasks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_onboarding_tasks
    ADD CONSTRAINT agent_onboarding_tasks_pkey PRIMARY KEY (id);

--
-- Name: agent_skills agent_skills_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_skills
    ADD CONSTRAINT agent_skills_pkey PRIMARY KEY (id);

--
-- Name: agent_training_progress agent_training_progress_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agent_training_progress
    ADD CONSTRAINT agent_training_progress_pkey PRIMARY KEY (id);

--
-- Name: app_configurations app_configurations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_configurations
    ADD CONSTRAINT app_configurations_pkey PRIMARY KEY (id);

--
-- Name: app_guards app_guards_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_guards
    ADD CONSTRAINT app_guards_pkey PRIMARY KEY (id);

--
-- Name: app_registry app_registry_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_registry
    ADD CONSTRAINT app_registry_name_key UNIQUE (name);

--
-- Name: app_registry app_registry_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_registry
    ADD CONSTRAINT app_registry_pkey PRIMARY KEY (id);

--
-- Name: app_subscriptions app_subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_subscriptions
    ADD CONSTRAINT app_subscriptions_pkey PRIMARY KEY (id);

--
-- Name: certification_levels certification_levels_level_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certification_levels
    ADD CONSTRAINT certification_levels_level_name_key UNIQUE (level_name);

--
-- Name: certification_levels certification_levels_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.certification_levels
    ADD CONSTRAINT certification_levels_pkey PRIMARY KEY (id);

--
-- Name: conversation_routing conversation_routing_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.conversation_routing
    ADD CONSTRAINT conversation_routing_pkey PRIMARY KEY (id);

--
-- Name: customer_notes customer_notes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.customer_notes
    ADD CONSTRAINT customer_notes_pkey PRIMARY KEY (id);

--
-- Name: device_bindings device_bindings_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.device_bindings
    ADD CONSTRAINT device_bindings_pkey PRIMARY KEY (id);

--
-- Name: escalation_rules escalation_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.escalation_rules
    ADD CONSTRAINT escalation_rules_pkey PRIMARY KEY (id);

--
-- Name: guard_rules guard_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.guard_rules
    ADD CONSTRAINT guard_rules_pkey PRIMARY KEY (id);

--
-- Name: license_features license_features_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_features
    ADD CONSTRAINT license_features_pkey PRIMARY KEY (id);

--
-- Name: license_limits license_limits_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_limits
    ADD CONSTRAINT license_limits_pkey PRIMARY KEY (id);

--
-- Name: license_modules license_modules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_modules
    ADD CONSTRAINT license_modules_pkey PRIMARY KEY (id);

--
-- Name: licenses licenses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.licenses
    ADD CONSTRAINT licenses_pkey PRIMARY KEY (id);

--
-- Name: routing_rules routing_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routing_rules
    ADD CONSTRAINT routing_rules_pkey PRIMARY KEY (id);

--
-- Name: supervisor_sessions supervisor_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.supervisor_sessions
    ADD CONSTRAINT supervisor_sessions_pkey PRIMARY KEY (id);

--
-- Name: training_categories training_categories_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.training_categories
    ADD CONSTRAINT training_categories_pkey PRIMARY KEY (id);

--
-- Name: training_materials training_materials_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.training_materials
    ADD CONSTRAINT training_materials_pkey PRIMARY KEY (id);

--
-- Name: license_features uk_license_features; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_features
    ADD CONSTRAINT uk_license_features UNIQUE (license_id, feature);

--
-- Name: license_limits uk_license_limits; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_limits
    ADD CONSTRAINT uk_license_limits UNIQUE (license_id, limit_key);

--
-- Name: license_modules uk_license_modules; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_modules
    ADD CONSTRAINT uk_license_modules UNIQUE (license_id, module);

--
-- Name: vip_configs vip_configs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vip_configs
    ADD CONSTRAINT vip_configs_pkey PRIMARY KEY (id);

--
-- Name: idx_agent_certifications_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_certifications_agent ON public.agent_certifications USING btree (agent_id);

--
-- Name: idx_agent_onboarding_progress_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_onboarding_progress_agent ON public.agent_onboarding_progress USING btree (agent_id);

--
-- Name: idx_agent_skills_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_skills_agent ON public.agent_skills USING btree (agent_id);

--
-- Name: idx_agent_training_progress_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_agent_training_progress_agent ON public.agent_training_progress USING btree (agent_id);

--
-- Name: idx_app_configurations_app; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_configurations_app ON public.app_configurations USING btree (app_id);

--
-- Name: idx_app_configurations_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_configurations_key ON public.app_configurations USING btree (config_key);

--
-- Name: idx_app_guards_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_guards_active ON public.app_guards USING btree (is_active);

--
-- Name: idx_app_guards_app; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_guards_app ON public.app_guards USING btree (app_id);

--
-- Name: idx_app_guards_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_guards_tenant ON public.app_guards USING btree (tenant_id);

--
-- Name: idx_app_guards_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_guards_type ON public.app_guards USING btree (guard_type);

--
-- Name: idx_app_registry_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_registry_active ON public.app_registry USING btree (is_active);

--
-- Name: idx_app_registry_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_registry_status ON public.app_registry USING btree (status);

--
-- Name: idx_app_registry_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_registry_tenant ON public.app_registry USING btree (tenant_id);

--
-- Name: idx_app_registry_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_registry_type ON public.app_registry USING btree (app_type);

--
-- Name: idx_app_subscriptions_app; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_subscriptions_app ON public.app_subscriptions USING btree (app_id);

--
-- Name: idx_app_subscriptions_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_subscriptions_status ON public.app_subscriptions USING btree (subscription_status);

--
-- Name: idx_app_subscriptions_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_subscriptions_tenant ON public.app_subscriptions USING btree (tenant_id);

--
-- Name: idx_app_subscriptions_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_app_subscriptions_user ON public.app_subscriptions USING btree (user_id);

--
-- Name: idx_conversation_routing_agent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_routing_agent ON public.conversation_routing USING btree (assigned_agent_id);

--
-- Name: idx_conversation_routing_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_routing_conversation ON public.conversation_routing USING btree (conversation_id);

--
-- Name: idx_conversation_routing_queue; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_routing_queue ON public.conversation_routing USING btree (queue_position);

--
-- Name: idx_conversation_routing_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_routing_status ON public.conversation_routing USING btree (routing_status);

--
-- Name: idx_conversation_routing_team; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_routing_team ON public.conversation_routing USING btree (assigned_team_id);

--
-- Name: idx_conversation_routing_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_conversation_routing_tenant ON public.conversation_routing USING btree (tenant_id);

--
-- Name: idx_conversation_routing_unique; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_conversation_routing_unique ON public.conversation_routing USING btree (conversation_id);

--
-- Name: idx_customer_notes_profile; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_customer_notes_profile ON public.customer_notes USING btree (customer_profile_id);

--
-- Name: idx_customer_notes_profile_created; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_customer_notes_profile_created ON public.customer_notes USING btree (customer_profile_id, created_at);

--
-- Name: idx_customer_notes_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_customer_notes_tenant ON public.customer_notes USING btree (tenant_id);

--
-- Name: idx_device_binding_device; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_device_binding_device ON public.device_bindings USING btree (device_id);

--
-- Name: idx_device_binding_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_device_binding_user ON public.device_bindings USING btree (user_id);

--
-- Name: idx_device_binding_user_device; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_device_binding_user_device ON public.device_bindings USING btree (user_id, device_id);

--
-- Name: idx_escalation_rules_action; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_rules_action ON public.escalation_rules USING btree (escalation_action);

--
-- Name: idx_escalation_rules_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_rules_active ON public.escalation_rules USING btree (is_active);

--
-- Name: idx_escalation_rules_priority; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_rules_priority ON public.escalation_rules USING btree (priority);

--
-- Name: idx_escalation_rules_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_rules_tenant ON public.escalation_rules USING btree (tenant_id);

--
-- Name: idx_escalation_rules_trigger_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_escalation_rules_trigger_type ON public.escalation_rules USING btree (trigger_type);

--
-- Name: idx_guard_rules_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_guard_rules_active ON public.guard_rules USING btree (is_active);

--
-- Name: idx_guard_rules_app_guard; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_guard_rules_app_guard ON public.guard_rules USING btree (app_guard_id);

--
-- Name: idx_license_features_license; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_license_features_license ON public.license_features USING btree (license_id);

--
-- Name: idx_license_limits_license; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_license_limits_license ON public.license_limits USING btree (license_id);

--
-- Name: idx_license_modules_license; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_license_modules_license ON public.license_modules USING btree (license_id);

--
-- Name: idx_licenses_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_licenses_active ON public.licenses USING btree (is_active);

--
-- Name: idx_licenses_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_licenses_expires ON public.licenses USING btree (expires_at);

--
-- Name: idx_licenses_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_licenses_user ON public.licenses USING btree (user_id);

--
-- Name: idx_routing_rules_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_rules_active ON public.routing_rules USING btree (is_active);

--
-- Name: idx_routing_rules_default; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_rules_default ON public.routing_rules USING btree (is_default);

--
-- Name: idx_routing_rules_priority; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_rules_priority ON public.routing_rules USING btree (priority);

--
-- Name: idx_routing_rules_target_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_rules_target_type ON public.routing_rules USING btree (target_type);

--
-- Name: idx_routing_rules_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_rules_tenant ON public.routing_rules USING btree (tenant_id);

--
-- Name: idx_routing_rules_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_routing_rules_type ON public.routing_rules USING btree (rule_type);

--
-- Name: idx_supervisor_sessions_conversation; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_supervisor_sessions_conversation ON public.supervisor_sessions USING btree (conversation_id);

--
-- Name: idx_supervisor_sessions_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_supervisor_sessions_status ON public.supervisor_sessions USING btree (session_status);

--
-- Name: idx_supervisor_sessions_supervisor; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_supervisor_sessions_supervisor ON public.supervisor_sessions USING btree (supervisor_id);

--
-- Name: idx_supervisor_sessions_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_supervisor_sessions_tenant ON public.supervisor_sessions USING btree (tenant_id);

--
-- Name: idx_supervisor_sessions_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_supervisor_sessions_type ON public.supervisor_sessions USING btree (session_type);

--
-- Name: idx_supervisor_sessions_unique_active; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_supervisor_sessions_unique_active ON public.supervisor_sessions USING btree (supervisor_id, conversation_id) WHERE ((session_status)::text = 'ACTIVE'::text);

--
-- Name: idx_training_materials_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_training_materials_category ON public.training_materials USING btree (category_id);

--
-- Name: idx_vip_config_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_vip_config_tenant ON public.vip_configs USING btree (tenant_id);

--
-- Name: uk_vip_config_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_vip_config_tenant ON public.vip_configs USING btree (tenant_id);

--
-- Name: app_configurations fk_app_configurations_app; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_configurations
    ADD CONSTRAINT fk_app_configurations_app FOREIGN KEY (app_id) REFERENCES public.app_registry(id) ON DELETE CASCADE;

--
-- Name: guard_rules fk_guard_rules_app_guard; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.guard_rules
    ADD CONSTRAINT fk_guard_rules_app_guard FOREIGN KEY (app_guard_id) REFERENCES public.app_guards(id) ON DELETE CASCADE;

--
-- Name: license_features fk_license_features_license; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_features
    ADD CONSTRAINT fk_license_features_license FOREIGN KEY (license_id) REFERENCES public.licenses(id) ON DELETE CASCADE;

--
-- Name: license_limits fk_license_limits_license; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_limits
    ADD CONSTRAINT fk_license_limits_license FOREIGN KEY (license_id) REFERENCES public.licenses(id) ON DELETE CASCADE;

--
-- Name: license_modules fk_license_modules_license; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.license_modules
    ADD CONSTRAINT fk_license_modules_license FOREIGN KEY (license_id) REFERENCES public.licenses(id) ON DELETE CASCADE;

--
-- PostgreSQL database dump complete
--
