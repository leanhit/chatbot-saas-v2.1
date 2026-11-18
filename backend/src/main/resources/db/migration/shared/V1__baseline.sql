
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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: discount_user_usage; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.discount_user_usage (
    discount_id bigint NOT NULL,
    user_id bigint NOT NULL
);

--
-- Name: discounts; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.discounts (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint,
    code character varying(50) NOT NULL,
    name character varying(100) NOT NULL,
    discount_type character varying(20) NOT NULL,
    discount_value numeric(10,2) NOT NULL,
    minimum_amount numeric(10,2) DEFAULT 0.00 NOT NULL,
    maximum_discount numeric(10,2) DEFAULT 0.00 NOT NULL,
    usage_limit integer DEFAULT 0 NOT NULL,
    usage_count integer DEFAULT 0 NOT NULL,
    usage_limit_per_user integer DEFAULT 0 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    valid_from timestamp without time zone NOT NULL,
    valid_until timestamp without time zone NOT NULL,
    description text,
    applicable_package_id character varying(50),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: discounts_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.discounts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: discounts_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.discounts_id_seq OWNED BY public.discounts.id;

--
-- Name: grpc_call_failures; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.grpc_call_failures (
    id bigint NOT NULL,
    method character varying(512) NOT NULL,
    status_code character varying(32) NOT NULL,
    status_description character varying(1024),
    tenant_id bigint,
    occurred_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: grpc_call_failures_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.grpc_call_failures_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: grpc_call_failures_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.grpc_call_failures_id_seq OWNED BY public.grpc_call_failures.id;

--
-- Name: invoices; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.invoices (
    id bigint NOT NULL,
    invoice_number character varying(50) NOT NULL,
    payment_id bigint NOT NULL,
    user_id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    user_email character varying(100) NOT NULL,
    user_name character varying(100) NOT NULL,
    subtotal numeric(15,2) NOT NULL,
    discount_amount numeric(15,2) DEFAULT 0 NOT NULL,
    tax_amount numeric(15,2) DEFAULT 0 NOT NULL,
    total_amount numeric(15,2) NOT NULL,
    currency character varying(10) DEFAULT 'VND'::character varying NOT NULL,
    discount_code character varying(50),
    package_id character varying(50),
    package_name character varying(100),
    status character varying(50) DEFAULT 'PAID'::character varying NOT NULL,
    notes text,
    payment_method character varying(50),
    payment_reference text,
    due_date timestamp without time zone,
    paid_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: TABLE invoices; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.invoices IS 'Stores billing invoices for payment transactions';

--
-- Name: invoices_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.invoices_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: invoices_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.invoices_id_seq OWNED BY public.invoices.id;

--
-- Name: merchant_api_keys; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.merchant_api_keys (
    id bigint NOT NULL,
    tenant_key character varying(255),
    merchant_id bigint,
    tenant_id bigint NOT NULL,
    name character varying(100) NOT NULL,
    api_key_hash character varying(255),
    api_secret_hash character varying(255),
    webhook_secret character varying(255) NOT NULL,
    webhook_url character varying(500),
    rate_limit_per_minute integer DEFAULT 100 NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    usage_count bigint DEFAULT 0 NOT NULL,
    last_used_at timestamp without time zone,
    expires_at timestamp without time zone,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    api_key character varying(255),
    api_secret character varying(255),
    description character varying(500)
);

--
-- Name: TABLE merchant_api_keys; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.merchant_api_keys IS 'Stores API keys for merchant payment gateway';

--
-- Name: merchant_api_keys_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.merchant_api_keys_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: merchant_api_keys_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.merchant_api_keys_id_seq OWNED BY public.merchant_api_keys.id;

--
-- Name: merchant_payment_sessions; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.merchant_payment_sessions (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    session_id character varying(100) NOT NULL,
    merchant_id bigint NOT NULL,
    merchant_order_id character varying(100) NOT NULL,
    amount numeric(15,2) NOT NULL,
    currency character varying(10) DEFAULT 'VND'::character varying NOT NULL,
    description text,
    return_url character varying(500),
    cancel_url character varying(500),
    metadata text,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    payment_reference_code character varying(100),
    bank_transaction_id character varying(100),
    expires_at timestamp without time zone,
    completed_at timestamp without time zone,
    cancelled_at timestamp without time zone,
    failed_at timestamp without time zone,
    failure_reason character varying(500),
    webhook_sent_at timestamp without time zone,
    webhook_status character varying(50),
    webhook_retry_count integer DEFAULT 0 NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: TABLE merchant_payment_sessions; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.merchant_payment_sessions IS 'Stores payment sessions for merchant transactions';

--
-- Name: merchant_payment_sessions_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.merchant_payment_sessions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: merchant_payment_sessions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.merchant_payment_sessions_id_seq OWNED BY public.merchant_payment_sessions.id;

--
-- Name: package_upgrade_audit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.package_upgrade_audit (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    user_id bigint NOT NULL,
    payment_reference_code character varying(255) NOT NULL,
    from_package_id character varying(255),
    to_package_id character varying(255) NOT NULL,
    payment_amount numeric(15,2) NOT NULL,
    currency character varying(10) DEFAULT 'VND'::character varying NOT NULL,
    bank_transaction_id character varying(255),
    upgrade_status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    failure_reason text,
    processed_at timestamp without time zone,
    ip_address character varying(100),
    user_agent text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone
);

--
-- Name: TABLE package_upgrade_audit; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.package_upgrade_audit IS 'Audit log of subscription package upgrades and downgrades';

--
-- Name: package_upgrade_audit_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.package_upgrade_audit_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: package_upgrade_audit_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.package_upgrade_audit_id_seq OWNED BY public.package_upgrade_audit.id;

--
-- Name: packages; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.packages (
    id bigint NOT NULL,
    package_id character varying(50) NOT NULL,
    name character varying(100) NOT NULL,
    price numeric(10,2) DEFAULT 0.00 NOT NULL,
    currency character varying(10) DEFAULT 'VND'::character varying NOT NULL,
    duration character varying(50) NOT NULL,
    description text,
    message_limit integer NOT NULL,
    chatbot_limit integer NOT NULL,
    has_priority_support boolean DEFAULT false NOT NULL,
    has_analytics boolean DEFAULT false NOT NULL,
    has_advanced_analytics boolean DEFAULT false NOT NULL,
    has_custom_integrations boolean DEFAULT false NOT NULL,
    has_dedicated_support boolean DEFAULT false NOT NULL,
    has_custom_features boolean DEFAULT false NOT NULL,
    has_sla_guarantee boolean DEFAULT false NOT NULL,
    isactive boolean DEFAULT true NOT NULL,
    sort_order integer NOT NULL,
    badge character varying(20),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: packages_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.packages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: packages_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.packages_id_seq OWNED BY public.packages.id;

--
-- Name: payment_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.payment_audit_logs (
    id bigint NOT NULL,
    tenant_key character varying(255),
    payment_reference_code character varying(50) NOT NULL,
    user_id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    action character varying(50) NOT NULL,
    old_status character varying(20),
    new_status character varying(20),
    amount numeric(15,2),
    description text,
    ip_address character varying(45),
    user_agent text,
    request_id character varying(100),
    metadata text,
    created_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: TABLE payment_audit_logs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.payment_audit_logs IS 'Audit log table for tracking all payment operations';

--
-- Name: COLUMN payment_audit_logs.action; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_audit_logs.action IS 'Action type: PAYMENT_CREATED, PAYMENT_COMPLETED, PAYMENT_FAILED, etc.';

--
-- Name: COLUMN payment_audit_logs.metadata; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.payment_audit_logs.metadata IS 'Additional metadata in JSONB format';

--
-- Name: payment_audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payment_audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: payment_audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.payment_audit_logs_id_seq OWNED BY public.payment_audit_logs.id;

--
-- Name: simple_payments; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.simple_payments (
    id bigint NOT NULL,
    tenant_key character varying(255),
    user_id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    amount numeric(15,2) NOT NULL,
    currency character varying(10) DEFAULT 'VND'::character varying NOT NULL,
    reference_code character varying(50) NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    bank_transaction_id character varying(100),
    description text,
    qr_content text,
    expires_at timestamp without time zone NOT NULL,
    target_package_id character varying(100),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    completed_at timestamp without time zone,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: simple_payments_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.simple_payments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: simple_payments_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.simple_payments_id_seq OWNED BY public.simple_payments.id;

--
-- Name: webhook_dead_letters; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.webhook_dead_letters (
    id bigint NOT NULL,
    webhook_id bigint NOT NULL,
    webhook_name character varying(100) NOT NULL,
    webhook_url character varying(500) NOT NULL,
    event_type character varying(50) NOT NULL,
    payment_reference_code character varying(100) NOT NULL,
    retry_attempts integer NOT NULL,
    payload text NOT NULL,
    last_error text NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying,
    processed_at timestamp without time zone,
    processed_by character varying(255),
    processing_notes text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: TABLE webhook_dead_letters; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.webhook_dead_letters IS 'Dead letter queue for failed webhooks';

--
-- Name: COLUMN webhook_dead_letters.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.webhook_dead_letters.status IS 'Processing status (PENDING, PROCESSED, DISCARDED)';

--
-- Name: webhook_dead_letters_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.webhook_dead_letters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: webhook_dead_letters_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.webhook_dead_letters_id_seq OWNED BY public.webhook_dead_letters.id;

--
-- Name: webhook_dispatch_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.webhook_dispatch_logs (
    id bigint NOT NULL,
    tenant_key character varying(255),
    merchant_id bigint NOT NULL,
    session_id character varying(100) NOT NULL,
    webhook_url character varying(500) NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    retry_count integer DEFAULT 0 NOT NULL,
    max_retries integer DEFAULT 5 NOT NULL,
    next_retry_at timestamp without time zone,
    error_message text,
    request_payload text,
    response_payload text,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: TABLE webhook_dispatch_logs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.webhook_dispatch_logs IS 'Stores webhook dispatch logs with retry logic';

--
-- Name: webhook_dispatch_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.webhook_dispatch_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: webhook_dispatch_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.webhook_dispatch_logs_id_seq OWNED BY public.webhook_dispatch_logs.id;

--
-- Name: webhook_events; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.webhook_events (
    webhook_id bigint NOT NULL,
    event_type character varying(50) NOT NULL
);

--
-- Name: TABLE webhook_events; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.webhook_events IS 'Webhook event types collection';

--
-- Name: COLUMN webhook_events.event_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.webhook_events.event_type IS 'Event type (PAYMENT_CREATED, PAYMENT_COMPLETED, etc.)';

--
-- Name: webhooks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.webhooks (
    id bigint NOT NULL,
    name character varying(100) NOT NULL,
    url character varying(500) NOT NULL,
    secret character varying(50) NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    retry_count integer DEFAULT 3 NOT NULL,
    current_retry_attempt integer DEFAULT 0 NOT NULL,
    timeout_seconds integer DEFAULT 10 NOT NULL,
    next_retry_at timestamp without time zone,
    last_error text,
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    last_triggered_at timestamp without time zone,
    success_count integer DEFAULT 0,
    failure_count integer DEFAULT 0,
    description text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: TABLE webhooks; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.webhooks IS 'Webhook configuration for payment events';

--
-- Name: COLUMN webhooks.url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.webhooks.url IS 'Webhook endpoint URL';

--
-- Name: COLUMN webhooks.secret; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.webhooks.secret IS 'Webhook secret for signature verification';

--
-- Name: COLUMN webhooks.status; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.webhooks.status IS 'Webhook status (ACTIVE, FAILED, DISABLED)';

--
-- Name: webhooks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.webhooks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: webhooks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.webhooks_id_seq OWNED BY public.webhooks.id;

--
-- Name: discounts id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.discounts ALTER COLUMN id SET DEFAULT nextval('public.discounts_id_seq'::regclass);

--
-- Name: grpc_call_failures id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grpc_call_failures ALTER COLUMN id SET DEFAULT nextval('public.grpc_call_failures_id_seq'::regclass);

--
-- Name: invoices id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices ALTER COLUMN id SET DEFAULT nextval('public.invoices_id_seq'::regclass);

--
-- Name: merchant_api_keys id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_api_keys ALTER COLUMN id SET DEFAULT nextval('public.merchant_api_keys_id_seq'::regclass);

--
-- Name: merchant_payment_sessions id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_payment_sessions ALTER COLUMN id SET DEFAULT nextval('public.merchant_payment_sessions_id_seq'::regclass);

--
-- Name: package_upgrade_audit id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.package_upgrade_audit ALTER COLUMN id SET DEFAULT nextval('public.package_upgrade_audit_id_seq'::regclass);

--
-- Name: packages id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.packages ALTER COLUMN id SET DEFAULT nextval('public.packages_id_seq'::regclass);

--
-- Name: payment_audit_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_audit_logs ALTER COLUMN id SET DEFAULT nextval('public.payment_audit_logs_id_seq'::regclass);

--
-- Name: simple_payments id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simple_payments ALTER COLUMN id SET DEFAULT nextval('public.simple_payments_id_seq'::regclass);

--
-- Name: webhook_dead_letters id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhook_dead_letters ALTER COLUMN id SET DEFAULT nextval('public.webhook_dead_letters_id_seq'::regclass);

--
-- Name: webhook_dispatch_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhook_dispatch_logs ALTER COLUMN id SET DEFAULT nextval('public.webhook_dispatch_logs_id_seq'::regclass);

--
-- Name: webhooks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhooks ALTER COLUMN id SET DEFAULT nextval('public.webhooks_id_seq'::regclass);

--
-- Name: discount_user_usage discount_user_usage_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.discount_user_usage
    ADD CONSTRAINT discount_user_usage_pkey PRIMARY KEY (discount_id, user_id);

--
-- Name: discounts discounts_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.discounts
    ADD CONSTRAINT discounts_code_key UNIQUE (code);

--
-- Name: discounts discounts_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.discounts
    ADD CONSTRAINT discounts_pkey PRIMARY KEY (id);

--
-- Name: grpc_call_failures grpc_call_failures_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grpc_call_failures
    ADD CONSTRAINT grpc_call_failures_pkey PRIMARY KEY (id);

--
-- Name: invoices invoices_invoice_number_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_invoice_number_key UNIQUE (invoice_number);

--
-- Name: invoices invoices_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.invoices
    ADD CONSTRAINT invoices_pkey PRIMARY KEY (id);

--
-- Name: merchant_api_keys merchant_api_keys_api_key_hash_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_api_keys
    ADD CONSTRAINT merchant_api_keys_api_key_hash_key UNIQUE (api_key_hash);

--
-- Name: merchant_api_keys merchant_api_keys_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_api_keys
    ADD CONSTRAINT merchant_api_keys_pkey PRIMARY KEY (id);

--
-- Name: merchant_payment_sessions merchant_payment_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_payment_sessions
    ADD CONSTRAINT merchant_payment_sessions_pkey PRIMARY KEY (id);

--
-- Name: merchant_payment_sessions merchant_payment_sessions_session_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.merchant_payment_sessions
    ADD CONSTRAINT merchant_payment_sessions_session_id_key UNIQUE (session_id);

--
-- Name: package_upgrade_audit package_upgrade_audit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.package_upgrade_audit
    ADD CONSTRAINT package_upgrade_audit_pkey PRIMARY KEY (id);

--
-- Name: packages packages_package_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.packages
    ADD CONSTRAINT packages_package_id_key UNIQUE (package_id);

--
-- Name: packages packages_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.packages
    ADD CONSTRAINT packages_pkey PRIMARY KEY (id);

--
-- Name: payment_audit_logs payment_audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.payment_audit_logs
    ADD CONSTRAINT payment_audit_logs_pkey PRIMARY KEY (id);

--
-- Name: simple_payments simple_payments_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simple_payments
    ADD CONSTRAINT simple_payments_pkey PRIMARY KEY (id);

--
-- Name: simple_payments simple_payments_reference_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.simple_payments
    ADD CONSTRAINT simple_payments_reference_code_key UNIQUE (reference_code);

--
-- Name: webhook_dead_letters webhook_dead_letters_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhook_dead_letters
    ADD CONSTRAINT webhook_dead_letters_pkey PRIMARY KEY (id);

--
-- Name: webhook_dispatch_logs webhook_dispatch_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhook_dispatch_logs
    ADD CONSTRAINT webhook_dispatch_logs_pkey PRIMARY KEY (id);

--
-- Name: webhook_events webhook_events_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhook_events
    ADD CONSTRAINT webhook_events_pkey PRIMARY KEY (webhook_id, event_type);

--
-- Name: webhooks webhooks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhooks
    ADD CONSTRAINT webhooks_pkey PRIMARY KEY (id);

--
-- Name: webhooks webhooks_url_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhooks
    ADD CONSTRAINT webhooks_url_key UNIQUE (url);

--
-- Name: idx_discounts_applicable_package; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_discounts_applicable_package ON public.discounts USING btree (applicable_package_id);

--
-- Name: idx_discounts_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_discounts_code ON public.discounts USING btree (code);

--
-- Name: idx_discounts_is_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_discounts_is_active ON public.discounts USING btree (is_active);

--
-- Name: idx_discounts_valid_period; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_discounts_valid_period ON public.discounts USING btree (valid_from, valid_until);

--
-- Name: idx_grpc_fail_method; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_grpc_fail_method ON public.grpc_call_failures USING btree (method);

--
-- Name: idx_grpc_fail_occurred; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_grpc_fail_occurred ON public.grpc_call_failures USING btree (occurred_at);

--
-- Name: idx_invoices_invoice_number; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_invoices_invoice_number ON public.invoices USING btree (invoice_number);

--
-- Name: idx_invoices_payment_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_invoices_payment_id ON public.invoices USING btree (payment_id);

--
-- Name: idx_invoices_tenant_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_invoices_tenant_id ON public.invoices USING btree (tenant_id);

--
-- Name: idx_invoices_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_invoices_user_id ON public.invoices USING btree (user_id);

--
-- Name: idx_merchant_api_keys_api_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_merchant_api_keys_api_key ON public.merchant_api_keys USING btree (api_key);

--
-- Name: idx_merchant_api_keys_api_key_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_api_keys_api_key_hash ON public.merchant_api_keys USING btree (api_key_hash);

--
-- Name: idx_merchant_api_keys_is_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_api_keys_is_active ON public.merchant_api_keys USING btree (is_active);

--
-- Name: idx_merchant_api_keys_merchant_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_api_keys_merchant_id ON public.merchant_api_keys USING btree (merchant_id);

--
-- Name: idx_merchant_api_keys_tenant_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_api_keys_tenant_id ON public.merchant_api_keys USING btree (tenant_id);

--
-- Name: idx_merchant_payment_sessions_merchant_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_payment_sessions_merchant_id ON public.merchant_payment_sessions USING btree (merchant_id);

--
-- Name: idx_merchant_payment_sessions_merchant_order_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_payment_sessions_merchant_order_id ON public.merchant_payment_sessions USING btree (merchant_order_id);

--
-- Name: idx_merchant_payment_sessions_payment_reference_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_payment_sessions_payment_reference_code ON public.merchant_payment_sessions USING btree (payment_reference_code);

--
-- Name: idx_merchant_payment_sessions_session_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_payment_sessions_session_id ON public.merchant_payment_sessions USING btree (session_id);

--
-- Name: idx_merchant_payment_sessions_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_merchant_payment_sessions_status ON public.merchant_payment_sessions USING btree (status);

--
-- Name: idx_package_upgrade_audit_ref_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_package_upgrade_audit_ref_code ON public.package_upgrade_audit USING btree (payment_reference_code);

--
-- Name: idx_package_upgrade_audit_tenant_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_package_upgrade_audit_tenant_id ON public.package_upgrade_audit USING btree (tenant_id);

--
-- Name: idx_package_upgrade_audit_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_package_upgrade_audit_user_id ON public.package_upgrade_audit USING btree (user_id);

--
-- Name: idx_packages_isactive; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_packages_isactive ON public.packages USING btree (isactive);

--
-- Name: idx_packages_package_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_packages_package_id ON public.packages USING btree (package_id);

--
-- Name: idx_packages_sort_order; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_packages_sort_order ON public.packages USING btree (sort_order);

--
-- Name: idx_payment_audit_logs_action; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_audit_logs_action ON public.payment_audit_logs USING btree (action);

--
-- Name: idx_payment_audit_logs_created_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_audit_logs_created_at ON public.payment_audit_logs USING btree (created_at);

--
-- Name: idx_payment_audit_logs_payment_reference; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_audit_logs_payment_reference ON public.payment_audit_logs USING btree (payment_reference_code);

--
-- Name: idx_payment_audit_logs_tenant_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_audit_logs_tenant_id ON public.payment_audit_logs USING btree (tenant_id);

--
-- Name: idx_payment_audit_logs_user_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_payment_audit_logs_user_id ON public.payment_audit_logs USING btree (user_id);

--
-- Name: idx_simple_payments_expires; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_simple_payments_expires ON public.simple_payments USING btree (expires_at);

--
-- Name: idx_simple_payments_reference; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_simple_payments_reference ON public.simple_payments USING btree (reference_code);

--
-- Name: idx_simple_payments_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_simple_payments_status ON public.simple_payments USING btree (status);

--
-- Name: idx_simple_payments_user_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_simple_payments_user_tenant ON public.simple_payments USING btree (user_id, tenant_id);

--
-- Name: idx_webhook_dead_letters_payment; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhook_dead_letters_payment ON public.webhook_dead_letters USING btree (payment_reference_code);

--
-- Name: idx_webhook_dead_letters_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhook_dead_letters_status ON public.webhook_dead_letters USING btree (status);

--
-- Name: idx_webhook_dead_letters_webhook; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhook_dead_letters_webhook ON public.webhook_dead_letters USING btree (webhook_id);

--
-- Name: idx_webhook_dispatch_logs_merchant_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhook_dispatch_logs_merchant_id ON public.webhook_dispatch_logs USING btree (merchant_id);

--
-- Name: idx_webhook_dispatch_logs_next_retry_at; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhook_dispatch_logs_next_retry_at ON public.webhook_dispatch_logs USING btree (next_retry_at);

--
-- Name: idx_webhook_dispatch_logs_session_id; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhook_dispatch_logs_session_id ON public.webhook_dispatch_logs USING btree (session_id);

--
-- Name: idx_webhook_dispatch_logs_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhook_dispatch_logs_status ON public.webhook_dispatch_logs USING btree (status);

--
-- Name: idx_webhook_events_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhook_events_type ON public.webhook_events USING btree (event_type);

--
-- Name: idx_webhooks_is_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhooks_is_active ON public.webhooks USING btree (is_active);

--
-- Name: idx_webhooks_next_retry; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhooks_next_retry ON public.webhooks USING btree (next_retry_at);

--
-- Name: idx_webhooks_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhooks_status ON public.webhooks USING btree (status);

--
-- Name: idx_webhooks_url; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_webhooks_url ON public.webhooks USING btree (url);

--
-- Name: discount_user_usage fk_discount_user_usage_discount; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.discount_user_usage
    ADD CONSTRAINT fk_discount_user_usage_discount FOREIGN KEY (discount_id) REFERENCES public.discounts(id) ON DELETE CASCADE;

--
-- Name: webhook_events fk_webhook_events_webhook; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.webhook_events
    ADD CONSTRAINT fk_webhook_events_webhook FOREIGN KEY (webhook_id) REFERENCES public.webhooks(id) ON DELETE CASCADE;

--
-- PostgreSQL database dump complete
--
