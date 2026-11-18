
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
-- Name: addresses; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.addresses (
    id bigint NOT NULL,
    tenant_id bigint,
    owner_type character varying(30) NOT NULL,
    owner_id bigint NOT NULL,
    house_number character varying(255),
    street character varying(255),
    ward character varying(255),
    district character varying(255),
    province character varying(255),
    country character varying(255)
);

--
-- Name: addresses_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.addresses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: addresses_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.addresses_id_seq OWNED BY public.addresses.id;

--
-- Name: user_profiles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.user_profiles (
    id bigint NOT NULL,
    user_id bigint NOT NULL,
    fullname character varying(100),
    phone_number character varying(50),
    avatar character varying(500),
    gender character varying(10),
    bio character varying(500),
    email character varying(255),
    job_title character varying(100),
    department character varying(100),
    company character varying(100),
    linkedin_url character varying(500),
    website character varying(500),
    location character varying(200),
    skills character varying(1000),
    experience character varying(1000),
    education character varying(500),
    certifications character varying(500),
    languages character varying(255),
    availability character varying(100),
    hourly_rate character varying(50),
    portfolio_url character varying(500),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

--
-- Name: TABLE user_profiles; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.user_profiles IS 'User profile information';

--
-- Name: COLUMN user_profiles.user_id; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_profiles.user_id IS 'Reference to users table';

--
-- Name: COLUMN user_profiles.fullname; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_profiles.fullname IS 'User full name';

--
-- Name: COLUMN user_profiles.phone_number; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_profiles.phone_number IS 'User phone number';

--
-- Name: COLUMN user_profiles.avatar; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.user_profiles.avatar IS 'User avatar URL';

--
-- Name: user_profiles_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.user_profiles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: user_profiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.user_profiles_id_seq OWNED BY public.user_profiles.id;

--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    tenant_key character varying(255),
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    system_role character varying(50) NOT NULL,
    is_active boolean DEFAULT true NOT NULL,
    created_by character varying(255),
    updated_by character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    balance numeric(15,2) DEFAULT 0.00 NOT NULL,
    version bigint DEFAULT 0,
    is_deleted boolean DEFAULT false NOT NULL,
    totp_secret character varying(64),
    totp_enabled boolean DEFAULT false NOT NULL,
    CONSTRAINT chk_balance_non_negative CHECK ((balance >= 0.00))
);

--
-- Name: CONSTRAINT chk_balance_non_negative ON users; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON CONSTRAINT chk_balance_non_negative ON public.users IS 'Prevents negative balance values';

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;

--
-- Name: addresses id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.addresses ALTER COLUMN id SET DEFAULT nextval('public.addresses_id_seq'::regclass);

--
-- Name: user_profiles id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profiles ALTER COLUMN id SET DEFAULT nextval('public.user_profiles_id_seq'::regclass);

--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);

--
-- Name: addresses addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.addresses
    ADD CONSTRAINT addresses_pkey PRIMARY KEY (id);

--
-- Name: user_profiles user_profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_pkey PRIMARY KEY (id);

--
-- Name: user_profiles user_profiles_user_id_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT user_profiles_user_id_key UNIQUE (user_id);

--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);

--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
-- Name: idx_address_owner; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_address_owner ON public.addresses USING btree (tenant_id, owner_type, owner_id);

--
-- Name: idx_address_tenant; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_address_tenant ON public.addresses USING btree (tenant_id);

--
-- Name: idx_user_profile_user; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_user_profile_user ON public.user_profiles USING btree (user_id);

--
-- Name: idx_users_active; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_active ON public.users USING btree (is_active);

--
-- Name: idx_users_balance; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_balance ON public.users USING btree (balance);

--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_email ON public.users USING btree (email);

--
-- Name: idx_users_role; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_users_role ON public.users USING btree (system_role);

--
-- Name: user_profiles fk_user_profile_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.user_profiles
    ADD CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;

--
-- PostgreSQL database dump complete
--
