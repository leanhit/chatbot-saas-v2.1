
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
-- Name: environment_configs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.environment_configs (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    environment character varying(50) NOT NULL,
    config_key character varying(200) NOT NULL,
    config_value text,
    is_encrypted boolean DEFAULT false NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    description character varying(500),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: environment_configs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.environment_configs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: environment_configs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.environment_configs_id_seq OWNED BY public.environment_configs.id;

--
-- Name: runtime_configs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.runtime_configs (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    config_key character varying(200) NOT NULL,
    config_value text,
    default_value text,
    config_type character varying(50) NOT NULL,
    config_scope character varying(50) NOT NULL,
    user_id bigint,
    is_encrypted boolean DEFAULT false NOT NULL,
    is_readonly boolean DEFAULT false NOT NULL,
    description character varying(500),
    version integer DEFAULT 1 NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: TABLE runtime_configs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.runtime_configs IS 'Runtime configuration storage for tenant-specific settings';

--
-- Name: COLUMN runtime_configs.config_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.runtime_configs.config_key IS 'Unique configuration key';

--
-- Name: COLUMN runtime_configs.config_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.runtime_configs.config_value IS 'Configuration value (may be encrypted)';

--
-- Name: COLUMN runtime_configs.config_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.runtime_configs.config_type IS 'Configuration type (STRING, INTEGER, BOOLEAN, JSON)';

--
-- Name: COLUMN runtime_configs.config_scope; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.runtime_configs.config_scope IS 'Configuration scope (SYSTEM, TENANT, USER)';

--
-- Name: COLUMN runtime_configs.version; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.runtime_configs.version IS 'Configuration version for optimistic locking';

--
-- Name: runtime_configs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.runtime_configs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: runtime_configs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.runtime_configs_id_seq OWNED BY public.runtime_configs.id;

--
-- Name: system_configs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.system_configs (
    id bigint NOT NULL,
    config_key character varying(100) NOT NULL,
    config_value text NOT NULL,
    config_category character varying(50) DEFAULT 'GENERAL'::character varying NOT NULL,
    config_type character varying(20) DEFAULT 'STRING'::character varying NOT NULL,
    description text,
    is_sensitive boolean DEFAULT false NOT NULL,
    is_encrypted boolean DEFAULT false NOT NULL,
    updated_by character varying(100),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone,
    CONSTRAINT chk_config_type CHECK (((config_type)::text = ANY ((ARRAY['STRING'::character varying, 'INTEGER'::character varying, 'BOOLEAN'::character varying, 'JSON'::character varying])::text[])))
);

--
-- Name: TABLE system_configs; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.system_configs IS 'System configuration storage for runtime settings including bank API configuration';

--
-- Name: COLUMN system_configs.config_key; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_configs.config_key IS 'Unique configuration key (e.g., payment.bank-api.provider)';

--
-- Name: COLUMN system_configs.config_value; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_configs.config_value IS 'Configuration value (may be encrypted if is_encrypted=true)';

--
-- Name: COLUMN system_configs.config_category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_configs.config_category IS 'Configuration category for grouping (e.g., BANK, WEBHOOK, EMAIL)';

--
-- Name: COLUMN system_configs.config_type; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_configs.config_type IS 'Data type of the configuration value';

--
-- Name: COLUMN system_configs.is_sensitive; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_configs.is_sensitive IS 'Whether this config contains sensitive data (API keys, secrets)';

--
-- Name: COLUMN system_configs.is_encrypted; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_configs.is_encrypted IS 'Whether the config value is encrypted in storage';

--
-- Name: COLUMN system_configs.updated_by; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.system_configs.updated_by IS 'Username of the admin who last updated this config';

--
-- Name: system_configs_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.system_configs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: system_configs_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.system_configs_id_seq OWNED BY public.system_configs.id;

--
-- Name: environment_configs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environment_configs ALTER COLUMN id SET DEFAULT nextval('public.environment_configs_id_seq'::regclass);

--
-- Name: runtime_configs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.runtime_configs ALTER COLUMN id SET DEFAULT nextval('public.runtime_configs_id_seq'::regclass);

--
-- Name: system_configs id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_configs ALTER COLUMN id SET DEFAULT nextval('public.system_configs_id_seq'::regclass);

--
-- Name: environment_configs environment_configs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.environment_configs
    ADD CONSTRAINT environment_configs_pkey PRIMARY KEY (id);

--
-- Name: runtime_configs runtime_configs_config_key_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.runtime_configs
    ADD CONSTRAINT runtime_configs_config_key_key UNIQUE (config_key);

--
-- Name: runtime_configs runtime_configs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.runtime_configs
    ADD CONSTRAINT runtime_configs_pkey PRIMARY KEY (id);

--
-- Name: system_configs system_configs_config_key_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_configs
    ADD CONSTRAINT system_configs_config_key_key UNIQUE (config_key);

--
-- Name: system_configs system_configs_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.system_configs
    ADD CONSTRAINT system_configs_pkey PRIMARY KEY (id);

--
-- Name: idx_environment_configs_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_environment_configs_active ON public.environment_configs USING btree (is_active);

--
-- Name: idx_environment_configs_env; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_environment_configs_env ON public.environment_configs USING btree (environment);

--
-- Name: idx_environment_configs_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_environment_configs_key ON public.environment_configs USING btree (config_key);

--
-- Name: idx_environment_configs_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_environment_configs_tenant ON public.environment_configs USING btree (tenant_id);

--
-- Name: idx_runtime_configs_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_runtime_configs_key ON public.runtime_configs USING btree (config_key);

--
-- Name: idx_runtime_configs_scope; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_runtime_configs_scope ON public.runtime_configs USING btree (config_scope);

--
-- Name: idx_runtime_configs_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_runtime_configs_tenant ON public.runtime_configs USING btree (tenant_id);

--
-- Name: idx_runtime_configs_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_runtime_configs_type ON public.runtime_configs USING btree (config_type);

--
-- Name: idx_runtime_configs_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_runtime_configs_user ON public.runtime_configs USING btree (user_id);

--
-- Name: idx_system_configs_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_system_configs_category ON public.system_configs USING btree (config_category);

--
-- Name: idx_system_configs_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_system_configs_key ON public.system_configs USING btree (config_key);

--
-- Name: idx_system_configs_sensitive; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_system_configs_sensitive ON public.system_configs USING btree (is_sensitive);

--
-- PostgreSQL database dump complete
--
