
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
-- Name: category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.category (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    name character varying(255) NOT NULL,
    description text,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: TABLE category; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.category IS 'File category for organizing uploaded files';

--
-- Name: COLUMN category.name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.category.name IS 'Category name (unique)';

--
-- Name: facebook_connection; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.facebook_connection (
    id uuid NOT NULL,
    tenant_key character varying(255),
    bot_id character varying(255),
    bot_name character varying(255),
    owner_id character varying(255),
    page_id character varying(255),
    fanpage_url text,
    page_access_token text,
    user_access_token text,
    token_updated_at timestamp without time zone,
    token_expires_at timestamp without time zone,
    fb_user_id character varying(255),
    is_enabled boolean DEFAULT true,
    is_active boolean DEFAULT true,
    chatbot_provider character varying(20) DEFAULT 'PENNYBOT'::character varying,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    tenant_id bigint NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL,
    is_healthy boolean DEFAULT true,
    last_used_at timestamp without time zone,
    last_health_check_at timestamp without time zone,
    health_check_failures integer DEFAULT 0
);

--
-- Name: facebook_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.facebook_users (
    id bigint NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    psid character varying(255) NOT NULL,
    name character varying(255),
    profile_pic character varying(1000),
    odoo_partner_id integer,
    page_id character varying(255) NOT NULL,
    last_interaction timestamp without time zone,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: TABLE facebook_users; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.facebook_users IS 'Facebook user information';

--
-- Name: COLUMN facebook_users.psid; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.facebook_users.psid IS 'Facebook Page-scoped ID';

--
-- Name: COLUMN facebook_users.odoo_partner_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.facebook_users.odoo_partner_id IS 'Odoo partner ID for integration';

--
-- Name: COLUMN facebook_users.page_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.facebook_users.page_id IS 'Facebook page ID';

--
-- Name: COLUMN facebook_users.last_interaction; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.facebook_users.last_interaction IS 'Last interaction timestamp';

--
-- Name: facebook_users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.facebook_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: facebook_users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.facebook_users_id_seq OWNED BY public.facebook_users.id;

--
-- Name: file_metadata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.file_metadata (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_key character varying(255),
    tenant_id bigint NOT NULL,
    file_name character varying(255) NOT NULL,
    file_url character varying(1000) NOT NULL,
    file_size bigint,
    content_type character varying(255),
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    title character varying(255),
    description text,
    category_id uuid NOT NULL,
    user_id bigint NOT NULL,
    code character varying(100),
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    is_deleted boolean DEFAULT false NOT NULL
);

--
-- Name: TABLE file_metadata; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.file_metadata IS 'File metadata for uploaded files';

--
-- Name: COLUMN file_metadata.file_name; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file_metadata.file_name IS 'Original file name';

--
-- Name: COLUMN file_metadata.file_url; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file_metadata.file_url IS 'URL to access the file';

--
-- Name: COLUMN file_metadata.category_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file_metadata.category_id IS 'Category reference';

--
-- Name: COLUMN file_metadata.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file_metadata.user_id IS 'User who uploaded the file';

--
-- Name: COLUMN file_metadata.code; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.file_metadata.code IS 'File code for filtering';

--
-- Name: file_tags; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.file_tags (
    file_id uuid NOT NULL,
    tag character varying(255) NOT NULL
);

--
-- Name: TABLE file_tags; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.file_tags IS 'File tags collection';

--
-- Name: facebook_users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.facebook_users ALTER COLUMN id SET DEFAULT nextval('public.facebook_users_id_seq'::regclass);

--
-- Name: category category_name_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_name_key UNIQUE (name);

--
-- Name: category category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pkey PRIMARY KEY (id);

--
-- Name: facebook_connection facebook_connection_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.facebook_connection
    ADD CONSTRAINT facebook_connection_pkey PRIMARY KEY (id);

--
-- Name: facebook_users facebook_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.facebook_users
    ADD CONSTRAINT facebook_users_pkey PRIMARY KEY (id);

--
-- Name: file_metadata file_metadata_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_metadata
    ADD CONSTRAINT file_metadata_pkey PRIMARY KEY (id);

--
-- Name: file_tags file_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_tags
    ADD CONSTRAINT file_tags_pkey PRIMARY KEY (file_id, tag);

--
-- Name: facebook_connection uk_facebook_connection_tenant_page; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.facebook_connection
    ADD CONSTRAINT uk_facebook_connection_tenant_page UNIQUE (tenant_id, page_id);

--
-- Name: facebook_users uk_facebook_user_tenant_psid; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.facebook_users
    ADD CONSTRAINT uk_facebook_user_tenant_psid UNIQUE (tenant_id, psid);

--
-- Name: idx_category_name; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_category_name ON public.category USING btree (name);

--
-- Name: idx_category_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_category_tenant ON public.category USING btree (tenant_id);

--
-- Name: idx_category_tenant_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_category_tenant_key ON public.category USING btree (tenant_key);

--
-- Name: idx_facebook_connection_is_deleted; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_connection_is_deleted ON public.facebook_connection USING btree (is_deleted);

--
-- Name: idx_facebook_connection_owner; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_connection_owner ON public.facebook_connection USING btree (owner_id);

--
-- Name: idx_facebook_connection_page; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_connection_page ON public.facebook_connection USING btree (page_id);

--
-- Name: idx_facebook_connection_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_connection_tenant ON public.facebook_connection USING btree (tenant_id);

--
-- Name: idx_facebook_connection_tenant_key; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_connection_tenant_key ON public.facebook_connection USING btree (tenant_key);

--
-- Name: idx_facebook_user_odoo_partner; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_user_odoo_partner ON public.facebook_users USING btree (odoo_partner_id);

--
-- Name: idx_facebook_user_page; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_user_page ON public.facebook_users USING btree (page_id);

--
-- Name: idx_facebook_user_psid; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_user_psid ON public.facebook_users USING btree (psid);

--
-- Name: idx_facebook_user_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_facebook_user_tenant ON public.facebook_users USING btree (tenant_id);

--
-- Name: idx_file_metadata_category; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_file_metadata_category ON public.file_metadata USING btree (category_id);

--
-- Name: idx_file_metadata_code; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_file_metadata_code ON public.file_metadata USING btree (code);

--
-- Name: idx_file_metadata_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_file_metadata_tenant ON public.file_metadata USING btree (tenant_id);

--
-- Name: idx_file_metadata_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_file_metadata_user ON public.file_metadata USING btree (user_id);

--
-- Name: idx_file_tags_file; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_file_tags_file ON public.file_tags USING btree (file_id);

--
-- Name: idx_file_tags_tag; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_file_tags_tag ON public.file_tags USING btree (tag);

--
-- Name: file_metadata fk_file_category; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_metadata
    ADD CONSTRAINT fk_file_category FOREIGN KEY (category_id) REFERENCES public.category(id) ON DELETE CASCADE;

--
-- Name: file_tags fk_file_tags_file; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.file_tags
    ADD CONSTRAINT fk_file_tags_file FOREIGN KEY (file_id) REFERENCES public.file_metadata(id) ON DELETE CASCADE;

--
-- PostgreSQL database dump complete
--
