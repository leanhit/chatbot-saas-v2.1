
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
-- Name: data_deletion_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_deletion_requests (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    tenant_key character varying(255),
    requested_by character varying(255) NOT NULL,
    reason text,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    scheduled_at timestamp without time zone,
    processed_at timestamp without time zone,
    processed_by character varying(255),
    result_summary text,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);

--
-- Name: data_deletion_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.data_deletion_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: data_deletion_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.data_deletion_requests_id_seq OWNED BY public.data_deletion_requests.id;

--
-- Name: tenant_api_keys; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_api_keys (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    key_prefix character varying(20) NOT NULL,
    key_hash character varying(128) NOT NULL,
    role character varying(20) DEFAULT 'MEMBER'::character varying NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    last_used_at timestamp without time zone,
    usage_count bigint DEFAULT 0 NOT NULL,
    expires_at timestamp without time zone,
    created_by character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);

--
-- Name: tenant_api_keys_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_api_keys_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenant_api_keys_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_api_keys_id_seq OWNED BY public.tenant_api_keys.id;

--
-- Name: tenant_audit_logs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_audit_logs (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    user_id character varying(255) NOT NULL,
    action character varying(255) NOT NULL,
    details text,
    ip_address character varying(100),
    user_agent character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: tenant_audit_logs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_audit_logs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenant_audit_logs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_audit_logs_id_seq OWNED BY public.tenant_audit_logs.id;

--
-- Name: tenant_invitations; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_invitations (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    email character varying(255) NOT NULL,
    role character varying(50) NOT NULL,
    token character varying(255) NOT NULL,
    status character varying(50) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    expires_at timestamp without time zone,
    invited_by bigint
);

--
-- Name: tenant_invitations_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_invitations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenant_invitations_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_invitations_id_seq OWNED BY public.tenant_invitations.id;

--
-- Name: tenant_join_requests; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_join_requests (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    user_id bigint NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: tenant_join_requests_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_join_requests_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenant_join_requests_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_join_requests_id_seq OWNED BY public.tenant_join_requests.id;

--
-- Name: tenant_members; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_members (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    user_id bigint NOT NULL,
    role character varying(50) NOT NULL,
    status character varying(50) DEFAULT 'PENDING'::character varying NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    joined_at timestamp without time zone
);

--
-- Name: tenant_members_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_members_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenant_members_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_members_id_seq OWNED BY public.tenant_members.id;

--
-- Name: tenant_professionals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_professionals (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    provider_type character varying(50) NOT NULL,
    professional_id character varying(255) NOT NULL,
    specialty character varying(255),
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    job_title character varying(255),
    department character varying(255),
    company character varying(200),
    linkedin_url character varying(255),
    website character varying(255),
    location character varying(255),
    skills text,
    experience text,
    education text,
    certifications text,
    languages character varying(255),
    availability character varying(255),
    hourly_rate character varying(100),
    portfolio_url character varying(255)
);

--
-- Name: tenant_professionals_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_professionals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenant_professionals_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_professionals_id_seq OWNED BY public.tenant_professionals.id;

--
-- Name: tenant_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_profiles (
    tenant_id bigint NOT NULL,
    tenant_key character varying(255),
    description character varying(1000),
    industry character varying(100),
    plan character varying(50),
    company_size character varying(50),
    legal_name character varying(255),
    tax_code character varying(255),
    contact_email character varying(255),
    contact_phone character varying(255),
    website character varying(255),
    logo_url character varying(255),
    favicon_url character varying(255),
    primary_color character varying(50),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: tenant_webhook_deliveries; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_webhook_deliveries (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    webhook_id bigint NOT NULL,
    event_type character varying(255) NOT NULL,
    conversation_id bigint,
    payload text NOT NULL,
    status character varying(20) DEFAULT 'PENDING'::character varying NOT NULL,
    attempts integer DEFAULT 0 NOT NULL,
    next_retry_at timestamp without time zone DEFAULT now() NOT NULL,
    last_error text,
    delivered_at timestamp without time zone,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL,
    delivery_key character varying(64)
);

--
-- Name: tenant_webhook_deliveries_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_webhook_deliveries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenant_webhook_deliveries_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_webhook_deliveries_id_seq OWNED BY public.tenant_webhook_deliveries.id;

--
-- Name: tenant_webhooks; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenant_webhooks (
    id bigint NOT NULL,
    tenant_id bigint NOT NULL,
    url character varying(2048) NOT NULL,
    secret character varying(255),
    events text,
    is_active boolean DEFAULT true NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    updated_at timestamp without time zone DEFAULT now() NOT NULL
);

--
-- Name: tenant_webhooks_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenant_webhooks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenant_webhooks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenant_webhooks_id_seq OWNED BY public.tenant_webhooks.id;

--
-- Name: tenants; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tenants (
    id bigint NOT NULL,
    tenantkey character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    status character varying(50) DEFAULT 'ACTIVE'::character varying NOT NULL,
    visibility character varying(50) DEFAULT 'PUBLIC'::character varying NOT NULL,
    expires_at timestamp without time zone,
    current_package_id character varying(100),
    package_activated_at timestamp without time zone,
    created_by character varying(255),
    updated_by character varying(255),
    createdat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updatedat timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: tenants_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tenants_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: tenants_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tenants_id_seq OWNED BY public.tenants.id;

--
-- Name: data_deletion_requests id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_deletion_requests ALTER COLUMN id SET DEFAULT nextval('public.data_deletion_requests_id_seq'::regclass);

--
-- Name: tenant_api_keys id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_api_keys ALTER COLUMN id SET DEFAULT nextval('public.tenant_api_keys_id_seq'::regclass);

--
-- Name: tenant_audit_logs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_audit_logs ALTER COLUMN id SET DEFAULT nextval('public.tenant_audit_logs_id_seq'::regclass);

--
-- Name: tenant_invitations id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_invitations ALTER COLUMN id SET DEFAULT nextval('public.tenant_invitations_id_seq'::regclass);

--
-- Name: tenant_join_requests id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_join_requests ALTER COLUMN id SET DEFAULT nextval('public.tenant_join_requests_id_seq'::regclass);

--
-- Name: tenant_members id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_members ALTER COLUMN id SET DEFAULT nextval('public.tenant_members_id_seq'::regclass);

--
-- Name: tenant_professionals id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_professionals ALTER COLUMN id SET DEFAULT nextval('public.tenant_professionals_id_seq'::regclass);

--
-- Name: tenant_webhook_deliveries id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_webhook_deliveries ALTER COLUMN id SET DEFAULT nextval('public.tenant_webhook_deliveries_id_seq'::regclass);

--
-- Name: tenant_webhooks id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_webhooks ALTER COLUMN id SET DEFAULT nextval('public.tenant_webhooks_id_seq'::regclass);

--
-- Name: tenants id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenants ALTER COLUMN id SET DEFAULT nextval('public.tenants_id_seq'::regclass);

--
-- Name: data_deletion_requests data_deletion_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_deletion_requests
    ADD CONSTRAINT data_deletion_requests_pkey PRIMARY KEY (id);

--
-- Name: tenant_api_keys tenant_api_keys_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_api_keys
    ADD CONSTRAINT tenant_api_keys_pkey PRIMARY KEY (id);

--
-- Name: tenant_audit_logs tenant_audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_audit_logs
    ADD CONSTRAINT tenant_audit_logs_pkey PRIMARY KEY (id);

--
-- Name: tenant_invitations tenant_invitations_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_invitations
    ADD CONSTRAINT tenant_invitations_pkey PRIMARY KEY (id);

--
-- Name: tenant_join_requests tenant_join_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_join_requests
    ADD CONSTRAINT tenant_join_requests_pkey PRIMARY KEY (id);

--
-- Name: tenant_members tenant_members_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_members
    ADD CONSTRAINT tenant_members_pkey PRIMARY KEY (id);

--
-- Name: tenant_professionals tenant_professionals_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_professionals
    ADD CONSTRAINT tenant_professionals_pkey PRIMARY KEY (id);

--
-- Name: tenant_profiles tenant_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_profiles
    ADD CONSTRAINT tenant_profiles_pkey PRIMARY KEY (tenant_id);

--
-- Name: tenant_webhook_deliveries tenant_webhook_deliveries_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_webhook_deliveries
    ADD CONSTRAINT tenant_webhook_deliveries_pkey PRIMARY KEY (id);

--
-- Name: tenant_webhooks tenant_webhooks_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_webhooks
    ADD CONSTRAINT tenant_webhooks_pkey PRIMARY KEY (id);

--
-- Name: tenants tenants_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenants
    ADD CONSTRAINT tenants_pkey PRIMARY KEY (id);

--
-- Name: tenants tenants_tenantkey_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenants
    ADD CONSTRAINT tenants_tenantkey_key UNIQUE (tenantkey);

--
-- Name: tenant_professionals uk_tenant_professional; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_professionals
    ADD CONSTRAINT uk_tenant_professional UNIQUE (tenant_id, provider_type, professional_id);

--
-- Name: tenant_members uk_tenant_user; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tenant_members
    ADD CONSTRAINT uk_tenant_user UNIQUE (tenant_id, user_id);

--
-- Name: idx_deletion_req_due; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_deletion_req_due ON public.data_deletion_requests USING btree (status, scheduled_at);

--
-- Name: idx_deletion_req_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_deletion_req_tenant ON public.data_deletion_requests USING btree (tenant_id, status);

--
-- Name: idx_tenant_api_keys_hash; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX idx_tenant_api_keys_hash ON public.tenant_api_keys USING btree (key_hash);

--
-- Name: idx_tenant_api_keys_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_api_keys_tenant ON public.tenant_api_keys USING btree (tenant_id);

--
-- Name: idx_tenant_audit_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_audit_tenant ON public.tenant_audit_logs USING btree (tenant_id);

--
-- Name: idx_tenant_audit_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_audit_user ON public.tenant_audit_logs USING btree (user_id);

--
-- Name: idx_tenant_invitations_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_invitations_email ON public.tenant_invitations USING btree (email);

--
-- Name: idx_tenant_invitations_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_invitations_tenant ON public.tenant_invitations USING btree (tenant_id);

--
-- Name: idx_tenant_invitations_token; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_invitations_token ON public.tenant_invitations USING btree (token);

--
-- Name: idx_tenant_join_requests_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_join_requests_tenant ON public.tenant_join_requests USING btree (tenant_id);

--
-- Name: idx_tenant_join_requests_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_join_requests_user ON public.tenant_join_requests USING btree (user_id);

--
-- Name: idx_tenant_members_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_members_status ON public.tenant_members USING btree (status);

--
-- Name: idx_tenant_members_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_members_tenant ON public.tenant_members USING btree (tenant_id);

--
-- Name: idx_tenant_members_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_members_user ON public.tenant_members USING btree (user_id);

--
-- Name: idx_tenant_professional_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_professional_tenant ON public.tenant_professionals USING btree (tenant_id);

--
-- Name: idx_tenant_profile_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_profile_tenant ON public.tenant_profiles USING btree (tenant_id);

--
-- Name: idx_tenant_webhook_deliveries_retry; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_webhook_deliveries_retry ON public.tenant_webhook_deliveries USING btree (status, next_retry_at) WHERE ((status)::text = 'PENDING'::text);

--
-- Name: idx_tenant_webhook_deliveries_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_webhook_deliveries_tenant ON public.tenant_webhook_deliveries USING btree (tenant_id);

--
-- Name: idx_tenant_webhooks_tenant_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenant_webhooks_tenant_active ON public.tenant_webhooks USING btree (tenant_id) WHERE (is_active = true);

--
-- Name: idx_tenants_status; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenants_status ON public.tenants USING btree (status);

--
-- Name: idx_tenants_tenantkey; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenants_tenantkey ON public.tenants USING btree (tenantkey);

--
-- Name: idx_tenants_visibility; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_tenants_visibility ON public.tenants USING btree (visibility);

--
-- Name: uq_tenant_webhook_deliveries_key; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_tenant_webhook_deliveries_key ON public.tenant_webhook_deliveries USING btree (delivery_key);

--
-- PostgreSQL database dump complete
--
