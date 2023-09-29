--
-- PostgreSQL database dump
--

-- Dumped from database version 12.16 (Debian 12.16-1.pgdg120+1)
-- Dumped by pg_dump version 12.16

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
-- Name: awx; Type: DATABASE; Schema: -; Owner: awx
--

\connect awx

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
-- Name: _unpartitioned_main_adhoccommandevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public._unpartitioned_main_adhoccommandevent (
    id bigint NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    host_name character varying(1024) NOT NULL,
    event character varying(100) NOT NULL,
    event_data text NOT NULL,
    failed boolean NOT NULL,
    changed boolean NOT NULL,
    counter integer NOT NULL,
    host_id integer,
    ad_hoc_command_id integer NOT NULL,
    end_line integer NOT NULL,
    start_line integer NOT NULL,
    stdout text NOT NULL,
    uuid character varying(1024) NOT NULL,
    verbosity integer NOT NULL,
    CONSTRAINT main_adhoccommandevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_adhoccommandevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_adhoccommandevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_adhoccommandevent_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public._unpartitioned_main_adhoccommandevent OWNER TO awx;

--
-- Name: _unpartitioned_main_inventoryupdateevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public._unpartitioned_main_inventoryupdateevent (
    id bigint NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    event_data text NOT NULL,
    uuid character varying(1024) NOT NULL,
    counter integer NOT NULL,
    stdout text NOT NULL,
    verbosity integer NOT NULL,
    start_line integer NOT NULL,
    end_line integer NOT NULL,
    inventory_update_id integer NOT NULL,
    CONSTRAINT main_inventoryupdateevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_inventoryupdateevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_inventoryupdateevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_inventoryupdateevent_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public._unpartitioned_main_inventoryupdateevent OWNER TO awx;

--
-- Name: _unpartitioned_main_jobevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public._unpartitioned_main_jobevent (
    id bigint NOT NULL,
    created timestamp with time zone,
    modified timestamp with time zone NOT NULL,
    event character varying(100) NOT NULL,
    event_data text NOT NULL,
    failed boolean NOT NULL,
    changed boolean NOT NULL,
    host_name character varying(1024) NOT NULL,
    play character varying(1024) NOT NULL,
    role character varying(1024) NOT NULL,
    task character varying(1024) NOT NULL,
    counter integer NOT NULL,
    host_id integer,
    job_id integer NOT NULL,
    uuid character varying(1024) NOT NULL,
    parent_uuid character varying(1024) NOT NULL,
    end_line integer NOT NULL,
    playbook character varying(1024) NOT NULL,
    start_line integer NOT NULL,
    stdout text NOT NULL,
    verbosity integer NOT NULL,
    CONSTRAINT main_jobevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_jobevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_jobevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_jobevent_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public._unpartitioned_main_jobevent OWNER TO awx;

--
-- Name: _unpartitioned_main_projectupdateevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public._unpartitioned_main_projectupdateevent (
    id bigint NOT NULL,
    created timestamp with time zone,
    modified timestamp with time zone NOT NULL,
    event character varying(100) NOT NULL,
    event_data text NOT NULL,
    failed boolean NOT NULL,
    changed boolean NOT NULL,
    uuid character varying(1024) NOT NULL,
    playbook character varying(1024) NOT NULL,
    play character varying(1024) NOT NULL,
    role character varying(1024) NOT NULL,
    task character varying(1024) NOT NULL,
    counter integer NOT NULL,
    stdout text NOT NULL,
    verbosity integer NOT NULL,
    start_line integer NOT NULL,
    end_line integer NOT NULL,
    project_update_id integer NOT NULL,
    CONSTRAINT main_projectupdateevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_projectupdateevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_projectupdateevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_projectupdateevent_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public._unpartitioned_main_projectupdateevent OWNER TO awx;

--
-- Name: _unpartitioned_main_systemjobevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public._unpartitioned_main_systemjobevent (
    id bigint NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    event_data text NOT NULL,
    uuid character varying(1024) NOT NULL,
    counter integer NOT NULL,
    stdout text NOT NULL,
    verbosity integer NOT NULL,
    start_line integer NOT NULL,
    end_line integer NOT NULL,
    system_job_id integer NOT NULL,
    CONSTRAINT main_systemjobevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_systemjobevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_systemjobevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_systemjobevent_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public._unpartitioned_main_systemjobevent OWNER TO awx;

--
-- Name: auth_group; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.auth_group (
    id integer NOT NULL,
    name character varying(150) NOT NULL
);


ALTER TABLE public.auth_group OWNER TO awx;

--
-- Name: auth_group_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.auth_group ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.auth_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: auth_group_permissions; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.auth_group_permissions (
    id integer NOT NULL,
    group_id integer NOT NULL,
    permission_id integer NOT NULL
);


ALTER TABLE public.auth_group_permissions OWNER TO awx;

--
-- Name: auth_group_permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.auth_group_permissions ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.auth_group_permissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: auth_permission; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.auth_permission (
    id integer NOT NULL,
    name character varying(255) NOT NULL,
    content_type_id integer NOT NULL,
    codename character varying(100) NOT NULL
);


ALTER TABLE public.auth_permission OWNER TO awx;

--
-- Name: auth_permission_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.auth_permission ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.auth_permission_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: auth_user; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.auth_user (
    id integer NOT NULL,
    password character varying(128) NOT NULL,
    last_login timestamp with time zone,
    is_superuser boolean NOT NULL,
    username character varying(150) NOT NULL,
    first_name character varying(150) NOT NULL,
    last_name character varying(150) NOT NULL,
    email character varying(254) NOT NULL,
    is_staff boolean NOT NULL,
    is_active boolean NOT NULL,
    date_joined timestamp with time zone NOT NULL
);


ALTER TABLE public.auth_user OWNER TO awx;

--
-- Name: auth_user_groups; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.auth_user_groups (
    id integer NOT NULL,
    user_id integer NOT NULL,
    group_id integer NOT NULL
);


ALTER TABLE public.auth_user_groups OWNER TO awx;

--
-- Name: auth_user_groups_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.auth_user_groups ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.auth_user_groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: auth_user_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.auth_user ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.auth_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: auth_user_user_permissions; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.auth_user_user_permissions (
    id integer NOT NULL,
    user_id integer NOT NULL,
    permission_id integer NOT NULL
);


ALTER TABLE public.auth_user_user_permissions OWNER TO awx;

--
-- Name: auth_user_user_permissions_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.auth_user_user_permissions ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.auth_user_user_permissions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: conf_setting; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.conf_setting (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    key character varying(255) NOT NULL,
    value jsonb,
    user_id integer
);


ALTER TABLE public.conf_setting OWNER TO awx;

--
-- Name: conf_setting_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.conf_setting ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.conf_setting_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: django_content_type; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.django_content_type (
    id integer NOT NULL,
    app_label character varying(100) NOT NULL,
    model character varying(100) NOT NULL
);


ALTER TABLE public.django_content_type OWNER TO awx;

--
-- Name: django_content_type_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.django_content_type ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.django_content_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: django_migrations; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.django_migrations (
    id integer NOT NULL,
    app character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    applied timestamp with time zone NOT NULL
);


ALTER TABLE public.django_migrations OWNER TO awx;

--
-- Name: django_migrations_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.django_migrations ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.django_migrations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: django_session; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.django_session (
    session_key character varying(40) NOT NULL,
    session_data text NOT NULL,
    expire_date timestamp with time zone NOT NULL
);


ALTER TABLE public.django_session OWNER TO awx;

--
-- Name: django_site; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.django_site (
    id integer NOT NULL,
    domain character varying(100) NOT NULL,
    name character varying(50) NOT NULL
);


ALTER TABLE public.django_site OWNER TO awx;

--
-- Name: django_site_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.django_site ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.django_site_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream (
    id integer NOT NULL,
    operation character varying(13) NOT NULL,
    "timestamp" timestamp with time zone NOT NULL,
    changes text NOT NULL,
    object_relationship_type text NOT NULL,
    object1 text NOT NULL,
    object2 text NOT NULL,
    actor_id integer,
    action_node character varying(512) NOT NULL,
    deleted_actor jsonb,
    setting jsonb NOT NULL
);


ALTER TABLE public.main_activitystream OWNER TO awx;

--
-- Name: main_activitystream_ad_hoc_command; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_ad_hoc_command (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    adhoccommand_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_ad_hoc_command OWNER TO awx;

--
-- Name: main_activitystream_ad_hoc_command_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_ad_hoc_command ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_ad_hoc_command_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_credential; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_credential (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    credential_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_credential OWNER TO awx;

--
-- Name: main_activitystream_credential_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_credential ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_credential_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_credential_type; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_credential_type (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    credentialtype_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_credential_type OWNER TO awx;

--
-- Name: main_activitystream_credential_type_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_credential_type ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_credential_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_execution_environment; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_execution_environment (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    executionenvironment_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_execution_environment OWNER TO awx;

--
-- Name: main_activitystream_execution_environment_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_execution_environment ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_execution_environment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_group; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_group (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    group_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_group OWNER TO awx;

--
-- Name: main_activitystream_group_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_group ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_host; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_host (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    host_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_host OWNER TO awx;

--
-- Name: main_activitystream_host_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_host ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_host_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_instance; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_instance (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    instance_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_instance OWNER TO awx;

--
-- Name: main_activitystream_instance_group; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_instance_group (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    instancegroup_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_instance_group OWNER TO awx;

--
-- Name: main_activitystream_instance_group_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_instance_group ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_instance_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_instance ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_inventory; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_inventory (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    inventory_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_inventory OWNER TO awx;

--
-- Name: main_activitystream_inventory_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_inventory ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_inventory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_inventory_source; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_inventory_source (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    inventorysource_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_inventory_source OWNER TO awx;

--
-- Name: main_activitystream_inventory_source_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_inventory_source ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_inventory_source_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_inventory_update; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_inventory_update (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    inventoryupdate_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_inventory_update OWNER TO awx;

--
-- Name: main_activitystream_inventory_update_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_inventory_update ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_inventory_update_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_job; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_job (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    job_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_job OWNER TO awx;

--
-- Name: main_activitystream_job_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_job ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_job_template; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_job_template (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    jobtemplate_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_job_template OWNER TO awx;

--
-- Name: main_activitystream_job_template_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_job_template ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_job_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_label; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_label (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    label_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_label OWNER TO awx;

--
-- Name: main_activitystream_label_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_label ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_label_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_notification; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_notification (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    notification_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_notification OWNER TO awx;

--
-- Name: main_activitystream_notification_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_notification ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_notification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_notification_template; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_notification_template (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_notification_template OWNER TO awx;

--
-- Name: main_activitystream_notification_template_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_notification_template ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_notification_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_o_auth2_access_token; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_o_auth2_access_token (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    oauth2accesstoken_id bigint NOT NULL
);


ALTER TABLE public.main_activitystream_o_auth2_access_token OWNER TO awx;

--
-- Name: main_activitystream_o_auth2_access_token_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_o_auth2_access_token ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_o_auth2_access_token_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_o_auth2_application; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_o_auth2_application (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    oauth2application_id bigint NOT NULL
);


ALTER TABLE public.main_activitystream_o_auth2_application OWNER TO awx;

--
-- Name: main_activitystream_o_auth2_application_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_o_auth2_application ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_o_auth2_application_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_organization; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_organization (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    organization_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_organization OWNER TO awx;

--
-- Name: main_activitystream_organization_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_organization ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_organization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_project; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_project (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    project_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_project OWNER TO awx;

--
-- Name: main_activitystream_project_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_project ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_project_update; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_project_update (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    projectupdate_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_project_update OWNER TO awx;

--
-- Name: main_activitystream_project_update_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_project_update ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_project_update_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_role; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_role (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    role_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_role OWNER TO awx;

--
-- Name: main_activitystream_role_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_role ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_schedule; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_schedule (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    schedule_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_schedule OWNER TO awx;

--
-- Name: main_activitystream_schedule_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_schedule ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_team; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_team (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    team_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_team OWNER TO awx;

--
-- Name: main_activitystream_team_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_team ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_team_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_unified_job; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_unified_job (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    unifiedjob_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_unified_job OWNER TO awx;

--
-- Name: main_activitystream_unified_job_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_unified_job ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_unified_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_unified_job_template; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_unified_job_template (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    unifiedjobtemplate_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_unified_job_template OWNER TO awx;

--
-- Name: main_activitystream_unified_job_template_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_unified_job_template ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_unified_job_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_user; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_user (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_user OWNER TO awx;

--
-- Name: main_activitystream_user_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_user ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_workflow_approval; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_workflow_approval (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    workflowapproval_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_workflow_approval OWNER TO awx;

--
-- Name: main_activitystream_workflow_approval_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_workflow_approval ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_workflow_approval_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_workflow_approval_template; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_workflow_approval_template (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    workflowapprovaltemplate_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_workflow_approval_template OWNER TO awx;

--
-- Name: main_activitystream_workflow_approval_template_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_workflow_approval_template ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_workflow_approval_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_workflow_job; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_workflow_job (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    workflowjob_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_workflow_job OWNER TO awx;

--
-- Name: main_activitystream_workflow_job_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_workflow_job ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_workflow_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_workflow_job_node; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_workflow_job_node (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    workflowjobnode_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_workflow_job_node OWNER TO awx;

--
-- Name: main_activitystream_workflow_job_node_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_workflow_job_node ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_workflow_job_node_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_workflow_job_template; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_workflow_job_template (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    workflowjobtemplate_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_workflow_job_template OWNER TO awx;

--
-- Name: main_activitystream_workflow_job_template_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_workflow_job_template ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_workflow_job_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_activitystream_workflow_job_template_node; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_activitystream_workflow_job_template_node (
    id integer NOT NULL,
    activitystream_id integer NOT NULL,
    workflowjobtemplatenode_id integer NOT NULL
);


ALTER TABLE public.main_activitystream_workflow_job_template_node OWNER TO awx;

--
-- Name: main_activitystream_workflow_job_template_node_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_activitystream_workflow_job_template_node ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_activitystream_workflow_job_template_node_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_adhoccommand; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_adhoccommand (
    unifiedjob_ptr_id integer NOT NULL,
    job_type character varying(64) NOT NULL,
    "limit" text NOT NULL,
    module_name character varying(1024) NOT NULL,
    module_args text NOT NULL,
    forks integer NOT NULL,
    verbosity integer NOT NULL,
    become_enabled boolean NOT NULL,
    credential_id integer,
    inventory_id integer,
    extra_vars text NOT NULL,
    diff_mode boolean NOT NULL,
    CONSTRAINT main_adhoccommand_forks_check CHECK ((forks >= 0)),
    CONSTRAINT main_adhoccommand_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public.main_adhoccommand OWNER TO awx;

--
-- Name: main_adhoccommandevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_adhoccommandevent (
    id bigint NOT NULL,
    created timestamp with time zone,
    modified timestamp with time zone NOT NULL,
    host_name character varying(1024) NOT NULL,
    event character varying(100) NOT NULL,
    event_data text NOT NULL,
    failed boolean NOT NULL,
    changed boolean NOT NULL,
    counter integer NOT NULL,
    host_id integer,
    ad_hoc_command_id integer NOT NULL,
    end_line integer NOT NULL,
    start_line integer NOT NULL,
    stdout text NOT NULL,
    uuid character varying(1024) NOT NULL,
    verbosity integer NOT NULL,
    job_created timestamp with time zone NOT NULL,
    CONSTRAINT main_adhoccommandevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_adhoccommandevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_adhoccommandevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_adhoccommandevent_verbosity_check CHECK ((verbosity >= 0))
)
PARTITION BY RANGE (job_created);


ALTER TABLE public.main_adhoccommandevent OWNER TO awx;

--
-- Name: main_adhoccommandevent_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public._unpartitioned_main_adhoccommandevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_adhoccommandevent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_adhoccommandevent_id_seq1; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_adhoccommandevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_adhoccommandevent_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_credential; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_credential (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    organization_id integer,
    admin_role_id integer,
    use_role_id integer,
    read_role_id integer,
    inputs jsonb NOT NULL,
    credential_type_id integer NOT NULL,
    managed boolean NOT NULL
);


ALTER TABLE public.main_credential OWNER TO awx;

--
-- Name: main_credential_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_credential ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_credential_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_credentialinputsource; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_credentialinputsource (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    input_field_name character varying(1024) NOT NULL,
    metadata jsonb NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    source_credential_id integer,
    target_credential_id integer
);


ALTER TABLE public.main_credentialinputsource OWNER TO awx;

--
-- Name: main_credentialinputsource_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_credentialinputsource ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_credentialinputsource_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_credentialtype; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_credentialtype (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    kind character varying(32) NOT NULL,
    managed boolean NOT NULL,
    inputs jsonb NOT NULL,
    injectors jsonb NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    namespace character varying(1024)
);


ALTER TABLE public.main_credentialtype OWNER TO awx;

--
-- Name: main_credentialtype_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_credentialtype ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_credentialtype_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_custominventoryscript; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_custominventoryscript (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    script text NOT NULL,
    created_by_id integer,
    modified_by_id integer
);


ALTER TABLE public.main_custominventoryscript OWNER TO awx;

--
-- Name: main_custominventoryscript_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_custominventoryscript ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_custominventoryscript_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_executionenvironment; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_executionenvironment (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    image character varying(1024) NOT NULL,
    managed boolean NOT NULL,
    created_by_id integer,
    credential_id integer,
    modified_by_id integer,
    organization_id integer,
    name character varying(512) NOT NULL,
    pull character varying(16) NOT NULL
);


ALTER TABLE public.main_executionenvironment OWNER TO awx;

--
-- Name: main_executionenvironment_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_executionenvironment ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_executionenvironment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_group; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_group (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    variables text NOT NULL,
    created_by_id integer,
    inventory_id integer NOT NULL,
    modified_by_id integer
);


ALTER TABLE public.main_group OWNER TO awx;

--
-- Name: main_group_hosts; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_group_hosts (
    id integer NOT NULL,
    group_id integer NOT NULL,
    host_id integer NOT NULL
);


ALTER TABLE public.main_group_hosts OWNER TO awx;

--
-- Name: main_group_hosts_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_group_hosts ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_group_hosts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_group_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_group ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_group_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_group_inventory_sources; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_group_inventory_sources (
    id integer NOT NULL,
    group_id integer NOT NULL,
    inventorysource_id integer NOT NULL
);


ALTER TABLE public.main_group_inventory_sources OWNER TO awx;

--
-- Name: main_group_inventory_sources_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_group_inventory_sources ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_group_inventory_sources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_group_parents; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_group_parents (
    id integer NOT NULL,
    from_group_id integer NOT NULL,
    to_group_id integer NOT NULL
);


ALTER TABLE public.main_group_parents OWNER TO awx;

--
-- Name: main_group_parents_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_group_parents ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_group_parents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_host; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_host (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    enabled boolean NOT NULL,
    instance_id character varying(1024) NOT NULL,
    variables text NOT NULL,
    created_by_id integer,
    inventory_id integer NOT NULL,
    last_job_host_summary_id integer,
    modified_by_id integer,
    last_job_id integer,
    ansible_facts jsonb NOT NULL,
    ansible_facts_modified timestamp with time zone
);


ALTER TABLE public.main_host OWNER TO awx;

--
-- Name: main_host_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_host ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_host_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_host_inventory_sources; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_host_inventory_sources (
    id integer NOT NULL,
    host_id integer NOT NULL,
    inventorysource_id integer NOT NULL
);


ALTER TABLE public.main_host_inventory_sources OWNER TO awx;

--
-- Name: main_host_inventory_sources_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_host_inventory_sources ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_host_inventory_sources_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_hostmetric; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_hostmetric (
    hostname character varying(512) NOT NULL,
    first_automation timestamp with time zone NOT NULL,
    last_automation timestamp with time zone NOT NULL,
    last_deleted timestamp with time zone,
    automated_counter bigint NOT NULL,
    deleted_counter integer NOT NULL,
    deleted boolean NOT NULL,
    used_in_inventories integer,
    id integer NOT NULL
);


ALTER TABLE public.main_hostmetric OWNER TO awx;

--
-- Name: main_hostmetric_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_hostmetric ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_hostmetric_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_hostmetricsummarymonthly; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_hostmetricsummarymonthly (
    id integer NOT NULL,
    date date NOT NULL,
    license_consumed bigint NOT NULL,
    license_capacity bigint NOT NULL,
    hosts_added integer NOT NULL,
    hosts_deleted integer NOT NULL,
    indirectly_managed_hosts integer NOT NULL
);


ALTER TABLE public.main_hostmetricsummarymonthly OWNER TO awx;

--
-- Name: main_hostmetricsummarymonthly_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_hostmetricsummarymonthly ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_hostmetricsummarymonthly_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_instance; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_instance (
    id integer NOT NULL,
    uuid character varying(40) NOT NULL,
    hostname character varying(250) NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    capacity integer NOT NULL,
    version character varying(120) NOT NULL,
    capacity_adjustment numeric(3,2) NOT NULL,
    cpu numeric(4,1) NOT NULL,
    memory bigint NOT NULL,
    cpu_capacity integer NOT NULL,
    mem_capacity integer NOT NULL,
    enabled boolean NOT NULL,
    managed_by_policy boolean NOT NULL,
    ip_address character varying(50),
    node_type character varying(16) NOT NULL,
    last_seen timestamp with time zone,
    errors text NOT NULL,
    last_health_check timestamp with time zone,
    listener_port integer NOT NULL,
    node_state character varying(16) NOT NULL,
    health_check_started timestamp with time zone,
    CONSTRAINT main_instance_capacity_check CHECK ((capacity >= 0)),
    CONSTRAINT main_instance_listener_port_check CHECK ((listener_port >= 0))
);


ALTER TABLE public.main_instance OWNER TO awx;

--
-- Name: main_instance_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_instance ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_instance_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_instancegroup; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_instancegroup (
    id integer NOT NULL,
    name character varying(250) NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    policy_instance_list jsonb NOT NULL,
    policy_instance_minimum integer NOT NULL,
    policy_instance_percentage integer NOT NULL,
    credential_id integer,
    pod_spec_override text NOT NULL,
    is_container_group boolean NOT NULL,
    max_concurrent_jobs integer NOT NULL,
    max_forks integer NOT NULL,
    admin_role_id integer,
    read_role_id integer,
    use_role_id integer
);


ALTER TABLE public.main_instancegroup OWNER TO awx;

--
-- Name: main_instancegroup_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_instancegroup ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_instancegroup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_instancegroup_instances; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_instancegroup_instances (
    id integer NOT NULL,
    instancegroup_id integer NOT NULL,
    instance_id integer NOT NULL
);


ALTER TABLE public.main_instancegroup_instances OWNER TO awx;

--
-- Name: main_instancegroup_instances_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_instancegroup_instances ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_instancegroup_instances_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_instancelink; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_instancelink (
    id integer NOT NULL,
    source_id integer NOT NULL,
    target_id integer NOT NULL,
    link_state character varying(16) NOT NULL
);


ALTER TABLE public.main_instancelink OWNER TO awx;

--
-- Name: main_instancelink_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_instancelink ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_instancelink_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_inventory; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_inventory (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    variables text NOT NULL,
    has_active_failures boolean NOT NULL,
    total_hosts integer NOT NULL,
    hosts_with_active_failures integer NOT NULL,
    total_groups integer NOT NULL,
    has_inventory_sources boolean NOT NULL,
    total_inventory_sources integer NOT NULL,
    inventory_sources_with_failures integer NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    organization_id integer,
    admin_role_id integer,
    adhoc_role_id integer,
    update_role_id integer,
    use_role_id integer,
    read_role_id integer,
    host_filter text,
    kind character varying(32) NOT NULL,
    pending_deletion boolean NOT NULL,
    prevent_instance_group_fallback boolean NOT NULL,
    CONSTRAINT main_inventory_hosts_with_active_failures_check CHECK ((hosts_with_active_failures >= 0)),
    CONSTRAINT main_inventory_inventory_sources_with_failures_check CHECK ((inventory_sources_with_failures >= 0)),
    CONSTRAINT main_inventory_total_groups_check CHECK ((total_groups >= 0)),
    CONSTRAINT main_inventory_total_hosts_check CHECK ((total_hosts >= 0)),
    CONSTRAINT main_inventory_total_inventory_sources_check CHECK ((total_inventory_sources >= 0))
);


ALTER TABLE public.main_inventory OWNER TO awx;

--
-- Name: main_inventory_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_inventory ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_inventory_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_inventory_labels; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_inventory_labels (
    id integer NOT NULL,
    inventory_id integer NOT NULL,
    label_id integer NOT NULL
);


ALTER TABLE public.main_inventory_labels OWNER TO awx;

--
-- Name: main_inventory_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_inventory_labels ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_inventory_labels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_inventoryconstructedinventorymembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_inventoryconstructedinventorymembership (
    id integer NOT NULL,
    "position" integer,
    constructed_inventory_id integer NOT NULL,
    input_inventory_id integer NOT NULL,
    CONSTRAINT main_inventoryconstructedinventorymembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_inventoryconstructedinventorymembership OWNER TO awx;

--
-- Name: main_inventoryconstructedinventorymembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_inventoryconstructedinventorymembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_inventoryconstructedinventorymembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_inventoryinstancegroupmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_inventoryinstancegroupmembership (
    id integer NOT NULL,
    "position" integer,
    instancegroup_id integer NOT NULL,
    inventory_id integer NOT NULL,
    CONSTRAINT main_inventoryinstancegroupmembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_inventoryinstancegroupmembership OWNER TO awx;

--
-- Name: main_inventoryinstancegroupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_inventoryinstancegroupmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_inventoryinstancegroupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_inventorysource; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_inventorysource (
    unifiedjobtemplate_ptr_id integer NOT NULL,
    source character varying(32) NOT NULL,
    source_path character varying(1024) NOT NULL,
    source_vars text NOT NULL,
    overwrite boolean NOT NULL,
    overwrite_vars boolean NOT NULL,
    update_on_launch boolean NOT NULL,
    update_cache_timeout integer NOT NULL,
    inventory_id integer,
    timeout integer NOT NULL,
    source_project_id integer,
    verbosity integer NOT NULL,
    custom_virtualenv character varying(100),
    enabled_value text NOT NULL,
    enabled_var text NOT NULL,
    host_filter text NOT NULL,
    scm_branch character varying(1024) NOT NULL,
    "limit" text NOT NULL,
    CONSTRAINT main_inventorysource_update_cache_timeout_check CHECK ((update_cache_timeout >= 0)),
    CONSTRAINT main_inventorysource_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public.main_inventorysource OWNER TO awx;

--
-- Name: main_inventoryupdate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_inventoryupdate (
    unifiedjob_ptr_id integer NOT NULL,
    source character varying(32) NOT NULL,
    source_path character varying(1024) NOT NULL,
    source_vars text NOT NULL,
    overwrite boolean NOT NULL,
    overwrite_vars boolean NOT NULL,
    license_error boolean NOT NULL,
    inventory_source_id integer NOT NULL,
    timeout integer NOT NULL,
    source_project_update_id integer,
    verbosity integer NOT NULL,
    inventory_id integer,
    custom_virtualenv character varying(100),
    org_host_limit_error boolean NOT NULL,
    enabled_value text NOT NULL,
    enabled_var text NOT NULL,
    host_filter text NOT NULL,
    scm_revision character varying(1024) NOT NULL,
    scm_branch character varying(1024) NOT NULL,
    "limit" text NOT NULL,
    CONSTRAINT main_inventoryupdate_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public.main_inventoryupdate OWNER TO awx;

--
-- Name: main_inventoryupdateevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_inventoryupdateevent (
    id bigint NOT NULL,
    created timestamp with time zone,
    modified timestamp with time zone NOT NULL,
    event_data text NOT NULL,
    uuid character varying(1024) NOT NULL,
    counter integer NOT NULL,
    stdout text NOT NULL,
    verbosity integer NOT NULL,
    start_line integer NOT NULL,
    end_line integer NOT NULL,
    inventory_update_id integer NOT NULL,
    job_created timestamp with time zone NOT NULL,
    CONSTRAINT main_inventoryupdateevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_inventoryupdateevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_inventoryupdateevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_inventoryupdateevent_verbosity_check CHECK ((verbosity >= 0))
)
PARTITION BY RANGE (job_created);


ALTER TABLE public.main_inventoryupdateevent OWNER TO awx;

--
-- Name: main_inventoryupdateevent_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public._unpartitioned_main_inventoryupdateevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_inventoryupdateevent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_inventoryupdateevent_id_seq1; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_inventoryupdateevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_inventoryupdateevent_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_job; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_job (
    unifiedjob_ptr_id integer NOT NULL,
    job_type character varying(64) NOT NULL,
    playbook character varying(1024) NOT NULL,
    forks integer NOT NULL,
    "limit" text NOT NULL,
    verbosity integer NOT NULL,
    extra_vars text NOT NULL,
    job_tags text NOT NULL,
    force_handlers boolean NOT NULL,
    skip_tags character varying(1024) NOT NULL,
    start_at_task character varying(1024) NOT NULL,
    become_enabled boolean NOT NULL,
    inventory_id integer,
    job_template_id integer,
    project_id integer,
    allow_simultaneous boolean NOT NULL,
    artifacts text NOT NULL,
    timeout integer NOT NULL,
    scm_revision character varying(1024) NOT NULL,
    project_update_id integer,
    use_fact_cache boolean NOT NULL,
    diff_mode boolean NOT NULL,
    job_slice_count integer NOT NULL,
    job_slice_number integer NOT NULL,
    custom_virtualenv character varying(100),
    scm_branch character varying(1024) NOT NULL,
    webhook_credential_id integer,
    webhook_guid character varying(128) NOT NULL,
    webhook_service character varying(16) NOT NULL,
    survey_passwords jsonb NOT NULL,
    CONSTRAINT main_job_forks_check CHECK ((forks >= 0)),
    CONSTRAINT main_job_job_slice_count_check CHECK ((job_slice_count >= 0)),
    CONSTRAINT main_job_job_slice_number_check CHECK ((job_slice_number >= 0)),
    CONSTRAINT main_job_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public.main_job OWNER TO awx;

--
-- Name: main_jobevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_jobevent (
    id bigint NOT NULL,
    created timestamp with time zone,
    modified timestamp with time zone NOT NULL,
    event character varying(100) NOT NULL,
    event_data text NOT NULL,
    failed boolean NOT NULL,
    changed boolean NOT NULL,
    host_name character varying(1024) NOT NULL,
    play character varying(1024) NOT NULL,
    role character varying(1024) NOT NULL,
    task character varying(1024) NOT NULL,
    counter integer NOT NULL,
    host_id integer,
    job_id integer,
    uuid character varying(1024) NOT NULL,
    parent_uuid character varying(1024) NOT NULL,
    end_line integer NOT NULL,
    playbook character varying(1024) NOT NULL,
    start_line integer NOT NULL,
    stdout text NOT NULL,
    verbosity integer NOT NULL,
    job_created timestamp with time zone NOT NULL,
    CONSTRAINT main_jobevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_jobevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_jobevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_jobevent_verbosity_check CHECK ((verbosity >= 0))
)
PARTITION BY RANGE (job_created);


ALTER TABLE public.main_jobevent OWNER TO awx;

--
-- Name: main_jobevent_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public._unpartitioned_main_jobevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_jobevent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_jobevent_id_seq1; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_jobevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_jobevent_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_jobhostsummary; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_jobhostsummary (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    host_name character varying(1024) NOT NULL,
    changed integer NOT NULL,
    dark integer NOT NULL,
    failures integer NOT NULL,
    ok integer NOT NULL,
    processed integer NOT NULL,
    skipped integer NOT NULL,
    failed boolean NOT NULL,
    host_id integer,
    job_id integer NOT NULL,
    ignored integer NOT NULL,
    rescued integer NOT NULL,
    constructed_host_id integer,
    CONSTRAINT main_jobhostsummary_changed_check CHECK ((changed >= 0)),
    CONSTRAINT main_jobhostsummary_dark_check CHECK ((dark >= 0)),
    CONSTRAINT main_jobhostsummary_failures_check CHECK ((failures >= 0)),
    CONSTRAINT main_jobhostsummary_ignored_check CHECK ((ignored >= 0)),
    CONSTRAINT main_jobhostsummary_ok_check CHECK ((ok >= 0)),
    CONSTRAINT main_jobhostsummary_processed_check CHECK ((processed >= 0)),
    CONSTRAINT main_jobhostsummary_rescued_check CHECK ((rescued >= 0)),
    CONSTRAINT main_jobhostsummary_skipped_check CHECK ((skipped >= 0))
);


ALTER TABLE public.main_jobhostsummary OWNER TO awx;

--
-- Name: main_jobhostsummary_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_jobhostsummary ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_jobhostsummary_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_joblaunchconfig; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_joblaunchconfig (
    id integer NOT NULL,
    extra_data text NOT NULL,
    inventory_id integer,
    job_id integer NOT NULL,
    execution_environment_id integer,
    char_prompts jsonb NOT NULL,
    survey_passwords jsonb NOT NULL
);


ALTER TABLE public.main_joblaunchconfig OWNER TO awx;

--
-- Name: main_joblaunchconfig_credentials; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_joblaunchconfig_credentials (
    id integer NOT NULL,
    joblaunchconfig_id integer NOT NULL,
    credential_id integer NOT NULL
);


ALTER TABLE public.main_joblaunchconfig_credentials OWNER TO awx;

--
-- Name: main_joblaunchconfig_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_joblaunchconfig_credentials ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_joblaunchconfig_credentials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_joblaunchconfig_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_joblaunchconfig ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_joblaunchconfig_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_joblaunchconfig_labels; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_joblaunchconfig_labels (
    id integer NOT NULL,
    joblaunchconfig_id integer NOT NULL,
    label_id integer NOT NULL
);


ALTER TABLE public.main_joblaunchconfig_labels OWNER TO awx;

--
-- Name: main_joblaunchconfig_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_joblaunchconfig_labels ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_joblaunchconfig_labels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_joblaunchconfiginstancegroupmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_joblaunchconfiginstancegroupmembership (
    id integer NOT NULL,
    "position" integer,
    instancegroup_id integer NOT NULL,
    joblaunchconfig_id integer NOT NULL,
    CONSTRAINT main_joblaunchconfiginstancegroupmembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_joblaunchconfiginstancegroupmembership OWNER TO awx;

--
-- Name: main_joblaunchconfiginstancegroupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_joblaunchconfiginstancegroupmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_joblaunchconfiginstancegroupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_jobtemplate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_jobtemplate (
    unifiedjobtemplate_ptr_id integer NOT NULL,
    job_type character varying(64) NOT NULL,
    playbook character varying(1024) NOT NULL,
    forks integer NOT NULL,
    "limit" text NOT NULL,
    verbosity integer NOT NULL,
    extra_vars text NOT NULL,
    job_tags text NOT NULL,
    force_handlers boolean NOT NULL,
    skip_tags character varying(1024) NOT NULL,
    start_at_task character varying(1024) NOT NULL,
    become_enabled boolean NOT NULL,
    host_config_key character varying(1024) NOT NULL,
    ask_variables_on_launch boolean NOT NULL,
    survey_enabled boolean NOT NULL,
    survey_spec jsonb NOT NULL,
    inventory_id integer,
    project_id integer,
    admin_role_id integer,
    execute_role_id integer,
    read_role_id integer,
    ask_limit_on_launch boolean NOT NULL,
    ask_inventory_on_launch boolean NOT NULL,
    ask_credential_on_launch boolean NOT NULL,
    ask_job_type_on_launch boolean NOT NULL,
    ask_tags_on_launch boolean NOT NULL,
    allow_simultaneous boolean NOT NULL,
    ask_skip_tags_on_launch boolean NOT NULL,
    timeout integer NOT NULL,
    use_fact_cache boolean NOT NULL,
    ask_verbosity_on_launch boolean NOT NULL,
    ask_diff_mode_on_launch boolean NOT NULL,
    diff_mode boolean NOT NULL,
    custom_virtualenv character varying(100),
    job_slice_count integer NOT NULL,
    ask_scm_branch_on_launch boolean NOT NULL,
    scm_branch character varying(1024) NOT NULL,
    webhook_credential_id integer,
    webhook_key character varying(64) NOT NULL,
    webhook_service character varying(16) NOT NULL,
    ask_execution_environment_on_launch boolean NOT NULL,
    ask_forks_on_launch boolean NOT NULL,
    ask_instance_groups_on_launch boolean NOT NULL,
    ask_job_slice_count_on_launch boolean NOT NULL,
    ask_labels_on_launch boolean NOT NULL,
    ask_timeout_on_launch boolean NOT NULL,
    prevent_instance_group_fallback boolean NOT NULL,
    CONSTRAINT main_jobtemplate_forks_check CHECK ((forks >= 0)),
    CONSTRAINT main_jobtemplate_job_slice_count_check CHECK ((job_slice_count >= 0)),
    CONSTRAINT main_jobtemplate_verbosity_check CHECK ((verbosity >= 0))
);


ALTER TABLE public.main_jobtemplate OWNER TO awx;

--
-- Name: main_label; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_label (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    organization_id integer NOT NULL
);


ALTER TABLE public.main_label OWNER TO awx;

--
-- Name: main_label_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_label ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_label_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_notification; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_notification (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    status character varying(20) NOT NULL,
    error text NOT NULL,
    notifications_sent integer NOT NULL,
    notification_type character varying(32) NOT NULL,
    recipients text NOT NULL,
    subject text NOT NULL,
    notification_template_id integer NOT NULL,
    body jsonb NOT NULL
);


ALTER TABLE public.main_notification OWNER TO awx;

--
-- Name: main_notification_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_notification ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_notification_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_notificationtemplate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_notificationtemplate (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    notification_type character varying(32) NOT NULL,
    notification_configuration jsonb NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    organization_id integer,
    messages jsonb
);


ALTER TABLE public.main_notificationtemplate OWNER TO awx;

--
-- Name: main_notificationtemplate_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_notificationtemplate ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_notificationtemplate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_oauth2accesstoken; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_oauth2accesstoken (
    id bigint NOT NULL,
    token character varying(255) NOT NULL,
    expires timestamp with time zone NOT NULL,
    scope text NOT NULL,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone NOT NULL,
    description text NOT NULL,
    last_used timestamp with time zone,
    application_id bigint,
    user_id integer,
    source_refresh_token_id bigint,
    modified timestamp with time zone NOT NULL,
    id_token_id bigint
);


ALTER TABLE public.main_oauth2accesstoken OWNER TO awx;

--
-- Name: main_oauth2accesstoken_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_oauth2accesstoken ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_oauth2accesstoken_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_oauth2application; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_oauth2application (
    id bigint NOT NULL,
    client_id character varying(100) NOT NULL,
    redirect_uris text NOT NULL,
    client_type character varying(32) NOT NULL,
    authorization_grant_type character varying(32) NOT NULL,
    client_secret character varying(1024) NOT NULL,
    name character varying(255) NOT NULL,
    skip_authorization boolean NOT NULL,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone NOT NULL,
    description text NOT NULL,
    logo_data text NOT NULL,
    user_id integer,
    organization_id integer,
    algorithm character varying(5) NOT NULL
);


ALTER TABLE public.main_oauth2application OWNER TO awx;

--
-- Name: main_oauth2application_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_oauth2application ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_oauth2application_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_organization; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_organization (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    admin_role_id integer,
    auditor_role_id integer,
    member_role_id integer,
    read_role_id integer,
    custom_virtualenv character varying(100),
    execute_role_id integer,
    job_template_admin_role_id integer,
    credential_admin_role_id integer,
    inventory_admin_role_id integer,
    project_admin_role_id integer,
    workflow_admin_role_id integer,
    notification_admin_role_id integer,
    max_hosts integer NOT NULL,
    approval_role_id integer,
    default_environment_id integer,
    execution_environment_admin_role_id integer,
    CONSTRAINT main_organization_max_hosts_check CHECK ((max_hosts >= 0))
);


ALTER TABLE public.main_organization OWNER TO awx;

--
-- Name: main_organization_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_organization ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_organization_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_organization_notification_templates_approvals; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_organization_notification_templates_approvals (
    id integer NOT NULL,
    organization_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_organization_notification_templates_approvals OWNER TO awx;

--
-- Name: main_organization_notification_templates_approvals_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_organization_notification_templates_approvals ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_organization_notification_templates_approvals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_organization_notification_templates_error; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_organization_notification_templates_error (
    id integer NOT NULL,
    organization_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_organization_notification_templates_error OWNER TO awx;

--
-- Name: main_organization_notification_templates_error_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_organization_notification_templates_error ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_organization_notification_templates_error_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_organization_notification_templates_started; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_organization_notification_templates_started (
    id integer NOT NULL,
    organization_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_organization_notification_templates_started OWNER TO awx;

--
-- Name: main_organization_notification_templates_started_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_organization_notification_templates_started ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_organization_notification_templates_started_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_organization_notification_templates_success; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_organization_notification_templates_success (
    id integer NOT NULL,
    organization_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_organization_notification_templates_success OWNER TO awx;

--
-- Name: main_organization_notification_templates_success_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_organization_notification_templates_success ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_organization_notification_templates_success_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_organizationgalaxycredentialmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_organizationgalaxycredentialmembership (
    id integer NOT NULL,
    "position" integer,
    credential_id integer NOT NULL,
    organization_id integer NOT NULL,
    CONSTRAINT main_organizationgalaxycredentialmembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_organizationgalaxycredentialmembership OWNER TO awx;

--
-- Name: main_organizationgalaxycredentialmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_organizationgalaxycredentialmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_organizationgalaxycredentialmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_organizationinstancegroupmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_organizationinstancegroupmembership (
    id integer NOT NULL,
    "position" integer,
    instancegroup_id integer NOT NULL,
    organization_id integer NOT NULL,
    CONSTRAINT main_organizationinstancegroupmembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_organizationinstancegroupmembership OWNER TO awx;

--
-- Name: main_organizationinstancegroupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_organizationinstancegroupmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_organizationinstancegroupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_profile; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_profile (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    ldap_dn character varying(1024) NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE public.main_profile OWNER TO awx;

--
-- Name: main_profile_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_profile ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_profile_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_project; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_project (
    unifiedjobtemplate_ptr_id integer NOT NULL,
    local_path character varying(1024) NOT NULL,
    scm_type character varying(8) NOT NULL,
    scm_url character varying(1024) NOT NULL,
    scm_branch character varying(256) NOT NULL,
    scm_clean boolean NOT NULL,
    scm_delete_on_update boolean NOT NULL,
    scm_update_on_launch boolean NOT NULL,
    scm_update_cache_timeout integer NOT NULL,
    credential_id integer,
    admin_role_id integer,
    use_role_id integer,
    update_role_id integer,
    read_role_id integer,
    timeout integer NOT NULL,
    scm_revision character varying(1024) NOT NULL,
    playbook_files jsonb NOT NULL,
    inventory_files jsonb NOT NULL,
    custom_virtualenv character varying(100),
    scm_refspec character varying(1024) NOT NULL,
    allow_override boolean NOT NULL,
    default_environment_id integer,
    scm_track_submodules boolean NOT NULL,
    signature_validation_credential_id integer,
    CONSTRAINT main_project_scm_update_cache_timeout_check CHECK ((scm_update_cache_timeout >= 0))
);


ALTER TABLE public.main_project OWNER TO awx;

--
-- Name: main_projectupdate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_projectupdate (
    unifiedjob_ptr_id integer NOT NULL,
    local_path character varying(1024) NOT NULL,
    scm_type character varying(8) NOT NULL,
    scm_url character varying(1024) NOT NULL,
    scm_branch character varying(256) NOT NULL,
    scm_clean boolean NOT NULL,
    scm_delete_on_update boolean NOT NULL,
    credential_id integer,
    project_id integer NOT NULL,
    timeout integer NOT NULL,
    job_type character varying(64) NOT NULL,
    scm_refspec character varying(1024) NOT NULL,
    scm_revision character varying(1024) NOT NULL,
    job_tags character varying(1024) NOT NULL,
    scm_track_submodules boolean NOT NULL
);


ALTER TABLE public.main_projectupdate OWNER TO awx;

--
-- Name: main_projectupdateevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_projectupdateevent (
    id bigint NOT NULL,
    created timestamp with time zone,
    modified timestamp with time zone NOT NULL,
    event character varying(100) NOT NULL,
    event_data text NOT NULL,
    failed boolean NOT NULL,
    changed boolean NOT NULL,
    uuid character varying(1024) NOT NULL,
    playbook character varying(1024) NOT NULL,
    play character varying(1024) NOT NULL,
    role character varying(1024) NOT NULL,
    task character varying(1024) NOT NULL,
    counter integer NOT NULL,
    stdout text NOT NULL,
    verbosity integer NOT NULL,
    start_line integer NOT NULL,
    end_line integer NOT NULL,
    project_update_id integer NOT NULL,
    job_created timestamp with time zone NOT NULL,
    CONSTRAINT main_projectupdateevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_projectupdateevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_projectupdateevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_projectupdateevent_verbosity_check CHECK ((verbosity >= 0))
)
PARTITION BY RANGE (job_created);


ALTER TABLE public.main_projectupdateevent OWNER TO awx;

--
-- Name: main_projectupdateevent_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public._unpartitioned_main_projectupdateevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_projectupdateevent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_projectupdateevent_id_seq1; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_projectupdateevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_projectupdateevent_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_rbac_role_ancestors; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_rbac_role_ancestors (
    id integer NOT NULL,
    role_field text NOT NULL,
    content_type_id integer NOT NULL,
    object_id integer NOT NULL,
    ancestor_id integer NOT NULL,
    descendent_id integer NOT NULL,
    CONSTRAINT main_rbac_role_ancestors_content_type_id_check CHECK ((content_type_id >= 0)),
    CONSTRAINT main_rbac_role_ancestors_object_id_check CHECK ((object_id >= 0))
);


ALTER TABLE public.main_rbac_role_ancestors OWNER TO awx;

--
-- Name: main_rbac_role_ancestors_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_rbac_role_ancestors ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_rbac_role_ancestors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_rbac_roles; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_rbac_roles (
    id integer NOT NULL,
    role_field text NOT NULL,
    singleton_name text,
    implicit_parents text NOT NULL,
    content_type_id integer,
    object_id integer,
    CONSTRAINT main_rbac_roles_object_id_check CHECK ((object_id >= 0))
);


ALTER TABLE public.main_rbac_roles OWNER TO awx;

--
-- Name: main_rbac_roles_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_rbac_roles ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_rbac_roles_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_rbac_roles_members; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_rbac_roles_members (
    id integer NOT NULL,
    role_id integer NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE public.main_rbac_roles_members OWNER TO awx;

--
-- Name: main_rbac_roles_members_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_rbac_roles_members ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_rbac_roles_members_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_rbac_roles_parents; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_rbac_roles_parents (
    id integer NOT NULL,
    from_role_id integer NOT NULL,
    to_role_id integer NOT NULL
);


ALTER TABLE public.main_rbac_roles_parents OWNER TO awx;

--
-- Name: main_rbac_roles_parents_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_rbac_roles_parents ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_rbac_roles_parents_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_schedule; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_schedule (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    enabled boolean NOT NULL,
    dtstart timestamp with time zone,
    dtend timestamp with time zone,
    rrule text NOT NULL,
    next_run timestamp with time zone,
    extra_data text NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    unified_job_template_id integer NOT NULL,
    char_prompts jsonb NOT NULL,
    inventory_id integer,
    survey_passwords jsonb NOT NULL,
    execution_environment_id integer
);


ALTER TABLE public.main_schedule OWNER TO awx;

--
-- Name: main_schedule_credentials; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_schedule_credentials (
    id integer NOT NULL,
    schedule_id integer NOT NULL,
    credential_id integer NOT NULL
);


ALTER TABLE public.main_schedule_credentials OWNER TO awx;

--
-- Name: main_schedule_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_schedule_credentials ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_schedule_credentials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_schedule_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_schedule ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_schedule_labels; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_schedule_labels (
    id integer NOT NULL,
    schedule_id integer NOT NULL,
    label_id integer NOT NULL
);


ALTER TABLE public.main_schedule_labels OWNER TO awx;

--
-- Name: main_schedule_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_schedule_labels ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_schedule_labels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_scheduleinstancegroupmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_scheduleinstancegroupmembership (
    id integer NOT NULL,
    "position" integer,
    instancegroup_id integer NOT NULL,
    schedule_id integer NOT NULL,
    CONSTRAINT main_scheduleinstancegroupmembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_scheduleinstancegroupmembership OWNER TO awx;

--
-- Name: main_scheduleinstancegroupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_scheduleinstancegroupmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_scheduleinstancegroupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_smartinventorymembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_smartinventorymembership (
    id integer NOT NULL,
    host_id integer NOT NULL,
    inventory_id integer NOT NULL
);


ALTER TABLE public.main_smartinventorymembership OWNER TO awx;

--
-- Name: main_smartinventorymembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_smartinventorymembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_smartinventorymembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_systemjob; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_systemjob (
    unifiedjob_ptr_id integer NOT NULL,
    job_type character varying(32) NOT NULL,
    extra_vars text NOT NULL,
    system_job_template_id integer
);


ALTER TABLE public.main_systemjob OWNER TO awx;

--
-- Name: main_systemjobevent; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_systemjobevent (
    id bigint NOT NULL,
    created timestamp with time zone,
    modified timestamp with time zone NOT NULL,
    event_data text NOT NULL,
    uuid character varying(1024) NOT NULL,
    counter integer NOT NULL,
    stdout text NOT NULL,
    verbosity integer NOT NULL,
    start_line integer NOT NULL,
    end_line integer NOT NULL,
    system_job_id integer NOT NULL,
    job_created timestamp with time zone NOT NULL,
    CONSTRAINT main_systemjobevent_counter_check CHECK ((counter >= 0)),
    CONSTRAINT main_systemjobevent_end_line_check CHECK ((end_line >= 0)),
    CONSTRAINT main_systemjobevent_start_line_check CHECK ((start_line >= 0)),
    CONSTRAINT main_systemjobevent_verbosity_check CHECK ((verbosity >= 0))
)
PARTITION BY RANGE (job_created);


ALTER TABLE public.main_systemjobevent OWNER TO awx;

--
-- Name: main_systemjobevent_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public._unpartitioned_main_systemjobevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_systemjobevent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_systemjobevent_id_seq1; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_systemjobevent ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_systemjobevent_id_seq1
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_systemjobtemplate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_systemjobtemplate (
    unifiedjobtemplate_ptr_id integer NOT NULL,
    job_type character varying(32) NOT NULL
);


ALTER TABLE public.main_systemjobtemplate OWNER TO awx;

--
-- Name: main_team; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_team (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    organization_id integer NOT NULL,
    admin_role_id integer,
    member_role_id integer,
    read_role_id integer
);


ALTER TABLE public.main_team OWNER TO awx;

--
-- Name: main_team_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_team ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_team_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_towerschedulestate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_towerschedulestate (
    id integer NOT NULL,
    schedule_last_run timestamp with time zone NOT NULL
);


ALTER TABLE public.main_towerschedulestate OWNER TO awx;

--
-- Name: main_towerschedulestate_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_towerschedulestate ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_towerschedulestate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjob; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjob (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    old_pk integer,
    launch_type character varying(20) NOT NULL,
    cancel_flag boolean NOT NULL,
    status character varying(20) NOT NULL,
    failed boolean NOT NULL,
    started timestamp with time zone,
    finished timestamp with time zone,
    elapsed numeric(12,3) NOT NULL,
    job_args text NOT NULL,
    job_cwd character varying(1024) NOT NULL,
    job_explanation text NOT NULL,
    start_args text NOT NULL,
    result_stdout_text text,
    result_traceback text NOT NULL,
    celery_task_id character varying(100) NOT NULL,
    created_by_id integer,
    modified_by_id integer,
    polymorphic_ctype_id integer,
    schedule_id integer,
    unified_job_template_id integer,
    execution_node text NOT NULL,
    instance_group_id integer,
    emitted_events integer NOT NULL,
    controller_node text NOT NULL,
    canceled_on timestamp with time zone,
    dependencies_processed boolean NOT NULL,
    organization_id integer,
    execution_environment_id integer,
    installed_collections jsonb NOT NULL,
    ansible_version character varying(255) NOT NULL,
    work_unit_id character varying(255),
    host_status_counts jsonb,
    preferred_instance_groups_cache jsonb,
    task_impact integer NOT NULL,
    job_env jsonb NOT NULL,
    CONSTRAINT main_unifiedjob_emitted_events_check CHECK ((emitted_events >= 0)),
    CONSTRAINT main_unifiedjob_old_pk_check CHECK ((old_pk >= 0)),
    CONSTRAINT main_unifiedjob_task_impact_check CHECK ((task_impact >= 0))
);


ALTER TABLE public.main_unifiedjob OWNER TO awx;

--
-- Name: main_unifiedjob_credentials; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjob_credentials (
    id integer NOT NULL,
    unifiedjob_id integer NOT NULL,
    credential_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjob_credentials OWNER TO awx;

--
-- Name: main_unifiedjob_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjob_credentials ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjob_credentials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjob_dependent_jobs; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjob_dependent_jobs (
    id integer NOT NULL,
    from_unifiedjob_id integer NOT NULL,
    to_unifiedjob_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjob_dependent_jobs OWNER TO awx;

--
-- Name: main_unifiedjob_dependent_jobs_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjob_dependent_jobs ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjob_dependent_jobs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjob_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjob ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjob_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjob_labels; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjob_labels (
    id integer NOT NULL,
    unifiedjob_id integer NOT NULL,
    label_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjob_labels OWNER TO awx;

--
-- Name: main_unifiedjob_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjob_labels ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjob_labels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjob_notifications; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjob_notifications (
    id integer NOT NULL,
    unifiedjob_id integer NOT NULL,
    notification_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjob_notifications OWNER TO awx;

--
-- Name: main_unifiedjob_notifications_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjob_notifications ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjob_notifications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjobtemplate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjobtemplate (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    description text NOT NULL,
    name character varying(512) NOT NULL,
    old_pk integer,
    last_job_failed boolean NOT NULL,
    last_job_run timestamp with time zone,
    next_job_run timestamp with time zone,
    status character varying(32) NOT NULL,
    created_by_id integer,
    current_job_id integer,
    last_job_id integer,
    modified_by_id integer,
    next_schedule_id integer,
    polymorphic_ctype_id integer,
    organization_id integer,
    execution_environment_id integer,
    CONSTRAINT main_unifiedjobtemplate_old_pk_check CHECK ((old_pk >= 0))
);


ALTER TABLE public.main_unifiedjobtemplate OWNER TO awx;

--
-- Name: main_unifiedjobtemplate_credentials; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjobtemplate_credentials (
    id integer NOT NULL,
    unifiedjobtemplate_id integer NOT NULL,
    credential_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjobtemplate_credentials OWNER TO awx;

--
-- Name: main_unifiedjobtemplate_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjobtemplate_credentials ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjobtemplate_credentials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjobtemplate_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjobtemplate ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjobtemplate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjobtemplate_labels; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjobtemplate_labels (
    id integer NOT NULL,
    unifiedjobtemplate_id integer NOT NULL,
    label_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjobtemplate_labels OWNER TO awx;

--
-- Name: main_unifiedjobtemplate_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjobtemplate_labels ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjobtemplate_labels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjobtemplate_notification_templates_error; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjobtemplate_notification_templates_error (
    id integer NOT NULL,
    unifiedjobtemplate_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjobtemplate_notification_templates_error OWNER TO awx;

--
-- Name: main_unifiedjobtemplate_notification_templates_error_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjobtemplate_notification_templates_error ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjobtemplate_notification_templates_error_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjobtemplate_notification_templates_started; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjobtemplate_notification_templates_started (
    id integer NOT NULL,
    unifiedjobtemplate_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjobtemplate_notification_templates_started OWNER TO awx;

--
-- Name: main_unifiedjobtemplate_notification_templates_started_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjobtemplate_notification_templates_started ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjobtemplate_notification_templates_started_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjobtemplate_notification_templates_success; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjobtemplate_notification_templates_success (
    id integer NOT NULL,
    unifiedjobtemplate_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_unifiedjobtemplate_notification_templates_success OWNER TO awx;

--
-- Name: main_unifiedjobtemplate_notification_templates_success_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjobtemplate_notification_templates_success ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjobtemplate_notification_templates_success_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_unifiedjobtemplateinstancegroupmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_unifiedjobtemplateinstancegroupmembership (
    id integer NOT NULL,
    "position" integer,
    instancegroup_id integer NOT NULL,
    unifiedjobtemplate_id integer NOT NULL,
    CONSTRAINT main_unifiedjobtemplateinstancegroupmembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_unifiedjobtemplateinstancegroupmembership OWNER TO awx;

--
-- Name: main_unifiedjobtemplateinstancegroupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_unifiedjobtemplateinstancegroupmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_unifiedjobtemplateinstancegroupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_usersessionmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_usersessionmembership (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    session_id character varying(40) NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE public.main_usersessionmembership OWNER TO awx;

--
-- Name: main_usersessionmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_usersessionmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_usersessionmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowapproval; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowapproval (
    unifiedjob_ptr_id integer NOT NULL,
    workflow_approval_template_id integer,
    timeout integer NOT NULL,
    timed_out boolean NOT NULL,
    approved_or_denied_by_id integer,
    expires timestamp with time zone
);


ALTER TABLE public.main_workflowapproval OWNER TO awx;

--
-- Name: main_workflowapprovaltemplate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowapprovaltemplate (
    unifiedjobtemplate_ptr_id integer NOT NULL,
    timeout integer NOT NULL
);


ALTER TABLE public.main_workflowapprovaltemplate OWNER TO awx;

--
-- Name: main_workflowjob; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjob (
    unifiedjob_ptr_id integer NOT NULL,
    extra_vars text NOT NULL,
    workflow_job_template_id integer,
    allow_simultaneous boolean NOT NULL,
    is_sliced_job boolean NOT NULL,
    job_template_id integer,
    inventory_id integer,
    webhook_credential_id integer,
    webhook_guid character varying(128) NOT NULL,
    webhook_service character varying(16) NOT NULL,
    is_bulk_job boolean NOT NULL,
    char_prompts jsonb NOT NULL,
    survey_passwords jsonb NOT NULL
);


ALTER TABLE public.main_workflowjob OWNER TO awx;

--
-- Name: main_workflowjobinstancegroupmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobinstancegroupmembership (
    id integer NOT NULL,
    "position" integer,
    instancegroup_id integer NOT NULL,
    workflowjobnode_id integer NOT NULL,
    CONSTRAINT main_workflowjobinstancegroupmembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_workflowjobinstancegroupmembership OWNER TO awx;

--
-- Name: main_workflowjobinstancegroupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobinstancegroupmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobinstancegroupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobnode; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobnode (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    job_id integer,
    unified_job_template_id integer,
    workflow_job_id integer,
    inventory_id integer,
    ancestor_artifacts text NOT NULL,
    extra_data text NOT NULL,
    do_not_run boolean NOT NULL,
    all_parents_must_converge boolean NOT NULL,
    identifier character varying(512) NOT NULL,
    execution_environment_id integer,
    char_prompts jsonb NOT NULL,
    survey_passwords jsonb NOT NULL
);


ALTER TABLE public.main_workflowjobnode OWNER TO awx;

--
-- Name: main_workflowjobnode_always_nodes; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobnode_always_nodes (
    id integer NOT NULL,
    from_workflowjobnode_id integer NOT NULL,
    to_workflowjobnode_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobnode_always_nodes OWNER TO awx;

--
-- Name: main_workflowjobnode_always_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobnode_always_nodes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobnode_always_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobnode_credentials; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobnode_credentials (
    id integer NOT NULL,
    workflowjobnode_id integer NOT NULL,
    credential_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobnode_credentials OWNER TO awx;

--
-- Name: main_workflowjobnode_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobnode_credentials ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobnode_credentials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobnode_failure_nodes; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobnode_failure_nodes (
    id integer NOT NULL,
    from_workflowjobnode_id integer NOT NULL,
    to_workflowjobnode_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobnode_failure_nodes OWNER TO awx;

--
-- Name: main_workflowjobnode_failure_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobnode_failure_nodes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobnode_failure_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobnode_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobnode ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobnode_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobnode_labels; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobnode_labels (
    id integer NOT NULL,
    workflowjobnode_id integer NOT NULL,
    label_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobnode_labels OWNER TO awx;

--
-- Name: main_workflowjobnode_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobnode_labels ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobnode_labels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobnode_success_nodes; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobnode_success_nodes (
    id integer NOT NULL,
    from_workflowjobnode_id integer NOT NULL,
    to_workflowjobnode_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobnode_success_nodes OWNER TO awx;

--
-- Name: main_workflowjobnode_success_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobnode_success_nodes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobnode_success_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobnodebaseinstancegroupmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobnodebaseinstancegroupmembership (
    id integer NOT NULL,
    "position" integer,
    instancegroup_id integer NOT NULL,
    workflowjobnode_id integer NOT NULL,
    CONSTRAINT main_workflowjobnodebaseinstancegroupmembership_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_workflowjobnodebaseinstancegroupmembership OWNER TO awx;

--
-- Name: main_workflowjobnodebaseinstancegroupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobnodebaseinstancegroupmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobnodebaseinstancegroupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobtemplate; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplate (
    unifiedjobtemplate_ptr_id integer NOT NULL,
    extra_vars text NOT NULL,
    admin_role_id integer,
    execute_role_id integer,
    read_role_id integer,
    survey_enabled boolean NOT NULL,
    survey_spec jsonb NOT NULL,
    allow_simultaneous boolean NOT NULL,
    ask_variables_on_launch boolean NOT NULL,
    ask_inventory_on_launch boolean NOT NULL,
    inventory_id integer,
    approval_role_id integer,
    ask_limit_on_launch boolean NOT NULL,
    ask_scm_branch_on_launch boolean NOT NULL,
    char_prompts jsonb NOT NULL,
    webhook_credential_id integer,
    webhook_key character varying(64) NOT NULL,
    webhook_service character varying(16) NOT NULL,
    ask_labels_on_launch boolean NOT NULL,
    ask_skip_tags_on_launch boolean NOT NULL,
    ask_tags_on_launch boolean NOT NULL
);


ALTER TABLE public.main_workflowjobtemplate OWNER TO awx;

--
-- Name: main_workflowjobtemplate_notification_templates_approvals; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplate_notification_templates_approvals (
    id integer NOT NULL,
    workflowjobtemplate_id integer NOT NULL,
    notificationtemplate_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobtemplate_notification_templates_approvals OWNER TO awx;

--
-- Name: main_workflowjobtemplate_notification_templates_approval_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobtemplate_notification_templates_approvals ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobtemplate_notification_templates_approval_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobtemplatenode; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplatenode (
    id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL,
    unified_job_template_id integer,
    workflow_job_template_id integer NOT NULL,
    char_prompts jsonb NOT NULL,
    inventory_id integer,
    extra_data text NOT NULL,
    survey_passwords jsonb NOT NULL,
    all_parents_must_converge boolean NOT NULL,
    identifier character varying(512) NOT NULL,
    execution_environment_id integer
);


ALTER TABLE public.main_workflowjobtemplatenode OWNER TO awx;

--
-- Name: main_workflowjobtemplatenode_always_nodes; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplatenode_always_nodes (
    id integer NOT NULL,
    from_workflowjobtemplatenode_id integer NOT NULL,
    to_workflowjobtemplatenode_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobtemplatenode_always_nodes OWNER TO awx;

--
-- Name: main_workflowjobtemplatenode_always_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobtemplatenode_always_nodes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobtemplatenode_always_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobtemplatenode_credentials; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplatenode_credentials (
    id integer NOT NULL,
    workflowjobtemplatenode_id integer NOT NULL,
    credential_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobtemplatenode_credentials OWNER TO awx;

--
-- Name: main_workflowjobtemplatenode_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobtemplatenode_credentials ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobtemplatenode_credentials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobtemplatenode_failure_nodes; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplatenode_failure_nodes (
    id integer NOT NULL,
    from_workflowjobtemplatenode_id integer NOT NULL,
    to_workflowjobtemplatenode_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobtemplatenode_failure_nodes OWNER TO awx;

--
-- Name: main_workflowjobtemplatenode_failure_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobtemplatenode_failure_nodes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobtemplatenode_failure_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobtemplatenode_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobtemplatenode ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobtemplatenode_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobtemplatenode_labels; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplatenode_labels (
    id integer NOT NULL,
    workflowjobtemplatenode_id integer NOT NULL,
    label_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobtemplatenode_labels OWNER TO awx;

--
-- Name: main_workflowjobtemplatenode_labels_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobtemplatenode_labels ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobtemplatenode_labels_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobtemplatenode_success_nodes; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplatenode_success_nodes (
    id integer NOT NULL,
    from_workflowjobtemplatenode_id integer NOT NULL,
    to_workflowjobtemplatenode_id integer NOT NULL
);


ALTER TABLE public.main_workflowjobtemplatenode_success_nodes OWNER TO awx;

--
-- Name: main_workflowjobtemplatenode_success_nodes_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobtemplatenode_success_nodes ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobtemplatenode_success_nodes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: main_workflowjobtemplatenodebaseinstancegroupmembership; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.main_workflowjobtemplatenodebaseinstancegroupmembership (
    id integer NOT NULL,
    "position" integer,
    instancegroup_id integer NOT NULL,
    workflowjobtemplatenode_id integer NOT NULL,
    CONSTRAINT main_workflowjobtemplatenodebaseinstancegroupmem_position_check CHECK (("position" >= 0))
);


ALTER TABLE public.main_workflowjobtemplatenodebaseinstancegroupmembership OWNER TO awx;

--
-- Name: main_workflowjobtemplatenodebaseinstancegroupmembership_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.main_workflowjobtemplatenodebaseinstancegroupmembership ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.main_workflowjobtemplatenodebaseinstancegroupmembership_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: oauth2_provider_grant; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.oauth2_provider_grant (
    id bigint NOT NULL,
    code character varying(255) NOT NULL,
    expires timestamp with time zone NOT NULL,
    redirect_uri text NOT NULL,
    scope text NOT NULL,
    application_id bigint NOT NULL,
    user_id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone NOT NULL,
    code_challenge character varying(128) NOT NULL,
    code_challenge_method character varying(10) NOT NULL,
    nonce character varying(255) NOT NULL,
    claims text NOT NULL
);


ALTER TABLE public.oauth2_provider_grant OWNER TO awx;

--
-- Name: oauth2_provider_grant_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.oauth2_provider_grant ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.oauth2_provider_grant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: oauth2_provider_idtoken; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.oauth2_provider_idtoken (
    id bigint NOT NULL,
    jti uuid NOT NULL,
    expires timestamp with time zone NOT NULL,
    scope text NOT NULL,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone NOT NULL,
    application_id bigint,
    user_id integer
);


ALTER TABLE public.oauth2_provider_idtoken OWNER TO awx;

--
-- Name: oauth2_provider_idtoken_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.oauth2_provider_idtoken ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.oauth2_provider_idtoken_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: oauth2_provider_refreshtoken; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.oauth2_provider_refreshtoken (
    id bigint NOT NULL,
    token character varying(255) NOT NULL,
    access_token_id bigint,
    application_id bigint NOT NULL,
    user_id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    updated timestamp with time zone NOT NULL,
    revoked timestamp with time zone
);


ALTER TABLE public.oauth2_provider_refreshtoken OWNER TO awx;

--
-- Name: oauth2_provider_refreshtoken_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.oauth2_provider_refreshtoken ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.oauth2_provider_refreshtoken_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: social_auth_association; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.social_auth_association (
    id integer NOT NULL,
    server_url character varying(255) NOT NULL,
    handle character varying(255) NOT NULL,
    secret character varying(255) NOT NULL,
    issued integer NOT NULL,
    lifetime integer NOT NULL,
    assoc_type character varying(64) NOT NULL
);


ALTER TABLE public.social_auth_association OWNER TO awx;

--
-- Name: social_auth_association_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.social_auth_association ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.social_auth_association_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: social_auth_code; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.social_auth_code (
    id integer NOT NULL,
    email character varying(254) NOT NULL,
    code character varying(32) NOT NULL,
    verified boolean NOT NULL,
    "timestamp" timestamp with time zone NOT NULL
);


ALTER TABLE public.social_auth_code OWNER TO awx;

--
-- Name: social_auth_code_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.social_auth_code ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.social_auth_code_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: social_auth_nonce; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.social_auth_nonce (
    id integer NOT NULL,
    server_url character varying(255) NOT NULL,
    "timestamp" integer NOT NULL,
    salt character varying(65) NOT NULL
);


ALTER TABLE public.social_auth_nonce OWNER TO awx;

--
-- Name: social_auth_nonce_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.social_auth_nonce ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.social_auth_nonce_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: social_auth_partial; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.social_auth_partial (
    id integer NOT NULL,
    token character varying(32) NOT NULL,
    next_step smallint NOT NULL,
    backend character varying(32) NOT NULL,
    data text NOT NULL,
    "timestamp" timestamp with time zone NOT NULL,
    CONSTRAINT social_auth_partial_next_step_check CHECK ((next_step >= 0))
);


ALTER TABLE public.social_auth_partial OWNER TO awx;

--
-- Name: social_auth_partial_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.social_auth_partial ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.social_auth_partial_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: social_auth_usersocialauth; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.social_auth_usersocialauth (
    id integer NOT NULL,
    provider character varying(32) NOT NULL,
    uid character varying(255) NOT NULL,
    extra_data text NOT NULL,
    user_id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    modified timestamp with time zone NOT NULL
);


ALTER TABLE public.social_auth_usersocialauth OWNER TO awx;

--
-- Name: social_auth_usersocialauth_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.social_auth_usersocialauth ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.social_auth_usersocialauth_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: sso_userenterpriseauth; Type: TABLE; Schema: public; Owner: awx
--

CREATE TABLE public.sso_userenterpriseauth (
    id integer NOT NULL,
    provider character varying(32) NOT NULL,
    user_id integer NOT NULL
);


ALTER TABLE public.sso_userenterpriseauth OWNER TO awx;

--
-- Name: sso_userenterpriseauth_id_seq; Type: SEQUENCE; Schema: public; Owner: awx
--

ALTER TABLE public.sso_userenterpriseauth ALTER COLUMN id ADD GENERATED BY DEFAULT AS IDENTITY (
    SEQUENCE NAME public.sso_userenterpriseauth_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: _unpartitioned_main_adhoccommandevent; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public._unpartitioned_main_adhoccommandevent (id, created, modified, host_name, event, event_data, failed, changed, counter, host_id, ad_hoc_command_id, end_line, start_line, stdout, uuid, verbosity) FROM stdin;
\.


--
-- Data for Name: _unpartitioned_main_inventoryupdateevent; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public._unpartitioned_main_inventoryupdateevent (id, created, modified, event_data, uuid, counter, stdout, verbosity, start_line, end_line, inventory_update_id) FROM stdin;
\.


--
-- Data for Name: _unpartitioned_main_jobevent; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public._unpartitioned_main_jobevent (id, created, modified, event, event_data, failed, changed, host_name, play, role, task, counter, host_id, job_id, uuid, parent_uuid, end_line, playbook, start_line, stdout, verbosity) FROM stdin;
\.


--
-- Data for Name: _unpartitioned_main_projectupdateevent; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public._unpartitioned_main_projectupdateevent (id, created, modified, event, event_data, failed, changed, uuid, playbook, play, role, task, counter, stdout, verbosity, start_line, end_line, project_update_id) FROM stdin;
\.


--
-- Data for Name: _unpartitioned_main_systemjobevent; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public._unpartitioned_main_systemjobevent (id, created, modified, event_data, uuid, counter, stdout, verbosity, start_line, end_line, system_job_id) FROM stdin;
\.


--
-- Data for Name: auth_group; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.auth_group (id, name) FROM stdin;
\.


--
-- Data for Name: auth_group_permissions; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.auth_group_permissions (id, group_id, permission_id) FROM stdin;
\.


--
-- Data for Name: auth_permission; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.auth_permission (id, name, content_type_id, codename) FROM stdin;
1	Can add permission	12	add_permission
2	Can change permission	12	change_permission
3	Can delete permission	12	delete_permission
4	Can view permission	12	view_permission
5	Can add group	13	add_group
6	Can change group	13	change_group
7	Can delete group	13	delete_group
8	Can view group	13	view_group
9	Can add user	2	add_user
10	Can change user	2	change_user
11	Can delete user	2	delete_user
12	Can view user	2	view_user
13	Can add content type	14	add_contenttype
14	Can change content type	14	change_contenttype
15	Can delete content type	14	delete_contenttype
16	Can view content type	14	view_contenttype
17	Can add session	15	add_session
18	Can change session	15	change_session
19	Can delete session	15	delete_session
20	Can view session	15	view_session
21	Can add site	16	add_site
22	Can change site	16	change_site
23	Can delete site	16	delete_site
24	Can view site	16	view_site
25	Can add grant	17	add_grant
26	Can change grant	17	change_grant
27	Can delete grant	17	delete_grant
28	Can view grant	17	view_grant
29	Can add refresh token	18	add_refreshtoken
30	Can change refresh token	18	change_refreshtoken
31	Can delete refresh token	18	delete_refreshtoken
32	Can view refresh token	18	view_refreshtoken
33	Can add id token	19	add_idtoken
34	Can change id token	19	change_idtoken
35	Can delete id token	19	delete_idtoken
36	Can view id token	19	view_idtoken
37	Can add association	20	add_association
38	Can change association	20	change_association
39	Can delete association	20	delete_association
40	Can view association	20	view_association
41	Can add code	21	add_code
42	Can change code	21	change_code
43	Can delete code	21	delete_code
44	Can view code	21	view_code
45	Can add nonce	22	add_nonce
46	Can change nonce	22	change_nonce
47	Can delete nonce	22	delete_nonce
48	Can view nonce	22	view_nonce
49	Can add user social auth	23	add_usersocialauth
50	Can change user social auth	23	change_usersocialauth
51	Can delete user social auth	23	delete_usersocialauth
52	Can view user social auth	23	view_usersocialauth
53	Can add partial	24	add_partial
54	Can change partial	24	change_partial
55	Can delete partial	24	delete_partial
56	Can view partial	24	view_partial
57	Can add setting	25	add_setting
58	Can change setting	25	change_setting
59	Can delete setting	25	delete_setting
60	Can view setting	25	view_setting
61	Can add activity stream	26	add_activitystream
62	Can change activity stream	26	change_activitystream
63	Can delete activity stream	26	delete_activitystream
64	Can view activity stream	26	view_activitystream
65	Can add ad hoc command event	27	add_adhoccommandevent
66	Can change ad hoc command event	27	change_adhoccommandevent
67	Can delete ad hoc command event	27	delete_adhoccommandevent
68	Can view ad hoc command event	27	view_adhoccommandevent
69	Can add credential	28	add_credential
70	Can change credential	28	change_credential
71	Can delete credential	28	delete_credential
72	Can view credential	28	view_credential
73	Can add custom inventory script	29	add_custominventoryscript
74	Can change custom inventory script	29	change_custominventoryscript
75	Can delete custom inventory script	29	delete_custominventoryscript
76	Can view custom inventory script	29	view_custominventoryscript
77	Can add group	30	add_group
78	Can change group	30	change_group
79	Can delete group	30	delete_group
80	Can view group	30	view_group
81	Can add host	31	add_host
82	Can change host	31	change_host
83	Can delete host	31	delete_host
84	Can view host	31	view_host
85	Can add instance	32	add_instance
86	Can change instance	32	change_instance
87	Can delete instance	32	delete_instance
88	Can view instance	32	view_instance
89	Can add inventory	33	add_inventory
90	Can change inventory	33	change_inventory
91	Can delete inventory	33	delete_inventory
92	Can view inventory	33	view_inventory
93	Can add job event	34	add_jobevent
94	Can change job event	34	change_jobevent
95	Can delete job event	34	delete_jobevent
96	Can view job event	34	view_jobevent
97	Can add job host summary	35	add_jobhostsummary
98	Can change job host summary	35	change_jobhostsummary
99	Can delete job host summary	35	delete_jobhostsummary
100	Can view job host summary	35	view_jobhostsummary
101	Can add organization	36	add_organization
102	Can change organization	36	change_organization
103	Can delete organization	36	delete_organization
104	Can view organization	36	view_organization
105	Can add profile	37	add_profile
106	Can change profile	37	change_profile
107	Can delete profile	37	delete_profile
108	Can view profile	37	view_profile
109	Can add schedule	38	add_schedule
110	Can change schedule	38	change_schedule
111	Can delete schedule	38	delete_schedule
112	Can view schedule	38	view_schedule
113	Can add team	39	add_team
114	Can change team	39	change_team
115	Can delete team	39	delete_team
116	Can view team	39	view_team
117	Can add unified job	40	add_unifiedjob
118	Can change unified job	40	change_unifiedjob
119	Can delete unified job	40	delete_unifiedjob
120	Can view unified job	40	view_unifiedjob
121	Can add unified job template	41	add_unifiedjobtemplate
122	Can change unified job template	41	change_unifiedjobtemplate
123	Can delete unified job template	41	delete_unifiedjobtemplate
124	Can view unified job template	41	view_unifiedjobtemplate
125	Can add ad hoc command	7	add_adhoccommand
126	Can change ad hoc command	7	change_adhoccommand
127	Can delete ad hoc command	7	delete_adhoccommand
128	Can view ad hoc command	7	view_adhoccommand
129	Can add inventory source	3	add_inventorysource
130	Can change inventory source	3	change_inventorysource
131	Can delete inventory source	3	delete_inventorysource
132	Can view inventory source	3	view_inventorysource
133	Can add inventory update	8	add_inventoryupdate
134	Can change inventory update	8	change_inventoryupdate
135	Can delete inventory update	8	delete_inventoryupdate
136	Can view inventory update	8	view_inventoryupdate
137	Can add job	9	add_job
138	Can change job	9	change_job
139	Can delete job	9	delete_job
140	Can view job	9	view_job
141	Can add job template	4	add_jobtemplate
142	Can change job template	4	change_jobtemplate
143	Can delete job template	4	delete_jobtemplate
144	Can view job template	4	view_jobtemplate
145	Can add project	5	add_project
146	Can change project	5	change_project
147	Can delete project	5	delete_project
148	Can view project	5	view_project
149	Can add project update	10	add_projectupdate
150	Can change project update	10	change_projectupdate
151	Can delete project update	10	delete_projectupdate
152	Can view project update	10	view_projectupdate
153	Can add system job	42	add_systemjob
154	Can change system job	42	change_systemjob
155	Can delete system job	42	delete_systemjob
156	Can view system job	42	view_systemjob
157	Can add system job template	1	add_systemjobtemplate
158	Can change system job template	1	change_systemjobtemplate
159	Can delete system job template	1	delete_systemjobtemplate
160	Can view system job template	1	view_systemjobtemplate
161	Can add notification	43	add_notification
162	Can change notification	43	change_notification
163	Can delete notification	43	delete_notification
164	Can view notification	43	view_notification
165	Can add notification template	44	add_notificationtemplate
166	Can change notification template	44	change_notificationtemplate
167	Can delete notification template	44	delete_notificationtemplate
168	Can view notification template	44	view_notificationtemplate
169	Can add role	45	add_role
170	Can change role	45	change_role
171	Can delete role	45	delete_role
172	Can view role	45	view_role
173	Can add role ancestor entry	46	add_roleancestorentry
174	Can change role ancestor entry	46	change_roleancestorentry
175	Can delete role ancestor entry	46	delete_roleancestorentry
176	Can view role ancestor entry	46	view_roleancestorentry
177	Can add label	47	add_label
178	Can change label	47	change_label
179	Can delete label	47	delete_label
180	Can view label	47	view_label
181	Can add workflow job	11	add_workflowjob
182	Can change workflow job	11	change_workflowjob
183	Can delete workflow job	11	delete_workflowjob
184	Can view workflow job	11	view_workflowjob
185	Can add workflow job node	48	add_workflowjobnode
186	Can change workflow job node	48	change_workflowjobnode
187	Can delete workflow job node	48	delete_workflowjobnode
188	Can view workflow job node	48	view_workflowjobnode
189	Can add workflow job template	6	add_workflowjobtemplate
190	Can change workflow job template	6	change_workflowjobtemplate
191	Can delete workflow job template	6	delete_workflowjobtemplate
192	Can view workflow job template	6	view_workflowjobtemplate
193	Can add workflow job template node	49	add_workflowjobtemplatenode
194	Can change workflow job template node	49	change_workflowjobtemplatenode
195	Can delete workflow job template node	49	delete_workflowjobtemplatenode
196	Can view workflow job template node	49	view_workflowjobtemplatenode
197	Can add tower schedule state	50	add_towerschedulestate
198	Can change tower schedule state	50	change_towerschedulestate
199	Can delete tower schedule state	50	delete_towerschedulestate
200	Can view tower schedule state	50	view_towerschedulestate
201	Can add smart inventory membership	51	add_smartinventorymembership
202	Can change smart inventory membership	51	change_smartinventorymembership
203	Can delete smart inventory membership	51	delete_smartinventorymembership
204	Can view smart inventory membership	51	view_smartinventorymembership
205	Can add credential type	52	add_credentialtype
206	Can change credential type	52	change_credentialtype
207	Can delete credential type	52	delete_credentialtype
208	Can view credential type	52	view_credentialtype
209	Can add instance group	53	add_instancegroup
210	Can change instance group	53	change_instancegroup
211	Can delete instance group	53	delete_instancegroup
212	Can view instance group	53	view_instancegroup
213	Can add job launch config	54	add_joblaunchconfig
214	Can change job launch config	54	change_joblaunchconfig
215	Can delete job launch config	54	delete_joblaunchconfig
216	Can view job launch config	54	view_joblaunchconfig
217	Can add unified job deprecated stdout	55	add_unifiedjobdeprecatedstdout
218	Can change unified job deprecated stdout	55	change_unifiedjobdeprecatedstdout
219	Can delete unified job deprecated stdout	55	delete_unifiedjobdeprecatedstdout
220	Can view unified job deprecated stdout	55	view_unifiedjobdeprecatedstdout
221	Can add inventory update event	56	add_inventoryupdateevent
222	Can change inventory update event	56	change_inventoryupdateevent
223	Can delete inventory update event	56	delete_inventoryupdateevent
224	Can view inventory update event	56	view_inventoryupdateevent
225	Can add project update event	57	add_projectupdateevent
226	Can change project update event	57	change_projectupdateevent
227	Can delete project update event	57	delete_projectupdateevent
228	Can view project update event	57	view_projectupdateevent
229	Can add system job event	58	add_systemjobevent
230	Can change system job event	58	change_systemjobevent
231	Can delete system job event	58	delete_systemjobevent
232	Can view system job event	58	view_systemjobevent
233	Can add user session membership	59	add_usersessionmembership
234	Can change user session membership	59	change_usersessionmembership
235	Can delete user session membership	59	delete_usersessionmembership
236	Can view user session membership	59	view_usersessionmembership
237	Can add application	60	add_oauth2application
238	Can change application	60	change_oauth2application
239	Can delete application	60	delete_oauth2application
240	Can view application	60	view_oauth2application
241	Can add access token	61	add_oauth2accesstoken
242	Can change access token	61	change_oauth2accesstoken
243	Can delete access token	61	delete_oauth2accesstoken
244	Can view access token	61	view_oauth2accesstoken
245	Can add credential input source	62	add_credentialinputsource
246	Can change credential input source	62	change_credentialinputsource
247	Can delete credential input source	62	delete_credentialinputsource
248	Can view credential input source	62	view_credentialinputsource
249	Can add inventory instance group membership	63	add_inventoryinstancegroupmembership
250	Can change inventory instance group membership	63	change_inventoryinstancegroupmembership
251	Can delete inventory instance group membership	63	delete_inventoryinstancegroupmembership
252	Can view inventory instance group membership	63	view_inventoryinstancegroupmembership
253	Can add organization instance group membership	64	add_organizationinstancegroupmembership
254	Can change organization instance group membership	64	change_organizationinstancegroupmembership
255	Can delete organization instance group membership	64	delete_organizationinstancegroupmembership
256	Can view organization instance group membership	64	view_organizationinstancegroupmembership
257	Can add unified job template instance group membership	65	add_unifiedjobtemplateinstancegroupmembership
258	Can change unified job template instance group membership	65	change_unifiedjobtemplateinstancegroupmembership
259	Can delete unified job template instance group membership	65	delete_unifiedjobtemplateinstancegroupmembership
260	Can view unified job template instance group membership	65	view_unifiedjobtemplateinstancegroupmembership
261	Can add workflow approval template	66	add_workflowapprovaltemplate
262	Can change workflow approval template	66	change_workflowapprovaltemplate
263	Can delete workflow approval template	66	delete_workflowapprovaltemplate
264	Can view workflow approval template	66	view_workflowapprovaltemplate
265	Can add workflow approval	67	add_workflowapproval
266	Can change workflow approval	67	change_workflowapproval
267	Can delete workflow approval	67	delete_workflowapproval
268	Can view workflow approval	67	view_workflowapproval
269	Can add organization galaxy credential membership	68	add_organizationgalaxycredentialmembership
270	Can change organization galaxy credential membership	68	change_organizationgalaxycredentialmembership
271	Can delete organization galaxy credential membership	68	delete_organizationgalaxycredentialmembership
272	Can view organization galaxy credential membership	68	view_organizationgalaxycredentialmembership
273	Can add execution environment	69	add_executionenvironment
274	Can change execution environment	69	change_executionenvironment
275	Can delete execution environment	69	delete_executionenvironment
276	Can view execution environment	69	view_executionenvironment
277	Can add host metric	70	add_hostmetric
278	Can change host metric	70	change_hostmetric
279	Can delete host metric	70	delete_hostmetric
280	Can view host metric	70	view_hostmetric
281	Can add unpartitioned ad hoc command event	71	add_unpartitionedadhoccommandevent
282	Can change unpartitioned ad hoc command event	71	change_unpartitionedadhoccommandevent
283	Can delete unpartitioned ad hoc command event	71	delete_unpartitionedadhoccommandevent
284	Can view unpartitioned ad hoc command event	71	view_unpartitionedadhoccommandevent
285	Can add unpartitioned inventory update event	72	add_unpartitionedinventoryupdateevent
286	Can change unpartitioned inventory update event	72	change_unpartitionedinventoryupdateevent
287	Can delete unpartitioned inventory update event	72	delete_unpartitionedinventoryupdateevent
288	Can view unpartitioned inventory update event	72	view_unpartitionedinventoryupdateevent
289	Can add unpartitioned job event	73	add_unpartitionedjobevent
290	Can change unpartitioned job event	73	change_unpartitionedjobevent
291	Can delete unpartitioned job event	73	delete_unpartitionedjobevent
292	Can view unpartitioned job event	73	view_unpartitionedjobevent
293	Can add unpartitioned project update event	74	add_unpartitionedprojectupdateevent
294	Can change unpartitioned project update event	74	change_unpartitionedprojectupdateevent
295	Can delete unpartitioned project update event	74	delete_unpartitionedprojectupdateevent
296	Can view unpartitioned project update event	74	view_unpartitionedprojectupdateevent
297	Can add unpartitioned system job event	75	add_unpartitionedsystemjobevent
298	Can change unpartitioned system job event	75	change_unpartitionedsystemjobevent
299	Can delete unpartitioned system job event	75	delete_unpartitionedsystemjobevent
300	Can view unpartitioned system job event	75	view_unpartitionedsystemjobevent
301	Can add instance link	76	add_instancelink
302	Can change instance link	76	change_instancelink
303	Can delete instance link	76	delete_instancelink
304	Can view instance link	76	view_instancelink
305	Can add workflow job template node base instance group membership	77	add_workflowjobtemplatenodebaseinstancegroupmembership
306	Can change workflow job template node base instance group membership	77	change_workflowjobtemplatenodebaseinstancegroupmembership
307	Can delete workflow job template node base instance group membership	77	delete_workflowjobtemplatenodebaseinstancegroupmembership
308	Can view workflow job template node base instance group membership	77	view_workflowjobtemplatenodebaseinstancegroupmembership
309	Can add workflow job node base instance group membership	78	add_workflowjobnodebaseinstancegroupmembership
310	Can change workflow job node base instance group membership	78	change_workflowjobnodebaseinstancegroupmembership
311	Can delete workflow job node base instance group membership	78	delete_workflowjobnodebaseinstancegroupmembership
312	Can view workflow job node base instance group membership	78	view_workflowjobnodebaseinstancegroupmembership
313	Can add workflow job instance group membership	79	add_workflowjobinstancegroupmembership
314	Can change workflow job instance group membership	79	change_workflowjobinstancegroupmembership
315	Can delete workflow job instance group membership	79	delete_workflowjobinstancegroupmembership
316	Can view workflow job instance group membership	79	view_workflowjobinstancegroupmembership
317	Can add schedule instance group membership	80	add_scheduleinstancegroupmembership
318	Can change schedule instance group membership	80	change_scheduleinstancegroupmembership
319	Can delete schedule instance group membership	80	delete_scheduleinstancegroupmembership
320	Can view schedule instance group membership	80	view_scheduleinstancegroupmembership
321	Can add job launch config instance group membership	81	add_joblaunchconfiginstancegroupmembership
322	Can change job launch config instance group membership	81	change_joblaunchconfiginstancegroupmembership
323	Can delete job launch config instance group membership	81	delete_joblaunchconfiginstancegroupmembership
324	Can view job launch config instance group membership	81	view_joblaunchconfiginstancegroupmembership
325	Can add host metric summary monthly	82	add_hostmetricsummarymonthly
326	Can change host metric summary monthly	82	change_hostmetricsummarymonthly
327	Can delete host metric summary monthly	82	delete_hostmetricsummarymonthly
328	Can view host metric summary monthly	82	view_hostmetricsummarymonthly
329	Can add inventory constructed inventory membership	83	add_inventoryconstructedinventorymembership
330	Can change inventory constructed inventory membership	83	change_inventoryconstructedinventorymembership
331	Can delete inventory constructed inventory membership	83	delete_inventoryconstructedinventorymembership
332	Can view inventory constructed inventory membership	83	view_inventoryconstructedinventorymembership
333	Can add user enterprise auth	84	add_userenterpriseauth
334	Can change user enterprise auth	84	change_userenterpriseauth
335	Can delete user enterprise auth	84	delete_userenterpriseauth
336	Can view user enterprise auth	84	view_userenterpriseauth
\.


--
-- Data for Name: auth_user; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.auth_user (id, password, last_login, is_superuser, username, first_name, last_name, email, is_staff, is_active, date_joined) FROM stdin;
1	pbkdf2_sha256$600000$vQ7k4k6RiHYEgYLFEmIL34$h+oixZUSCUFqBQXfk+9MP2oO3Qx6eiujsCncs0ILIXo=	\N	t	admin			admin@localhost	t	t	2023-09-28 09:16:03.270523+00
\.


--
-- Data for Name: auth_user_groups; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.auth_user_groups (id, user_id, group_id) FROM stdin;
\.


--
-- Data for Name: auth_user_user_permissions; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.auth_user_user_permissions (id, user_id, permission_id) FROM stdin;
\.


--
-- Data for Name: conf_setting; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.conf_setting (id, created, modified, key, value, user_id) FROM stdin;
1	2023-09-28 09:14:25.240499+00	2023-09-28 09:14:25.240506+00	INSTALL_UUID	"8c254265-de11-4b4a-85aa-9b3a5eea2b9d"	\N
\.


--
-- Data for Name: django_content_type; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.django_content_type (id, app_label, model) FROM stdin;
1	main	systemjobtemplate
2	auth	user
3	main	inventorysource
4	main	jobtemplate
5	main	project
6	main	workflowjobtemplate
7	main	adhoccommand
8	main	inventoryupdate
9	main	job
10	main	projectupdate
11	main	workflowjob
12	auth	permission
13	auth	group
14	contenttypes	contenttype
15	sessions	session
16	sites	site
17	oauth2_provider	grant
18	oauth2_provider	refreshtoken
19	oauth2_provider	idtoken
20	social_django	association
21	social_django	code
22	social_django	nonce
23	social_django	usersocialauth
24	social_django	partial
25	conf	setting
26	main	activitystream
27	main	adhoccommandevent
28	main	credential
29	main	custominventoryscript
30	main	group
31	main	host
32	main	instance
33	main	inventory
34	main	jobevent
35	main	jobhostsummary
36	main	organization
37	main	profile
38	main	schedule
39	main	team
40	main	unifiedjob
41	main	unifiedjobtemplate
42	main	systemjob
43	main	notification
44	main	notificationtemplate
45	main	role
46	main	roleancestorentry
47	main	label
48	main	workflowjobnode
49	main	workflowjobtemplatenode
50	main	towerschedulestate
51	main	smartinventorymembership
52	main	credentialtype
53	main	instancegroup
54	main	joblaunchconfig
55	main	unifiedjobdeprecatedstdout
56	main	inventoryupdateevent
57	main	projectupdateevent
58	main	systemjobevent
59	main	usersessionmembership
60	main	oauth2application
61	main	oauth2accesstoken
62	main	credentialinputsource
63	main	inventoryinstancegroupmembership
64	main	organizationinstancegroupmembership
65	main	unifiedjobtemplateinstancegroupmembership
66	main	workflowapprovaltemplate
67	main	workflowapproval
68	main	organizationgalaxycredentialmembership
69	main	executionenvironment
70	main	hostmetric
71	main	unpartitionedadhoccommandevent
72	main	unpartitionedinventoryupdateevent
73	main	unpartitionedjobevent
74	main	unpartitionedprojectupdateevent
75	main	unpartitionedsystemjobevent
76	main	instancelink
77	main	workflowjobtemplatenodebaseinstancegroupmembership
78	main	workflowjobnodebaseinstancegroupmembership
79	main	workflowjobinstancegroupmembership
80	main	scheduleinstancegroupmembership
81	main	joblaunchconfiginstancegroupmembership
82	main	hostmetricsummarymonthly
83	main	inventoryconstructedinventorymembership
84	sso	userenterpriseauth
\.


--
-- Data for Name: django_migrations; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.django_migrations (id, app, name, applied) FROM stdin;
1	contenttypes	0001_initial	2023-09-28 09:13:03.684836+00
2	contenttypes	0002_remove_content_type_name	2023-09-28 09:13:03.786484+00
3	auth	0001_initial	2023-09-28 09:13:03.930479+00
4	main	0001_initial	2023-09-28 09:13:10.294005+00
5	main	0002_v300_tower_settings_changes	2023-09-28 09:13:20.221258+00
6	main	0003_v300_notification_changes	2023-09-28 09:13:20.228769+00
7	main	0004_v300_fact_changes	2023-09-28 09:13:20.236014+00
8	main	0005_v300_migrate_facts	2023-09-28 09:13:20.243996+00
9	main	0006_v300_active_flag_cleanup	2023-09-28 09:13:20.252549+00
10	main	0007_v300_active_flag_removal	2023-09-28 09:13:20.260792+00
11	main	0008_v300_rbac_changes	2023-09-28 09:13:20.269791+00
12	main	0009_v300_rbac_migrations	2023-09-28 09:13:20.278728+00
13	main	0010_v300_create_system_job_templates	2023-09-28 09:13:20.287719+00
14	main	0011_v300_credential_domain_field	2023-09-28 09:13:20.296241+00
15	main	0012_v300_create_labels	2023-09-28 09:13:20.304931+00
16	main	0013_v300_label_changes	2023-09-28 09:13:20.312664+00
17	main	0014_v300_invsource_cred	2023-09-28 09:13:20.320918+00
18	main	0015_v300_label_changes	2023-09-28 09:13:20.328742+00
19	main	0016_v300_prompting_changes	2023-09-28 09:13:20.334581+00
20	main	0017_v300_prompting_migrations	2023-09-28 09:13:20.341134+00
21	main	0018_v300_host_ordering	2023-09-28 09:13:20.34812+00
22	main	0019_v300_new_azure_credential	2023-09-28 09:13:20.354334+00
23	main	0020_v300_labels_changes	2023-09-28 09:13:22.515882+00
24	main	0021_v300_activity_stream	2023-09-28 09:13:22.521927+00
25	main	0022_v300_adhoc_extravars	2023-09-28 09:13:22.52801+00
26	main	0023_v300_activity_stream_ordering	2023-09-28 09:13:22.534106+00
27	main	0024_v300_jobtemplate_allow_simul	2023-09-28 09:13:22.540682+00
28	main	0025_v300_update_rbac_parents	2023-09-28 09:13:22.547002+00
29	main	0026_v300_credential_unique	2023-09-28 09:13:22.553059+00
30	main	0027_v300_team_migrations	2023-09-28 09:13:22.559203+00
31	main	0028_v300_org_team_cascade	2023-09-28 09:13:22.565834+00
32	main	0029_v302_add_ask_skip_tags	2023-09-28 09:13:36.059779+00
33	main	0030_v302_job_survey_passwords	2023-09-28 09:13:36.067228+00
34	main	0031_v302_migrate_survey_passwords	2023-09-28 09:13:36.074497+00
35	main	0032_v302_credential_permissions_update	2023-09-28 09:13:36.082411+00
36	main	0033_v303_v245_host_variable_fix	2023-09-28 09:13:36.090441+00
37	main	0034_v310_release	2023-09-28 09:13:36.097966+00
38	conf	0001_initial	2023-09-28 09:13:36.209051+00
39	conf	0002_v310_copy_tower_settings	2023-09-28 09:13:36.483732+00
40	main	0035_v310_remove_tower_settings	2023-09-28 09:13:37.265151+00
41	main	0036_v311_insights	2023-09-28 09:13:37.271192+00
42	main	0037_v313_instance_version	2023-09-28 09:13:37.277538+00
43	main	0006_v320_release	2023-09-28 09:13:47.994878+00
44	main	0007_v320_data_migrations	2023-09-28 09:13:48.004557+00
45	main	0008_v320_drop_v1_credential_fields	2023-09-28 09:13:51.745991+00
46	main	0009_v322_add_setting_field_for_activity_stream	2023-09-28 09:13:51.840051+00
47	main	0010_v322_add_ovirt4_tower_inventory	2023-09-28 09:13:52.203049+00
48	main	0011_v322_encrypt_survey_passwords	2023-09-28 09:13:52.212146+00
49	main	0012_v322_update_cred_types	2023-09-28 09:13:52.220614+00
50	main	0013_v330_multi_credential	2023-09-28 09:13:53.83463+00
51	auth	0002_alter_permission_name_max_length	2023-09-28 09:13:53.972651+00
52	auth	0003_alter_user_email_max_length	2023-09-28 09:13:54.266536+00
53	auth	0004_alter_user_username_opts	2023-09-28 09:13:54.361818+00
54	auth	0005_alter_user_last_login_null	2023-09-28 09:13:54.462525+00
55	auth	0006_require_contenttypes_0002	2023-09-28 09:13:54.472321+00
56	auth	0007_alter_validators_add_error_messages	2023-09-28 09:13:54.566387+00
57	auth	0008_alter_user_username_max_length	2023-09-28 09:13:54.670189+00
58	auth	0009_alter_user_last_name_max_length	2023-09-28 09:13:54.776739+00
59	auth	0010_alter_group_name_max_length	2023-09-28 09:13:55.044389+00
60	auth	0011_update_proxy_permissions	2023-09-28 09:13:55.136693+00
61	auth	0012_alter_user_first_name_max_length	2023-09-28 09:13:55.238749+00
62	conf	0003_v310_JSONField_changes	2023-09-28 09:13:55.315201+00
63	conf	0004_v320_reencrypt	2023-09-28 09:13:55.324108+00
64	conf	0005_v330_rename_two_session_settings	2023-09-28 09:13:55.434206+00
65	conf	0006_v331_ldap_group_type	2023-09-28 09:13:55.706691+00
66	conf	0007_v380_rename_more_settings	2023-09-28 09:13:55.822856+00
67	conf	0008_subscriptions	2023-09-28 09:13:56.063379+00
68	conf	0009_rename_proot_settings	2023-09-28 09:13:56.174468+00
69	conf	0010_change_to_JSONField	2023-09-28 09:13:56.253704+00
70	sessions	0001_initial	2023-09-28 09:13:56.28462+00
71	main	0014_v330_saved_launchtime_configs	2023-09-28 09:13:59.930816+00
72	main	0015_v330_blank_start_args	2023-09-28 09:13:59.939972+00
73	main	0016_v330_non_blank_workflow	2023-09-28 09:14:00.247377+00
74	main	0017_v330_move_deprecated_stdout	2023-09-28 09:14:00.495441+00
75	main	0018_v330_add_additional_stdout_events	2023-09-28 09:14:01.543387+00
76	main	0019_v330_custom_virtualenv	2023-09-28 09:14:01.935944+00
77	main	0020_v330_instancegroup_policies	2023-09-28 09:14:02.136022+00
78	main	0021_v330_declare_new_rbac_roles	2023-09-28 09:14:04.703042+00
79	main	0022_v330_create_new_rbac_roles	2023-09-28 09:14:04.938539+00
80	main	0023_v330_inventory_multicred	2023-09-28 09:14:05.601814+00
81	main	0024_v330_create_user_session_membership	2023-09-28 09:14:05.771795+00
82	main	0025_v330_add_oauth_activity_stream_registrar	2023-09-28 09:14:06.550755+00
83	oauth2_provider	0001_initial	2023-09-28 09:14:07.46734+00
84	oauth2_provider	0002_auto_20190406_1805	2023-09-28 09:14:07.61474+00
85	oauth2_provider	0003_auto_20201211_1314	2023-09-28 09:14:07.8671+00
86	oauth2_provider	0004_auto_20200902_2022	2023-09-28 09:14:08.776064+00
87	oauth2_provider	0005_auto_20211222_2352	2023-09-28 09:14:09.796986+00
88	main	0026_v330_delete_authtoken	2023-09-28 09:14:09.931272+00
89	main	0027_v330_emitted_events	2023-09-28 09:14:10.040581+00
90	main	0028_v330_add_tower_verify	2023-09-28 09:14:10.167094+00
91	main	0030_v330_modify_application	2023-09-28 09:14:10.552924+00
92	main	0031_v330_encrypt_oauth2_secret	2023-09-28 09:14:10.680697+00
93	main	0032_v330_polymorphic_delete	2023-09-28 09:14:10.809572+00
94	main	0033_v330_oauth_help_text	2023-09-28 09:14:11.706007+00
95	main	0034_v330_delete_user_role	2023-09-28 09:14:12.262277+00
96	main	0035_v330_more_oauth2_help_text	2023-09-28 09:14:12.398207+00
97	main	0036_v330_credtype_remove_become_methods	2023-09-28 09:14:12.520494+00
98	main	0037_v330_remove_legacy_fact_cleanup	2023-09-28 09:14:12.679252+00
99	main	0038_v330_add_deleted_activitystream_actor	2023-09-28 09:14:13.001541+00
100	main	0039_v330_custom_venv_help_text	2023-09-28 09:14:13.370493+00
101	main	0040_v330_unifiedjob_controller_node	2023-09-28 09:14:13.499641+00
102	main	0041_v330_update_oauth_refreshtoken	2023-09-28 09:14:13.88994+00
103	main	0042_v330_org_member_role_deparent	2023-09-28 09:14:14.286973+00
104	main	0043_v330_oauth2accesstoken_modified	2023-09-28 09:14:14.3704+00
105	main	0044_v330_add_inventory_update_inventory	2023-09-28 09:14:14.825968+00
106	main	0045_v330_instance_managed_by_policy	2023-09-28 09:14:14.844421+00
107	main	0046_v330_remove_client_credentials_grant	2023-09-28 09:14:14.976778+00
108	main	0047_v330_activitystream_instance	2023-09-28 09:14:15.18388+00
109	main	0048_v330_django_created_modified_by_model_name	2023-09-28 09:14:19.379835+00
110	main	0049_v330_validate_instance_capacity_adjustment	2023-09-28 09:14:19.502686+00
111	main	0050_v340_drop_celery_tables	2023-09-28 09:14:19.540478+00
112	main	0051_v340_job_slicing	2023-09-28 09:14:20.396355+00
113	main	0052_v340_remove_project_scm_delete_on_next_update	2023-09-28 09:14:20.493615+00
114	main	0053_v340_workflow_inventory	2023-09-28 09:14:21.169277+00
115	main	0054_v340_workflow_convergence	2023-09-28 09:14:21.286998+00
116	main	0055_v340_add_grafana_notification	2023-09-28 09:14:21.776451+00
117	main	0056_v350_custom_venv_history	2023-09-28 09:14:21.96648+00
118	main	0057_v350_remove_become_method_type	2023-09-28 09:14:22.076961+00
119	main	0058_v350_remove_limit_limit	2023-09-28 09:14:22.554877+00
120	main	0059_v350_remove_adhoc_limit	2023-09-28 09:14:22.693671+00
121	main	0060_v350_update_schedule_uniqueness_constraint	2023-09-28 09:14:22.933331+00
122	main	0061_v350_track_native_credentialtype_source	2023-09-28 09:14:23.391143+00
123	main	0062_v350_new_playbook_stats	2023-09-28 09:14:23.544403+00
124	main	0063_v350_org_host_limits	2023-09-28 09:14:23.724693+00
125	main	0064_v350_analytics_state	2023-09-28 09:14:23.7418+00
126	main	0065_v350_index_job_status	2023-09-28 09:14:23.902805+00
127	main	0066_v350_inventorysource_custom_virtualenv	2023-09-28 09:14:24.245259+00
128	main	0067_v350_credential_plugins	2023-09-28 09:14:24.795988+00
129	main	0068_v350_index_event_created	2023-09-28 09:14:25.125684+00
130	main	0069_v350_generate_unique_install_uuid	2023-09-28 09:14:25.249018+00
131	main	0070_v350_gce_instance_id	2023-09-28 09:14:25.378311+00
132	main	0071_v350_remove_system_tracking	2023-09-28 09:14:25.548163+00
133	main	0072_v350_deprecate_fields	2023-09-28 09:14:27.80743+00
134	main	0073_v360_create_instance_group_m2m	2023-09-28 09:14:28.406573+00
135	main	0074_v360_migrate_instance_group_relations	2023-09-28 09:14:28.550864+00
136	main	0075_v360_remove_old_instance_group_relations	2023-09-28 09:14:29.129725+00
137	main	0076_v360_add_new_instance_group_relations	2023-09-28 09:14:29.522249+00
138	main	0077_v360_add_default_orderings	2023-09-28 09:14:30.795464+00
139	main	0078_v360_clear_sessions_tokens_jt	2023-09-28 09:14:31.18643+00
140	main	0079_v360_rm_implicit_oauth2_apps	2023-09-28 09:14:31.33227+00
141	main	0080_v360_replace_job_origin	2023-09-28 09:14:32.181066+00
142	main	0081_v360_notify_on_start	2023-09-28 09:14:33.407967+00
143	main	0082_v360_webhook_http_method	2023-09-28 09:14:33.52891+00
144	main	0083_v360_job_branch_override	2023-09-28 09:14:34.484441+00
145	main	0084_v360_token_description	2023-09-28 09:14:34.629485+00
146	main	0085_v360_add_notificationtemplate_messages	2023-09-28 09:14:35.19062+00
147	main	0086_v360_workflow_approval	2023-09-28 09:14:37.013449+00
148	main	0087_v360_update_credential_injector_help_text	2023-09-28 09:14:37.585973+00
149	main	0088_v360_dashboard_optimizations	2023-09-28 09:14:38.024167+00
150	main	0089_v360_new_job_event_types	2023-09-28 09:14:38.168948+00
151	main	0090_v360_WFJT_prompts	2023-09-28 09:14:39.583877+00
152	main	0091_v360_approval_node_notifications	2023-09-28 09:14:40.195591+00
153	main	0092_v360_webhook_mixin	2023-09-28 09:14:41.215572+00
154	main	0093_v360_personal_access_tokens	2023-09-28 09:14:41.601484+00
155	main	0094_v360_webhook_mixin2	2023-09-28 09:14:42.655198+00
156	main	0095_v360_increase_instance_version_length	2023-09-28 09:14:42.787679+00
157	main	0096_v360_container_groups	2023-09-28 09:14:43.57197+00
158	main	0097_v360_workflowapproval_approved_or_denied_by	2023-09-28 09:14:43.712327+00
159	main	0098_v360_rename_cyberark_aim_credential_type	2023-09-28 09:14:43.9956+00
160	main	0099_v361_license_cleanup	2023-09-28 09:14:44.11901+00
161	main	0100_v370_projectupdate_job_tags	2023-09-28 09:14:44.227022+00
162	main	0101_v370_generate_new_uuids_for_iso_nodes	2023-09-28 09:14:44.560471+00
163	main	0102_v370_unifiedjob_canceled	2023-09-28 09:14:44.685424+00
164	main	0103_v370_remove_computed_fields	2023-09-28 09:14:46.088678+00
165	main	0104_v370_cleanup_old_scan_jts	2023-09-28 09:14:46.34526+00
166	main	0105_v370_remove_jobevent_parent_and_hosts	2023-09-28 09:14:46.608621+00
167	main	0106_v370_remove_inventory_groups_with_active_failures	2023-09-28 09:14:46.721392+00
168	main	0107_v370_workflow_convergence_api_toggle	2023-09-28 09:14:47.200403+00
169	main	0108_v370_unifiedjob_dependencies_processed	2023-09-28 09:14:47.317549+00
170	main	0109_v370_job_template_organization_field	2023-09-28 09:14:49.701307+00
171	main	0110_v370_instance_ip_address	2023-09-28 09:14:49.750609+00
172	main	0111_v370_delete_channelgroup	2023-09-28 09:14:49.764628+00
173	main	0112_v370_workflow_node_identifier	2023-09-28 09:14:50.788266+00
174	main	0113_v370_event_bigint	2023-09-28 09:14:51.663835+00
175	main	0114_v370_remove_deprecated_manual_inventory_sources	2023-09-28 09:14:52.597938+00
176	main	0115_v370_schedule_set_null	2023-09-28 09:14:52.863397+00
177	main	0116_v400_remove_hipchat_notifications	2023-09-28 09:14:53.43277+00
178	main	0117_v400_remove_cloudforms_inventory	2023-09-28 09:14:54.015706+00
179	main	0118_add_remote_archive_scm_type	2023-09-28 09:14:54.256952+00
180	main	0119_inventory_plugins	2023-09-28 09:14:55.880074+00
181	main	0120_galaxy_credentials	2023-09-28 09:14:56.773426+00
182	main	0121_delete_toweranalyticsstate	2023-09-28 09:14:56.78571+00
183	main	0122_really_remove_cloudforms_inventory	2023-09-28 09:14:56.920113+00
184	main	0123_drop_hg_support	2023-09-28 09:14:57.496666+00
185	main	0124_execution_environments	2023-09-28 09:14:58.444051+00
186	main	0125_more_ee_modeling_changes	2023-09-28 09:14:59.336712+00
187	main	0126_executionenvironment_container_options	2023-09-28 09:14:59.446283+00
188	main	0127_reset_pod_spec_override	2023-09-28 09:14:59.779588+00
189	main	0128_organiaztion_read_roles_ee_admin	2023-09-28 09:14:59.917756+00
190	main	0129_unifiedjob_installed_collections	2023-09-28 09:15:00.04244+00
191	main	0130_ee_polymorphic_set_null	2023-09-28 09:15:00.741674+00
192	main	0131_undo_org_polymorphic_ee	2023-09-28 09:15:00.873969+00
193	main	0132_instancegroup_is_container_group	2023-09-28 09:15:01.305125+00
194	main	0133_centrify_vault_credtype	2023-09-28 09:15:01.559877+00
195	main	0134_unifiedjob_ansible_version	2023-09-28 09:15:01.669292+00
196	main	0135_schedule_sort_fallback_to_id	2023-09-28 09:15:01.773283+00
197	main	0136_scm_track_submodules	2023-09-28 09:15:01.955341+00
198	main	0137_custom_inventory_scripts_removal_data	2023-09-28 09:15:02.275321+00
199	main	0138_custom_inventory_scripts_removal	2023-09-28 09:15:03.944453+00
200	main	0139_isolated_removal	2023-09-28 09:15:04.374479+00
201	main	0140_rename	2023-09-28 09:15:05.924735+00
202	main	0141_remove_isolated_instances	2023-09-28 09:15:06.250008+00
203	main	0142_update_ee_image_field_description	2023-09-28 09:15:06.373665+00
204	main	0143_hostmetric	2023-09-28 09:15:06.40715+00
205	main	0144_event_partitions	2023-09-28 09:15:09.385765+00
206	main	0145_deregister_managed_ee_objs	2023-09-28 09:15:09.5055+00
207	main	0146_add_insights_inventory	2023-09-28 09:15:10.062809+00
208	main	0147_validate_ee_image_field	2023-09-28 09:15:10.186+00
209	main	0148_unifiedjob_receptor_unit_id	2023-09-28 09:15:10.298733+00
210	main	0149_remove_inventory_insights_credential	2023-09-28 09:15:10.435008+00
211	main	0150_rename_inv_sources_inv_updates	2023-09-28 09:15:11.310146+00
212	main	0151_rename_managed_by_tower	2023-09-28 09:15:11.689366+00
213	main	0152_instance_node_type	2023-09-28 09:15:11.728413+00
214	main	0153_instance_last_seen	2023-09-28 09:15:12.173205+00
215	main	0154_set_default_uuid	2023-09-28 09:15:12.306532+00
216	main	0155_improved_health_check	2023-09-28 09:15:12.377625+00
217	main	0156_capture_mesh_topology	2023-09-28 09:15:12.769231+00
218	main	0157_inventory_labels	2023-09-28 09:15:12.924251+00
219	main	0158_make_instance_cpu_decimal	2023-09-28 09:15:13.370207+00
220	main	0159_deprecate_inventory_source_UoPU_field	2023-09-28 09:15:13.504289+00
221	main	0160_alter_schedule_rrule	2023-09-28 09:15:13.645778+00
222	main	0161_unifiedjob_host_status_counts	2023-09-28 09:15:13.782486+00
223	main	0162_alter_unifiedjob_dependent_jobs	2023-09-28 09:15:13.935592+00
224	main	0163_convert_job_tags_to_textfield	2023-09-28 09:15:14.202849+00
225	main	0164_remove_inventorysource_update_on_project_update	2023-09-28 09:15:14.807981+00
226	main	0165_task_manager_refactor	2023-09-28 09:15:15.122825+00
227	main	0166_alter_jobevent_host	2023-09-28 09:15:15.421177+00
228	main	0167_project_signature_validation_credential	2023-09-28 09:15:16.230644+00
229	main	0168_inventoryupdate_scm_revision	2023-09-28 09:15:16.342955+00
230	main	0169_jt_prompt_everything_on_launch	2023-09-28 09:15:21.064305+00
231	main	0170_node_and_link_state	2023-09-28 09:15:21.514501+00
232	main	0171_add_health_check_started	2023-09-28 09:15:21.556785+00
233	main	0172_prevent_instance_fallback	2023-09-28 09:15:21.799651+00
234	main	0173_instancegroup_max_limits	2023-09-28 09:15:22.368476+00
235	main	0174_ensure_org_ee_admin_roles	2023-09-28 09:15:22.673146+00
236	main	0175_workflowjob_is_bulk_job	2023-09-28 09:15:22.788472+00
237	main	0176_inventorysource_scm_branch	2023-09-28 09:15:23.000964+00
238	main	0177_instance_group_role_addition	2023-09-28 09:15:23.84482+00
239	main	0178_instance_group_admin_migration	2023-09-28 09:15:24.317964+00
240	main	0179_change_cyberark_plugin_names	2023-09-28 09:15:24.5784+00
241	main	0180_add_hostmetric_fields	2023-09-28 09:15:24.677676+00
242	main	0181_hostmetricsummarymonthly	2023-09-28 09:15:24.699514+00
243	main	0182_constructed_inventory	2023-09-28 09:15:26.738973+00
244	main	0183_pre_django_upgrade	2023-09-28 09:15:26.959093+00
245	main	0184_django_indexes	2023-09-28 09:15:50.799678+00
246	main	0185_move_JSONBlob_to_JSONField	2023-09-28 09:15:56.613617+00
247	main	0186_drop_django_taggit	2023-09-28 09:15:56.935566+00
248	sites	0001_initial	2023-09-28 09:15:56.961871+00
249	sites	0002_alter_domain_unique	2023-09-28 09:15:56.98505+00
250	default	0001_initial	2023-09-28 09:15:57.716515+00
251	social_auth	0001_initial	2023-09-28 09:15:57.723894+00
252	default	0002_add_related_name	2023-09-28 09:15:57.878022+00
253	social_auth	0002_add_related_name	2023-09-28 09:15:57.884241+00
254	default	0003_alter_email_max_length	2023-09-28 09:15:57.903956+00
255	social_auth	0003_alter_email_max_length	2023-09-28 09:15:57.910532+00
256	default	0004_auto_20160423_0400	2023-09-28 09:15:58.017277+00
257	social_auth	0004_auto_20160423_0400	2023-09-28 09:15:58.025122+00
258	social_auth	0005_auto_20160727_2333	2023-09-28 09:15:58.048+00
259	social_django	0006_partial	2023-09-28 09:15:58.079586+00
260	social_django	0007_code_timestamp	2023-09-28 09:15:58.103996+00
261	social_django	0008_partial_timestamp	2023-09-28 09:15:58.128867+00
262	social_django	0009_auto_20191118_0520	2023-09-28 09:15:58.304277+00
263	social_django	0010_uid_db_index	2023-09-28 09:15:58.398154+00
264	sso	0001_initial	2023-09-28 09:15:58.631047+00
265	sso	0002_expand_provider_options	2023-09-28 09:15:58.718588+00
266	sso	0003_convert_saml_string_to_list	2023-09-28 09:15:58.86477+00
267	social_django	0004_auto_20160423_0400	2023-09-28 09:15:58.888462+00
268	social_django	0005_auto_20160727_2333	2023-09-28 09:15:58.89604+00
269	social_django	0001_initial	2023-09-28 09:15:58.903669+00
270	social_django	0002_add_related_name	2023-09-28 09:15:58.911976+00
271	social_django	0003_alter_email_max_length	2023-09-28 09:15:58.918814+00
272	main	0005_squashed_v310_v313_updates	2023-09-28 09:15:58.926534+00
273	main	0003_squashed_v300_v303_updates	2023-09-28 09:15:58.933613+00
274	main	0002_squashed_v300_release	2023-09-28 09:15:58.941547+00
275	main	0004_squashed_v310_release	2023-09-28 09:15:58.948193+00
\.


--
-- Data for Name: django_session; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.django_session (session_key, session_data, expire_date) FROM stdin;
\.


--
-- Data for Name: django_site; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.django_site (id, domain, name) FROM stdin;
1	example.com	example.com
\.


--
-- Data for Name: main_activitystream; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream (id, operation, "timestamp", changes, object_relationship_type, object1, object2, actor_id, action_node, deleted_actor, setting) FROM stdin;
1	create	2023-09-28 09:16:03.552426+00	{"username": "admin", "first_name": "", "last_name": "", "email": "admin@localhost", "is_superuser": true, "password": "hidden", "id": 1}		user		\N	awxweb	\N	{}
2	create	2023-09-28 09:16:06.770401+00	{"name": "Default", "description": "", "max_hosts": 0, "default_environment": null, "id": 1}		organization		1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
3	create	2023-09-28 09:16:06.97185+00	{"name": "Demo Project", "description": "", "local_path": "", "scm_type": "git", "scm_url": "https://github.com/ansible/ansible-tower-samples", "scm_branch": "", "scm_refspec": "", "scm_clean": false, "scm_track_submodules": false, "scm_delete_on_update": false, "credential": null, "timeout": 0, "organization": "Default-1", "scm_update_on_launch": false, "scm_update_cache_timeout": 0, "allow_override": false, "default_environment": null, "signature_validation_credential": null, "id": 6}		project		1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
4	create	2023-09-28 09:16:07.118786+00	{"name": "Demo Credential", "description": "", "organization": null, "credential_type": "Machine-1", "inputs": "hidden", "id": 1}		credential		1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
5	associate	2023-09-28 09:16:07.174135+00	{"object1": "credential", "object1_pk": 1, "object2": "user", "object2_pk": 1, "action": "associate", "relationship": "awx.main.models.rbac.Role_members"}	awx.main.models.credential.Credential.admin_role	credential	user	1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
6	create	2023-09-28 09:16:07.278197+00	{"name": "Ansible Galaxy", "description": "", "organization": null, "credential_type": "Ansible Galaxy/Automation Hub API Token-18", "inputs": "hidden", "id": 2}		credential		1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
7	associate	2023-09-28 09:16:07.32226+00	{"object1": "organization", "object1_pk": 1, "object2": "credential", "object2_pk": 2, "action": "associate", "relationship": "awx.main.models.organization.OrganizationGalaxyCredentialMembership"}	awx.main.models.organization.OrganizationGalaxyCredentialMembership	organization	credential	1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
8	create	2023-09-28 09:16:07.471182+00	{"name": "Demo Inventory", "description": "", "organization": "Default-1", "kind": "", "host_filter": null, "variables": "", "prevent_instance_group_fallback": false, "id": 1}		inventory		1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
9	create	2023-09-28 09:16:07.491652+00	{"name": "localhost", "description": "", "inventory": "Demo Inventory-1", "enabled": true, "instance_id": "", "variables": "ansible_connection: local\\nansible_python_interpreter: '{{ ansible_playbook_python }}'", "id": 1}		host		1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
10	create	2023-09-28 09:16:07.629188+00	{"name": "Demo Job Template", "description": "", "job_type": "run", "inventory": "Demo Inventory-1", "project": "Demo Project-6", "playbook": "hello_world.yml", "scm_branch": "", "forks": 0, "limit": "", "verbosity": 0, "extra_vars": "", "job_tags": "", "force_handlers": false, "skip_tags": "", "start_at_task": "", "timeout": 0, "use_fact_cache": false, "execution_environment": null, "host_config_key": "", "ask_scm_branch_on_launch": false, "ask_diff_mode_on_launch": false, "ask_variables_on_launch": false, "ask_limit_on_launch": false, "ask_tags_on_launch": false, "ask_skip_tags_on_launch": false, "ask_job_type_on_launch": false, "ask_verbosity_on_launch": false, "ask_inventory_on_launch": false, "ask_credential_on_launch": false, "ask_execution_environment_on_launch": false, "ask_labels_on_launch": false, "ask_forks_on_launch": false, "ask_job_slice_count_on_launch": false, "ask_timeout_on_launch": false, "ask_instance_groups_on_launch": false, "survey_enabled": false, "become_enabled": false, "diff_mode": false, "allow_simultaneous": false, "job_slice_count": 1, "webhook_service": "", "webhook_credential": null, "prevent_instance_group_fallback": false, "survey_spec": "{}", "id": 7}		job_template		1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
11	associate	2023-09-28 09:16:07.65173+00	{"object1": "job_template", "object1_pk": 7, "object2": "credential", "object2_pk": 1, "action": "associate", "relationship": "awx.main.models.unified_jobs.UnifiedJobTemplate_credentials"}	awx.main.models.unified_jobs.UnifiedJobTemplate_credentials	job_template	credential	1	awxweb	{"id": 1, "username": "admin", "last_name": "", "first_name": ""}	{}
12	create	2023-09-28 09:16:10.787233+00	{"name": "AWX EE (latest)", "description": "", "organization": null, "image": "quay.io/ansible/awx-ee:latest", "credential": null, "pull": "", "id": 1}		execution_environment		\N	awxweb	\N	{}
13	create	2023-09-28 09:16:10.813305+00	{"name": "Control Plane Execution Environment", "description": "", "organization": null, "image": "quay.io/ansible/awx-ee:latest", "credential": null, "pull": "", "id": 2}		execution_environment		\N	awxweb	\N	{}
14	create	2023-09-28 09:16:13.653522+00	{"hostname": "awxweb", "capacity_adjustment": "1", "enabled": true, "managed_by_policy": true, "node_type": "hybrid", "node_state": "installed", "listener_port": 27199, "id": 1}		instance		\N	awxweb	\N	{}
15	create	2023-09-28 09:16:16.604222+00	{"name": "controlplane", "max_concurrent_jobs": 0, "max_forks": 0, "is_container_group": false, "credential": null, "policy_instance_percentage": 0, "policy_instance_minimum": 0, "policy_instance_list": "[]", "pod_spec_override": "", "id": 1}		instance_group		\N	awxweb	\N	{}
16	update	2023-09-28 09:16:16.61898+00	{"policy_instance_percentage": [0, 100]}		instance_group		\N	awxweb	\N	{}
17	create	2023-09-28 09:16:19.693174+00	{"name": "default", "max_concurrent_jobs": 0, "max_forks": 0, "is_container_group": false, "credential": null, "policy_instance_percentage": 0, "policy_instance_minimum": 0, "policy_instance_list": "[]", "pod_spec_override": "", "id": 2}		instance_group		\N	awxweb	\N	{}
18	update	2023-09-28 09:16:19.702149+00	{"policy_instance_percentage": [0, 100]}		instance_group		\N	awxweb	\N	{}
\.


--
-- Data for Name: main_activitystream_ad_hoc_command; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_ad_hoc_command (id, activitystream_id, adhoccommand_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_credential; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_credential (id, activitystream_id, credential_id) FROM stdin;
1	4	1
2	5	1
3	6	2
4	7	2
5	11	1
\.


--
-- Data for Name: main_activitystream_credential_type; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_credential_type (id, activitystream_id, credentialtype_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_execution_environment; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_execution_environment (id, activitystream_id, executionenvironment_id) FROM stdin;
1	12	1
2	13	2
\.


--
-- Data for Name: main_activitystream_group; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_group (id, activitystream_id, group_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_host; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_host (id, activitystream_id, host_id) FROM stdin;
1	9	1
\.


--
-- Data for Name: main_activitystream_instance; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_instance (id, activitystream_id, instance_id) FROM stdin;
1	14	1
\.


--
-- Data for Name: main_activitystream_instance_group; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_instance_group (id, activitystream_id, instancegroup_id) FROM stdin;
1	15	1
2	16	1
3	17	2
4	18	2
\.


--
-- Data for Name: main_activitystream_inventory; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_inventory (id, activitystream_id, inventory_id) FROM stdin;
1	8	1
\.


--
-- Data for Name: main_activitystream_inventory_source; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_inventory_source (id, activitystream_id, inventorysource_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_inventory_update; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_inventory_update (id, activitystream_id, inventoryupdate_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_job; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_job (id, activitystream_id, job_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_job_template; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_job_template (id, activitystream_id, jobtemplate_id) FROM stdin;
1	10	7
2	11	7
\.


--
-- Data for Name: main_activitystream_label; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_label (id, activitystream_id, label_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_notification; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_notification (id, activitystream_id, notification_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_notification_template; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_notification_template (id, activitystream_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_o_auth2_access_token; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_o_auth2_access_token (id, activitystream_id, oauth2accesstoken_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_o_auth2_application; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_o_auth2_application (id, activitystream_id, oauth2application_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_organization; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_organization (id, activitystream_id, organization_id) FROM stdin;
1	2	1
2	7	1
\.


--
-- Data for Name: main_activitystream_project; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_project (id, activitystream_id, project_id) FROM stdin;
1	3	6
\.


--
-- Data for Name: main_activitystream_project_update; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_project_update (id, activitystream_id, projectupdate_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_role; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_role (id, activitystream_id, role_id) FROM stdin;
1	5	20
\.


--
-- Data for Name: main_activitystream_schedule; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_schedule (id, activitystream_id, schedule_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_team; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_team (id, activitystream_id, team_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_unified_job; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_unified_job (id, activitystream_id, unifiedjob_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_unified_job_template; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_unified_job_template (id, activitystream_id, unifiedjobtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_user; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_user (id, activitystream_id, user_id) FROM stdin;
1	1	1
2	5	1
\.


--
-- Data for Name: main_activitystream_workflow_approval; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_workflow_approval (id, activitystream_id, workflowapproval_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_workflow_approval_template; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_workflow_approval_template (id, activitystream_id, workflowapprovaltemplate_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_workflow_job; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_workflow_job (id, activitystream_id, workflowjob_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_workflow_job_node; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_workflow_job_node (id, activitystream_id, workflowjobnode_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_workflow_job_template; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_workflow_job_template (id, activitystream_id, workflowjobtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_activitystream_workflow_job_template_node; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_activitystream_workflow_job_template_node (id, activitystream_id, workflowjobtemplatenode_id) FROM stdin;
\.


--
-- Data for Name: main_adhoccommand; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_adhoccommand (unifiedjob_ptr_id, job_type, "limit", module_name, module_args, forks, verbosity, become_enabled, credential_id, inventory_id, extra_vars, diff_mode) FROM stdin;
\.


--
-- Data for Name: main_credential; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_credential (id, created, modified, description, name, created_by_id, modified_by_id, organization_id, admin_role_id, use_role_id, read_role_id, inputs, credential_type_id, managed) FROM stdin;
1	2023-09-28 09:16:07.03865+00	2023-09-28 09:16:07.038666+00		Demo Credential	1	1	\N	20	21	22	{"username": "admin"}	1	f
2	2023-09-28 09:16:07.20478+00	2023-09-28 09:16:07.204791+00		Ansible Galaxy	1	1	\N	23	24	25	{"url": "https://galaxy.ansible.com/"}	18	t
\.


--
-- Data for Name: main_credentialinputsource; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_credentialinputsource (id, created, modified, description, input_field_name, metadata, created_by_id, modified_by_id, source_credential_id, target_credential_id) FROM stdin;
\.


--
-- Data for Name: main_credentialtype; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_credentialtype (id, created, modified, description, name, kind, managed, inputs, injectors, created_by_id, modified_by_id, namespace) FROM stdin;
8	2023-09-28 09:14:23.304757+00	2023-09-28 09:14:23.304757+00		Red Hat Satellite 6	cloud	t	{}	{}	\N	\N	satellite6
9	2023-09-28 09:14:23.309471+00	2023-09-28 09:14:23.309471+00		Google Compute Engine	cloud	t	{}	{}	\N	\N	gce
10	2023-09-28 09:14:23.31353+00	2023-09-28 09:14:23.31353+00		Microsoft Azure Resource Manager	cloud	t	{}	{}	\N	\N	azure_rm
11	2023-09-28 09:14:23.319063+00	2023-09-28 09:14:23.319063+00		GitHub Personal Access Token	token	t	{}	{}	\N	\N	github_token
12	2023-09-28 09:14:23.323003+00	2023-09-28 09:14:23.323003+00		GitLab Personal Access Token	token	t	{}	{}	\N	\N	gitlab_token
13	2023-09-28 09:14:23.326694+00	2023-09-28 09:14:23.326694+00		Insights	insights	t	{}	{}	\N	\N	insights
14	2023-09-28 09:14:23.330335+00	2023-09-28 09:14:23.330335+00		Red Hat Virtualization	cloud	t	{}	{}	\N	\N	rhv
15	2023-09-28 09:14:23.334786+00	2023-09-28 09:14:23.334786+00		Red Hat Ansible Automation Platform	cloud	t	{}	{}	\N	\N	controller
16	2023-09-28 09:14:23.338187+00	2023-09-28 09:14:23.338187+00		OpenShift or Kubernetes API Bearer Token	kubernetes	t	{}	{}	\N	\N	kubernetes_bearer_token
17	2023-09-28 09:14:23.341907+00	2023-09-28 09:14:23.341907+00		Container Registry	registry	t	{}	{}	\N	\N	registry
18	2023-09-28 09:14:23.345049+00	2023-09-28 09:14:23.345049+00		Ansible Galaxy/Automation Hub API Token	galaxy	t	{}	{}	\N	\N	galaxy_api_token
19	2023-09-28 09:14:23.348926+00	2023-09-28 09:14:23.348926+00		GPG Public Key	cryptography	t	{}	{}	\N	\N	gpg_public_key
20	2023-09-28 09:14:23.352867+00	2023-09-28 09:14:23.352867+00		CyberArk Central Credential Provider Lookup	external	t	{}	{}	\N	\N	aim
21	2023-09-28 09:14:23.356777+00	2023-09-28 09:14:23.356777+00		AWS Secrets Manager lookup	external	t	{}	{}	\N	\N	aws_secretsmanager_credential
22	2023-09-28 09:14:23.360459+00	2023-09-28 09:14:23.360459+00		Microsoft Azure Key Vault	external	t	{}	{}	\N	\N	azure_kv
23	2023-09-28 09:14:23.363728+00	2023-09-28 09:14:23.363728+00		Centrify Vault Credential Provider Lookup	external	t	{}	{}	\N	\N	centrify_vault_kv
24	2023-09-28 09:14:23.368995+00	2023-09-28 09:14:23.368995+00		CyberArk Conjur Secrets Manager Lookup	external	t	{}	{}	\N	\N	conjur
25	2023-09-28 09:14:23.37263+00	2023-09-28 09:14:23.37263+00		HashiCorp Vault Secret Lookup	external	t	{}	{}	\N	\N	hashivault_kv
26	2023-09-28 09:14:23.376605+00	2023-09-28 09:14:23.376605+00		HashiCorp Vault Signed SSH	external	t	{}	{}	\N	\N	hashivault_ssh
27	2023-09-28 09:14:23.380072+00	2023-09-28 09:14:23.380072+00		Thycotic DevOps Secrets Vault	external	t	{}	{}	\N	\N	thycotic_dsv
28	2023-09-28 09:14:23.383648+00	2023-09-28 09:14:23.383648+00		Thycotic Secret Server	external	t	{}	{}	\N	\N	thycotic_tss
1	2023-09-28 09:14:23.274695+00	2023-09-28 09:14:23.274695+00		Machine	ssh	t	{}	{}	\N	\N	ssh
2	2023-09-28 09:14:23.279585+00	2023-09-28 09:14:23.279585+00		Source Control	scm	t	{}	{}	\N	\N	scm
3	2023-09-28 09:14:23.28371+00	2023-09-28 09:14:23.28371+00		Vault	vault	t	{}	{}	\N	\N	vault
4	2023-09-28 09:14:23.28767+00	2023-09-28 09:14:23.28767+00		Network	net	t	{}	{}	\N	\N	net
5	2023-09-28 09:14:23.291814+00	2023-09-28 09:14:23.291814+00		Amazon Web Services	cloud	t	{}	{}	\N	\N	aws
6	2023-09-28 09:14:23.29667+00	2023-09-28 09:14:23.29667+00		OpenStack	cloud	t	{}	{}	\N	\N	openstack
7	2023-09-28 09:14:23.300435+00	2023-09-28 09:14:23.300435+00		VMware vCenter	cloud	t	{}	{}	\N	\N	vmware
\.


--
-- Data for Name: main_custominventoryscript; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_custominventoryscript (id, created, modified, description, name, script, created_by_id, modified_by_id) FROM stdin;
\.


--
-- Data for Name: main_executionenvironment; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_executionenvironment (id, created, modified, description, image, managed, created_by_id, credential_id, modified_by_id, organization_id, name, pull) FROM stdin;
1	2023-09-28 09:16:10.780758+00	2023-09-28 09:16:10.800481+00		quay.io/ansible/awx-ee:latest	f	\N	\N	\N	\N	AWX EE (latest)	
2	2023-09-28 09:16:10.809825+00	2023-09-28 09:16:10.821088+00		quay.io/ansible/awx-ee:latest	t	\N	\N	\N	\N	Control Plane Execution Environment	
\.


--
-- Data for Name: main_group; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_group (id, created, modified, description, name, variables, created_by_id, inventory_id, modified_by_id) FROM stdin;
\.


--
-- Data for Name: main_group_hosts; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_group_hosts (id, group_id, host_id) FROM stdin;
\.


--
-- Data for Name: main_group_inventory_sources; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_group_inventory_sources (id, group_id, inventorysource_id) FROM stdin;
\.


--
-- Data for Name: main_group_parents; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_group_parents (id, from_group_id, to_group_id) FROM stdin;
\.


--
-- Data for Name: main_host; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_host (id, created, modified, description, name, enabled, instance_id, variables, created_by_id, inventory_id, last_job_host_summary_id, modified_by_id, last_job_id, ansible_facts, ansible_facts_modified) FROM stdin;
1	2023-09-28 09:16:07.48707+00	2023-09-28 09:16:07.487081+00		localhost	t		ansible_connection: local\nansible_python_interpreter: '{{ ansible_playbook_python }}'	1	1	\N	1	\N	{}	\N
\.


--
-- Data for Name: main_host_inventory_sources; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_host_inventory_sources (id, host_id, inventorysource_id) FROM stdin;
\.


--
-- Data for Name: main_hostmetric; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_hostmetric (hostname, first_automation, last_automation, last_deleted, automated_counter, deleted_counter, deleted, used_in_inventories, id) FROM stdin;
\.


--
-- Data for Name: main_hostmetricsummarymonthly; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_hostmetricsummarymonthly (id, date, license_consumed, license_capacity, hosts_added, hosts_deleted, indirectly_managed_hosts) FROM stdin;
\.


--
-- Data for Name: main_instance; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_instance (id, uuid, hostname, created, modified, capacity, version, capacity_adjustment, cpu, memory, cpu_capacity, mem_capacity, enabled, managed_by_policy, ip_address, node_type, last_seen, errors, last_health_check, listener_port, node_state, health_check_started) FROM stdin;
1	5204c970-f787-427d-94a8-074137d44ab5	awxweb	2023-09-28 09:16:13.646444+00	2023-09-28 09:16:13.646473+00	79	22.7.1.dev0+g4cd90163fc.d20230928	1.00	8.0	10446909440	32	79	t	t	\N	hybrid	2023-09-28 09:45:31.66686+00		2023-09-28 09:45:31.66686+00	27199	ready	\N
\.


--
-- Data for Name: main_instancegroup; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_instancegroup (id, name, created, modified, policy_instance_list, policy_instance_minimum, policy_instance_percentage, credential_id, pod_spec_override, is_container_group, max_concurrent_jobs, max_forks, admin_role_id, read_role_id, use_role_id) FROM stdin;
1	controlplane	2023-09-28 09:16:16.501188+00	2023-09-28 09:16:16.624776+00	[]	0	100	\N		f	0	0	34	36	35
2	default	2023-09-28 09:16:19.613671+00	2023-09-28 09:16:19.70542+00	[]	0	100	\N		f	0	0	37	39	38
\.


--
-- Data for Name: main_instancegroup_instances; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_instancegroup_instances (id, instancegroup_id, instance_id) FROM stdin;
1	1	1
2	2	1
\.


--
-- Data for Name: main_instancelink; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_instancelink (id, source_id, target_id, link_state) FROM stdin;
\.


--
-- Data for Name: main_inventory; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_inventory (id, created, modified, description, name, variables, has_active_failures, total_hosts, hosts_with_active_failures, total_groups, has_inventory_sources, total_inventory_sources, inventory_sources_with_failures, created_by_id, modified_by_id, organization_id, admin_role_id, adhoc_role_id, update_role_id, use_role_id, read_role_id, host_filter, kind, pending_deletion, prevent_instance_group_fallback) FROM stdin;
1	2023-09-28 09:16:07.349699+00	2023-09-28 09:16:07.349711+00		Demo Inventory		f	0	0	0	f	0	0	1	1	1	26	28	27	29	30	\N		f	f
\.


--
-- Data for Name: main_inventory_labels; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_inventory_labels (id, inventory_id, label_id) FROM stdin;
\.


--
-- Data for Name: main_inventoryconstructedinventorymembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_inventoryconstructedinventorymembership (id, "position", constructed_inventory_id, input_inventory_id) FROM stdin;
\.


--
-- Data for Name: main_inventoryinstancegroupmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_inventoryinstancegroupmembership (id, "position", instancegroup_id, inventory_id) FROM stdin;
\.


--
-- Data for Name: main_inventorysource; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_inventorysource (unifiedjobtemplate_ptr_id, source, source_path, source_vars, overwrite, overwrite_vars, update_on_launch, update_cache_timeout, inventory_id, timeout, source_project_id, verbosity, custom_virtualenv, enabled_value, enabled_var, host_filter, scm_branch, "limit") FROM stdin;
\.


--
-- Data for Name: main_inventoryupdate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_inventoryupdate (unifiedjob_ptr_id, source, source_path, source_vars, overwrite, overwrite_vars, license_error, inventory_source_id, timeout, source_project_update_id, verbosity, inventory_id, custom_virtualenv, org_host_limit_error, enabled_value, enabled_var, host_filter, scm_revision, scm_branch, "limit") FROM stdin;
\.


--
-- Data for Name: main_job; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_job (unifiedjob_ptr_id, job_type, playbook, forks, "limit", verbosity, extra_vars, job_tags, force_handlers, skip_tags, start_at_task, become_enabled, inventory_id, job_template_id, project_id, allow_simultaneous, artifacts, timeout, scm_revision, project_update_id, use_fact_cache, diff_mode, job_slice_count, job_slice_number, custom_virtualenv, scm_branch, webhook_credential_id, webhook_guid, webhook_service, survey_passwords) FROM stdin;
\.


--
-- Data for Name: main_jobhostsummary; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_jobhostsummary (id, created, modified, host_name, changed, dark, failures, ok, processed, skipped, failed, host_id, job_id, ignored, rescued, constructed_host_id) FROM stdin;
\.


--
-- Data for Name: main_joblaunchconfig; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_joblaunchconfig (id, extra_data, inventory_id, job_id, execution_environment_id, char_prompts, survey_passwords) FROM stdin;
\.


--
-- Data for Name: main_joblaunchconfig_credentials; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_joblaunchconfig_credentials (id, joblaunchconfig_id, credential_id) FROM stdin;
\.


--
-- Data for Name: main_joblaunchconfig_labels; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_joblaunchconfig_labels (id, joblaunchconfig_id, label_id) FROM stdin;
\.


--
-- Data for Name: main_joblaunchconfiginstancegroupmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_joblaunchconfiginstancegroupmembership (id, "position", instancegroup_id, joblaunchconfig_id) FROM stdin;
\.


--
-- Data for Name: main_jobtemplate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_jobtemplate (unifiedjobtemplate_ptr_id, job_type, playbook, forks, "limit", verbosity, extra_vars, job_tags, force_handlers, skip_tags, start_at_task, become_enabled, host_config_key, ask_variables_on_launch, survey_enabled, survey_spec, inventory_id, project_id, admin_role_id, execute_role_id, read_role_id, ask_limit_on_launch, ask_inventory_on_launch, ask_credential_on_launch, ask_job_type_on_launch, ask_tags_on_launch, allow_simultaneous, ask_skip_tags_on_launch, timeout, use_fact_cache, ask_verbosity_on_launch, ask_diff_mode_on_launch, diff_mode, custom_virtualenv, job_slice_count, ask_scm_branch_on_launch, scm_branch, webhook_credential_id, webhook_key, webhook_service, ask_execution_environment_on_launch, ask_forks_on_launch, ask_instance_groups_on_launch, ask_job_slice_count_on_launch, ask_labels_on_launch, ask_timeout_on_launch, prevent_instance_group_fallback) FROM stdin;
7	run	hello_world.yml	0		0			f			f		f	f	{}	1	6	31	32	33	f	f	f	f	f	f	f	0	f	f	f	f	\N	1	f		\N			f	f	f	f	f	f	f
\.


--
-- Data for Name: main_label; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_label (id, created, modified, description, name, created_by_id, modified_by_id, organization_id) FROM stdin;
\.


--
-- Data for Name: main_notification; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_notification (id, created, modified, status, error, notifications_sent, notification_type, recipients, subject, notification_template_id, body) FROM stdin;
\.


--
-- Data for Name: main_notificationtemplate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_notificationtemplate (id, created, modified, description, name, notification_type, notification_configuration, created_by_id, modified_by_id, organization_id, messages) FROM stdin;
\.


--
-- Data for Name: main_oauth2accesstoken; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_oauth2accesstoken (id, token, expires, scope, created, updated, description, last_used, application_id, user_id, source_refresh_token_id, modified, id_token_id) FROM stdin;
\.


--
-- Data for Name: main_oauth2application; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_oauth2application (id, client_id, redirect_uris, client_type, authorization_grant_type, client_secret, name, skip_authorization, created, updated, description, logo_data, user_id, organization_id, algorithm) FROM stdin;
\.


--
-- Data for Name: main_organization; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_organization (id, created, modified, description, name, created_by_id, modified_by_id, admin_role_id, auditor_role_id, member_role_id, read_role_id, custom_virtualenv, execute_role_id, job_template_admin_role_id, credential_admin_role_id, inventory_admin_role_id, project_admin_role_id, workflow_admin_role_id, notification_admin_role_id, max_hosts, approval_role_id, default_environment_id, execution_environment_admin_role_id) FROM stdin;
1	2023-09-28 09:16:06.507309+00	2023-09-28 09:16:06.507321+00		Default	1	1	2	11	12	13	\N	3	9	6	5	4	7	8	0	14	\N	10
\.


--
-- Data for Name: main_organization_notification_templates_approvals; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_organization_notification_templates_approvals (id, organization_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_organization_notification_templates_error; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_organization_notification_templates_error (id, organization_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_organization_notification_templates_started; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_organization_notification_templates_started (id, organization_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_organization_notification_templates_success; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_organization_notification_templates_success (id, organization_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_organizationgalaxycredentialmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_organizationgalaxycredentialmembership (id, "position", credential_id, organization_id) FROM stdin;
1	0	2	1
\.


--
-- Data for Name: main_organizationinstancegroupmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_organizationinstancegroupmembership (id, "position", instancegroup_id, organization_id) FROM stdin;
\.


--
-- Data for Name: main_profile; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_profile (id, created, modified, ldap_dn, user_id) FROM stdin;
\.


--
-- Data for Name: main_project; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_project (unifiedjobtemplate_ptr_id, local_path, scm_type, scm_url, scm_branch, scm_clean, scm_delete_on_update, scm_update_on_launch, scm_update_cache_timeout, credential_id, admin_role_id, use_role_id, update_role_id, read_role_id, timeout, scm_revision, playbook_files, inventory_files, custom_virtualenv, scm_refspec, allow_override, default_environment_id, scm_track_submodules, signature_validation_credential_id) FROM stdin;
6	_6__demo_project	git	https://github.com/ansible/ansible-tower-samples		f	f	f	0	\N	16	17	18	19	0	347e44fea036c94d5f60e544de006453ee5c71ad	["hello_world.yml"]	[]	\N		f	\N	f	\N
\.


--
-- Data for Name: main_projectupdate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_projectupdate (unifiedjob_ptr_id, local_path, scm_type, scm_url, scm_branch, scm_clean, scm_delete_on_update, credential_id, project_id, timeout, job_type, scm_refspec, scm_revision, job_tags, scm_track_submodules) FROM stdin;
\.


--
-- Data for Name: main_rbac_role_ancestors; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_rbac_role_ancestors (id, role_field, content_type_id, object_id, ancestor_id, descendent_id) FROM stdin;
1	system_administrator	0	0	1	1
2	job_template_admin_role	36	1	9	9
3	admin_role	36	1	1	2
4	admin_role	36	1	2	2
5	member_role	36	1	12	12
6	execution_environment_admin_role	36	1	10	10
7	auditor_role	36	1	11	11
8	execute_role	36	1	3	3
9	credential_admin_role	36	1	6	6
10	read_role	36	1	13	13
11	notification_admin_role	36	1	8	8
12	approval_role	36	1	14	14
13	project_admin_role	36	1	4	4
14	workflow_admin_role	36	1	7	7
15	inventory_admin_role	36	1	5	5
16	system_auditor	0	0	15	15
17	notification_admin_role	36	1	1	8
18	member_role	36	1	1	12
19	approval_role	36	1	1	14
20	approval_role	36	1	2	14
21	read_role	36	1	7	13
22	job_template_admin_role	36	1	2	9
23	read_role	36	1	10	13
24	workflow_admin_role	36	1	1	7
25	credential_admin_role	36	1	2	6
26	inventory_admin_role	36	1	1	5
27	workflow_admin_role	36	1	2	7
28	project_admin_role	36	1	1	4
29	read_role	36	1	6	13
30	auditor_role	36	1	15	11
31	read_role	36	1	8	13
32	read_role	36	1	11	13
33	read_role	36	1	4	13
34	execute_role	36	1	1	3
35	read_role	36	1	5	13
36	read_role	36	1	3	13
37	execution_environment_admin_role	36	1	2	10
38	read_role	36	1	12	13
39	member_role	36	1	2	12
40	inventory_admin_role	36	1	2	5
41	job_template_admin_role	36	1	1	9
42	credential_admin_role	36	1	1	6
43	notification_admin_role	36	1	2	8
44	read_role	36	1	9	13
45	project_admin_role	36	1	2	4
46	read_role	36	1	14	13
47	execute_role	36	1	2	3
48	execution_environment_admin_role	36	1	1	10
49	read_role	36	1	15	13
50	read_role	36	1	2	13
51	read_role	36	1	1	13
52	update_role	5	6	18	18
53	use_role	5	6	17	17
54	admin_role	5	6	4	16
55	admin_role	5	6	1	16
56	admin_role	5	6	16	16
57	read_role	5	6	11	19
58	admin_role	5	6	2	16
59	read_role	5	6	15	19
60	read_role	5	6	19	19
61	update_role	5	6	1	18
62	use_role	5	6	16	17
63	use_role	5	6	4	17
64	read_role	5	6	18	19
65	use_role	5	6	1	17
66	update_role	5	6	16	18
67	use_role	5	6	2	17
68	update_role	5	6	2	18
69	read_role	5	6	17	19
70	update_role	5	6	4	18
71	read_role	5	6	16	19
72	read_role	5	6	4	19
73	read_role	5	6	1	19
74	read_role	5	6	2	19
75	use_role	28	1	21	21
76	admin_role	28	1	20	20
77	read_role	28	1	15	22
78	admin_role	28	1	1	20
79	read_role	28	1	22	22
80	read_role	28	1	20	22
81	use_role	28	1	20	21
82	read_role	28	1	1	22
83	use_role	28	1	1	21
84	read_role	28	1	21	22
85	admin_role	28	2	23	23
86	read_role	28	2	15	25
87	admin_role	28	2	1	23
88	use_role	28	2	24	24
89	read_role	28	2	25	25
90	read_role	28	2	24	25
91	use_role	28	2	1	24
92	read_role	28	2	1	25
93	read_role	28	2	23	25
94	use_role	28	2	23	24
95	adhoc_role	33	1	28	28
96	admin_role	33	1	26	26
97	admin_role	33	1	5	26
98	update_role	33	1	27	27
99	admin_role	33	1	1	26
100	use_role	33	1	29	29
101	read_role	33	1	30	30
102	read_role	33	1	11	30
103	read_role	33	1	15	30
104	admin_role	33	1	2	26
105	read_role	33	1	29	30
106	read_role	33	1	26	30
107	read_role	33	1	1	30
108	read_role	33	1	2	30
109	update_role	33	1	2	27
110	update_role	33	1	1	27
111	use_role	33	1	28	29
112	adhoc_role	33	1	1	28
113	adhoc_role	33	1	2	28
114	adhoc_role	33	1	5	28
115	read_role	33	1	5	30
116	update_role	33	1	5	27
117	read_role	33	1	27	30
118	adhoc_role	33	1	26	28
119	update_role	33	1	26	27
120	use_role	33	1	2	29
121	read_role	33	1	28	30
122	use_role	33	1	1	29
123	use_role	33	1	26	29
124	use_role	33	1	5	29
125	execute_role	4	7	3	32
126	execute_role	4	7	32	32
127	read_role	4	7	11	33
128	admin_role	4	7	31	31
129	execute_role	4	7	2	32
130	admin_role	4	7	2	31
131	execute_role	4	7	1	32
132	admin_role	4	7	9	31
133	admin_role	4	7	1	31
134	read_role	4	7	15	33
135	read_role	4	7	33	33
136	execute_role	4	7	9	32
137	read_role	4	7	3	33
138	read_role	4	7	1	33
139	read_role	4	7	2	33
140	read_role	4	7	31	33
141	read_role	4	7	9	33
142	read_role	4	7	32	33
143	execute_role	4	7	31	32
144	read_role	53	1	15	36
145	admin_role	53	1	1	34
146	use_role	53	1	35	35
147	admin_role	53	1	34	34
148	read_role	53	1	36	36
149	use_role	53	1	1	35
150	read_role	53	1	1	36
151	read_role	53	1	35	36
152	use_role	53	1	34	35
153	read_role	53	1	34	36
154	admin_role	53	2	37	37
155	admin_role	53	2	1	37
156	use_role	53	2	38	38
157	read_role	53	2	39	39
158	read_role	53	2	15	39
159	read_role	53	2	37	39
160	use_role	53	2	37	38
161	read_role	53	2	1	39
162	use_role	53	2	1	38
163	read_role	53	2	38	39
\.


--
-- Data for Name: main_rbac_roles; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_rbac_roles (id, role_field, singleton_name, implicit_parents, content_type_id, object_id) FROM stdin;
1	system_administrator	system_administrator	[]	\N	\N
2	admin_role	\N	[1]	36	1
3	execute_role	\N	[2]	36	1
4	project_admin_role	\N	[2]	36	1
5	inventory_admin_role	\N	[2]	36	1
6	credential_admin_role	\N	[2]	36	1
7	workflow_admin_role	\N	[2]	36	1
8	notification_admin_role	\N	[2]	36	1
9	job_template_admin_role	\N	[2]	36	1
10	execution_environment_admin_role	\N	[2]	36	1
15	system_auditor	system_auditor	[]	\N	\N
11	auditor_role	\N	[15]	36	1
12	member_role	\N	[2]	36	1
13	read_role	\N	[3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14]	36	1
14	approval_role	\N	[2]	36	1
16	admin_role	\N	[1, 4]	5	6
17	use_role	\N	[16]	5	6
18	update_role	\N	[16]	5	6
19	read_role	\N	[11, 15, 17, 18]	5	6
20	admin_role	\N	[1]	28	1
21	use_role	\N	[20]	28	1
22	read_role	\N	[15, 20, 21]	28	1
23	admin_role	\N	[1]	28	2
24	use_role	\N	[23]	28	2
25	read_role	\N	[15, 23, 24]	28	2
26	admin_role	\N	[5]	33	1
27	update_role	\N	[26]	33	1
28	adhoc_role	\N	[26]	33	1
29	use_role	\N	[28]	33	1
30	read_role	\N	[11, 26, 27, 29]	33	1
31	admin_role	\N	[9]	4	7
32	execute_role	\N	[3, 31]	4	7
33	read_role	\N	[11, 31, 32]	4	7
34	admin_role	\N	[1]	53	1
35	use_role	\N	[34]	53	1
36	read_role	\N	[15, 34, 35]	53	1
37	admin_role	\N	[1]	53	2
38	use_role	\N	[37]	53	2
39	read_role	\N	[15, 37, 38]	53	2
\.


--
-- Data for Name: main_rbac_roles_members; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_rbac_roles_members (id, role_id, user_id) FROM stdin;
1	1	1
2	20	1
\.


--
-- Data for Name: main_rbac_roles_parents; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_rbac_roles_parents (id, from_role_id, to_role_id) FROM stdin;
1	2	1
2	3	2
3	4	2
4	5	2
5	6	2
6	7	2
7	8	2
8	9	2
9	10	2
10	11	15
11	12	2
12	13	3
13	13	4
14	13	5
15	13	6
16	13	7
17	13	8
18	13	9
19	13	10
20	13	11
21	13	12
22	13	14
23	14	2
24	16	1
25	16	4
26	17	16
27	18	16
28	19	17
29	19	18
30	19	11
31	19	15
32	20	1
33	21	20
34	22	20
35	22	21
36	22	15
37	23	1
38	24	23
39	25	24
40	25	23
41	25	15
42	26	5
43	27	26
44	28	26
45	29	28
46	30	11
47	30	26
48	30	27
49	30	29
50	31	9
51	32	3
52	32	31
53	33	32
54	33	11
55	33	31
56	34	1
57	35	34
58	36	34
59	36	35
60	36	15
61	37	1
62	38	37
63	39	37
64	39	38
65	39	15
\.


--
-- Data for Name: main_schedule; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_schedule (id, created, modified, description, name, enabled, dtstart, dtend, rrule, next_run, extra_data, created_by_id, modified_by_id, unified_job_template_id, char_prompts, inventory_id, survey_passwords, execution_environment_id) FROM stdin;
1	2023-09-28 09:13:16.366843+00	2023-09-28 09:13:16.366843+00	Automatically Generated Schedule	Cleanup Job Schedule	t	2023-10-01 09:13:16+00	\N	DTSTART:20230928T091316Z RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=SU	2023-10-01 09:13:16+00	{"days": "120"}	\N	\N	1	{}	\N	{}	\N
2	2023-09-28 09:13:16.366843+00	2023-09-28 09:13:16.366843+00	Automatically Generated Schedule	Cleanup Activity Schedule	t	2023-10-03 09:13:16+00	\N	DTSTART:20230928T091316Z RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=TU	2023-10-03 09:13:16+00	{"days": "355"}	\N	\N	2	{}	\N	{}	\N
4	2023-09-28 09:14:30.925574+00	2023-09-28 09:14:30.925574+00	Cleans out expired browser sessions	Cleanup Expired Sessions	t	2023-09-28 09:14:30+00	\N	DTSTART:20230928T091430Z RRULE:FREQ=WEEKLY;INTERVAL=1	2023-10-05 09:14:30+00	{}	\N	\N	4	{}	\N	{}	\N
5	2023-09-28 09:14:31.035815+00	2023-09-28 09:14:31.035815+00	Removes expired OAuth 2 access and refresh tokens	Cleanup Expired OAuth 2 Tokens	t	2023-09-28 09:14:31+00	\N	DTSTART:20230928T091431Z RRULE:FREQ=WEEKLY;INTERVAL=1	2023-10-05 09:14:31+00	{}	\N	\N	5	{}	\N	{}	\N
\.


--
-- Data for Name: main_schedule_credentials; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_schedule_credentials (id, schedule_id, credential_id) FROM stdin;
\.


--
-- Data for Name: main_schedule_labels; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_schedule_labels (id, schedule_id, label_id) FROM stdin;
\.


--
-- Data for Name: main_scheduleinstancegroupmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_scheduleinstancegroupmembership (id, "position", instancegroup_id, schedule_id) FROM stdin;
\.


--
-- Data for Name: main_smartinventorymembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_smartinventorymembership (id, host_id, inventory_id) FROM stdin;
\.


--
-- Data for Name: main_systemjob; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_systemjob (unifiedjob_ptr_id, job_type, extra_vars, system_job_template_id) FROM stdin;
\.


--
-- Data for Name: main_systemjobtemplate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_systemjobtemplate (unifiedjobtemplate_ptr_id, job_type) FROM stdin;
1	cleanup_jobs
2	cleanup_activitystream
4	cleanup_sessions
5	cleanup_tokens
\.


--
-- Data for Name: main_team; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_team (id, created, modified, description, name, created_by_id, modified_by_id, organization_id, admin_role_id, member_role_id, read_role_id) FROM stdin;
\.


--
-- Data for Name: main_towerschedulestate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_towerschedulestate (id, schedule_last_run) FROM stdin;
1	2023-09-28 09:46:00.667698+00
\.


--
-- Data for Name: main_unifiedjob; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjob (id, created, modified, description, name, old_pk, launch_type, cancel_flag, status, failed, started, finished, elapsed, job_args, job_cwd, job_explanation, start_args, result_stdout_text, result_traceback, celery_task_id, created_by_id, modified_by_id, polymorphic_ctype_id, schedule_id, unified_job_template_id, execution_node, instance_group_id, emitted_events, controller_node, canceled_on, dependencies_processed, organization_id, execution_environment_id, installed_collections, ansible_version, work_unit_id, host_status_counts, preferred_instance_groups_cache, task_impact, job_env) FROM stdin;
\.


--
-- Data for Name: main_unifiedjob_credentials; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjob_credentials (id, unifiedjob_id, credential_id) FROM stdin;
\.


--
-- Data for Name: main_unifiedjob_dependent_jobs; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjob_dependent_jobs (id, from_unifiedjob_id, to_unifiedjob_id) FROM stdin;
\.


--
-- Data for Name: main_unifiedjob_labels; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjob_labels (id, unifiedjob_id, label_id) FROM stdin;
\.


--
-- Data for Name: main_unifiedjob_notifications; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjob_notifications (id, unifiedjob_id, notification_id) FROM stdin;
\.


--
-- Data for Name: main_unifiedjobtemplate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjobtemplate (id, created, modified, description, name, old_pk, last_job_failed, last_job_run, next_job_run, status, created_by_id, current_job_id, last_job_id, modified_by_id, next_schedule_id, polymorphic_ctype_id, organization_id, execution_environment_id) FROM stdin;
6	2023-09-28 09:16:06.798976+00	2023-09-28 09:16:06.798989+00		Demo Project	\N	f	\N	\N	successful	1	\N	\N	1	\N	5	1	\N
7	2023-09-28 09:16:07.517762+00	2023-09-28 09:16:07.517774+00		Demo Job Template	\N	f	\N	\N	never updated	1	\N	\N	1	\N	4	1	\N
1	2023-09-28 09:13:16.366843+00	2023-09-28 09:13:16.366843+00	Remove job history	Cleanup Job Details	\N	f	\N	2023-10-01 09:13:16+00	ok	\N	\N	\N	\N	1	1	\N	\N
2	2023-09-28 09:13:16.366843+00	2023-09-28 09:13:16.366843+00	Remove activity stream history	Cleanup Activity Stream	\N	f	\N	2023-10-03 09:13:16+00	ok	\N	\N	\N	\N	2	1	\N	\N
4	2023-09-28 09:14:30.925574+00	2023-09-28 09:14:30.925574+00	Cleans out expired browser sessions	Cleanup Expired Sessions	\N	f	\N	2023-10-05 09:14:30+00	ok	\N	\N	\N	\N	4	1	\N	\N
5	2023-09-28 09:14:31.035815+00	2023-09-28 09:14:31.035815+00	Cleanup expired OAuth 2 access and refresh tokens	Cleanup Expired OAuth 2 Tokens	\N	f	\N	2023-10-05 09:14:31+00	ok	\N	\N	\N	\N	5	1	\N	\N
\.


--
-- Data for Name: main_unifiedjobtemplate_credentials; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjobtemplate_credentials (id, unifiedjobtemplate_id, credential_id) FROM stdin;
1	7	1
\.


--
-- Data for Name: main_unifiedjobtemplate_labels; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjobtemplate_labels (id, unifiedjobtemplate_id, label_id) FROM stdin;
\.


--
-- Data for Name: main_unifiedjobtemplate_notification_templates_error; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjobtemplate_notification_templates_error (id, unifiedjobtemplate_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_unifiedjobtemplate_notification_templates_started; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjobtemplate_notification_templates_started (id, unifiedjobtemplate_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_unifiedjobtemplate_notification_templates_success; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjobtemplate_notification_templates_success (id, unifiedjobtemplate_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_unifiedjobtemplateinstancegroupmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_unifiedjobtemplateinstancegroupmembership (id, "position", instancegroup_id, unifiedjobtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_usersessionmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_usersessionmembership (id, created, session_id, user_id) FROM stdin;
\.


--
-- Data for Name: main_workflowapproval; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowapproval (unifiedjob_ptr_id, workflow_approval_template_id, timeout, timed_out, approved_or_denied_by_id, expires) FROM stdin;
\.


--
-- Data for Name: main_workflowapprovaltemplate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowapprovaltemplate (unifiedjobtemplate_ptr_id, timeout) FROM stdin;
\.


--
-- Data for Name: main_workflowjob; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjob (unifiedjob_ptr_id, extra_vars, workflow_job_template_id, allow_simultaneous, is_sliced_job, job_template_id, inventory_id, webhook_credential_id, webhook_guid, webhook_service, is_bulk_job, char_prompts, survey_passwords) FROM stdin;
\.


--
-- Data for Name: main_workflowjobinstancegroupmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobinstancegroupmembership (id, "position", instancegroup_id, workflowjobnode_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobnode; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobnode (id, created, modified, job_id, unified_job_template_id, workflow_job_id, inventory_id, ancestor_artifacts, extra_data, do_not_run, all_parents_must_converge, identifier, execution_environment_id, char_prompts, survey_passwords) FROM stdin;
\.


--
-- Data for Name: main_workflowjobnode_always_nodes; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobnode_always_nodes (id, from_workflowjobnode_id, to_workflowjobnode_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobnode_credentials; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobnode_credentials (id, workflowjobnode_id, credential_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobnode_failure_nodes; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobnode_failure_nodes (id, from_workflowjobnode_id, to_workflowjobnode_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobnode_labels; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobnode_labels (id, workflowjobnode_id, label_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobnode_success_nodes; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobnode_success_nodes (id, from_workflowjobnode_id, to_workflowjobnode_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobnodebaseinstancegroupmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobnodebaseinstancegroupmembership (id, "position", instancegroup_id, workflowjobnode_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplate; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplate (unifiedjobtemplate_ptr_id, extra_vars, admin_role_id, execute_role_id, read_role_id, survey_enabled, survey_spec, allow_simultaneous, ask_variables_on_launch, ask_inventory_on_launch, inventory_id, approval_role_id, ask_limit_on_launch, ask_scm_branch_on_launch, char_prompts, webhook_credential_id, webhook_key, webhook_service, ask_labels_on_launch, ask_skip_tags_on_launch, ask_tags_on_launch) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplate_notification_templates_approvals; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplate_notification_templates_approvals (id, workflowjobtemplate_id, notificationtemplate_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplatenode; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplatenode (id, created, modified, unified_job_template_id, workflow_job_template_id, char_prompts, inventory_id, extra_data, survey_passwords, all_parents_must_converge, identifier, execution_environment_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplatenode_always_nodes; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplatenode_always_nodes (id, from_workflowjobtemplatenode_id, to_workflowjobtemplatenode_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplatenode_credentials; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplatenode_credentials (id, workflowjobtemplatenode_id, credential_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplatenode_failure_nodes; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplatenode_failure_nodes (id, from_workflowjobtemplatenode_id, to_workflowjobtemplatenode_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplatenode_labels; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplatenode_labels (id, workflowjobtemplatenode_id, label_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplatenode_success_nodes; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplatenode_success_nodes (id, from_workflowjobtemplatenode_id, to_workflowjobtemplatenode_id) FROM stdin;
\.


--
-- Data for Name: main_workflowjobtemplatenodebaseinstancegroupmembership; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.main_workflowjobtemplatenodebaseinstancegroupmembership (id, "position", instancegroup_id, workflowjobtemplatenode_id) FROM stdin;
\.


--
-- Data for Name: oauth2_provider_grant; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.oauth2_provider_grant (id, code, expires, redirect_uri, scope, application_id, user_id, created, updated, code_challenge, code_challenge_method, nonce, claims) FROM stdin;
\.


--
-- Data for Name: oauth2_provider_idtoken; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.oauth2_provider_idtoken (id, jti, expires, scope, created, updated, application_id, user_id) FROM stdin;
\.


--
-- Data for Name: oauth2_provider_refreshtoken; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.oauth2_provider_refreshtoken (id, token, access_token_id, application_id, user_id, created, updated, revoked) FROM stdin;
\.


--
-- Data for Name: social_auth_association; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.social_auth_association (id, server_url, handle, secret, issued, lifetime, assoc_type) FROM stdin;
\.


--
-- Data for Name: social_auth_code; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.social_auth_code (id, email, code, verified, "timestamp") FROM stdin;
\.


--
-- Data for Name: social_auth_nonce; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.social_auth_nonce (id, server_url, "timestamp", salt) FROM stdin;
\.


--
-- Data for Name: social_auth_partial; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.social_auth_partial (id, token, next_step, backend, data, "timestamp") FROM stdin;
\.


--
-- Data for Name: social_auth_usersocialauth; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.social_auth_usersocialauth (id, provider, uid, extra_data, user_id, created, modified) FROM stdin;
\.


--
-- Data for Name: sso_userenterpriseauth; Type: TABLE DATA; Schema: public; Owner: awx
--

COPY public.sso_userenterpriseauth (id, provider, user_id) FROM stdin;
\.


--
-- Name: auth_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.auth_group_id_seq', 1, false);


--
-- Name: auth_group_permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.auth_group_permissions_id_seq', 1, false);


--
-- Name: auth_permission_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.auth_permission_id_seq', 336, true);


--
-- Name: auth_user_groups_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.auth_user_groups_id_seq', 1, false);


--
-- Name: auth_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.auth_user_id_seq', 1, true);


--
-- Name: auth_user_user_permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.auth_user_user_permissions_id_seq', 1, false);


--
-- Name: conf_setting_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.conf_setting_id_seq', 1, true);


--
-- Name: django_content_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.django_content_type_id_seq', 84, true);


--
-- Name: django_migrations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.django_migrations_id_seq', 275, true);


--
-- Name: django_site_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.django_site_id_seq', 1, true);


--
-- Name: main_activitystream_ad_hoc_command_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_ad_hoc_command_id_seq', 1, false);


--
-- Name: main_activitystream_credential_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_credential_id_seq', 5, true);


--
-- Name: main_activitystream_credential_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_credential_type_id_seq', 1, false);


--
-- Name: main_activitystream_execution_environment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_execution_environment_id_seq', 2, true);


--
-- Name: main_activitystream_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_group_id_seq', 1, false);


--
-- Name: main_activitystream_host_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_host_id_seq', 1, true);


--
-- Name: main_activitystream_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_id_seq', 18, true);


--
-- Name: main_activitystream_instance_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_instance_group_id_seq', 4, true);


--
-- Name: main_activitystream_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_instance_id_seq', 1, true);


--
-- Name: main_activitystream_inventory_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_inventory_id_seq', 1, true);


--
-- Name: main_activitystream_inventory_source_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_inventory_source_id_seq', 1, false);


--
-- Name: main_activitystream_inventory_update_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_inventory_update_id_seq', 1, false);


--
-- Name: main_activitystream_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_job_id_seq', 1, false);


--
-- Name: main_activitystream_job_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_job_template_id_seq', 2, true);


--
-- Name: main_activitystream_label_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_label_id_seq', 1, false);


--
-- Name: main_activitystream_notification_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_notification_id_seq', 1, false);


--
-- Name: main_activitystream_notification_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_notification_template_id_seq', 1, false);


--
-- Name: main_activitystream_o_auth2_access_token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_o_auth2_access_token_id_seq', 1, false);


--
-- Name: main_activitystream_o_auth2_application_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_o_auth2_application_id_seq', 1, false);


--
-- Name: main_activitystream_organization_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_organization_id_seq', 2, true);


--
-- Name: main_activitystream_project_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_project_id_seq', 1, true);


--
-- Name: main_activitystream_project_update_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_project_update_id_seq', 1, false);


--
-- Name: main_activitystream_role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_role_id_seq', 1, true);


--
-- Name: main_activitystream_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_schedule_id_seq', 1, false);


--
-- Name: main_activitystream_team_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_team_id_seq', 1, false);


--
-- Name: main_activitystream_unified_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_unified_job_id_seq', 1, false);


--
-- Name: main_activitystream_unified_job_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_unified_job_template_id_seq', 1, false);


--
-- Name: main_activitystream_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_user_id_seq', 2, true);


--
-- Name: main_activitystream_workflow_approval_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_workflow_approval_id_seq', 1, false);


--
-- Name: main_activitystream_workflow_approval_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_workflow_approval_template_id_seq', 1, false);


--
-- Name: main_activitystream_workflow_job_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_workflow_job_id_seq', 1, false);


--
-- Name: main_activitystream_workflow_job_node_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_workflow_job_node_id_seq', 1, false);


--
-- Name: main_activitystream_workflow_job_template_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_workflow_job_template_id_seq', 1, false);


--
-- Name: main_activitystream_workflow_job_template_node_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_activitystream_workflow_job_template_node_id_seq', 1, false);


--
-- Name: main_adhoccommandevent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_adhoccommandevent_id_seq', 1, false);


--
-- Name: main_adhoccommandevent_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_adhoccommandevent_id_seq1', 1, false);


--
-- Name: main_credential_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_credential_id_seq', 2, true);


--
-- Name: main_credentialinputsource_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_credentialinputsource_id_seq', 1, false);


--
-- Name: main_credentialtype_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_credentialtype_id_seq', 28, true);


--
-- Name: main_custominventoryscript_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_custominventoryscript_id_seq', 1, false);


--
-- Name: main_executionenvironment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_executionenvironment_id_seq', 2, true);


--
-- Name: main_group_hosts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_group_hosts_id_seq', 1, false);


--
-- Name: main_group_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_group_id_seq', 1, false);


--
-- Name: main_group_inventory_sources_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_group_inventory_sources_id_seq', 1, false);


--
-- Name: main_group_parents_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_group_parents_id_seq', 1, false);


--
-- Name: main_host_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_host_id_seq', 1, true);


--
-- Name: main_host_inventory_sources_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_host_inventory_sources_id_seq', 1, false);


--
-- Name: main_hostmetric_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_hostmetric_id_seq', 1, false);


--
-- Name: main_hostmetricsummarymonthly_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_hostmetricsummarymonthly_id_seq', 1, false);


--
-- Name: main_instance_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_instance_id_seq', 1, true);


--
-- Name: main_instancegroup_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_instancegroup_id_seq', 2, true);


--
-- Name: main_instancegroup_instances_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_instancegroup_instances_id_seq', 2, true);


--
-- Name: main_instancelink_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_instancelink_id_seq', 1, false);


--
-- Name: main_inventory_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_inventory_id_seq', 1, true);


--
-- Name: main_inventory_labels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_inventory_labels_id_seq', 1, false);


--
-- Name: main_inventoryconstructedinventorymembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_inventoryconstructedinventorymembership_id_seq', 1, false);


--
-- Name: main_inventoryinstancegroupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_inventoryinstancegroupmembership_id_seq', 1, false);


--
-- Name: main_inventoryupdateevent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_inventoryupdateevent_id_seq', 1, false);


--
-- Name: main_inventoryupdateevent_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_inventoryupdateevent_id_seq1', 1, false);


--
-- Name: main_jobevent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_jobevent_id_seq', 1, false);


--
-- Name: main_jobevent_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_jobevent_id_seq1', 1, false);


--
-- Name: main_jobhostsummary_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_jobhostsummary_id_seq', 1, false);


--
-- Name: main_joblaunchconfig_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_joblaunchconfig_credentials_id_seq', 1, false);


--
-- Name: main_joblaunchconfig_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_joblaunchconfig_id_seq', 1, false);


--
-- Name: main_joblaunchconfig_labels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_joblaunchconfig_labels_id_seq', 1, false);


--
-- Name: main_joblaunchconfiginstancegroupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_joblaunchconfiginstancegroupmembership_id_seq', 1, false);


--
-- Name: main_label_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_label_id_seq', 1, false);


--
-- Name: main_notification_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_notification_id_seq', 1, false);


--
-- Name: main_notificationtemplate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_notificationtemplate_id_seq', 1, false);


--
-- Name: main_oauth2accesstoken_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_oauth2accesstoken_id_seq', 1, false);


--
-- Name: main_oauth2application_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_oauth2application_id_seq', 1, false);


--
-- Name: main_organization_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_organization_id_seq', 1, true);


--
-- Name: main_organization_notification_templates_approvals_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_organization_notification_templates_approvals_id_seq', 1, false);


--
-- Name: main_organization_notification_templates_error_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_organization_notification_templates_error_id_seq', 1, false);


--
-- Name: main_organization_notification_templates_started_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_organization_notification_templates_started_id_seq', 1, false);


--
-- Name: main_organization_notification_templates_success_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_organization_notification_templates_success_id_seq', 1, false);


--
-- Name: main_organizationgalaxycredentialmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_organizationgalaxycredentialmembership_id_seq', 1, true);


--
-- Name: main_organizationinstancegroupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_organizationinstancegroupmembership_id_seq', 1, false);


--
-- Name: main_profile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_profile_id_seq', 1, false);


--
-- Name: main_projectupdateevent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_projectupdateevent_id_seq', 1, false);


--
-- Name: main_projectupdateevent_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_projectupdateevent_id_seq1', 1, false);


--
-- Name: main_rbac_role_ancestors_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_rbac_role_ancestors_id_seq', 163, true);


--
-- Name: main_rbac_roles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_rbac_roles_id_seq', 39, true);


--
-- Name: main_rbac_roles_members_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_rbac_roles_members_id_seq', 2, true);


--
-- Name: main_rbac_roles_parents_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_rbac_roles_parents_id_seq', 65, true);


--
-- Name: main_schedule_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_schedule_credentials_id_seq', 1, false);


--
-- Name: main_schedule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_schedule_id_seq', 5, true);


--
-- Name: main_schedule_labels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_schedule_labels_id_seq', 1, false);


--
-- Name: main_scheduleinstancegroupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_scheduleinstancegroupmembership_id_seq', 1, false);


--
-- Name: main_smartinventorymembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_smartinventorymembership_id_seq', 1, false);


--
-- Name: main_systemjobevent_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_systemjobevent_id_seq', 1, false);


--
-- Name: main_systemjobevent_id_seq1; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_systemjobevent_id_seq1', 1, false);


--
-- Name: main_team_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_team_id_seq', 1, false);


--
-- Name: main_towerschedulestate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_towerschedulestate_id_seq', 1, false);


--
-- Name: main_unifiedjob_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjob_credentials_id_seq', 1, false);


--
-- Name: main_unifiedjob_dependent_jobs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjob_dependent_jobs_id_seq', 1, false);


--
-- Name: main_unifiedjob_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjob_id_seq', 1, false);


--
-- Name: main_unifiedjob_labels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjob_labels_id_seq', 1, false);


--
-- Name: main_unifiedjob_notifications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjob_notifications_id_seq', 1, false);


--
-- Name: main_unifiedjobtemplate_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjobtemplate_credentials_id_seq', 1, true);


--
-- Name: main_unifiedjobtemplate_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjobtemplate_id_seq', 7, true);


--
-- Name: main_unifiedjobtemplate_labels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjobtemplate_labels_id_seq', 1, false);


--
-- Name: main_unifiedjobtemplate_notification_templates_error_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjobtemplate_notification_templates_error_id_seq', 1, false);


--
-- Name: main_unifiedjobtemplate_notification_templates_started_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjobtemplate_notification_templates_started_id_seq', 1, false);


--
-- Name: main_unifiedjobtemplate_notification_templates_success_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjobtemplate_notification_templates_success_id_seq', 1, false);


--
-- Name: main_unifiedjobtemplateinstancegroupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_unifiedjobtemplateinstancegroupmembership_id_seq', 1, false);


--
-- Name: main_usersessionmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_usersessionmembership_id_seq', 1, false);


--
-- Name: main_workflowjobinstancegroupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobinstancegroupmembership_id_seq', 1, false);


--
-- Name: main_workflowjobnode_always_nodes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobnode_always_nodes_id_seq', 1, false);


--
-- Name: main_workflowjobnode_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobnode_credentials_id_seq', 1, false);


--
-- Name: main_workflowjobnode_failure_nodes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobnode_failure_nodes_id_seq', 1, false);


--
-- Name: main_workflowjobnode_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobnode_id_seq', 1, false);


--
-- Name: main_workflowjobnode_labels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobnode_labels_id_seq', 1, false);


--
-- Name: main_workflowjobnode_success_nodes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobnode_success_nodes_id_seq', 1, false);


--
-- Name: main_workflowjobnodebaseinstancegroupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobnodebaseinstancegroupmembership_id_seq', 1, false);


--
-- Name: main_workflowjobtemplate_notification_templates_approval_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobtemplate_notification_templates_approval_id_seq', 1, false);


--
-- Name: main_workflowjobtemplatenode_always_nodes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobtemplatenode_always_nodes_id_seq', 1, false);


--
-- Name: main_workflowjobtemplatenode_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobtemplatenode_credentials_id_seq', 1, false);


--
-- Name: main_workflowjobtemplatenode_failure_nodes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobtemplatenode_failure_nodes_id_seq', 1, false);


--
-- Name: main_workflowjobtemplatenode_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobtemplatenode_id_seq', 1, false);


--
-- Name: main_workflowjobtemplatenode_labels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobtemplatenode_labels_id_seq', 1, false);


--
-- Name: main_workflowjobtemplatenode_success_nodes_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobtemplatenode_success_nodes_id_seq', 1, false);


--
-- Name: main_workflowjobtemplatenodebaseinstancegroupmembership_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.main_workflowjobtemplatenodebaseinstancegroupmembership_id_seq', 1, false);


--
-- Name: oauth2_provider_grant_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.oauth2_provider_grant_id_seq', 1, false);


--
-- Name: oauth2_provider_idtoken_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.oauth2_provider_idtoken_id_seq', 1, false);


--
-- Name: oauth2_provider_refreshtoken_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.oauth2_provider_refreshtoken_id_seq', 1, false);


--
-- Name: social_auth_association_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.social_auth_association_id_seq', 1, false);


--
-- Name: social_auth_code_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.social_auth_code_id_seq', 1, false);


--
-- Name: social_auth_nonce_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.social_auth_nonce_id_seq', 1, false);


--
-- Name: social_auth_partial_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.social_auth_partial_id_seq', 1, false);


--
-- Name: social_auth_usersocialauth_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.social_auth_usersocialauth_id_seq', 1, false);


--
-- Name: sso_userenterpriseauth_id_seq; Type: SEQUENCE SET; Schema: public; Owner: awx
--

SELECT pg_catalog.setval('public.sso_userenterpriseauth_id_seq', 1, false);


--
-- Name: auth_group auth_group_name_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_group
    ADD CONSTRAINT auth_group_name_key UNIQUE (name);


--
-- Name: auth_group_permissions auth_group_permissions_group_id_permission_id_0cd325b0_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_group_permissions
    ADD CONSTRAINT auth_group_permissions_group_id_permission_id_0cd325b0_uniq UNIQUE (group_id, permission_id);


--
-- Name: auth_group_permissions auth_group_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_group_permissions
    ADD CONSTRAINT auth_group_permissions_pkey PRIMARY KEY (id);


--
-- Name: auth_group auth_group_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_group
    ADD CONSTRAINT auth_group_pkey PRIMARY KEY (id);


--
-- Name: auth_permission auth_permission_content_type_id_codename_01ab375a_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_permission
    ADD CONSTRAINT auth_permission_content_type_id_codename_01ab375a_uniq UNIQUE (content_type_id, codename);


--
-- Name: auth_permission auth_permission_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_permission
    ADD CONSTRAINT auth_permission_pkey PRIMARY KEY (id);


--
-- Name: auth_user_groups auth_user_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user_groups
    ADD CONSTRAINT auth_user_groups_pkey PRIMARY KEY (id);


--
-- Name: auth_user_groups auth_user_groups_user_id_group_id_94350c0c_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user_groups
    ADD CONSTRAINT auth_user_groups_user_id_group_id_94350c0c_uniq UNIQUE (user_id, group_id);


--
-- Name: auth_user auth_user_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user
    ADD CONSTRAINT auth_user_pkey PRIMARY KEY (id);


--
-- Name: auth_user_user_permissions auth_user_user_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user_user_permissions
    ADD CONSTRAINT auth_user_user_permissions_pkey PRIMARY KEY (id);


--
-- Name: auth_user_user_permissions auth_user_user_permissions_user_id_permission_id_14a6b632_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user_user_permissions
    ADD CONSTRAINT auth_user_user_permissions_user_id_permission_id_14a6b632_uniq UNIQUE (user_id, permission_id);


--
-- Name: auth_user auth_user_username_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user
    ADD CONSTRAINT auth_user_username_key UNIQUE (username);


--
-- Name: conf_setting conf_setting_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.conf_setting
    ADD CONSTRAINT conf_setting_pkey PRIMARY KEY (id);


--
-- Name: django_content_type django_content_type_app_label_model_76bd3d3b_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.django_content_type
    ADD CONSTRAINT django_content_type_app_label_model_76bd3d3b_uniq UNIQUE (app_label, model);


--
-- Name: django_content_type django_content_type_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.django_content_type
    ADD CONSTRAINT django_content_type_pkey PRIMARY KEY (id);


--
-- Name: django_migrations django_migrations_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.django_migrations
    ADD CONSTRAINT django_migrations_pkey PRIMARY KEY (id);


--
-- Name: django_session django_session_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.django_session
    ADD CONSTRAINT django_session_pkey PRIMARY KEY (session_key);


--
-- Name: django_site django_site_domain_a2e37b91_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.django_site
    ADD CONSTRAINT django_site_domain_a2e37b91_uniq UNIQUE (domain);


--
-- Name: django_site django_site_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.django_site
    ADD CONSTRAINT django_site_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_ad_hoc_command main_activitystream_ad_h_activitystream_id_adhocc_710d9648_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_ad_hoc_command
    ADD CONSTRAINT main_activitystream_ad_h_activitystream_id_adhocc_710d9648_uniq UNIQUE (activitystream_id, adhoccommand_id);


--
-- Name: main_activitystream_ad_hoc_command main_activitystream_ad_hoc_command_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_ad_hoc_command
    ADD CONSTRAINT main_activitystream_ad_hoc_command_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_credential main_activitystream_cred_activitystream_id_creden_6b3be6d5_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_credential
    ADD CONSTRAINT main_activitystream_cred_activitystream_id_creden_6b3be6d5_uniq UNIQUE (activitystream_id, credential_id);


--
-- Name: main_activitystream_credential_type main_activitystream_cred_activitystream_id_creden_85746647_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_credential_type
    ADD CONSTRAINT main_activitystream_cred_activitystream_id_creden_85746647_uniq UNIQUE (activitystream_id, credentialtype_id);


--
-- Name: main_activitystream_credential main_activitystream_credential_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_credential
    ADD CONSTRAINT main_activitystream_credential_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_credential_type main_activitystream_credential_type_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_credential_type
    ADD CONSTRAINT main_activitystream_credential_type_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_execution_environment main_activitystream_exec_activitystream_id_execut_e6698de7_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_execution_environment
    ADD CONSTRAINT main_activitystream_exec_activitystream_id_execut_e6698de7_uniq UNIQUE (activitystream_id, executionenvironment_id);


--
-- Name: main_activitystream_execution_environment main_activitystream_execution_environment_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_execution_environment
    ADD CONSTRAINT main_activitystream_execution_environment_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_group main_activitystream_grou_activitystream_id_group__3068b98d_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_group
    ADD CONSTRAINT main_activitystream_grou_activitystream_id_group__3068b98d_uniq UNIQUE (activitystream_id, group_id);


--
-- Name: main_activitystream_group main_activitystream_group_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_group
    ADD CONSTRAINT main_activitystream_group_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_host main_activitystream_host_activitystream_id_host_i_7ec5e62e_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_host
    ADD CONSTRAINT main_activitystream_host_activitystream_id_host_i_7ec5e62e_uniq UNIQUE (activitystream_id, host_id);


--
-- Name: main_activitystream_host main_activitystream_host_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_host
    ADD CONSTRAINT main_activitystream_host_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_instance_group main_activitystream_inst_activitystream_id_instan_173bfccd_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_instance_group
    ADD CONSTRAINT main_activitystream_inst_activitystream_id_instan_173bfccd_uniq UNIQUE (activitystream_id, instancegroup_id);


--
-- Name: main_activitystream_instance main_activitystream_inst_activitystream_id_instan_eba71ee1_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_instance
    ADD CONSTRAINT main_activitystream_inst_activitystream_id_instan_eba71ee1_uniq UNIQUE (activitystream_id, instance_id);


--
-- Name: main_activitystream_instance_group main_activitystream_instance_group_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_instance_group
    ADD CONSTRAINT main_activitystream_instance_group_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_instance main_activitystream_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_instance
    ADD CONSTRAINT main_activitystream_instance_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_inventory_update main_activitystream_inve_activitystream_id_invent_28edee6e_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory_update
    ADD CONSTRAINT main_activitystream_inve_activitystream_id_invent_28edee6e_uniq UNIQUE (activitystream_id, inventoryupdate_id);


--
-- Name: main_activitystream_inventory main_activitystream_inve_activitystream_id_invent_410769d5_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory
    ADD CONSTRAINT main_activitystream_inve_activitystream_id_invent_410769d5_uniq UNIQUE (activitystream_id, inventory_id);


--
-- Name: main_activitystream_inventory_source main_activitystream_inve_activitystream_id_invent_e9d8f675_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory_source
    ADD CONSTRAINT main_activitystream_inve_activitystream_id_invent_e9d8f675_uniq UNIQUE (activitystream_id, inventorysource_id);


--
-- Name: main_activitystream_inventory main_activitystream_inventory_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory
    ADD CONSTRAINT main_activitystream_inventory_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_inventory_source main_activitystream_inventory_source_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory_source
    ADD CONSTRAINT main_activitystream_inventory_source_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_inventory_update main_activitystream_inventory_update_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory_update
    ADD CONSTRAINT main_activitystream_inventory_update_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_job_template main_activitystream_job__activitystream_id_jobtem_ca7c997a_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_job_template
    ADD CONSTRAINT main_activitystream_job__activitystream_id_jobtem_ca7c997a_uniq UNIQUE (activitystream_id, jobtemplate_id);


--
-- Name: main_activitystream_job main_activitystream_job_activitystream_id_job_id_dbb86499_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_job
    ADD CONSTRAINT main_activitystream_job_activitystream_id_job_id_dbb86499_uniq UNIQUE (activitystream_id, job_id);


--
-- Name: main_activitystream_job main_activitystream_job_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_job
    ADD CONSTRAINT main_activitystream_job_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_job_template main_activitystream_job_template_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_job_template
    ADD CONSTRAINT main_activitystream_job_template_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_label main_activitystream_labe_activitystream_id_label__04ca98fb_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_label
    ADD CONSTRAINT main_activitystream_labe_activitystream_id_label__04ca98fb_uniq UNIQUE (activitystream_id, label_id);


--
-- Name: main_activitystream_label main_activitystream_label_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_label
    ADD CONSTRAINT main_activitystream_label_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_notification_template main_activitystream_noti_activitystream_id_notifi_2ecdc66e_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_notification_template
    ADD CONSTRAINT main_activitystream_noti_activitystream_id_notifi_2ecdc66e_uniq UNIQUE (activitystream_id, notificationtemplate_id);


--
-- Name: main_activitystream_notification main_activitystream_noti_activitystream_id_notifi_3f05835f_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_notification
    ADD CONSTRAINT main_activitystream_noti_activitystream_id_notifi_3f05835f_uniq UNIQUE (activitystream_id, notification_id);


--
-- Name: main_activitystream_notification main_activitystream_notification_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_notification
    ADD CONSTRAINT main_activitystream_notification_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_notification_template main_activitystream_notification_template_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_notification_template
    ADD CONSTRAINT main_activitystream_notification_template_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_o_auth2_application main_activitystream_o_au_activitystream_id_oauth2_50acf66a_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_o_auth2_application
    ADD CONSTRAINT main_activitystream_o_au_activitystream_id_oauth2_50acf66a_uniq UNIQUE (activitystream_id, oauth2application_id);


--
-- Name: main_activitystream_o_auth2_access_token main_activitystream_o_au_activitystream_id_oauth2_625b181f_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_o_auth2_access_token
    ADD CONSTRAINT main_activitystream_o_au_activitystream_id_oauth2_625b181f_uniq UNIQUE (activitystream_id, oauth2accesstoken_id);


--
-- Name: main_activitystream_o_auth2_access_token main_activitystream_o_auth2_access_token_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_o_auth2_access_token
    ADD CONSTRAINT main_activitystream_o_auth2_access_token_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_o_auth2_application main_activitystream_o_auth2_application_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_o_auth2_application
    ADD CONSTRAINT main_activitystream_o_auth2_application_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_organization main_activitystream_orga_activitystream_id_organi_ad587114_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_organization
    ADD CONSTRAINT main_activitystream_orga_activitystream_id_organi_ad587114_uniq UNIQUE (activitystream_id, organization_id);


--
-- Name: main_activitystream_organization main_activitystream_organization_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_organization
    ADD CONSTRAINT main_activitystream_organization_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream main_activitystream_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream
    ADD CONSTRAINT main_activitystream_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_project main_activitystream_proj_activitystream_id_projec_25dcced8_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_project
    ADD CONSTRAINT main_activitystream_proj_activitystream_id_projec_25dcced8_uniq UNIQUE (activitystream_id, project_id);


--
-- Name: main_activitystream_project_update main_activitystream_proj_activitystream_id_projec_a3be3a08_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_project_update
    ADD CONSTRAINT main_activitystream_proj_activitystream_id_projec_a3be3a08_uniq UNIQUE (activitystream_id, projectupdate_id);


--
-- Name: main_activitystream_project main_activitystream_project_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_project
    ADD CONSTRAINT main_activitystream_project_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_project_update main_activitystream_project_update_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_project_update
    ADD CONSTRAINT main_activitystream_project_update_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_role main_activitystream_role_activitystream_id_role_i_b51f6b40_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_role
    ADD CONSTRAINT main_activitystream_role_activitystream_id_role_i_b51f6b40_uniq UNIQUE (activitystream_id, role_id);


--
-- Name: main_activitystream_role main_activitystream_role_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_role
    ADD CONSTRAINT main_activitystream_role_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_schedule main_activitystream_sche_activitystream_id_schedu_a871c992_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_schedule
    ADD CONSTRAINT main_activitystream_sche_activitystream_id_schedu_a871c992_uniq UNIQUE (activitystream_id, schedule_id);


--
-- Name: main_activitystream_schedule main_activitystream_schedule_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_schedule
    ADD CONSTRAINT main_activitystream_schedule_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_team main_activitystream_team_activitystream_id_team_i_89af4b2a_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_team
    ADD CONSTRAINT main_activitystream_team_activitystream_id_team_i_89af4b2a_uniq UNIQUE (activitystream_id, team_id);


--
-- Name: main_activitystream_team main_activitystream_team_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_team
    ADD CONSTRAINT main_activitystream_team_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_unified_job main_activitystream_unif_activitystream_id_unifie_0fc17da3_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_unified_job
    ADD CONSTRAINT main_activitystream_unif_activitystream_id_unifie_0fc17da3_uniq UNIQUE (activitystream_id, unifiedjob_id);


--
-- Name: main_activitystream_unified_job_template main_activitystream_unif_activitystream_id_unifie_e4b906b4_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_unified_job_template
    ADD CONSTRAINT main_activitystream_unif_activitystream_id_unifie_e4b906b4_uniq UNIQUE (activitystream_id, unifiedjobtemplate_id);


--
-- Name: main_activitystream_unified_job main_activitystream_unified_job_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_unified_job
    ADD CONSTRAINT main_activitystream_unified_job_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_unified_job_template main_activitystream_unified_job_template_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_unified_job_template
    ADD CONSTRAINT main_activitystream_unified_job_template_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_user main_activitystream_user_activitystream_id_user_i_3fa08b1e_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_user
    ADD CONSTRAINT main_activitystream_user_activitystream_id_user_i_3fa08b1e_uniq UNIQUE (activitystream_id, user_id);


--
-- Name: main_activitystream_user main_activitystream_user_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_user
    ADD CONSTRAINT main_activitystream_user_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_workflow_approval_template main_activitystream_work_activitystream_id_workfl_6145f2cd_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_approval_template
    ADD CONSTRAINT main_activitystream_work_activitystream_id_workfl_6145f2cd_uniq UNIQUE (activitystream_id, workflowapprovaltemplate_id);


--
-- Name: main_activitystream_workflow_approval main_activitystream_work_activitystream_id_workfl_7c76df21_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_approval
    ADD CONSTRAINT main_activitystream_work_activitystream_id_workfl_7c76df21_uniq UNIQUE (activitystream_id, workflowapproval_id);


--
-- Name: main_activitystream_workflow_job_template main_activitystream_work_activitystream_id_workfl_9cf83c74_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_template
    ADD CONSTRAINT main_activitystream_work_activitystream_id_workfl_9cf83c74_uniq UNIQUE (activitystream_id, workflowjobtemplate_id);


--
-- Name: main_activitystream_workflow_job main_activitystream_work_activitystream_id_workfl_bfe2d0c3_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job
    ADD CONSTRAINT main_activitystream_work_activitystream_id_workfl_bfe2d0c3_uniq UNIQUE (activitystream_id, workflowjob_id);


--
-- Name: main_activitystream_workflow_job_template_node main_activitystream_work_activitystream_id_workfl_c3080a18_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_template_node
    ADD CONSTRAINT main_activitystream_work_activitystream_id_workfl_c3080a18_uniq UNIQUE (activitystream_id, workflowjobtemplatenode_id);


--
-- Name: main_activitystream_workflow_job_node main_activitystream_work_activitystream_id_workfl_d615af7e_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_node
    ADD CONSTRAINT main_activitystream_work_activitystream_id_workfl_d615af7e_uniq UNIQUE (activitystream_id, workflowjobnode_id);


--
-- Name: main_activitystream_workflow_approval main_activitystream_workflow_approval_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_approval
    ADD CONSTRAINT main_activitystream_workflow_approval_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_workflow_approval_template main_activitystream_workflow_approval_template_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_approval_template
    ADD CONSTRAINT main_activitystream_workflow_approval_template_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_workflow_job_node main_activitystream_workflow_job_node_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_node
    ADD CONSTRAINT main_activitystream_workflow_job_node_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_workflow_job main_activitystream_workflow_job_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job
    ADD CONSTRAINT main_activitystream_workflow_job_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_workflow_job_template_node main_activitystream_workflow_job_template_node_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_template_node
    ADD CONSTRAINT main_activitystream_workflow_job_template_node_pkey PRIMARY KEY (id);


--
-- Name: main_activitystream_workflow_job_template main_activitystream_workflow_job_template_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_template
    ADD CONSTRAINT main_activitystream_workflow_job_template_pkey PRIMARY KEY (id);


--
-- Name: main_adhoccommand main_adhoccommand_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_adhoccommand
    ADD CONSTRAINT main_adhoccommand_pkey PRIMARY KEY (unifiedjob_ptr_id);


--
-- Name: _unpartitioned_main_adhoccommandevent main_adhoccommandevent_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_adhoccommandevent
    ADD CONSTRAINT main_adhoccommandevent_pkey PRIMARY KEY (id);


--
-- Name: main_adhoccommandevent main_adhoccommandevent_pkey_new; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_adhoccommandevent
    ADD CONSTRAINT main_adhoccommandevent_pkey_new PRIMARY KEY (id, job_created);


--
-- Name: main_credential main_credential_organization_id_name_cre_55ee19c5_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_organization_id_name_cre_55ee19c5_uniq UNIQUE (organization_id, name, credential_type_id);


--
-- Name: main_credential main_credential_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_pkey PRIMARY KEY (id);


--
-- Name: main_credentialinputsource main_credentialinputsour_target_credential_id_inp_8e297f1b_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialinputsource
    ADD CONSTRAINT main_credentialinputsour_target_credential_id_inp_8e297f1b_uniq UNIQUE (target_credential_id, input_field_name);


--
-- Name: main_credentialinputsource main_credentialinputsource_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialinputsource
    ADD CONSTRAINT main_credentialinputsource_pkey PRIMARY KEY (id);


--
-- Name: main_credentialtype main_credentialtype_name_kind_af26d717_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialtype
    ADD CONSTRAINT main_credentialtype_name_kind_af26d717_uniq UNIQUE (name, kind);


--
-- Name: main_credentialtype main_credentialtype_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialtype
    ADD CONSTRAINT main_credentialtype_pkey PRIMARY KEY (id);


--
-- Name: main_custominventoryscript main_custominventoryscript_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_custominventoryscript
    ADD CONSTRAINT main_custominventoryscript_pkey PRIMARY KEY (id);


--
-- Name: main_executionenvironment main_executionenvironment_name_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_executionenvironment
    ADD CONSTRAINT main_executionenvironment_name_key UNIQUE (name);


--
-- Name: main_executionenvironment main_executionenvironment_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_executionenvironment
    ADD CONSTRAINT main_executionenvironment_pkey PRIMARY KEY (id);


--
-- Name: main_group_hosts main_group_hosts_group_id_host_id_0713d0ac_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_hosts
    ADD CONSTRAINT main_group_hosts_group_id_host_id_0713d0ac_uniq UNIQUE (group_id, host_id);


--
-- Name: main_group_hosts main_group_hosts_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_hosts
    ADD CONSTRAINT main_group_hosts_pkey PRIMARY KEY (id);


--
-- Name: main_group_inventory_sources main_group_inventory_sou_group_id_inventorysource_dcb51e86_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_inventory_sources
    ADD CONSTRAINT main_group_inventory_sou_group_id_inventorysource_dcb51e86_uniq UNIQUE (group_id, inventorysource_id);


--
-- Name: main_group_inventory_sources main_group_inventory_sources_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_inventory_sources
    ADD CONSTRAINT main_group_inventory_sources_pkey PRIMARY KEY (id);


--
-- Name: main_group main_group_name_inventory_id_459cfada_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group
    ADD CONSTRAINT main_group_name_inventory_id_459cfada_uniq UNIQUE (name, inventory_id);


--
-- Name: main_group_parents main_group_parents_from_group_id_to_group_id_8c9a3fcb_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_parents
    ADD CONSTRAINT main_group_parents_from_group_id_to_group_id_8c9a3fcb_uniq UNIQUE (from_group_id, to_group_id);


--
-- Name: main_group_parents main_group_parents_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_parents
    ADD CONSTRAINT main_group_parents_pkey PRIMARY KEY (id);


--
-- Name: main_group main_group_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group
    ADD CONSTRAINT main_group_pkey PRIMARY KEY (id);


--
-- Name: main_host_inventory_sources main_host_inventory_sour_host_id_inventorysource__bdf6a207_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host_inventory_sources
    ADD CONSTRAINT main_host_inventory_sour_host_id_inventorysource__bdf6a207_uniq UNIQUE (host_id, inventorysource_id);


--
-- Name: main_host_inventory_sources main_host_inventory_sources_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host_inventory_sources
    ADD CONSTRAINT main_host_inventory_sources_pkey PRIMARY KEY (id);


--
-- Name: main_host main_host_name_inventory_id_45aecd68_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host
    ADD CONSTRAINT main_host_name_inventory_id_45aecd68_uniq UNIQUE (name, inventory_id);


--
-- Name: main_host main_host_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host
    ADD CONSTRAINT main_host_pkey PRIMARY KEY (id);


--
-- Name: main_hostmetric main_hostmetric_hostname_87ac3c1f_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_hostmetric
    ADD CONSTRAINT main_hostmetric_hostname_87ac3c1f_uniq UNIQUE (hostname);


--
-- Name: main_hostmetric main_hostmetric_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_hostmetric
    ADD CONSTRAINT main_hostmetric_pkey PRIMARY KEY (id);


--
-- Name: main_hostmetricsummarymonthly main_hostmetricsummarymonthly_date_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_hostmetricsummarymonthly
    ADD CONSTRAINT main_hostmetricsummarymonthly_date_key UNIQUE (date);


--
-- Name: main_hostmetricsummarymonthly main_hostmetricsummarymonthly_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_hostmetricsummarymonthly
    ADD CONSTRAINT main_hostmetricsummarymonthly_pkey PRIMARY KEY (id);


--
-- Name: main_instance main_instance_hostname_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instance
    ADD CONSTRAINT main_instance_hostname_key UNIQUE (hostname);


--
-- Name: main_instance main_instance_ip_address_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instance
    ADD CONSTRAINT main_instance_ip_address_key UNIQUE (ip_address);


--
-- Name: main_instance main_instance_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instance
    ADD CONSTRAINT main_instance_pkey PRIMARY KEY (id);


--
-- Name: main_instancegroup_instances main_instancegroup_insta_instancegroup_id_instanc_d224c278_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup_instances
    ADD CONSTRAINT main_instancegroup_insta_instancegroup_id_instanc_d224c278_uniq UNIQUE (instancegroup_id, instance_id);


--
-- Name: main_instancegroup_instances main_instancegroup_instances_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup_instances
    ADD CONSTRAINT main_instancegroup_instances_pkey PRIMARY KEY (id);


--
-- Name: main_instancegroup main_instancegroup_name_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup
    ADD CONSTRAINT main_instancegroup_name_key UNIQUE (name);


--
-- Name: main_instancegroup main_instancegroup_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup
    ADD CONSTRAINT main_instancegroup_pkey PRIMARY KEY (id);


--
-- Name: main_instancelink main_instancelink_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancelink
    ADD CONSTRAINT main_instancelink_pkey PRIMARY KEY (id);


--
-- Name: main_instancelink main_instancelink_source_id_target_id_92817c9f_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancelink
    ADD CONSTRAINT main_instancelink_source_id_target_id_92817c9f_uniq UNIQUE (source_id, target_id);


--
-- Name: main_inventory_labels main_inventory_labels_inventory_id_label_id_b527d1a3_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory_labels
    ADD CONSTRAINT main_inventory_labels_inventory_id_label_id_b527d1a3_uniq UNIQUE (inventory_id, label_id);


--
-- Name: main_inventory_labels main_inventory_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory_labels
    ADD CONSTRAINT main_inventory_labels_pkey PRIMARY KEY (id);


--
-- Name: main_inventory main_inventory_name_organization_id_5137f34c_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_name_organization_id_5137f34c_uniq UNIQUE (name, organization_id);


--
-- Name: main_inventory main_inventory_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_pkey PRIMARY KEY (id);


--
-- Name: main_inventoryconstructedinventorymembership main_inventoryconstructedinventorymembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryconstructedinventorymembership
    ADD CONSTRAINT main_inventoryconstructedinventorymembership_pkey PRIMARY KEY (id);


--
-- Name: main_inventoryinstancegroupmembership main_inventoryinstancegroupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryinstancegroupmembership
    ADD CONSTRAINT main_inventoryinstancegroupmembership_pkey PRIMARY KEY (id);


--
-- Name: main_inventorysource main_inventorysource_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventorysource
    ADD CONSTRAINT main_inventorysource_pkey PRIMARY KEY (unifiedjobtemplate_ptr_id);


--
-- Name: main_inventoryupdate main_inventoryupdate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryupdate
    ADD CONSTRAINT main_inventoryupdate_pkey PRIMARY KEY (unifiedjob_ptr_id);


--
-- Name: _unpartitioned_main_inventoryupdateevent main_inventoryupdateevent_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_inventoryupdateevent
    ADD CONSTRAINT main_inventoryupdateevent_pkey PRIMARY KEY (id);


--
-- Name: main_inventoryupdateevent main_inventoryupdateevent_pkey_new; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryupdateevent
    ADD CONSTRAINT main_inventoryupdateevent_pkey_new PRIMARY KEY (id, job_created);


--
-- Name: main_job main_job_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_job
    ADD CONSTRAINT main_job_pkey PRIMARY KEY (unifiedjob_ptr_id);


--
-- Name: _unpartitioned_main_jobevent main_jobevent_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_jobevent
    ADD CONSTRAINT main_jobevent_pkey PRIMARY KEY (id);


--
-- Name: main_jobevent main_jobevent_pkey_new; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobevent
    ADD CONSTRAINT main_jobevent_pkey_new PRIMARY KEY (id, job_created);


--
-- Name: main_jobhostsummary main_jobhostsummary_job_id_host_name_eb22f938_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobhostsummary
    ADD CONSTRAINT main_jobhostsummary_job_id_host_name_eb22f938_uniq UNIQUE (job_id, host_name);


--
-- Name: main_jobhostsummary main_jobhostsummary_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobhostsummary
    ADD CONSTRAINT main_jobhostsummary_pkey PRIMARY KEY (id);


--
-- Name: main_joblaunchconfig_credentials main_joblaunchconfig_cre_joblaunchconfig_id_crede_77f9ef8b_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig_credentials
    ADD CONSTRAINT main_joblaunchconfig_cre_joblaunchconfig_id_crede_77f9ef8b_uniq UNIQUE (joblaunchconfig_id, credential_id);


--
-- Name: main_joblaunchconfig_credentials main_joblaunchconfig_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig_credentials
    ADD CONSTRAINT main_joblaunchconfig_credentials_pkey PRIMARY KEY (id);


--
-- Name: main_joblaunchconfig main_joblaunchconfig_job_id_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig
    ADD CONSTRAINT main_joblaunchconfig_job_id_key UNIQUE (job_id);


--
-- Name: main_joblaunchconfig_labels main_joblaunchconfig_lab_joblaunchconfig_id_label_bddd29c9_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig_labels
    ADD CONSTRAINT main_joblaunchconfig_lab_joblaunchconfig_id_label_bddd29c9_uniq UNIQUE (joblaunchconfig_id, label_id);


--
-- Name: main_joblaunchconfig_labels main_joblaunchconfig_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig_labels
    ADD CONSTRAINT main_joblaunchconfig_labels_pkey PRIMARY KEY (id);


--
-- Name: main_joblaunchconfig main_joblaunchconfig_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig
    ADD CONSTRAINT main_joblaunchconfig_pkey PRIMARY KEY (id);


--
-- Name: main_joblaunchconfiginstancegroupmembership main_joblaunchconfiginstancegroupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfiginstancegroupmembership
    ADD CONSTRAINT main_joblaunchconfiginstancegroupmembership_pkey PRIMARY KEY (id);


--
-- Name: main_jobtemplate main_jobtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobtemplate
    ADD CONSTRAINT main_jobtemplate_pkey PRIMARY KEY (unifiedjobtemplate_ptr_id);


--
-- Name: main_label main_label_name_organization_id_f79d7ac4_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_label
    ADD CONSTRAINT main_label_name_organization_id_f79d7ac4_uniq UNIQUE (name, organization_id);


--
-- Name: main_label main_label_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_label
    ADD CONSTRAINT main_label_pkey PRIMARY KEY (id);


--
-- Name: main_notification main_notification_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_notification
    ADD CONSTRAINT main_notification_pkey PRIMARY KEY (id);


--
-- Name: main_notificationtemplate main_notificationtemplate_organization_id_name_07260e01_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_notificationtemplate
    ADD CONSTRAINT main_notificationtemplate_organization_id_name_07260e01_uniq UNIQUE (organization_id, name);


--
-- Name: main_notificationtemplate main_notificationtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_notificationtemplate
    ADD CONSTRAINT main_notificationtemplate_pkey PRIMARY KEY (id);


--
-- Name: main_oauth2accesstoken main_oauth2accesstoken_id_token_id_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2accesstoken
    ADD CONSTRAINT main_oauth2accesstoken_id_token_id_key UNIQUE (id_token_id);


--
-- Name: main_oauth2accesstoken main_oauth2accesstoken_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2accesstoken
    ADD CONSTRAINT main_oauth2accesstoken_pkey PRIMARY KEY (id);


--
-- Name: main_oauth2accesstoken main_oauth2accesstoken_source_refresh_token_id_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2accesstoken
    ADD CONSTRAINT main_oauth2accesstoken_source_refresh_token_id_key UNIQUE (source_refresh_token_id);


--
-- Name: main_oauth2accesstoken main_oauth2accesstoken_token_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2accesstoken
    ADD CONSTRAINT main_oauth2accesstoken_token_key UNIQUE (token);


--
-- Name: main_oauth2application main_oauth2application_client_id_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2application
    ADD CONSTRAINT main_oauth2application_client_id_key UNIQUE (client_id);


--
-- Name: main_oauth2application main_oauth2application_name_organization_id_55462c8e_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2application
    ADD CONSTRAINT main_oauth2application_name_organization_id_55462c8e_uniq UNIQUE (name, organization_id);


--
-- Name: main_oauth2application main_oauth2application_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2application
    ADD CONSTRAINT main_oauth2application_pkey PRIMARY KEY (id);


--
-- Name: main_organization main_organization_name_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_name_key UNIQUE (name);


--
-- Name: main_organization_notification_templates_started main_organization_notifi_organization_id_notifica_2ef43b54_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_started
    ADD CONSTRAINT main_organization_notifi_organization_id_notifica_2ef43b54_uniq UNIQUE (organization_id, notificationtemplate_id);


--
-- Name: main_organization_notification_templates_success main_organization_notifi_organization_id_notifica_3ccf8832_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_success
    ADD CONSTRAINT main_organization_notifi_organization_id_notifica_3ccf8832_uniq UNIQUE (organization_id, notificationtemplate_id);


--
-- Name: main_organization_notification_templates_error main_organization_notifi_organization_id_notifica_88aa41f6_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_error
    ADD CONSTRAINT main_organization_notifi_organization_id_notifica_88aa41f6_uniq UNIQUE (organization_id, notificationtemplate_id);


--
-- Name: main_organization_notification_templates_approvals main_organization_notifi_organization_id_notifica_ec9bb02b_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_approvals
    ADD CONSTRAINT main_organization_notifi_organization_id_notifica_ec9bb02b_uniq UNIQUE (organization_id, notificationtemplate_id);


--
-- Name: main_organization_notification_templates_approvals main_organization_notification_templates_approvals_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_approvals
    ADD CONSTRAINT main_organization_notification_templates_approvals_pkey PRIMARY KEY (id);


--
-- Name: main_organization_notification_templates_error main_organization_notification_templates_error_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_error
    ADD CONSTRAINT main_organization_notification_templates_error_pkey PRIMARY KEY (id);


--
-- Name: main_organization_notification_templates_started main_organization_notification_templates_started_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_started
    ADD CONSTRAINT main_organization_notification_templates_started_pkey PRIMARY KEY (id);


--
-- Name: main_organization_notification_templates_success main_organization_notification_templates_success_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_success
    ADD CONSTRAINT main_organization_notification_templates_success_pkey PRIMARY KEY (id);


--
-- Name: main_organization main_organization_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_pkey PRIMARY KEY (id);


--
-- Name: main_organizationgalaxycredentialmembership main_organizationgalaxycredentialmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organizationgalaxycredentialmembership
    ADD CONSTRAINT main_organizationgalaxycredentialmembership_pkey PRIMARY KEY (id);


--
-- Name: main_organizationinstancegroupmembership main_organizationinstancegroupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organizationinstancegroupmembership
    ADD CONSTRAINT main_organizationinstancegroupmembership_pkey PRIMARY KEY (id);


--
-- Name: main_profile main_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_profile
    ADD CONSTRAINT main_profile_pkey PRIMARY KEY (id);


--
-- Name: main_profile main_profile_user_id_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_profile
    ADD CONSTRAINT main_profile_user_id_key UNIQUE (user_id);


--
-- Name: main_project main_project_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_pkey PRIMARY KEY (unifiedjobtemplate_ptr_id);


--
-- Name: main_projectupdate main_projectupdate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_projectupdate
    ADD CONSTRAINT main_projectupdate_pkey PRIMARY KEY (unifiedjob_ptr_id);


--
-- Name: _unpartitioned_main_projectupdateevent main_projectupdateevent_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_projectupdateevent
    ADD CONSTRAINT main_projectupdateevent_pkey PRIMARY KEY (id);


--
-- Name: main_projectupdateevent main_projectupdateevent_pkey_new; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_projectupdateevent
    ADD CONSTRAINT main_projectupdateevent_pkey_new PRIMARY KEY (id, job_created);


--
-- Name: main_rbac_role_ancestors main_rbac_role_ancestors_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_role_ancestors
    ADD CONSTRAINT main_rbac_role_ancestors_pkey PRIMARY KEY (id);


--
-- Name: main_rbac_roles_members main_rbac_roles_members_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles_members
    ADD CONSTRAINT main_rbac_roles_members_pkey PRIMARY KEY (id);


--
-- Name: main_rbac_roles_members main_rbac_roles_members_role_id_user_id_9803c082_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles_members
    ADD CONSTRAINT main_rbac_roles_members_role_id_user_id_9803c082_uniq UNIQUE (role_id, user_id);


--
-- Name: main_rbac_roles_parents main_rbac_roles_parents_from_role_id_to_role_id_1ab75c81_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles_parents
    ADD CONSTRAINT main_rbac_roles_parents_from_role_id_to_role_id_1ab75c81_uniq UNIQUE (from_role_id, to_role_id);


--
-- Name: main_rbac_roles_parents main_rbac_roles_parents_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles_parents
    ADD CONSTRAINT main_rbac_roles_parents_pkey PRIMARY KEY (id);


--
-- Name: main_rbac_roles main_rbac_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles
    ADD CONSTRAINT main_rbac_roles_pkey PRIMARY KEY (id);


--
-- Name: main_rbac_roles main_rbac_roles_singleton_name_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles
    ADD CONSTRAINT main_rbac_roles_singleton_name_key UNIQUE (singleton_name);


--
-- Name: main_schedule_credentials main_schedule_credential_schedule_id_credential_i_11bed4b0_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule_credentials
    ADD CONSTRAINT main_schedule_credential_schedule_id_credential_i_11bed4b0_uniq UNIQUE (schedule_id, credential_id);


--
-- Name: main_schedule_credentials main_schedule_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule_credentials
    ADD CONSTRAINT main_schedule_credentials_pkey PRIMARY KEY (id);


--
-- Name: main_schedule_labels main_schedule_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule_labels
    ADD CONSTRAINT main_schedule_labels_pkey PRIMARY KEY (id);


--
-- Name: main_schedule_labels main_schedule_labels_schedule_id_label_id_56639469_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule_labels
    ADD CONSTRAINT main_schedule_labels_schedule_id_label_id_56639469_uniq UNIQUE (schedule_id, label_id);


--
-- Name: main_schedule main_schedule_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule
    ADD CONSTRAINT main_schedule_pkey PRIMARY KEY (id);


--
-- Name: main_schedule main_schedule_unified_job_template_id_name_9ba35d7e_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule
    ADD CONSTRAINT main_schedule_unified_job_template_id_name_9ba35d7e_uniq UNIQUE (unified_job_template_id, name);


--
-- Name: main_scheduleinstancegroupmembership main_scheduleinstancegroupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_scheduleinstancegroupmembership
    ADD CONSTRAINT main_scheduleinstancegroupmembership_pkey PRIMARY KEY (id);


--
-- Name: main_smartinventorymembership main_smartinventorymembe_host_id_inventory_id_58137be6_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_smartinventorymembership
    ADD CONSTRAINT main_smartinventorymembe_host_id_inventory_id_58137be6_uniq UNIQUE (host_id, inventory_id);


--
-- Name: main_smartinventorymembership main_smartinventorymembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_smartinventorymembership
    ADD CONSTRAINT main_smartinventorymembership_pkey PRIMARY KEY (id);


--
-- Name: main_systemjob main_systemjob_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_systemjob
    ADD CONSTRAINT main_systemjob_pkey PRIMARY KEY (unifiedjob_ptr_id);


--
-- Name: _unpartitioned_main_systemjobevent main_systemjobevent_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_systemjobevent
    ADD CONSTRAINT main_systemjobevent_pkey PRIMARY KEY (id);


--
-- Name: main_systemjobevent main_systemjobevent_pkey_new; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_systemjobevent
    ADD CONSTRAINT main_systemjobevent_pkey_new PRIMARY KEY (id, job_created);


--
-- Name: main_systemjobtemplate main_systemjobtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_systemjobtemplate
    ADD CONSTRAINT main_systemjobtemplate_pkey PRIMARY KEY (unifiedjobtemplate_ptr_id);


--
-- Name: main_team main_team_organization_id_name_70f0184b_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_team
    ADD CONSTRAINT main_team_organization_id_name_70f0184b_uniq UNIQUE (organization_id, name);


--
-- Name: main_team main_team_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_team
    ADD CONSTRAINT main_team_pkey PRIMARY KEY (id);


--
-- Name: main_towerschedulestate main_towerschedulestate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_towerschedulestate
    ADD CONSTRAINT main_towerschedulestate_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjob_credentials main_unifiedjob_credenti_unifiedjob_id_credential_f4b12e17_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_credentials
    ADD CONSTRAINT main_unifiedjob_credenti_unifiedjob_id_credential_f4b12e17_uniq UNIQUE (unifiedjob_id, credential_id);


--
-- Name: main_unifiedjob_credentials main_unifiedjob_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_credentials
    ADD CONSTRAINT main_unifiedjob_credentials_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjob_dependent_jobs main_unifiedjob_dependen_from_unifiedjob_id_to_un_8ee8a967_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_dependent_jobs
    ADD CONSTRAINT main_unifiedjob_dependen_from_unifiedjob_id_to_un_8ee8a967_uniq UNIQUE (from_unifiedjob_id, to_unifiedjob_id);


--
-- Name: main_unifiedjob_dependent_jobs main_unifiedjob_dependent_jobs_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_dependent_jobs
    ADD CONSTRAINT main_unifiedjob_dependent_jobs_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjob_labels main_unifiedjob_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_labels
    ADD CONSTRAINT main_unifiedjob_labels_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjob_labels main_unifiedjob_labels_unifiedjob_id_label_id_f6e1dc96_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_labels
    ADD CONSTRAINT main_unifiedjob_labels_unifiedjob_id_label_id_f6e1dc96_uniq UNIQUE (unifiedjob_id, label_id);


--
-- Name: main_unifiedjob_notifications main_unifiedjob_notifica_unifiedjob_id_notificati_895ae806_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_notifications
    ADD CONSTRAINT main_unifiedjob_notifica_unifiedjob_id_notificati_895ae806_uniq UNIQUE (unifiedjob_id, notification_id);


--
-- Name: main_unifiedjob_notifications main_unifiedjob_notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_notifications
    ADD CONSTRAINT main_unifiedjob_notifications_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjob main_unifiedjob_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjobtemplate_credentials main_unifiedjobtemplate__unifiedjobtemplate_id_cr_e10bc7a4_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_credentials
    ADD CONSTRAINT main_unifiedjobtemplate__unifiedjobtemplate_id_cr_e10bc7a4_uniq UNIQUE (unifiedjobtemplate_id, credential_id);


--
-- Name: main_unifiedjobtemplate_labels main_unifiedjobtemplate__unifiedjobtemplate_id_la_ad69a027_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_labels
    ADD CONSTRAINT main_unifiedjobtemplate__unifiedjobtemplate_id_la_ad69a027_uniq UNIQUE (unifiedjobtemplate_id, label_id);


--
-- Name: main_unifiedjobtemplate_notification_templates_success main_unifiedjobtemplate__unifiedjobtemplate_id_no_113bd2d4_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_success
    ADD CONSTRAINT main_unifiedjobtemplate__unifiedjobtemplate_id_no_113bd2d4_uniq UNIQUE (unifiedjobtemplate_id, notificationtemplate_id);


--
-- Name: main_unifiedjobtemplate_notification_templates_error main_unifiedjobtemplate__unifiedjobtemplate_id_no_172864be_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_error
    ADD CONSTRAINT main_unifiedjobtemplate__unifiedjobtemplate_id_no_172864be_uniq UNIQUE (unifiedjobtemplate_id, notificationtemplate_id);


--
-- Name: main_unifiedjobtemplate_notification_templates_started main_unifiedjobtemplate__unifiedjobtemplate_id_no_5b15714c_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_started
    ADD CONSTRAINT main_unifiedjobtemplate__unifiedjobtemplate_id_no_5b15714c_uniq UNIQUE (unifiedjobtemplate_id, notificationtemplate_id);


--
-- Name: main_unifiedjobtemplate_credentials main_unifiedjobtemplate_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_credentials
    ADD CONSTRAINT main_unifiedjobtemplate_credentials_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjobtemplate_labels main_unifiedjobtemplate_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_labels
    ADD CONSTRAINT main_unifiedjobtemplate_labels_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjobtemplate_notification_templates_error main_unifiedjobtemplate_notification_templates_error_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_error
    ADD CONSTRAINT main_unifiedjobtemplate_notification_templates_error_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjobtemplate_notification_templates_started main_unifiedjobtemplate_notification_templates_started_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_started
    ADD CONSTRAINT main_unifiedjobtemplate_notification_templates_started_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjobtemplate_notification_templates_success main_unifiedjobtemplate_notification_templates_success_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_success
    ADD CONSTRAINT main_unifiedjobtemplate_notification_templates_success_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjobtemplate main_unifiedjobtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtemplate_pkey PRIMARY KEY (id);


--
-- Name: main_unifiedjobtemplateinstancegroupmembership main_unifiedjobtemplateinstancegroupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplateinstancegroupmembership
    ADD CONSTRAINT main_unifiedjobtemplateinstancegroupmembership_pkey PRIMARY KEY (id);


--
-- Name: main_usersessionmembership main_usersessionmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_usersessionmembership
    ADD CONSTRAINT main_usersessionmembership_pkey PRIMARY KEY (id);


--
-- Name: main_usersessionmembership main_usersessionmembership_session_id_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_usersessionmembership
    ADD CONSTRAINT main_usersessionmembership_session_id_key UNIQUE (session_id);


--
-- Name: main_workflowapproval main_workflowapproval_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowapproval
    ADD CONSTRAINT main_workflowapproval_pkey PRIMARY KEY (unifiedjob_ptr_id);


--
-- Name: main_workflowapprovaltemplate main_workflowapprovaltemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowapprovaltemplate
    ADD CONSTRAINT main_workflowapprovaltemplate_pkey PRIMARY KEY (unifiedjobtemplate_ptr_id);


--
-- Name: main_workflowjob main_workflowjob_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjob
    ADD CONSTRAINT main_workflowjob_pkey PRIMARY KEY (unifiedjob_ptr_id);


--
-- Name: main_workflowjobinstancegroupmembership main_workflowjobinstancegroupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobinstancegroupmembership
    ADD CONSTRAINT main_workflowjobinstancegroupmembership_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobnode_always_nodes main_workflowjobnode_alw_from_workflowjobnode_id__550e0051_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_always_nodes
    ADD CONSTRAINT main_workflowjobnode_alw_from_workflowjobnode_id__550e0051_uniq UNIQUE (from_workflowjobnode_id, to_workflowjobnode_id);


--
-- Name: main_workflowjobnode_always_nodes main_workflowjobnode_always_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_always_nodes
    ADD CONSTRAINT main_workflowjobnode_always_nodes_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobnode_credentials main_workflowjobnode_cre_workflowjobnode_id_crede_75628d2d_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_credentials
    ADD CONSTRAINT main_workflowjobnode_cre_workflowjobnode_id_crede_75628d2d_uniq UNIQUE (workflowjobnode_id, credential_id);


--
-- Name: main_workflowjobnode_credentials main_workflowjobnode_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_credentials
    ADD CONSTRAINT main_workflowjobnode_credentials_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobnode_failure_nodes main_workflowjobnode_fai_from_workflowjobnode_id__355631cb_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_failure_nodes
    ADD CONSTRAINT main_workflowjobnode_fai_from_workflowjobnode_id__355631cb_uniq UNIQUE (from_workflowjobnode_id, to_workflowjobnode_id);


--
-- Name: main_workflowjobnode_failure_nodes main_workflowjobnode_failure_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_failure_nodes
    ADD CONSTRAINT main_workflowjobnode_failure_nodes_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobnode main_workflowjobnode_job_id_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode
    ADD CONSTRAINT main_workflowjobnode_job_id_key UNIQUE (job_id);


--
-- Name: main_workflowjobnode_labels main_workflowjobnode_lab_workflowjobnode_id_label_f0763257_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_labels
    ADD CONSTRAINT main_workflowjobnode_lab_workflowjobnode_id_label_f0763257_uniq UNIQUE (workflowjobnode_id, label_id);


--
-- Name: main_workflowjobnode_labels main_workflowjobnode_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_labels
    ADD CONSTRAINT main_workflowjobnode_labels_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobnode main_workflowjobnode_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode
    ADD CONSTRAINT main_workflowjobnode_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobnode_success_nodes main_workflowjobnode_suc_from_workflowjobnode_id__59094efc_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_success_nodes
    ADD CONSTRAINT main_workflowjobnode_suc_from_workflowjobnode_id__59094efc_uniq UNIQUE (from_workflowjobnode_id, to_workflowjobnode_id);


--
-- Name: main_workflowjobnode_success_nodes main_workflowjobnode_success_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_success_nodes
    ADD CONSTRAINT main_workflowjobnode_success_nodes_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobnodebaseinstancegroupmembership main_workflowjobnodebaseinstancegroupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnodebaseinstancegroupmembership
    ADD CONSTRAINT main_workflowjobnodebaseinstancegroupmembership_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobtemplatenode_always_nodes main_workflowjobtemplate_from_workflowjobtemplate_01869c4a_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_always_nodes
    ADD CONSTRAINT main_workflowjobtemplate_from_workflowjobtemplate_01869c4a_uniq UNIQUE (from_workflowjobtemplatenode_id, to_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplatenode_failure_nodes main_workflowjobtemplate_from_workflowjobtemplate_5f970860_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_failure_nodes
    ADD CONSTRAINT main_workflowjobtemplate_from_workflowjobtemplate_5f970860_uniq UNIQUE (from_workflowjobtemplatenode_id, to_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplatenode_success_nodes main_workflowjobtemplate_from_workflowjobtemplate_b5f4a54a_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_success_nodes
    ADD CONSTRAINT main_workflowjobtemplate_from_workflowjobtemplate_b5f4a54a_uniq UNIQUE (from_workflowjobtemplatenode_id, to_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplatenode main_workflowjobtemplate_identifier_workflow_job__03484516_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode
    ADD CONSTRAINT main_workflowjobtemplate_identifier_workflow_job__03484516_uniq UNIQUE (identifier, workflow_job_template_id);


--
-- Name: main_workflowjobtemplate_notification_templates_approvals main_workflowjobtemplate_notification_templates_approvals_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate_notification_templates_approvals
    ADD CONSTRAINT main_workflowjobtemplate_notification_templates_approvals_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobtemplate main_workflowjobtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate
    ADD CONSTRAINT main_workflowjobtemplate_pkey PRIMARY KEY (unifiedjobtemplate_ptr_id);


--
-- Name: main_workflowjobtemplate_notification_templates_approvals main_workflowjobtemplate_workflowjobtemplate_id_n_4b1a7a0a_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate_notification_templates_approvals
    ADD CONSTRAINT main_workflowjobtemplate_workflowjobtemplate_id_n_4b1a7a0a_uniq UNIQUE (workflowjobtemplate_id, notificationtemplate_id);


--
-- Name: main_workflowjobtemplatenode_labels main_workflowjobtemplate_workflowjobtemplatenode__119dcc7c_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_labels
    ADD CONSTRAINT main_workflowjobtemplate_workflowjobtemplatenode__119dcc7c_uniq UNIQUE (workflowjobtemplatenode_id, label_id);


--
-- Name: main_workflowjobtemplatenode_credentials main_workflowjobtemplate_workflowjobtemplatenode__a6ba785b_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_credentials
    ADD CONSTRAINT main_workflowjobtemplate_workflowjobtemplatenode__a6ba785b_uniq UNIQUE (workflowjobtemplatenode_id, credential_id);


--
-- Name: main_workflowjobtemplatenode_always_nodes main_workflowjobtemplatenode_always_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_always_nodes
    ADD CONSTRAINT main_workflowjobtemplatenode_always_nodes_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobtemplatenode_credentials main_workflowjobtemplatenode_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_credentials
    ADD CONSTRAINT main_workflowjobtemplatenode_credentials_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobtemplatenode_failure_nodes main_workflowjobtemplatenode_failure_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_failure_nodes
    ADD CONSTRAINT main_workflowjobtemplatenode_failure_nodes_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobtemplatenode_labels main_workflowjobtemplatenode_labels_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_labels
    ADD CONSTRAINT main_workflowjobtemplatenode_labels_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobtemplatenode main_workflowjobtemplatenode_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode
    ADD CONSTRAINT main_workflowjobtemplatenode_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobtemplatenode_success_nodes main_workflowjobtemplatenode_success_nodes_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_success_nodes
    ADD CONSTRAINT main_workflowjobtemplatenode_success_nodes_pkey PRIMARY KEY (id);


--
-- Name: main_workflowjobtemplatenodebaseinstancegroupmembership main_workflowjobtemplatenodebaseinstancegroupmembership_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenodebaseinstancegroupmembership
    ADD CONSTRAINT main_workflowjobtemplatenodebaseinstancegroupmembership_pkey PRIMARY KEY (id);


--
-- Name: oauth2_provider_grant oauth2_provider_grant_code_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_grant
    ADD CONSTRAINT oauth2_provider_grant_code_key UNIQUE (code);


--
-- Name: oauth2_provider_grant oauth2_provider_grant_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_grant
    ADD CONSTRAINT oauth2_provider_grant_pkey PRIMARY KEY (id);


--
-- Name: oauth2_provider_idtoken oauth2_provider_idtoken_jti_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_idtoken
    ADD CONSTRAINT oauth2_provider_idtoken_jti_key UNIQUE (jti);


--
-- Name: oauth2_provider_idtoken oauth2_provider_idtoken_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_idtoken
    ADD CONSTRAINT oauth2_provider_idtoken_pkey PRIMARY KEY (id);


--
-- Name: oauth2_provider_refreshtoken oauth2_provider_refreshtoken_access_token_id_key; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_refreshtoken
    ADD CONSTRAINT oauth2_provider_refreshtoken_access_token_id_key UNIQUE (access_token_id);


--
-- Name: oauth2_provider_refreshtoken oauth2_provider_refreshtoken_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_refreshtoken
    ADD CONSTRAINT oauth2_provider_refreshtoken_pkey PRIMARY KEY (id);


--
-- Name: oauth2_provider_refreshtoken oauth2_provider_refreshtoken_token_revoked_af8a5134_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_refreshtoken
    ADD CONSTRAINT oauth2_provider_refreshtoken_token_revoked_af8a5134_uniq UNIQUE (token, revoked);


--
-- Name: social_auth_association social_auth_association_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_association
    ADD CONSTRAINT social_auth_association_pkey PRIMARY KEY (id);


--
-- Name: social_auth_association social_auth_association_server_url_handle_078befa2_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_association
    ADD CONSTRAINT social_auth_association_server_url_handle_078befa2_uniq UNIQUE (server_url, handle);


--
-- Name: social_auth_code social_auth_code_email_code_801b2d02_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_code
    ADD CONSTRAINT social_auth_code_email_code_801b2d02_uniq UNIQUE (email, code);


--
-- Name: social_auth_code social_auth_code_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_code
    ADD CONSTRAINT social_auth_code_pkey PRIMARY KEY (id);


--
-- Name: social_auth_nonce social_auth_nonce_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_nonce
    ADD CONSTRAINT social_auth_nonce_pkey PRIMARY KEY (id);


--
-- Name: social_auth_nonce social_auth_nonce_server_url_timestamp_salt_f6284463_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_nonce
    ADD CONSTRAINT social_auth_nonce_server_url_timestamp_salt_f6284463_uniq UNIQUE (server_url, "timestamp", salt);


--
-- Name: social_auth_partial social_auth_partial_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_partial
    ADD CONSTRAINT social_auth_partial_pkey PRIMARY KEY (id);


--
-- Name: social_auth_usersocialauth social_auth_usersocialauth_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_usersocialauth
    ADD CONSTRAINT social_auth_usersocialauth_pkey PRIMARY KEY (id);


--
-- Name: social_auth_usersocialauth social_auth_usersocialauth_provider_uid_e6b5e668_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_usersocialauth
    ADD CONSTRAINT social_auth_usersocialauth_provider_uid_e6b5e668_uniq UNIQUE (provider, uid);


--
-- Name: sso_userenterpriseauth sso_userenterpriseauth_pkey; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.sso_userenterpriseauth
    ADD CONSTRAINT sso_userenterpriseauth_pkey PRIMARY KEY (id);


--
-- Name: sso_userenterpriseauth sso_userenterpriseauth_user_id_provider_baa07d7f_uniq; Type: CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.sso_userenterpriseauth
    ADD CONSTRAINT sso_userenterpriseauth_user_id_provider_baa07d7f_uniq UNIQUE (user_id, provider);


--
-- Name: auth_group_name_a6ea08ec_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_group_name_a6ea08ec_like ON public.auth_group USING btree (name varchar_pattern_ops);


--
-- Name: auth_group_permissions_group_id_b120cbf9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_group_permissions_group_id_b120cbf9 ON public.auth_group_permissions USING btree (group_id);


--
-- Name: auth_group_permissions_permission_id_84c5c92e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_group_permissions_permission_id_84c5c92e ON public.auth_group_permissions USING btree (permission_id);


--
-- Name: auth_permission_content_type_id_2f476e4b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_permission_content_type_id_2f476e4b ON public.auth_permission USING btree (content_type_id);


--
-- Name: auth_user_groups_group_id_97559544; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_user_groups_group_id_97559544 ON public.auth_user_groups USING btree (group_id);


--
-- Name: auth_user_groups_user_id_6a12ed8b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_user_groups_user_id_6a12ed8b ON public.auth_user_groups USING btree (user_id);


--
-- Name: auth_user_user_permissions_permission_id_1fbb5f2c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_user_user_permissions_permission_id_1fbb5f2c ON public.auth_user_user_permissions USING btree (permission_id);


--
-- Name: auth_user_user_permissions_user_id_a95ead1b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_user_user_permissions_user_id_a95ead1b ON public.auth_user_user_permissions USING btree (user_id);


--
-- Name: auth_user_username_6821ab7c_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX auth_user_username_6821ab7c_like ON public.auth_user USING btree (username varchar_pattern_ops);


--
-- Name: conf_setting_user_id_ce9d5138; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX conf_setting_user_id_ce9d5138 ON public.conf_setting USING btree (user_id);


--
-- Name: django_session_expire_date_a5c62663; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX django_session_expire_date_a5c62663 ON public.django_session USING btree (expire_date);


--
-- Name: django_session_session_key_c0390e0f_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX django_session_session_key_c0390e0f_like ON public.django_session USING btree (session_key varchar_pattern_ops);


--
-- Name: django_site_domain_a2e37b91_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX django_site_domain_a2e37b91_like ON public.django_site USING btree (domain varchar_pattern_ops);


--
-- Name: host_ansible_facts_default_gin; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX host_ansible_facts_default_gin ON public.main_host USING gin (ansible_facts jsonb_path_ops);


--
-- Name: main_activitystream_actor_id_29aafc0f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_actor_id_29aafc0f ON public.main_activitystream USING btree (actor_id);


--
-- Name: main_activitystream_ad_hoc_command_activitystream_id_870ddb01; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_ad_hoc_command_activitystream_id_870ddb01 ON public.main_activitystream_ad_hoc_command USING btree (activitystream_id);


--
-- Name: main_activitystream_ad_hoc_command_adhoccommand_id_0df7bfcd; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_ad_hoc_command_adhoccommand_id_0df7bfcd ON public.main_activitystream_ad_hoc_command USING btree (adhoccommand_id);


--
-- Name: main_activitystream_credential_activitystream_id_4be1a957; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_credential_activitystream_id_4be1a957 ON public.main_activitystream_credential USING btree (activitystream_id);


--
-- Name: main_activitystream_credential_credential_id_d5911596; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_credential_credential_id_d5911596 ON public.main_activitystream_credential USING btree (credential_id);


--
-- Name: main_activitystream_credential_type_activitystream_id_b7a4b49d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_credential_type_activitystream_id_b7a4b49d ON public.main_activitystream_credential_type USING btree (activitystream_id);


--
-- Name: main_activitystream_credential_type_credentialtype_id_89572b10; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_credential_type_credentialtype_id_89572b10 ON public.main_activitystream_credential_type USING btree (credentialtype_id);


--
-- Name: main_activitystream_execut_activitystream_id_4938d427; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_execut_activitystream_id_4938d427 ON public.main_activitystream_execution_environment USING btree (activitystream_id);


--
-- Name: main_activitystream_execut_executionenvironment_id_b455fc65; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_execut_executionenvironment_id_b455fc65 ON public.main_activitystream_execution_environment USING btree (executionenvironment_id);


--
-- Name: main_activitystream_group_activitystream_id_94d31559; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_group_activitystream_id_94d31559 ON public.main_activitystream_group USING btree (activitystream_id);


--
-- Name: main_activitystream_group_group_id_fd48b400; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_group_group_id_fd48b400 ON public.main_activitystream_group USING btree (group_id);


--
-- Name: main_activitystream_host_activitystream_id_c4d91cb7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_host_activitystream_id_c4d91cb7 ON public.main_activitystream_host USING btree (activitystream_id);


--
-- Name: main_activitystream_host_host_id_0e598602; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_host_host_id_0e598602 ON public.main_activitystream_host USING btree (host_id);


--
-- Name: main_activitystream_instance_activitystream_id_04ccbf32; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_instance_activitystream_id_04ccbf32 ON public.main_activitystream_instance USING btree (activitystream_id);


--
-- Name: main_activitystream_instance_group_activitystream_id_e81ef38a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_instance_group_activitystream_id_e81ef38a ON public.main_activitystream_instance_group USING btree (activitystream_id);


--
-- Name: main_activitystream_instance_group_instancegroup_id_fca49f6c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_instance_group_instancegroup_id_fca49f6c ON public.main_activitystream_instance_group USING btree (instancegroup_id);


--
-- Name: main_activitystream_instance_instance_id_d10eb669; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_instance_instance_id_d10eb669 ON public.main_activitystream_instance USING btree (instance_id);


--
-- Name: main_activitystream_invent_inventorysource_id_235e699a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_invent_inventorysource_id_235e699a ON public.main_activitystream_inventory_source USING btree (inventorysource_id);


--
-- Name: main_activitystream_invent_inventoryupdate_id_817749c5; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_invent_inventoryupdate_id_817749c5 ON public.main_activitystream_inventory_update USING btree (inventoryupdate_id);


--
-- Name: main_activitystream_inventory_activitystream_id_4a1242eb; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_inventory_activitystream_id_4a1242eb ON public.main_activitystream_inventory USING btree (activitystream_id);


--
-- Name: main_activitystream_inventory_inventory_id_8daf9251; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_inventory_inventory_id_8daf9251 ON public.main_activitystream_inventory USING btree (inventory_id);


--
-- Name: main_activitystream_inventory_source_activitystream_id_d88c8423; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_inventory_source_activitystream_id_d88c8423 ON public.main_activitystream_inventory_source USING btree (activitystream_id);


--
-- Name: main_activitystream_inventory_update_activitystream_id_732f074a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_inventory_update_activitystream_id_732f074a ON public.main_activitystream_inventory_update USING btree (activitystream_id);


--
-- Name: main_activitystream_job_activitystream_id_b1f2ab1b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_job_activitystream_id_b1f2ab1b ON public.main_activitystream_job USING btree (activitystream_id);


--
-- Name: main_activitystream_job_job_id_aa6811b5; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_job_job_id_aa6811b5 ON public.main_activitystream_job USING btree (job_id);


--
-- Name: main_activitystream_job_template_activitystream_id_abd63b6d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_job_template_activitystream_id_abd63b6d ON public.main_activitystream_job_template USING btree (activitystream_id);


--
-- Name: main_activitystream_job_template_jobtemplate_id_c05e0b6c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_job_template_jobtemplate_id_c05e0b6c ON public.main_activitystream_job_template USING btree (jobtemplate_id);


--
-- Name: main_activitystream_label_activitystream_id_afd608d7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_label_activitystream_id_afd608d7 ON public.main_activitystream_label USING btree (activitystream_id);


--
-- Name: main_activitystream_label_label_id_b33683fb; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_label_label_id_b33683fb ON public.main_activitystream_label USING btree (label_id);


--
-- Name: main_activitystream_notifi_activitystream_id_214c1789; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_notifi_activitystream_id_214c1789 ON public.main_activitystream_notification_template USING btree (activitystream_id);


--
-- Name: main_activitystream_notifi_notificationtemplate_id_96d11a5d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_notifi_notificationtemplate_id_96d11a5d ON public.main_activitystream_notification_template USING btree (notificationtemplate_id);


--
-- Name: main_activitystream_notification_activitystream_id_7d39234a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_notification_activitystream_id_7d39234a ON public.main_activitystream_notification USING btree (activitystream_id);


--
-- Name: main_activitystream_notification_notification_id_bbfaa8ac; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_notification_notification_id_bbfaa8ac ON public.main_activitystream_notification USING btree (notification_id);


--
-- Name: main_activitystream_o_auth_activitystream_id_9cd33ed4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_o_auth_activitystream_id_9cd33ed4 ON public.main_activitystream_o_auth2_access_token USING btree (activitystream_id);


--
-- Name: main_activitystream_o_auth_activitystream_id_ea629ffe; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_o_auth_activitystream_id_ea629ffe ON public.main_activitystream_o_auth2_application USING btree (activitystream_id);


--
-- Name: main_activitystream_o_auth_oauth2accesstoken_id_76c333c8; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_o_auth_oauth2accesstoken_id_76c333c8 ON public.main_activitystream_o_auth2_access_token USING btree (oauth2accesstoken_id);


--
-- Name: main_activitystream_o_auth_oauth2application_id_23726fd8; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_o_auth_oauth2application_id_23726fd8 ON public.main_activitystream_o_auth2_application USING btree (oauth2application_id);


--
-- Name: main_activitystream_organization_activitystream_id_0283e075; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_organization_activitystream_id_0283e075 ON public.main_activitystream_organization USING btree (activitystream_id);


--
-- Name: main_activitystream_organization_organization_id_8ccdfd12; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_organization_organization_id_8ccdfd12 ON public.main_activitystream_organization USING btree (organization_id);


--
-- Name: main_activitystream_project_activitystream_id_f6aa28cc; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_project_activitystream_id_f6aa28cc ON public.main_activitystream_project USING btree (activitystream_id);


--
-- Name: main_activitystream_project_project_id_836f7b93; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_project_project_id_836f7b93 ON public.main_activitystream_project USING btree (project_id);


--
-- Name: main_activitystream_project_update_activitystream_id_2965eda0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_project_update_activitystream_id_2965eda0 ON public.main_activitystream_project_update USING btree (activitystream_id);


--
-- Name: main_activitystream_project_update_projectupdate_id_8ac4ba92; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_project_update_projectupdate_id_8ac4ba92 ON public.main_activitystream_project_update USING btree (projectupdate_id);


--
-- Name: main_activitystream_role_activitystream_id_d591eb98; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_role_activitystream_id_d591eb98 ON public.main_activitystream_role USING btree (activitystream_id);


--
-- Name: main_activitystream_role_role_id_e19fce37; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_role_role_id_e19fce37 ON public.main_activitystream_role USING btree (role_id);


--
-- Name: main_activitystream_schedule_activitystream_id_a5fd87ef; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_schedule_activitystream_id_a5fd87ef ON public.main_activitystream_schedule USING btree (activitystream_id);


--
-- Name: main_activitystream_schedule_schedule_id_9bde99e8; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_schedule_schedule_id_9bde99e8 ON public.main_activitystream_schedule USING btree (schedule_id);


--
-- Name: main_activitystream_team_activitystream_id_c4874e73; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_team_activitystream_id_c4874e73 ON public.main_activitystream_team USING btree (activitystream_id);


--
-- Name: main_activitystream_team_team_id_725f033a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_team_team_id_725f033a ON public.main_activitystream_team USING btree (team_id);


--
-- Name: main_activitystream_unifie_activitystream_id_e4ce5d15; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_unifie_activitystream_id_e4ce5d15 ON public.main_activitystream_unified_job_template USING btree (activitystream_id);


--
-- Name: main_activitystream_unifie_unifiedjobtemplate_id_71f8a21f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_unifie_unifiedjobtemplate_id_71f8a21f ON public.main_activitystream_unified_job_template USING btree (unifiedjobtemplate_id);


--
-- Name: main_activitystream_unified_job_activitystream_id_e29d497f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_unified_job_activitystream_id_e29d497f ON public.main_activitystream_unified_job USING btree (activitystream_id);


--
-- Name: main_activitystream_unified_job_unifiedjob_id_bd9f07c6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_unified_job_unifiedjob_id_bd9f07c6 ON public.main_activitystream_unified_job USING btree (unifiedjob_id);


--
-- Name: main_activitystream_user_activitystream_id_f120c9d1; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_user_activitystream_id_f120c9d1 ON public.main_activitystream_user USING btree (activitystream_id);


--
-- Name: main_activitystream_user_user_id_435f8320; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_user_user_id_435f8320 ON public.main_activitystream_user USING btree (user_id);


--
-- Name: main_activitystream_workfl_activitystream_id_14401444; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_activitystream_id_14401444 ON public.main_activitystream_workflow_approval USING btree (activitystream_id);


--
-- Name: main_activitystream_workfl_activitystream_id_259ad363; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_activitystream_id_259ad363 ON public.main_activitystream_workflow_job_template USING btree (activitystream_id);


--
-- Name: main_activitystream_workfl_activitystream_id_7e8e02aa; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_activitystream_id_7e8e02aa ON public.main_activitystream_workflow_approval_template USING btree (activitystream_id);


--
-- Name: main_activitystream_workfl_activitystream_id_b3d1beb6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_activitystream_id_b3d1beb6 ON public.main_activitystream_workflow_job_template_node USING btree (activitystream_id);


--
-- Name: main_activitystream_workfl_activitystream_id_c8397668; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_activitystream_id_c8397668 ON public.main_activitystream_workflow_job_node USING btree (activitystream_id);


--
-- Name: main_activitystream_workfl_workflowapproval_id_8d4193a7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_workflowapproval_id_8d4193a7 ON public.main_activitystream_workflow_approval USING btree (workflowapproval_id);


--
-- Name: main_activitystream_workfl_workflowapprovaltemplate_i_93e9e097; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_workflowapprovaltemplate_i_93e9e097 ON public.main_activitystream_workflow_approval_template USING btree (workflowapprovaltemplate_id);


--
-- Name: main_activitystream_workfl_workflowjobnode_id_85bb51d6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_workflowjobnode_id_85bb51d6 ON public.main_activitystream_workflow_job_node USING btree (workflowjobnode_id);


--
-- Name: main_activitystream_workfl_workflowjobtemplate_id_efd4c1aa; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_workflowjobtemplate_id_efd4c1aa ON public.main_activitystream_workflow_job_template USING btree (workflowjobtemplate_id);


--
-- Name: main_activitystream_workfl_workflowjobtemplatenode_id_a2630ab6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workfl_workflowjobtemplatenode_id_a2630ab6 ON public.main_activitystream_workflow_job_template_node USING btree (workflowjobtemplatenode_id);


--
-- Name: main_activitystream_workflow_job_activitystream_id_93d66e38; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workflow_job_activitystream_id_93d66e38 ON public.main_activitystream_workflow_job USING btree (activitystream_id);


--
-- Name: main_activitystream_workflow_job_workflowjob_id_c29366d7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_activitystream_workflow_job_workflowjob_id_c29366d7 ON public.main_activitystream_workflow_job USING btree (workflowjob_id);


--
-- Name: main_adhocc_ad_hoc__1e4d24_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhocc_ad_hoc__1e4d24_idx ON ONLY public.main_adhoccommandevent USING btree (ad_hoc_command_id, job_created, uuid);


--
-- Name: main_adhocc_ad_hoc__a57777_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhocc_ad_hoc__a57777_idx ON ONLY public.main_adhoccommandevent USING btree (ad_hoc_command_id, job_created, counter);


--
-- Name: main_adhocc_ad_hoc__e72142_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhocc_ad_hoc__e72142_idx ON ONLY public.main_adhoccommandevent USING btree (ad_hoc_command_id, job_created, event);


--
-- Name: main_adhoccommand_credential_id_da6b1c87; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommand_credential_id_da6b1c87 ON public.main_adhoccommand USING btree (credential_id);


--
-- Name: main_adhoccommand_inventory_id_b29bba0e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommand_inventory_id_b29bba0e ON public.main_adhoccommand USING btree (inventory_id);


--
-- Name: main_adhoccommandevent_ad_hoc_command_id_1721f1e2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommandevent_ad_hoc_command_id_1721f1e2 ON public._unpartitioned_main_adhoccommandevent USING btree (ad_hoc_command_id);


--
-- Name: main_adhoccommandevent_ad_hoc_command_id_end_line_f08bd1b4_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommandevent_ad_hoc_command_id_end_line_f08bd1b4_idx ON public._unpartitioned_main_adhoccommandevent USING btree (ad_hoc_command_id, end_line);


--
-- Name: main_adhoccommandevent_ad_hoc_command_id_event_85c463e3_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommandevent_ad_hoc_command_id_event_85c463e3_idx ON public._unpartitioned_main_adhoccommandevent USING btree (ad_hoc_command_id, event);


--
-- Name: main_adhoccommandevent_ad_hoc_command_id_start__6e575dd7_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommandevent_ad_hoc_command_id_start__6e575dd7_idx ON public._unpartitioned_main_adhoccommandevent USING btree (ad_hoc_command_id, start_line);


--
-- Name: main_adhoccommandevent_ad_hoc_command_id_uuid_f1fab1c8_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommandevent_ad_hoc_command_id_uuid_f1fab1c8_idx ON public._unpartitioned_main_adhoccommandevent USING btree (ad_hoc_command_id, uuid);


--
-- Name: main_adhoccommandevent_host_id_5613e329; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommandevent_host_id_5613e329 ON public._unpartitioned_main_adhoccommandevent USING btree (host_id);


--
-- Name: main_adhoccommandevent_host_id_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommandevent_host_id_idx ON ONLY public.main_adhoccommandevent USING btree (host_id);


--
-- Name: main_adhoccommandevent_modified_3e4ee2db; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_adhoccommandevent_modified_3e4ee2db ON ONLY public.main_adhoccommandevent USING btree (modified);


--
-- Name: main_credential_admin_role_id_6cd7ab86; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credential_admin_role_id_6cd7ab86 ON public.main_credential USING btree (admin_role_id);


--
-- Name: main_credential_created_by_id_237add04; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credential_created_by_id_237add04 ON public.main_credential USING btree (created_by_id);


--
-- Name: main_credential_credential_type_id_0120654c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credential_credential_type_id_0120654c ON public.main_credential USING btree (credential_type_id);


--
-- Name: main_credential_modified_by_id_c290955a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credential_modified_by_id_c290955a ON public.main_credential USING btree (modified_by_id);


--
-- Name: main_credential_organization_id_18d4ae89; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credential_organization_id_18d4ae89 ON public.main_credential USING btree (organization_id);


--
-- Name: main_credential_read_role_id_12be41a2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credential_read_role_id_12be41a2 ON public.main_credential USING btree (read_role_id);


--
-- Name: main_credential_use_role_id_122159d4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credential_use_role_id_122159d4 ON public.main_credential USING btree (use_role_id);


--
-- Name: main_credentialinputsource_created_by_id_d2dc637c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credentialinputsource_created_by_id_d2dc637c ON public.main_credentialinputsource USING btree (created_by_id);


--
-- Name: main_credentialinputsource_modified_by_id_e3fd88dd; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credentialinputsource_modified_by_id_e3fd88dd ON public.main_credentialinputsource USING btree (modified_by_id);


--
-- Name: main_credentialinputsource_source_credential_id_868d93af; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credentialinputsource_source_credential_id_868d93af ON public.main_credentialinputsource USING btree (source_credential_id);


--
-- Name: main_credentialinputsource_target_credential_id_4bf0e248; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credentialinputsource_target_credential_id_4bf0e248 ON public.main_credentialinputsource USING btree (target_credential_id);


--
-- Name: main_credentialtype_created_by_id_0f8451ed; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credentialtype_created_by_id_0f8451ed ON public.main_credentialtype USING btree (created_by_id);


--
-- Name: main_credentialtype_modified_by_id_b425580d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_credentialtype_modified_by_id_b425580d ON public.main_credentialtype USING btree (modified_by_id);


--
-- Name: main_custominventoryscript_created_by_id_45a39526; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_custominventoryscript_created_by_id_45a39526 ON public.main_custominventoryscript USING btree (created_by_id);


--
-- Name: main_custominventoryscript_modified_by_id_6c74f1d0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_custominventoryscript_modified_by_id_6c74f1d0 ON public.main_custominventoryscript USING btree (modified_by_id);


--
-- Name: main_executionenvironment_created_by_id_3808c16f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_executionenvironment_created_by_id_3808c16f ON public.main_executionenvironment USING btree (created_by_id);


--
-- Name: main_executionenvironment_credential_id_e91204b4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_executionenvironment_credential_id_e91204b4 ON public.main_executionenvironment USING btree (credential_id);


--
-- Name: main_executionenvironment_modified_by_id_fa58a43d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_executionenvironment_modified_by_id_fa58a43d ON public.main_executionenvironment USING btree (modified_by_id);


--
-- Name: main_executionenvironment_name_c1ad78d0_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_executionenvironment_name_c1ad78d0_like ON public.main_executionenvironment USING btree (name varchar_pattern_ops);


--
-- Name: main_executionenvironment_organization_id_66056df5; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_executionenvironment_organization_id_66056df5 ON public.main_executionenvironment USING btree (organization_id);


--
-- Name: main_group_created_by_id_326129d5; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_created_by_id_326129d5 ON public.main_group USING btree (created_by_id);


--
-- Name: main_group_hosts_group_id_524c3b29; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_hosts_group_id_524c3b29 ON public.main_group_hosts USING btree (group_id);


--
-- Name: main_group_hosts_host_id_672eaed0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_hosts_host_id_672eaed0 ON public.main_group_hosts USING btree (host_id);


--
-- Name: main_group_inventory_id_f9e83725; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_inventory_id_f9e83725 ON public.main_group USING btree (inventory_id);


--
-- Name: main_group_inventory_sources_group_id_1be295c4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_inventory_sources_group_id_1be295c4 ON public.main_group_inventory_sources USING btree (group_id);


--
-- Name: main_group_inventory_sources_inventorysource_id_5da14efc; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_inventory_sources_inventorysource_id_5da14efc ON public.main_group_inventory_sources USING btree (inventorysource_id);


--
-- Name: main_group_modified_by_id_20a1b654; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_modified_by_id_20a1b654 ON public.main_group USING btree (modified_by_id);


--
-- Name: main_group_parents_from_group_id_9d63324d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_parents_from_group_id_9d63324d ON public.main_group_parents USING btree (from_group_id);


--
-- Name: main_group_parents_to_group_id_851cc1ce; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_group_parents_to_group_id_851cc1ce ON public.main_group_parents USING btree (to_group_id);


--
-- Name: main_host_created_by_id_2b5e0abe; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_host_created_by_id_2b5e0abe ON public.main_host USING btree (created_by_id);


--
-- Name: main_host_inventory_id_e5bcdb08; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_host_inventory_id_e5bcdb08 ON public.main_host USING btree (inventory_id);


--
-- Name: main_host_inventory_sources_host_id_03f0dcdc; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_host_inventory_sources_host_id_03f0dcdc ON public.main_host_inventory_sources USING btree (host_id);


--
-- Name: main_host_inventory_sources_inventorysource_id_b25d3959; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_host_inventory_sources_inventorysource_id_b25d3959 ON public.main_host_inventory_sources USING btree (inventorysource_id);


--
-- Name: main_host_last_job_host_summary_id_b8bd727d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_host_last_job_host_summary_id_b8bd727d ON public.main_host USING btree (last_job_host_summary_id);


--
-- Name: main_host_last_job_id_d247075b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_host_last_job_id_d247075b ON public.main_host USING btree (last_job_id);


--
-- Name: main_host_modified_by_id_28b76283; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_host_modified_by_id_28b76283 ON public.main_host USING btree (modified_by_id);


--
-- Name: main_hostmetric_first_automation_b747a0a1; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_hostmetric_first_automation_b747a0a1 ON public.main_hostmetric USING btree (first_automation);


--
-- Name: main_hostmetric_hostname_87ac3c1f_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_hostmetric_hostname_87ac3c1f_like ON public.main_hostmetric USING btree (hostname varchar_pattern_ops);


--
-- Name: main_hostmetric_last_automation_11010683; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_hostmetric_last_automation_11010683 ON public.main_hostmetric USING btree (last_automation);


--
-- Name: main_hostmetric_last_deleted_d8249820; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_hostmetric_last_deleted_d8249820 ON public.main_hostmetric USING btree (last_deleted);


--
-- Name: main_instance_hostname_f2698dae_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instance_hostname_f2698dae_like ON public.main_instance USING btree (hostname varchar_pattern_ops);


--
-- Name: main_instance_ip_address_28190262_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instance_ip_address_28190262_like ON public.main_instance USING btree (ip_address varchar_pattern_ops);


--
-- Name: main_instancegroup_admin_role_id_03760535; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancegroup_admin_role_id_03760535 ON public.main_instancegroup USING btree (admin_role_id);


--
-- Name: main_instancegroup_credential_id_98351d10; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancegroup_credential_id_98351d10 ON public.main_instancegroup USING btree (credential_id);


--
-- Name: main_instancegroup_instances_instance_id_d41cb05c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancegroup_instances_instance_id_d41cb05c ON public.main_instancegroup_instances USING btree (instance_id);


--
-- Name: main_instancegroup_instances_instancegroup_id_b4b19635; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancegroup_instances_instancegroup_id_b4b19635 ON public.main_instancegroup_instances USING btree (instancegroup_id);


--
-- Name: main_instancegroup_name_bde73070_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancegroup_name_bde73070_like ON public.main_instancegroup USING btree (name varchar_pattern_ops);


--
-- Name: main_instancegroup_read_role_id_139c801e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancegroup_read_role_id_139c801e ON public.main_instancegroup USING btree (read_role_id);


--
-- Name: main_instancegroup_use_role_id_48ea7ecc; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancegroup_use_role_id_48ea7ecc ON public.main_instancegroup USING btree (use_role_id);


--
-- Name: main_instancelink_source_id_29f35cad; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancelink_source_id_29f35cad ON public.main_instancelink USING btree (source_id);


--
-- Name: main_instancelink_target_id_0ee650b4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_instancelink_target_id_0ee650b4 ON public.main_instancelink USING btree (target_id);


--
-- Name: main_invent_invento_364dcb_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_invent_invento_364dcb_idx ON ONLY public.main_inventoryupdateevent USING btree (inventory_update_id, job_created, counter);


--
-- Name: main_invent_invento_f72b21_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_invent_invento_f72b21_idx ON ONLY public.main_inventoryupdateevent USING btree (inventory_update_id, job_created, uuid);


--
-- Name: main_inventory_adhoc_role_id_b57042aa; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_adhoc_role_id_b57042aa ON public.main_inventory USING btree (adhoc_role_id);


--
-- Name: main_inventory_admin_role_id_3bb301cb; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_admin_role_id_3bb301cb ON public.main_inventory USING btree (admin_role_id);


--
-- Name: main_inventory_created_by_id_5d690781; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_created_by_id_5d690781 ON public.main_inventory USING btree (created_by_id);


--
-- Name: main_inventory_labels_inventory_id_3c7ecb7a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_labels_inventory_id_3c7ecb7a ON public.main_inventory_labels USING btree (inventory_id);


--
-- Name: main_inventory_labels_label_id_0ab1cd80; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_labels_label_id_0ab1cd80 ON public.main_inventory_labels USING btree (label_id);


--
-- Name: main_inventory_modified_by_id_a4a91734; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_modified_by_id_a4a91734 ON public.main_inventory USING btree (modified_by_id);


--
-- Name: main_inventory_organization_id_3ee77ea9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_organization_id_3ee77ea9 ON public.main_inventory USING btree (organization_id);


--
-- Name: main_inventory_read_role_id_270dd070; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_read_role_id_270dd070 ON public.main_inventory USING btree (read_role_id);


--
-- Name: main_inventory_update_role_id_be0903a1; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_update_role_id_be0903a1 ON public.main_inventory USING btree (update_role_id);


--
-- Name: main_inventory_use_role_id_77407b26; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventory_use_role_id_77407b26 ON public.main_inventory USING btree (use_role_id);


--
-- Name: main_inventoryconstructedi_constructed_inventory_id_7f494472; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryconstructedi_constructed_inventory_id_7f494472 ON public.main_inventoryconstructedinventorymembership USING btree (constructed_inventory_id);


--
-- Name: main_inventoryconstructedi_input_inventory_id_fc428cbb; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryconstructedi_input_inventory_id_fc428cbb ON public.main_inventoryconstructedinventorymembership USING btree (input_inventory_id);


--
-- Name: main_inventoryconstructedinventorymembership_position_7d2caaa0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryconstructedinventorymembership_position_7d2caaa0 ON public.main_inventoryconstructedinventorymembership USING btree ("position");


--
-- Name: main_inventoryinstancegroupmembership_instancegroup_id_8c752e87; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryinstancegroupmembership_instancegroup_id_8c752e87 ON public.main_inventoryinstancegroupmembership USING btree (instancegroup_id);


--
-- Name: main_inventoryinstancegroupmembership_inventory_id_76a877b6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryinstancegroupmembership_inventory_id_76a877b6 ON public.main_inventoryinstancegroupmembership USING btree (inventory_id);


--
-- Name: main_inventoryinstancegroupmembership_position_f7487717; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryinstancegroupmembership_position_f7487717 ON public.main_inventoryinstancegroupmembership USING btree ("position");


--
-- Name: main_inventorysource_inventory_id_3c1cac19; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventorysource_inventory_id_3c1cac19 ON public.main_inventorysource USING btree (inventory_id);


--
-- Name: main_inventorysource_source_project_id_5b9c4374; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventorysource_source_project_id_5b9c4374 ON public.main_inventorysource USING btree (source_project_id);


--
-- Name: main_inventoryupdate_inventory_id_e60f1f2e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryupdate_inventory_id_e60f1f2e ON public.main_inventoryupdate USING btree (inventory_id);


--
-- Name: main_inventoryupdate_inventory_source_id_bc4b2567; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryupdate_inventory_source_id_bc4b2567 ON public.main_inventoryupdate USING btree (inventory_source_id);


--
-- Name: main_inventoryupdate_source_project_update_id_b896d555; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryupdate_source_project_update_id_b896d555 ON public.main_inventoryupdate USING btree (source_project_update_id);


--
-- Name: main_inventoryupdateeven_inventory_update_id_end__da3bcc42_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryupdateeven_inventory_update_id_end__da3bcc42_idx ON public._unpartitioned_main_inventoryupdateevent USING btree (inventory_update_id, end_line);


--
-- Name: main_inventoryupdateeven_inventory_update_id_star_ee7580ed_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryupdateeven_inventory_update_id_star_ee7580ed_idx ON public._unpartitioned_main_inventoryupdateevent USING btree (inventory_update_id, start_line);


--
-- Name: main_inventoryupdateevent_inventory_update_id_8974f1f7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryupdateevent_inventory_update_id_8974f1f7 ON public._unpartitioned_main_inventoryupdateevent USING btree (inventory_update_id);


--
-- Name: main_inventoryupdateevent_inventory_update_id_uuid_c45a56f6_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryupdateevent_inventory_update_id_uuid_c45a56f6_idx ON public._unpartitioned_main_inventoryupdateevent USING btree (inventory_update_id, uuid);


--
-- Name: main_inventoryupdateevent_modified_e8e6da8b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_inventoryupdateevent_modified_e8e6da8b ON ONLY public.main_inventoryupdateevent USING btree (modified);


--
-- Name: main_job_inventory_id_1b436658; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_job_inventory_id_1b436658 ON public.main_job USING btree (inventory_id);


--
-- Name: main_job_job_template_id_070b0d56; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_job_job_template_id_070b0d56 ON public.main_job USING btree (job_template_id);


--
-- Name: main_job_project_id_a8f63894; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_job_project_id_a8f63894 ON public.main_job USING btree (project_id);


--
-- Name: main_job_project_update_id_5adf90ad; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_job_project_update_id_5adf90ad ON public.main_job USING btree (project_update_id);


--
-- Name: main_job_webhook_credential_id_40ca94fa; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_job_webhook_credential_id_40ca94fa ON public.main_job USING btree (webhook_credential_id);


--
-- Name: main_jobeve_job_id_0ddc6b_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobeve_job_id_0ddc6b_idx ON ONLY public.main_jobevent USING btree (job_id, job_created, event);


--
-- Name: main_jobeve_job_id_3c4a4a_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobeve_job_id_3c4a4a_idx ON ONLY public.main_jobevent USING btree (job_id, job_created, uuid);


--
-- Name: main_jobeve_job_id_40a56d_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobeve_job_id_40a56d_idx ON ONLY public.main_jobevent USING btree (job_id, job_created, parent_uuid);


--
-- Name: main_jobeve_job_id_51c382_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobeve_job_id_51c382_idx ON ONLY public.main_jobevent USING btree (job_id, job_created, counter);


--
-- Name: main_jobevent_created_1976e874; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_created_1976e874 ON public._unpartitioned_main_jobevent USING btree (created);


--
-- Name: main_jobevent_host_id_b03b6059; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_host_id_b03b6059 ON public._unpartitioned_main_jobevent USING btree (host_id);


--
-- Name: main_jobevent_host_id_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_host_id_idx ON ONLY public.main_jobevent USING btree (host_id);


--
-- Name: main_jobevent_job_id_571587e8; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_job_id_571587e8 ON public._unpartitioned_main_jobevent USING btree (job_id);


--
-- Name: main_jobevent_job_id_end_line_18215490_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_job_id_end_line_18215490_idx ON public._unpartitioned_main_jobevent USING btree (job_id, end_line);


--
-- Name: main_jobevent_job_id_event_dc5f44fe_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_job_id_event_dc5f44fe_idx ON public._unpartitioned_main_jobevent USING btree (job_id, event);


--
-- Name: main_jobevent_job_id_parent_uuid_8de74312_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_job_id_parent_uuid_8de74312_idx ON public._unpartitioned_main_jobevent USING btree (job_id, parent_uuid);


--
-- Name: main_jobevent_job_id_start_line_76ab73f6_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_job_id_start_line_76ab73f6_idx ON public._unpartitioned_main_jobevent USING btree (job_id, start_line);


--
-- Name: main_jobevent_job_id_uuid_3df694c5_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_job_id_uuid_3df694c5_idx ON public._unpartitioned_main_jobevent USING btree (job_id, uuid);


--
-- Name: main_jobevent_modified_52b12bb7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobevent_modified_52b12bb7 ON ONLY public.main_jobevent USING btree (modified);


--
-- Name: main_jobhostsummary_constructed_host_id_8ec8dc05; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobhostsummary_constructed_host_id_8ec8dc05 ON public.main_jobhostsummary USING btree (constructed_host_id);


--
-- Name: main_jobhostsummary_failed_42948cd9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobhostsummary_failed_42948cd9 ON public.main_jobhostsummary USING btree (failed);


--
-- Name: main_jobhostsummary_host_id_7d9f6bf9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobhostsummary_host_id_7d9f6bf9 ON public.main_jobhostsummary USING btree (host_id);


--
-- Name: main_jobhostsummary_job_id_8d60afa0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobhostsummary_job_id_8d60afa0 ON public.main_jobhostsummary USING btree (job_id);


--
-- Name: main_joblaunchconfig_credentials_credential_id_2f5c0487; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfig_credentials_credential_id_2f5c0487 ON public.main_joblaunchconfig_credentials USING btree (credential_id);


--
-- Name: main_joblaunchconfig_credentials_joblaunchconfig_id_37dc31b9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfig_credentials_joblaunchconfig_id_37dc31b9 ON public.main_joblaunchconfig_credentials USING btree (joblaunchconfig_id);


--
-- Name: main_joblaunchconfig_execution_environment_id_ddf8eeec; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfig_execution_environment_id_ddf8eeec ON public.main_joblaunchconfig USING btree (execution_environment_id);


--
-- Name: main_joblaunchconfig_inventory_id_f905306d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfig_inventory_id_f905306d ON public.main_joblaunchconfig USING btree (inventory_id);


--
-- Name: main_joblaunchconfig_labels_joblaunchconfig_id_004bb969; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfig_labels_joblaunchconfig_id_004bb969 ON public.main_joblaunchconfig_labels USING btree (joblaunchconfig_id);


--
-- Name: main_joblaunchconfig_labels_label_id_5a9a600e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfig_labels_label_id_5a9a600e ON public.main_joblaunchconfig_labels USING btree (label_id);


--
-- Name: main_joblaunchconfiginstan_instancegroup_id_e76ac8f9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfiginstan_instancegroup_id_e76ac8f9 ON public.main_joblaunchconfiginstancegroupmembership USING btree (instancegroup_id);


--
-- Name: main_joblaunchconfiginstan_joblaunchconfig_id_93eb971f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfiginstan_joblaunchconfig_id_93eb971f ON public.main_joblaunchconfiginstancegroupmembership USING btree (joblaunchconfig_id);


--
-- Name: main_joblaunchconfiginstancegroupmembership_position_02d8202f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_joblaunchconfiginstancegroupmembership_position_02d8202f ON public.main_joblaunchconfiginstancegroupmembership USING btree ("position");


--
-- Name: main_jobtemplate_admin_role_id_f9dc66ce; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobtemplate_admin_role_id_f9dc66ce ON public.main_jobtemplate USING btree (admin_role_id);


--
-- Name: main_jobtemplate_execute_role_id_c2f0db2c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobtemplate_execute_role_id_c2f0db2c ON public.main_jobtemplate USING btree (execute_role_id);


--
-- Name: main_jobtemplate_inventory_id_9b8df646; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobtemplate_inventory_id_9b8df646 ON public.main_jobtemplate USING btree (inventory_id);


--
-- Name: main_jobtemplate_project_id_36e80985; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobtemplate_project_id_36e80985 ON public.main_jobtemplate USING btree (project_id);


--
-- Name: main_jobtemplate_read_role_id_0e489c81; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobtemplate_read_role_id_0e489c81 ON public.main_jobtemplate USING btree (read_role_id);


--
-- Name: main_jobtemplate_webhook_credential_id_eff7fb4b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_jobtemplate_webhook_credential_id_eff7fb4b ON public.main_jobtemplate USING btree (webhook_credential_id);


--
-- Name: main_label_created_by_id_201182c0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_label_created_by_id_201182c0 ON public.main_label USING btree (created_by_id);


--
-- Name: main_label_modified_by_id_7f9aac68; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_label_modified_by_id_7f9aac68 ON public.main_label USING btree (modified_by_id);


--
-- Name: main_label_organization_id_78a1bd27; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_label_organization_id_78a1bd27 ON public.main_label USING btree (organization_id);


--
-- Name: main_notification_notification_template_id_9eed1d65; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_notification_notification_template_id_9eed1d65 ON public.main_notification USING btree (notification_template_id);


--
-- Name: main_notificationtemplate_created_by_id_1f77983a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_notificationtemplate_created_by_id_1f77983a ON public.main_notificationtemplate USING btree (created_by_id);


--
-- Name: main_notificationtemplate_modified_by_id_83c40510; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_notificationtemplate_modified_by_id_83c40510 ON public.main_notificationtemplate USING btree (modified_by_id);


--
-- Name: main_notificationtemplate_organization_id_15933abb; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_notificationtemplate_organization_id_15933abb ON public.main_notificationtemplate USING btree (organization_id);


--
-- Name: main_oauth2accesstoken_application_id_c21bc74c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_oauth2accesstoken_application_id_c21bc74c ON public.main_oauth2accesstoken USING btree (application_id);


--
-- Name: main_oauth2accesstoken_token_e3bb4f7a_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_oauth2accesstoken_token_e3bb4f7a_like ON public.main_oauth2accesstoken USING btree (token varchar_pattern_ops);


--
-- Name: main_oauth2accesstoken_user_id_71ee5fe6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_oauth2accesstoken_user_id_71ee5fe6 ON public.main_oauth2accesstoken USING btree (user_id);


--
-- Name: main_oauth2application_client_id_45882763_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_oauth2application_client_id_45882763_like ON public.main_oauth2application USING btree (client_id varchar_pattern_ops);


--
-- Name: main_oauth2application_client_secret_0b8ee789; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_oauth2application_client_secret_0b8ee789 ON public.main_oauth2application USING btree (client_secret);


--
-- Name: main_oauth2application_client_secret_0b8ee789_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_oauth2application_client_secret_0b8ee789_like ON public.main_oauth2application USING btree (client_secret varchar_pattern_ops);


--
-- Name: main_oauth2application_organization_id_5e579421; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_oauth2application_organization_id_5e579421 ON public.main_oauth2application USING btree (organization_id);


--
-- Name: main_oauth2application_user_id_c4dffdbb; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_oauth2application_user_id_c4dffdbb ON public.main_oauth2application USING btree (user_id);


--
-- Name: main_organization_admin_role_id_e3ffdd41; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_admin_role_id_e3ffdd41 ON public.main_organization USING btree (admin_role_id);


--
-- Name: main_organization_approval_role_id_14c1d96f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_approval_role_id_14c1d96f ON public.main_organization USING btree (approval_role_id);


--
-- Name: main_organization_auditor_role_id_f912df0a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_auditor_role_id_f912df0a ON public.main_organization USING btree (auditor_role_id);


--
-- Name: main_organization_created_by_id_141da798; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_created_by_id_141da798 ON public.main_organization USING btree (created_by_id);


--
-- Name: main_organization_credential_admin_role_id_55733eb5; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_credential_admin_role_id_55733eb5 ON public.main_organization USING btree (credential_admin_role_id);


--
-- Name: main_organization_default_environment_id_1696aac2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_default_environment_id_1696aac2 ON public.main_organization USING btree (default_environment_id);


--
-- Name: main_organization_execute_role_id_76038d3c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_execute_role_id_76038d3c ON public.main_organization USING btree (execute_role_id);


--
-- Name: main_organization_execution_environment_admin_role_id_f2351549; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_execution_environment_admin_role_id_f2351549 ON public.main_organization USING btree (execution_environment_admin_role_id);


--
-- Name: main_organization_inventory_admin_role_id_dae5c7e2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_inventory_admin_role_id_dae5c7e2 ON public.main_organization USING btree (inventory_admin_role_id);


--
-- Name: main_organization_job_template_admin_role_id_25a265c4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_job_template_admin_role_id_25a265c4 ON public.main_organization USING btree (job_template_admin_role_id);


--
-- Name: main_organization_member_role_id_201ff67a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_member_role_id_201ff67a ON public.main_organization USING btree (member_role_id);


--
-- Name: main_organization_modified_by_id_dec7a500; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_modified_by_id_dec7a500 ON public.main_organization USING btree (modified_by_id);


--
-- Name: main_organization_name_3afd4fc6_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_name_3afd4fc6_like ON public.main_organization USING btree (name varchar_pattern_ops);


--
-- Name: main_organization_notifica_notificationtemplate_id_1df2f173; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notifica_notificationtemplate_id_1df2f173 ON public.main_organization_notification_templates_started USING btree (notificationtemplate_id);


--
-- Name: main_organization_notifica_notificationtemplate_id_392029b7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notifica_notificationtemplate_id_392029b7 ON public.main_organization_notification_templates_approvals USING btree (notificationtemplate_id);


--
-- Name: main_organization_notifica_notificationtemplate_id_4edd98c4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notifica_notificationtemplate_id_4edd98c4 ON public.main_organization_notification_templates_success USING btree (notificationtemplate_id);


--
-- Name: main_organization_notifica_notificationtemplate_id_7b1480c0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notifica_notificationtemplate_id_7b1480c0 ON public.main_organization_notification_templates_error USING btree (notificationtemplate_id);


--
-- Name: main_organization_notifica_organization_id_44a19957; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notifica_organization_id_44a19957 ON public.main_organization_notification_templates_approvals USING btree (organization_id);


--
-- Name: main_organization_notifica_organization_id_48a058ac; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notifica_organization_id_48a058ac ON public.main_organization_notification_templates_started USING btree (organization_id);


--
-- Name: main_organization_notifica_organization_id_94b63d49; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notifica_organization_id_94b63d49 ON public.main_organization_notification_templates_error USING btree (organization_id);


--
-- Name: main_organization_notifica_organization_id_96635cd6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notifica_organization_id_96635cd6 ON public.main_organization_notification_templates_success USING btree (organization_id);


--
-- Name: main_organization_notification_admin_role_id_c36d2f0e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_notification_admin_role_id_c36d2f0e ON public.main_organization USING btree (notification_admin_role_id);


--
-- Name: main_organization_project_admin_role_id_442cfebe; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_project_admin_role_id_442cfebe ON public.main_organization USING btree (project_admin_role_id);


--
-- Name: main_organization_read_role_id_e143c386; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_read_role_id_e143c386 ON public.main_organization USING btree (read_role_id);


--
-- Name: main_organization_workflow_admin_role_id_52011cd3; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organization_workflow_admin_role_id_52011cd3 ON public.main_organization USING btree (workflow_admin_role_id);


--
-- Name: main_organizationgalaxycre_credential_id_7b6334f3; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organizationgalaxycre_credential_id_7b6334f3 ON public.main_organizationgalaxycredentialmembership USING btree (credential_id);


--
-- Name: main_organizationgalaxycre_organization_id_0fd9495c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organizationgalaxycre_organization_id_0fd9495c ON public.main_organizationgalaxycredentialmembership USING btree (organization_id);


--
-- Name: main_organizationgalaxycredentialmembership_position_9319aefd; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organizationgalaxycredentialmembership_position_9319aefd ON public.main_organizationgalaxycredentialmembership USING btree ("position");


--
-- Name: main_organizationinstanceg_instancegroup_id_526173a9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organizationinstanceg_instancegroup_id_526173a9 ON public.main_organizationinstancegroupmembership USING btree (instancegroup_id);


--
-- Name: main_organizationinstanceg_organization_id_35633383; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organizationinstanceg_organization_id_35633383 ON public.main_organizationinstancegroupmembership USING btree (organization_id);


--
-- Name: main_organizationinstancegroupmembership_position_00023fb0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_organizationinstancegroupmembership_position_00023fb0 ON public.main_organizationinstancegroupmembership USING btree ("position");


--
-- Name: main_projec_project_449bbd_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projec_project_449bbd_idx ON ONLY public.main_projectupdateevent USING btree (project_update_id, job_created, uuid);


--
-- Name: main_projec_project_69559a_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projec_project_69559a_idx ON ONLY public.main_projectupdateevent USING btree (project_update_id, job_created, counter);


--
-- Name: main_projec_project_c44b7c_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projec_project_c44b7c_idx ON ONLY public.main_projectupdateevent USING btree (project_update_id, job_created, event);


--
-- Name: main_project_admin_role_id_ba0e70c7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_project_admin_role_id_ba0e70c7 ON public.main_project USING btree (admin_role_id);


--
-- Name: main_project_credential_id_370ba2a3; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_project_credential_id_370ba2a3 ON public.main_project USING btree (credential_id);


--
-- Name: main_project_default_environment_id_01467429; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_project_default_environment_id_01467429 ON public.main_project USING btree (default_environment_id);


--
-- Name: main_project_read_role_id_39a01fd4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_project_read_role_id_39a01fd4 ON public.main_project USING btree (read_role_id);


--
-- Name: main_project_signature_validation_credential_id_41e77a69; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_project_signature_validation_credential_id_41e77a69 ON public.main_project USING btree (signature_validation_credential_id);


--
-- Name: main_project_update_role_id_36e33c42; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_project_update_role_id_36e33c42 ON public.main_project USING btree (update_role_id);


--
-- Name: main_project_use_role_id_7b6d9148; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_project_use_role_id_7b6d9148 ON public.main_project USING btree (use_role_id);


--
-- Name: main_projectupdate_credential_id_2f7d826a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdate_credential_id_2f7d826a ON public.main_projectupdate USING btree (credential_id);


--
-- Name: main_projectupdate_project_id_bdd73efe; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdate_project_id_bdd73efe ON public.main_projectupdate USING btree (project_id);


--
-- Name: main_projectupdateevent_created_55746b86; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdateevent_created_55746b86 ON public._unpartitioned_main_projectupdateevent USING btree (created);


--
-- Name: main_projectupdateevent_modified_9b0b80e7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdateevent_modified_9b0b80e7 ON ONLY public.main_projectupdateevent USING btree (modified);


--
-- Name: main_projectupdateevent_project_update_id_9d4358b2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdateevent_project_update_id_9d4358b2 ON public._unpartitioned_main_projectupdateevent USING btree (project_update_id);


--
-- Name: main_projectupdateevent_project_update_id_end_line_59914839_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdateevent_project_update_id_end_line_59914839_idx ON public._unpartitioned_main_projectupdateevent USING btree (project_update_id, end_line);


--
-- Name: main_projectupdateevent_project_update_id_event_d8c3c5e5_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdateevent_project_update_id_event_d8c3c5e5_idx ON public._unpartitioned_main_projectupdateevent USING btree (project_update_id, event);


--
-- Name: main_projectupdateevent_project_update_id_start__0447b41c_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdateevent_project_update_id_start__0447b41c_idx ON public._unpartitioned_main_projectupdateevent USING btree (project_update_id, start_line);


--
-- Name: main_projectupdateevent_project_update_id_uuid_c4ffd915_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_projectupdateevent_project_update_id_uuid_c4ffd915_idx ON public._unpartitioned_main_projectupdateevent USING btree (project_update_id, uuid);


--
-- Name: main_rbac_r_ancesto_22b9f0_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_r_ancesto_22b9f0_idx ON public.main_rbac_role_ancestors USING btree (ancestor_id, content_type_id, object_id);


--
-- Name: main_rbac_r_ancesto_b44606_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_r_ancesto_b44606_idx ON public.main_rbac_role_ancestors USING btree (ancestor_id, content_type_id, role_field);


--
-- Name: main_rbac_r_ancesto_c87b87_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_r_ancesto_c87b87_idx ON public.main_rbac_role_ancestors USING btree (ancestor_id, descendent_id);


--
-- Name: main_rbac_r_content_979bdd_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_r_content_979bdd_idx ON public.main_rbac_roles USING btree (content_type_id, object_id);


--
-- Name: main_rbac_role_ancestors_ancestor_id_c6aae106; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_role_ancestors_ancestor_id_c6aae106 ON public.main_rbac_role_ancestors USING btree (ancestor_id);


--
-- Name: main_rbac_role_ancestors_descendent_id_23bfc463; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_role_ancestors_descendent_id_23bfc463 ON public.main_rbac_role_ancestors USING btree (descendent_id);


--
-- Name: main_rbac_roles_content_type_id_756d6b30; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_roles_content_type_id_756d6b30 ON public.main_rbac_roles USING btree (content_type_id);


--
-- Name: main_rbac_roles_members_role_id_7318b4b7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_roles_members_role_id_7318b4b7 ON public.main_rbac_roles_members USING btree (role_id);


--
-- Name: main_rbac_roles_members_user_id_f5e05418; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_roles_members_user_id_f5e05418 ON public.main_rbac_roles_members USING btree (user_id);


--
-- Name: main_rbac_roles_parents_from_role_id_a02db9eb; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_roles_parents_from_role_id_a02db9eb ON public.main_rbac_roles_parents USING btree (from_role_id);


--
-- Name: main_rbac_roles_parents_to_role_id_c00b5087; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_roles_parents_to_role_id_c00b5087 ON public.main_rbac_roles_parents USING btree (to_role_id);


--
-- Name: main_rbac_roles_singleton_name_3f0df1dd_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_rbac_roles_singleton_name_3f0df1dd_like ON public.main_rbac_roles USING btree (singleton_name text_pattern_ops);


--
-- Name: main_schedule_created_by_id_4e647be2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_created_by_id_4e647be2 ON public.main_schedule USING btree (created_by_id);


--
-- Name: main_schedule_credentials_credential_id_ced5894e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_credentials_credential_id_ced5894e ON public.main_schedule_credentials USING btree (credential_id);


--
-- Name: main_schedule_credentials_schedule_id_03ecad04; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_credentials_schedule_id_03ecad04 ON public.main_schedule_credentials USING btree (schedule_id);


--
-- Name: main_schedule_execution_environment_id_90eefd45; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_execution_environment_id_90eefd45 ON public.main_schedule USING btree (execution_environment_id);


--
-- Name: main_schedule_inventory_id_43b7b69d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_inventory_id_43b7b69d ON public.main_schedule USING btree (inventory_id);


--
-- Name: main_schedule_labels_label_id_79a46df6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_labels_label_id_79a46df6 ON public.main_schedule_labels USING btree (label_id);


--
-- Name: main_schedule_labels_schedule_id_e2f00ec6; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_labels_schedule_id_e2f00ec6 ON public.main_schedule_labels USING btree (schedule_id);


--
-- Name: main_schedule_modified_by_id_3817bc47; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_modified_by_id_3817bc47 ON public.main_schedule USING btree (modified_by_id);


--
-- Name: main_schedule_unified_job_template_id_a9d931e2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_schedule_unified_job_template_id_a9d931e2 ON public.main_schedule USING btree (unified_job_template_id);


--
-- Name: main_scheduleinstancegroupmembership_instancegroup_id_2d5f236c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_scheduleinstancegroupmembership_instancegroup_id_2d5f236c ON public.main_scheduleinstancegroupmembership USING btree (instancegroup_id);


--
-- Name: main_scheduleinstancegroupmembership_position_f3766917; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_scheduleinstancegroupmembership_position_f3766917 ON public.main_scheduleinstancegroupmembership USING btree ("position");


--
-- Name: main_scheduleinstancegroupmembership_schedule_id_d8eb2c41; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_scheduleinstancegroupmembership_schedule_id_d8eb2c41 ON public.main_scheduleinstancegroupmembership USING btree (schedule_id);


--
-- Name: main_smartinventorymembership_host_id_c721cb8a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_smartinventorymembership_host_id_c721cb8a ON public.main_smartinventorymembership USING btree (host_id);


--
-- Name: main_smartinventorymembership_inventory_id_5e13df96; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_smartinventorymembership_inventory_id_5e13df96 ON public.main_smartinventorymembership USING btree (inventory_id);


--
-- Name: main_system_system__73537a_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_system_system__73537a_idx ON ONLY public.main_systemjobevent USING btree (system_job_id, job_created, counter);


--
-- Name: main_system_system__e39825_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_system_system__e39825_idx ON ONLY public.main_systemjobevent USING btree (system_job_id, job_created, uuid);


--
-- Name: main_systemjob_system_job_template_id_8bba2060; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_systemjob_system_job_template_id_8bba2060 ON public.main_systemjob USING btree (system_job_template_id);


--
-- Name: main_systemjobevent_modified_e4b3f14a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_systemjobevent_modified_e4b3f14a ON ONLY public.main_systemjobevent USING btree (modified);


--
-- Name: main_systemjobevent_system_job_id_91bbbfc1; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_systemjobevent_system_job_id_91bbbfc1 ON public._unpartitioned_main_systemjobevent USING btree (system_job_id);


--
-- Name: main_systemjobevent_system_job_id_end_line_9bb9848e_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_systemjobevent_system_job_id_end_line_9bb9848e_idx ON public._unpartitioned_main_systemjobevent USING btree (system_job_id, end_line);


--
-- Name: main_systemjobevent_system_job_id_start_line_60445b40_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_systemjobevent_system_job_id_start_line_60445b40_idx ON public._unpartitioned_main_systemjobevent USING btree (system_job_id, start_line);


--
-- Name: main_systemjobevent_system_job_id_uuid_b25996b0_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_systemjobevent_system_job_id_uuid_b25996b0_idx ON public._unpartitioned_main_systemjobevent USING btree (system_job_id, uuid);


--
-- Name: main_team_admin_role_id_a9e09a22; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_team_admin_role_id_a9e09a22 ON public.main_team USING btree (admin_role_id);


--
-- Name: main_team_created_by_id_c370350b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_team_created_by_id_c370350b ON public.main_team USING btree (created_by_id);


--
-- Name: main_team_member_role_id_a2f93dc9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_team_member_role_id_a2f93dc9 ON public.main_team USING btree (member_role_id);


--
-- Name: main_team_modified_by_id_9af533cd; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_team_modified_by_id_9af533cd ON public.main_team USING btree (modified_by_id);


--
-- Name: main_team_organization_id_8b31bbc1; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_team_organization_id_8b31bbc1 ON public.main_team USING btree (organization_id);


--
-- Name: main_team_read_role_id_ea02761f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_team_read_role_id_ea02761f ON public.main_team USING btree (read_role_id);


--
-- Name: main_unifiedjob_canceled_on_8695ca21; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_canceled_on_8695ca21 ON public.main_unifiedjob USING btree (canceled_on);


--
-- Name: main_unifiedjob_created_94704da7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_created_94704da7 ON public.main_unifiedjob USING btree (created);


--
-- Name: main_unifiedjob_created_by_id_d2a186ab; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_created_by_id_d2a186ab ON public.main_unifiedjob USING btree (created_by_id);


--
-- Name: main_unifiedjob_credentials_credential_id_661c8f49; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_credentials_credential_id_661c8f49 ON public.main_unifiedjob_credentials USING btree (credential_id);


--
-- Name: main_unifiedjob_credentials_unifiedjob_id_4ed7ff5d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_credentials_unifiedjob_id_4ed7ff5d ON public.main_unifiedjob_credentials USING btree (unifiedjob_id);


--
-- Name: main_unifiedjob_dependent_jobs_from_unifiedjob_id_c8d58e88; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_dependent_jobs_from_unifiedjob_id_c8d58e88 ON public.main_unifiedjob_dependent_jobs USING btree (from_unifiedjob_id);


--
-- Name: main_unifiedjob_dependent_jobs_to_unifiedjob_id_3f04cbcc; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_dependent_jobs_to_unifiedjob_id_3f04cbcc ON public.main_unifiedjob_dependent_jobs USING btree (to_unifiedjob_id);


--
-- Name: main_unifiedjob_execution_environment_id_b2eaf9c0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_execution_environment_id_b2eaf9c0 ON public.main_unifiedjob USING btree (execution_environment_id);


--
-- Name: main_unifiedjob_finished_eccf6159; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_finished_eccf6159 ON public.main_unifiedjob USING btree (finished);


--
-- Name: main_unifiedjob_instance_group_id_f76a06e2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_instance_group_id_f76a06e2 ON public.main_unifiedjob USING btree (instance_group_id);


--
-- Name: main_unifiedjob_labels_label_id_98814bad; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_labels_label_id_98814bad ON public.main_unifiedjob_labels USING btree (label_id);


--
-- Name: main_unifiedjob_labels_unifiedjob_id_bd008d37; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_labels_unifiedjob_id_bd008d37 ON public.main_unifiedjob_labels USING btree (unifiedjob_id);


--
-- Name: main_unifiedjob_launch_type_f97c0639; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_launch_type_f97c0639 ON public.main_unifiedjob USING btree (launch_type);


--
-- Name: main_unifiedjob_launch_type_f97c0639_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_launch_type_f97c0639_like ON public.main_unifiedjob USING btree (launch_type varchar_pattern_ops);


--
-- Name: main_unifiedjob_modified_by_id_14cbb9bc; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_modified_by_id_14cbb9bc ON public.main_unifiedjob USING btree (modified_by_id);


--
-- Name: main_unifiedjob_notifications_notification_id_cf3498bf; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_notifications_notification_id_cf3498bf ON public.main_unifiedjob_notifications USING btree (notification_id);


--
-- Name: main_unifiedjob_notifications_unifiedjob_id_65ab9c3c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_notifications_unifiedjob_id_65ab9c3c ON public.main_unifiedjob_notifications USING btree (unifiedjob_id);


--
-- Name: main_unifiedjob_organization_id_cbfa01d3; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_organization_id_cbfa01d3 ON public.main_unifiedjob USING btree (organization_id);


--
-- Name: main_unifiedjob_polymorphic_ctype_id_cb46239b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_polymorphic_ctype_id_cb46239b ON public.main_unifiedjob USING btree (polymorphic_ctype_id);


--
-- Name: main_unifiedjob_schedule_id_766ca767; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_schedule_id_766ca767 ON public.main_unifiedjob USING btree (schedule_id);


--
-- Name: main_unifiedjob_status_ea421be2; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_status_ea421be2 ON public.main_unifiedjob USING btree (status);


--
-- Name: main_unifiedjob_status_ea421be2_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_status_ea421be2_like ON public.main_unifiedjob USING btree (status varchar_pattern_ops);


--
-- Name: main_unifiedjob_unified_job_template_id_a398b197; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjob_unified_job_template_id_a398b197 ON public.main_unifiedjob USING btree (unified_job_template_id);


--
-- Name: main_unifiedjobtemplate_cr_unifiedjobtemplate_id_d98d7c79; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_cr_unifiedjobtemplate_id_d98d7c79 ON public.main_unifiedjobtemplate_credentials USING btree (unifiedjobtemplate_id);


--
-- Name: main_unifiedjobtemplate_created_by_id_1f5fadfa; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_created_by_id_1f5fadfa ON public.main_unifiedjobtemplate USING btree (created_by_id);


--
-- Name: main_unifiedjobtemplate_credentials_credential_id_fd216c80; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_credentials_credential_id_fd216c80 ON public.main_unifiedjobtemplate_credentials USING btree (credential_id);


--
-- Name: main_unifiedjobtemplate_current_job_id_8f449ab0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_current_job_id_8f449ab0 ON public.main_unifiedjobtemplate USING btree (current_job_id);


--
-- Name: main_unifiedjobtemplate_execution_environment_id_bed25866; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_execution_environment_id_bed25866 ON public.main_unifiedjobtemplate USING btree (execution_environment_id);


--
-- Name: main_unifiedjobtemplate_labels_label_id_d6a5ee75; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_labels_label_id_d6a5ee75 ON public.main_unifiedjobtemplate_labels USING btree (label_id);


--
-- Name: main_unifiedjobtemplate_labels_unifiedjobtemplate_id_c9307a9a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_labels_unifiedjobtemplate_id_c9307a9a ON public.main_unifiedjobtemplate_labels USING btree (unifiedjobtemplate_id);


--
-- Name: main_unifiedjobtemplate_last_job_id_7e983743; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_last_job_id_7e983743 ON public.main_unifiedjobtemplate USING btree (last_job_id);


--
-- Name: main_unifiedjobtemplate_modified_by_id_a8bf1de0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_modified_by_id_a8bf1de0 ON public.main_unifiedjobtemplate USING btree (modified_by_id);


--
-- Name: main_unifiedjobtemplate_next_schedule_id_955ff55d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_next_schedule_id_955ff55d ON public.main_unifiedjobtemplate USING btree (next_schedule_id);


--
-- Name: main_unifiedjobtemplate_no_notificationtemplate_id_9326cdf9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_no_notificationtemplate_id_9326cdf9 ON public.main_unifiedjobtemplate_notification_templates_success USING btree (notificationtemplate_id);


--
-- Name: main_unifiedjobtemplate_no_notificationtemplate_id_9793a63a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_no_notificationtemplate_id_9793a63a ON public.main_unifiedjobtemplate_notification_templates_started USING btree (notificationtemplate_id);


--
-- Name: main_unifiedjobtemplate_no_notificationtemplate_id_b19df8ac; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_no_notificationtemplate_id_b19df8ac ON public.main_unifiedjobtemplate_notification_templates_error USING btree (notificationtemplate_id);


--
-- Name: main_unifiedjobtemplate_no_unifiedjobtemplate_id_0ce91b23; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_no_unifiedjobtemplate_id_0ce91b23 ON public.main_unifiedjobtemplate_notification_templates_error USING btree (unifiedjobtemplate_id);


--
-- Name: main_unifiedjobtemplate_no_unifiedjobtemplate_id_3934753d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_no_unifiedjobtemplate_id_3934753d ON public.main_unifiedjobtemplate_notification_templates_success USING btree (unifiedjobtemplate_id);


--
-- Name: main_unifiedjobtemplate_no_unifiedjobtemplate_id_6e21dce4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_no_unifiedjobtemplate_id_6e21dce4 ON public.main_unifiedjobtemplate_notification_templates_started USING btree (unifiedjobtemplate_id);


--
-- Name: main_unifiedjobtemplate_organization_id_c63fa1a4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_organization_id_c63fa1a4 ON public.main_unifiedjobtemplate USING btree (organization_id);


--
-- Name: main_unifiedjobtemplate_polymorphic_ctype_id_ce19bb25; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplate_polymorphic_ctype_id_ce19bb25 ON public.main_unifiedjobtemplate USING btree (polymorphic_ctype_id);


--
-- Name: main_unifiedjobtemplateins_instancegroup_id_656188b4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplateins_instancegroup_id_656188b4 ON public.main_unifiedjobtemplateinstancegroupmembership USING btree (instancegroup_id);


--
-- Name: main_unifiedjobtemplateins_position_fd6edc28; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplateins_position_fd6edc28 ON public.main_unifiedjobtemplateinstancegroupmembership USING btree ("position");


--
-- Name: main_unifiedjobtemplateins_unifiedjobtemplate_id_e401e3d7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_unifiedjobtemplateins_unifiedjobtemplate_id_e401e3d7 ON public.main_unifiedjobtemplateinstancegroupmembership USING btree (unifiedjobtemplate_id);


--
-- Name: main_usersessionmembership_session_id_fbab60a5_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_usersessionmembership_session_id_fbab60a5_like ON public.main_usersessionmembership USING btree (session_id varchar_pattern_ops);


--
-- Name: main_usersessionmembership_user_id_fe163c98; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_usersessionmembership_user_id_fe163c98 ON public.main_usersessionmembership USING btree (user_id);


--
-- Name: main_workfl_identif_0cc025_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workfl_identif_0cc025_idx ON public.main_workflowjobtemplatenode USING btree (identifier);


--
-- Name: main_workfl_identif_87b752_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workfl_identif_87b752_idx ON public.main_workflowjobnode USING btree (identifier, workflow_job_id);


--
-- Name: main_workfl_identif_efdfe8_idx; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workfl_identif_efdfe8_idx ON public.main_workflowjobnode USING btree (identifier);


--
-- Name: main_workflowapproval_approved_or_denied_by_id_bb3eae41; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowapproval_approved_or_denied_by_id_bb3eae41 ON public.main_workflowapproval USING btree (approved_or_denied_by_id);


--
-- Name: main_workflowapproval_workflow_approval_template_id_b87dda8a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowapproval_workflow_approval_template_id_b87dda8a ON public.main_workflowapproval USING btree (workflow_approval_template_id);


--
-- Name: main_workflowjob_inventory_id_8c31355b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjob_inventory_id_8c31355b ON public.main_workflowjob USING btree (inventory_id);


--
-- Name: main_workflowjob_job_template_id_cceff2a3; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjob_job_template_id_cceff2a3 ON public.main_workflowjob USING btree (job_template_id);


--
-- Name: main_workflowjob_webhook_credential_id_57c9fece; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjob_webhook_credential_id_57c9fece ON public.main_workflowjob USING btree (webhook_credential_id);


--
-- Name: main_workflowjob_workflow_job_template_id_0d9a93a0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjob_workflow_job_template_id_0d9a93a0 ON public.main_workflowjob USING btree (workflow_job_template_id);


--
-- Name: main_workflowjobinstancegr_instancegroup_id_00dbe24d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobinstancegr_instancegroup_id_00dbe24d ON public.main_workflowjobinstancegroupmembership USING btree (instancegroup_id);


--
-- Name: main_workflowjobinstancegr_workflowjobnode_id_e18bb569; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobinstancegr_workflowjobnode_id_e18bb569 ON public.main_workflowjobinstancegroupmembership USING btree (workflowjobnode_id);


--
-- Name: main_workflowjobinstancegroupmembership_position_d2c9b3f8; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobinstancegroupmembership_position_d2c9b3f8 ON public.main_workflowjobinstancegroupmembership USING btree ("position");


--
-- Name: main_workflowjobnode_alway_from_workflowjobnode_id_19edb9d7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_alway_from_workflowjobnode_id_19edb9d7 ON public.main_workflowjobnode_always_nodes USING btree (from_workflowjobnode_id);


--
-- Name: main_workflowjobnode_alway_to_workflowjobnode_id_0edcda07; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_alway_to_workflowjobnode_id_0edcda07 ON public.main_workflowjobnode_always_nodes USING btree (to_workflowjobnode_id);


--
-- Name: main_workflowjobnode_credentials_credential_id_6de5a410; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_credentials_credential_id_6de5a410 ON public.main_workflowjobnode_credentials USING btree (credential_id);


--
-- Name: main_workflowjobnode_credentials_workflowjobnode_id_31f8c02b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_credentials_workflowjobnode_id_31f8c02b ON public.main_workflowjobnode_credentials USING btree (workflowjobnode_id);


--
-- Name: main_workflowjobnode_execution_environment_id_c593ca11; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_execution_environment_id_c593ca11 ON public.main_workflowjobnode USING btree (execution_environment_id);


--
-- Name: main_workflowjobnode_failu_from_workflowjobnode_id_2172a110; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_failu_from_workflowjobnode_id_2172a110 ON public.main_workflowjobnode_failure_nodes USING btree (from_workflowjobnode_id);


--
-- Name: main_workflowjobnode_failu_to_workflowjobnode_id_d2e09d9c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_failu_to_workflowjobnode_id_d2e09d9c ON public.main_workflowjobnode_failure_nodes USING btree (to_workflowjobnode_id);


--
-- Name: main_workflowjobnode_inventory_id_1dac2da9; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_inventory_id_1dac2da9 ON public.main_workflowjobnode USING btree (inventory_id);


--
-- Name: main_workflowjobnode_labels_label_id_0e6594a7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_labels_label_id_0e6594a7 ON public.main_workflowjobnode_labels USING btree (label_id);


--
-- Name: main_workflowjobnode_labels_workflowjobnode_id_14f419e1; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_labels_workflowjobnode_id_14f419e1 ON public.main_workflowjobnode_labels USING btree (workflowjobnode_id);


--
-- Name: main_workflowjobnode_succe_from_workflowjobnode_id_e04f9991; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_succe_from_workflowjobnode_id_e04f9991 ON public.main_workflowjobnode_success_nodes USING btree (from_workflowjobnode_id);


--
-- Name: main_workflowjobnode_succe_to_workflowjobnode_id_e6c8cbb4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_succe_to_workflowjobnode_id_e6c8cbb4 ON public.main_workflowjobnode_success_nodes USING btree (to_workflowjobnode_id);


--
-- Name: main_workflowjobnode_unified_job_template_id_8a30f93e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_unified_job_template_id_8a30f93e ON public.main_workflowjobnode USING btree (unified_job_template_id);


--
-- Name: main_workflowjobnode_workflow_job_id_dcd715c7; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnode_workflow_job_id_dcd715c7 ON public.main_workflowjobnode USING btree (workflow_job_id);


--
-- Name: main_workflowjobnodebasein_instancegroup_id_4e4faca5; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnodebasein_instancegroup_id_4e4faca5 ON public.main_workflowjobnodebaseinstancegroupmembership USING btree (instancegroup_id);


--
-- Name: main_workflowjobnodebasein_position_e440e34a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnodebasein_position_e440e34a ON public.main_workflowjobnodebaseinstancegroupmembership USING btree ("position");


--
-- Name: main_workflowjobnodebasein_workflowjobnode_id_47a05c0e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobnodebasein_workflowjobnode_id_47a05c0e ON public.main_workflowjobnodebaseinstancegroupmembership USING btree (workflowjobnode_id);


--
-- Name: main_workflowjobtemplate_admin_role_id_5675a40e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplate_admin_role_id_5675a40e ON public.main_workflowjobtemplate USING btree (admin_role_id);


--
-- Name: main_workflowjobtemplate_approval_role_id_220f0de1; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplate_approval_role_id_220f0de1 ON public.main_workflowjobtemplate USING btree (approval_role_id);


--
-- Name: main_workflowjobtemplate_execute_role_id_ad8970f4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplate_execute_role_id_ad8970f4 ON public.main_workflowjobtemplate USING btree (execute_role_id);


--
-- Name: main_workflowjobtemplate_inventory_id_99929499; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplate_inventory_id_99929499 ON public.main_workflowjobtemplate USING btree (inventory_id);


--
-- Name: main_workflowjobtemplate_n_notificationtemplate_id_3811d35e; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplate_n_notificationtemplate_id_3811d35e ON public.main_workflowjobtemplate_notification_templates_approvals USING btree (notificationtemplate_id);


--
-- Name: main_workflowjobtemplate_n_workflowjobtemplate_id_ce7a17be; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplate_n_workflowjobtemplate_id_ce7a17be ON public.main_workflowjobtemplate_notification_templates_approvals USING btree (workflowjobtemplate_id);


--
-- Name: main_workflowjobtemplate_read_role_id_acdd95ef; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplate_read_role_id_acdd95ef ON public.main_workflowjobtemplate USING btree (read_role_id);


--
-- Name: main_workflowjobtemplate_webhook_credential_id_ced1ad89; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplate_webhook_credential_id_ced1ad89 ON public.main_workflowjobtemplate USING btree (webhook_credential_id);


--
-- Name: main_workflowjobtemplateno_from_workflowjobtemplateno_8af14c32; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_from_workflowjobtemplateno_8af14c32 ON public.main_workflowjobtemplatenode_always_nodes USING btree (from_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplateno_from_workflowjobtemplateno_9e16f49d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_from_workflowjobtemplateno_9e16f49d ON public.main_workflowjobtemplatenode_success_nodes USING btree (from_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplateno_from_workflowjobtemplateno_fa405230; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_from_workflowjobtemplateno_fa405230 ON public.main_workflowjobtemplatenode_failure_nodes USING btree (from_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplateno_instancegroup_id_0c59a80a; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_instancegroup_id_0c59a80a ON public.main_workflowjobtemplatenodebaseinstancegroupmembership USING btree (instancegroup_id);


--
-- Name: main_workflowjobtemplateno_position_b6e6fca5; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_position_b6e6fca5 ON public.main_workflowjobtemplatenodebaseinstancegroupmembership USING btree ("position");


--
-- Name: main_workflowjobtemplateno_to_workflowjobtemplatenode_2c1db0ae; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_to_workflowjobtemplatenode_2c1db0ae ON public.main_workflowjobtemplatenode_failure_nodes USING btree (to_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplateno_to_workflowjobtemplatenode_6fe11708; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_to_workflowjobtemplatenode_6fe11708 ON public.main_workflowjobtemplatenode_always_nodes USING btree (to_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplateno_to_workflowjobtemplatenode_f16ee478; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_to_workflowjobtemplatenode_f16ee478 ON public.main_workflowjobtemplatenode_success_nodes USING btree (to_workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplateno_workflowjobtemplatenode_id_b91efe86; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_workflowjobtemplatenode_id_b91efe86 ON public.main_workflowjobtemplatenode_credentials USING btree (workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplateno_workflowjobtemplatenode_id_f75998d4; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_workflowjobtemplatenode_id_f75998d4 ON public.main_workflowjobtemplatenode_labels USING btree (workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplateno_workflowjobtemplatenode_id_fa0959c5; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplateno_workflowjobtemplatenode_id_fa0959c5 ON public.main_workflowjobtemplatenodebaseinstancegroupmembership USING btree (workflowjobtemplatenode_id);


--
-- Name: main_workflowjobtemplatenode_credentials_credential_id_e621c8d1; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplatenode_credentials_credential_id_e621c8d1 ON public.main_workflowjobtemplatenode_credentials USING btree (credential_id);


--
-- Name: main_workflowjobtemplatenode_execution_environment_id_ec5bba6d; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplatenode_execution_environment_id_ec5bba6d ON public.main_workflowjobtemplatenode USING btree (execution_environment_id);


--
-- Name: main_workflowjobtemplatenode_inventory_id_2fab864f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplatenode_inventory_id_2fab864f ON public.main_workflowjobtemplatenode USING btree (inventory_id);


--
-- Name: main_workflowjobtemplatenode_labels_label_id_b3f1a57f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplatenode_labels_label_id_b3f1a57f ON public.main_workflowjobtemplatenode_labels USING btree (label_id);


--
-- Name: main_workflowjobtemplatenode_unified_job_template_id_98b53e6c; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplatenode_unified_job_template_id_98b53e6c ON public.main_workflowjobtemplatenode USING btree (unified_job_template_id);


--
-- Name: main_workflowjobtemplatenode_workflow_job_template_id_2fd591f0; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX main_workflowjobtemplatenode_workflow_job_template_id_2fd591f0 ON public.main_workflowjobtemplatenode USING btree (workflow_job_template_id);


--
-- Name: oauth2_provider_grant_application_id_81923564; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX oauth2_provider_grant_application_id_81923564 ON public.oauth2_provider_grant USING btree (application_id);


--
-- Name: oauth2_provider_grant_code_49ab4ddf_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX oauth2_provider_grant_code_49ab4ddf_like ON public.oauth2_provider_grant USING btree (code varchar_pattern_ops);


--
-- Name: oauth2_provider_grant_user_id_e8f62af8; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX oauth2_provider_grant_user_id_e8f62af8 ON public.oauth2_provider_grant USING btree (user_id);


--
-- Name: oauth2_provider_idtoken_application_id_08c5ff4f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX oauth2_provider_idtoken_application_id_08c5ff4f ON public.oauth2_provider_idtoken USING btree (application_id);


--
-- Name: oauth2_provider_idtoken_user_id_dd512b59; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX oauth2_provider_idtoken_user_id_dd512b59 ON public.oauth2_provider_idtoken USING btree (user_id);


--
-- Name: oauth2_provider_refreshtoken_application_id_2d1c311b; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX oauth2_provider_refreshtoken_application_id_2d1c311b ON public.oauth2_provider_refreshtoken USING btree (application_id);


--
-- Name: oauth2_provider_refreshtoken_user_id_da837fce; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX oauth2_provider_refreshtoken_user_id_da837fce ON public.oauth2_provider_refreshtoken USING btree (user_id);


--
-- Name: social_auth_code_code_a2393167; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_code_code_a2393167 ON public.social_auth_code USING btree (code);


--
-- Name: social_auth_code_code_a2393167_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_code_code_a2393167_like ON public.social_auth_code USING btree (code varchar_pattern_ops);


--
-- Name: social_auth_code_timestamp_176b341f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_code_timestamp_176b341f ON public.social_auth_code USING btree ("timestamp");


--
-- Name: social_auth_partial_timestamp_50f2119f; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_partial_timestamp_50f2119f ON public.social_auth_partial USING btree ("timestamp");


--
-- Name: social_auth_partial_token_3017fea3; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_partial_token_3017fea3 ON public.social_auth_partial USING btree (token);


--
-- Name: social_auth_partial_token_3017fea3_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_partial_token_3017fea3_like ON public.social_auth_partial USING btree (token varchar_pattern_ops);


--
-- Name: social_auth_usersocialauth_uid_796e51dc; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_usersocialauth_uid_796e51dc ON public.social_auth_usersocialauth USING btree (uid);


--
-- Name: social_auth_usersocialauth_uid_796e51dc_like; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_usersocialauth_uid_796e51dc_like ON public.social_auth_usersocialauth USING btree (uid varchar_pattern_ops);


--
-- Name: social_auth_usersocialauth_user_id_17d28448; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX social_auth_usersocialauth_user_id_17d28448 ON public.social_auth_usersocialauth USING btree (user_id);


--
-- Name: sso_userenterpriseauth_user_id_5982f0ef; Type: INDEX; Schema: public; Owner: awx
--

CREATE INDEX sso_userenterpriseauth_user_id_5982f0ef ON public.sso_userenterpriseauth USING btree (user_id);


--
-- Name: auth_group_permissions auth_group_permissio_permission_id_84c5c92e_fk_auth_perm; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_group_permissions
    ADD CONSTRAINT auth_group_permissio_permission_id_84c5c92e_fk_auth_perm FOREIGN KEY (permission_id) REFERENCES public.auth_permission(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: auth_group_permissions auth_group_permissions_group_id_b120cbf9_fk_auth_group_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_group_permissions
    ADD CONSTRAINT auth_group_permissions_group_id_b120cbf9_fk_auth_group_id FOREIGN KEY (group_id) REFERENCES public.auth_group(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: auth_permission auth_permission_content_type_id_2f476e4b_fk_django_co; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_permission
    ADD CONSTRAINT auth_permission_content_type_id_2f476e4b_fk_django_co FOREIGN KEY (content_type_id) REFERENCES public.django_content_type(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: auth_user_groups auth_user_groups_group_id_97559544_fk_auth_group_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user_groups
    ADD CONSTRAINT auth_user_groups_group_id_97559544_fk_auth_group_id FOREIGN KEY (group_id) REFERENCES public.auth_group(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: auth_user_groups auth_user_groups_user_id_6a12ed8b_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user_groups
    ADD CONSTRAINT auth_user_groups_user_id_6a12ed8b_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: auth_user_user_permissions auth_user_user_permi_permission_id_1fbb5f2c_fk_auth_perm; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user_user_permissions
    ADD CONSTRAINT auth_user_user_permi_permission_id_1fbb5f2c_fk_auth_perm FOREIGN KEY (permission_id) REFERENCES public.auth_permission(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: auth_user_user_permissions auth_user_user_permissions_user_id_a95ead1b_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.auth_user_user_permissions
    ADD CONSTRAINT auth_user_user_permissions_user_id_a95ead1b_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: conf_setting conf_setting_user_id_ce9d5138_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.conf_setting
    ADD CONSTRAINT conf_setting_user_id_ce9d5138_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_organization main_activitystream__activitystream_id_0283e075_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_organization
    ADD CONSTRAINT main_activitystream__activitystream_id_0283e075_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_instance main_activitystream__activitystream_id_04ccbf32_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_instance
    ADD CONSTRAINT main_activitystream__activitystream_id_04ccbf32_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_approval main_activitystream__activitystream_id_14401444_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_approval
    ADD CONSTRAINT main_activitystream__activitystream_id_14401444_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_notification_template main_activitystream__activitystream_id_214c1789_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_notification_template
    ADD CONSTRAINT main_activitystream__activitystream_id_214c1789_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_job_template main_activitystream__activitystream_id_259ad363_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_template
    ADD CONSTRAINT main_activitystream__activitystream_id_259ad363_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_project_update main_activitystream__activitystream_id_2965eda0_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_project_update
    ADD CONSTRAINT main_activitystream__activitystream_id_2965eda0_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_execution_environment main_activitystream__activitystream_id_4938d427_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_execution_environment
    ADD CONSTRAINT main_activitystream__activitystream_id_4938d427_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_inventory main_activitystream__activitystream_id_4a1242eb_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory
    ADD CONSTRAINT main_activitystream__activitystream_id_4a1242eb_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_credential main_activitystream__activitystream_id_4be1a957_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_credential
    ADD CONSTRAINT main_activitystream__activitystream_id_4be1a957_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_inventory_update main_activitystream__activitystream_id_732f074a_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory_update
    ADD CONSTRAINT main_activitystream__activitystream_id_732f074a_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_notification main_activitystream__activitystream_id_7d39234a_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_notification
    ADD CONSTRAINT main_activitystream__activitystream_id_7d39234a_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_approval_template main_activitystream__activitystream_id_7e8e02aa_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_approval_template
    ADD CONSTRAINT main_activitystream__activitystream_id_7e8e02aa_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_ad_hoc_command main_activitystream__activitystream_id_870ddb01_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_ad_hoc_command
    ADD CONSTRAINT main_activitystream__activitystream_id_870ddb01_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_job main_activitystream__activitystream_id_93d66e38_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job
    ADD CONSTRAINT main_activitystream__activitystream_id_93d66e38_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_group main_activitystream__activitystream_id_94d31559_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_group
    ADD CONSTRAINT main_activitystream__activitystream_id_94d31559_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_o_auth2_access_token main_activitystream__activitystream_id_9cd33ed4_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_o_auth2_access_token
    ADD CONSTRAINT main_activitystream__activitystream_id_9cd33ed4_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_schedule main_activitystream__activitystream_id_a5fd87ef_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_schedule
    ADD CONSTRAINT main_activitystream__activitystream_id_a5fd87ef_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_job_template main_activitystream__activitystream_id_abd63b6d_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_job_template
    ADD CONSTRAINT main_activitystream__activitystream_id_abd63b6d_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_label main_activitystream__activitystream_id_afd608d7_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_label
    ADD CONSTRAINT main_activitystream__activitystream_id_afd608d7_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_job main_activitystream__activitystream_id_b1f2ab1b_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_job
    ADD CONSTRAINT main_activitystream__activitystream_id_b1f2ab1b_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_job_template_node main_activitystream__activitystream_id_b3d1beb6_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_template_node
    ADD CONSTRAINT main_activitystream__activitystream_id_b3d1beb6_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_credential_type main_activitystream__activitystream_id_b7a4b49d_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_credential_type
    ADD CONSTRAINT main_activitystream__activitystream_id_b7a4b49d_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_team main_activitystream__activitystream_id_c4874e73_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_team
    ADD CONSTRAINT main_activitystream__activitystream_id_c4874e73_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_host main_activitystream__activitystream_id_c4d91cb7_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_host
    ADD CONSTRAINT main_activitystream__activitystream_id_c4d91cb7_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_job_node main_activitystream__activitystream_id_c8397668_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_node
    ADD CONSTRAINT main_activitystream__activitystream_id_c8397668_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_role main_activitystream__activitystream_id_d591eb98_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_role
    ADD CONSTRAINT main_activitystream__activitystream_id_d591eb98_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_inventory_source main_activitystream__activitystream_id_d88c8423_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory_source
    ADD CONSTRAINT main_activitystream__activitystream_id_d88c8423_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_unified_job main_activitystream__activitystream_id_e29d497f_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_unified_job
    ADD CONSTRAINT main_activitystream__activitystream_id_e29d497f_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_unified_job_template main_activitystream__activitystream_id_e4ce5d15_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_unified_job_template
    ADD CONSTRAINT main_activitystream__activitystream_id_e4ce5d15_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_instance_group main_activitystream__activitystream_id_e81ef38a_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_instance_group
    ADD CONSTRAINT main_activitystream__activitystream_id_e81ef38a_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_o_auth2_application main_activitystream__activitystream_id_ea629ffe_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_o_auth2_application
    ADD CONSTRAINT main_activitystream__activitystream_id_ea629ffe_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_user main_activitystream__activitystream_id_f120c9d1_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_user
    ADD CONSTRAINT main_activitystream__activitystream_id_f120c9d1_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_project main_activitystream__activitystream_id_f6aa28cc_fk_main_acti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_project
    ADD CONSTRAINT main_activitystream__activitystream_id_f6aa28cc_fk_main_acti FOREIGN KEY (activitystream_id) REFERENCES public.main_activitystream(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_ad_hoc_command main_activitystream__adhoccommand_id_0df7bfcd_fk_main_adho; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_ad_hoc_command
    ADD CONSTRAINT main_activitystream__adhoccommand_id_0df7bfcd_fk_main_adho FOREIGN KEY (adhoccommand_id) REFERENCES public.main_adhoccommand(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_credential main_activitystream__credential_id_d5911596_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_credential
    ADD CONSTRAINT main_activitystream__credential_id_d5911596_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_credential_type main_activitystream__credentialtype_id_89572b10_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_credential_type
    ADD CONSTRAINT main_activitystream__credentialtype_id_89572b10_fk_main_cred FOREIGN KEY (credentialtype_id) REFERENCES public.main_credentialtype(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_execution_environment main_activitystream__executionenvironment_b455fc65_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_execution_environment
    ADD CONSTRAINT main_activitystream__executionenvironment_b455fc65_fk_main_exec FOREIGN KEY (executionenvironment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_instance main_activitystream__instance_id_d10eb669_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_instance
    ADD CONSTRAINT main_activitystream__instance_id_d10eb669_fk_main_inst FOREIGN KEY (instance_id) REFERENCES public.main_instance(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_instance_group main_activitystream__instancegroup_id_fca49f6c_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_instance_group
    ADD CONSTRAINT main_activitystream__instancegroup_id_fca49f6c_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_inventory main_activitystream__inventory_id_8daf9251_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory
    ADD CONSTRAINT main_activitystream__inventory_id_8daf9251_fk_main_inve FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_inventory_source main_activitystream__inventorysource_id_235e699a_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory_source
    ADD CONSTRAINT main_activitystream__inventorysource_id_235e699a_fk_main_inve FOREIGN KEY (inventorysource_id) REFERENCES public.main_inventorysource(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_inventory_update main_activitystream__inventoryupdate_id_817749c5_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_inventory_update
    ADD CONSTRAINT main_activitystream__inventoryupdate_id_817749c5_fk_main_inve FOREIGN KEY (inventoryupdate_id) REFERENCES public.main_inventoryupdate(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_job main_activitystream__job_id_aa6811b5_fk_main_job_; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_job
    ADD CONSTRAINT main_activitystream__job_id_aa6811b5_fk_main_job_ FOREIGN KEY (job_id) REFERENCES public.main_job(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_job_template main_activitystream__jobtemplate_id_c05e0b6c_fk_main_jobt; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_job_template
    ADD CONSTRAINT main_activitystream__jobtemplate_id_c05e0b6c_fk_main_jobt FOREIGN KEY (jobtemplate_id) REFERENCES public.main_jobtemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_notification main_activitystream__notification_id_bbfaa8ac_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_notification
    ADD CONSTRAINT main_activitystream__notification_id_bbfaa8ac_fk_main_noti FOREIGN KEY (notification_id) REFERENCES public.main_notification(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_notification_template main_activitystream__notificationtemplate_96d11a5d_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_notification_template
    ADD CONSTRAINT main_activitystream__notificationtemplate_96d11a5d_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_o_auth2_access_token main_activitystream__oauth2accesstoken_id_76c333c8_fk_main_oaut; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_o_auth2_access_token
    ADD CONSTRAINT main_activitystream__oauth2accesstoken_id_76c333c8_fk_main_oaut FOREIGN KEY (oauth2accesstoken_id) REFERENCES public.main_oauth2accesstoken(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_o_auth2_application main_activitystream__oauth2application_id_23726fd8_fk_main_oaut; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_o_auth2_application
    ADD CONSTRAINT main_activitystream__oauth2application_id_23726fd8_fk_main_oaut FOREIGN KEY (oauth2application_id) REFERENCES public.main_oauth2application(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_organization main_activitystream__organization_id_8ccdfd12_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_organization
    ADD CONSTRAINT main_activitystream__organization_id_8ccdfd12_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_project main_activitystream__project_id_836f7b93_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_project
    ADD CONSTRAINT main_activitystream__project_id_836f7b93_fk_main_proj FOREIGN KEY (project_id) REFERENCES public.main_project(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_project_update main_activitystream__projectupdate_id_8ac4ba92_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_project_update
    ADD CONSTRAINT main_activitystream__projectupdate_id_8ac4ba92_fk_main_proj FOREIGN KEY (projectupdate_id) REFERENCES public.main_projectupdate(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_schedule main_activitystream__schedule_id_9bde99e8_fk_main_sche; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_schedule
    ADD CONSTRAINT main_activitystream__schedule_id_9bde99e8_fk_main_sche FOREIGN KEY (schedule_id) REFERENCES public.main_schedule(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_unified_job main_activitystream__unifiedjob_id_bd9f07c6_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_unified_job
    ADD CONSTRAINT main_activitystream__unifiedjob_id_bd9f07c6_fk_main_unif FOREIGN KEY (unifiedjob_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_unified_job_template main_activitystream__unifiedjobtemplate_i_71f8a21f_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_unified_job_template
    ADD CONSTRAINT main_activitystream__unifiedjobtemplate_i_71f8a21f_fk_main_unif FOREIGN KEY (unifiedjobtemplate_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_approval main_activitystream__workflowapproval_id_8d4193a7_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_approval
    ADD CONSTRAINT main_activitystream__workflowapproval_id_8d4193a7_fk_main_work FOREIGN KEY (workflowapproval_id) REFERENCES public.main_workflowapproval(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_approval_template main_activitystream__workflowapprovaltemp_93e9e097_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_approval_template
    ADD CONSTRAINT main_activitystream__workflowapprovaltemp_93e9e097_fk_main_work FOREIGN KEY (workflowapprovaltemplate_id) REFERENCES public.main_workflowapprovaltemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_job main_activitystream__workflowjob_id_c29366d7_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job
    ADD CONSTRAINT main_activitystream__workflowjob_id_c29366d7_fk_main_work FOREIGN KEY (workflowjob_id) REFERENCES public.main_workflowjob(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_job_node main_activitystream__workflowjobnode_id_85bb51d6_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_node
    ADD CONSTRAINT main_activitystream__workflowjobnode_id_85bb51d6_fk_main_work FOREIGN KEY (workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_job_template main_activitystream__workflowjobtemplate__efd4c1aa_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_template
    ADD CONSTRAINT main_activitystream__workflowjobtemplate__efd4c1aa_fk_main_work FOREIGN KEY (workflowjobtemplate_id) REFERENCES public.main_workflowjobtemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_workflow_job_template_node main_activitystream__workflowjobtemplaten_a2630ab6_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_workflow_job_template_node
    ADD CONSTRAINT main_activitystream__workflowjobtemplaten_a2630ab6_fk_main_work FOREIGN KEY (workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream main_activitystream_actor_id_29aafc0f_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream
    ADD CONSTRAINT main_activitystream_actor_id_29aafc0f_fk_auth_user_id FOREIGN KEY (actor_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_group main_activitystream_group_group_id_fd48b400_fk_main_group_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_group
    ADD CONSTRAINT main_activitystream_group_group_id_fd48b400_fk_main_group_id FOREIGN KEY (group_id) REFERENCES public.main_group(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_host main_activitystream_host_host_id_0e598602_fk_main_host_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_host
    ADD CONSTRAINT main_activitystream_host_host_id_0e598602_fk_main_host_id FOREIGN KEY (host_id) REFERENCES public.main_host(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_label main_activitystream_label_label_id_b33683fb_fk_main_label_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_label
    ADD CONSTRAINT main_activitystream_label_label_id_b33683fb_fk_main_label_id FOREIGN KEY (label_id) REFERENCES public.main_label(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_role main_activitystream_role_role_id_e19fce37_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_role
    ADD CONSTRAINT main_activitystream_role_role_id_e19fce37_fk_main_rbac_roles_id FOREIGN KEY (role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_team main_activitystream_team_team_id_725f033a_fk_main_team_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_team
    ADD CONSTRAINT main_activitystream_team_team_id_725f033a_fk_main_team_id FOREIGN KEY (team_id) REFERENCES public.main_team(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_activitystream_user main_activitystream_user_user_id_435f8320_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_activitystream_user
    ADD CONSTRAINT main_activitystream_user_user_id_435f8320_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_adhoccommand main_adhoccommand_credential_id_da6b1c87_fk_main_credential_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_adhoccommand
    ADD CONSTRAINT main_adhoccommand_credential_id_da6b1c87_fk_main_credential_id FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_adhoccommand main_adhoccommand_inventory_id_b29bba0e_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_adhoccommand
    ADD CONSTRAINT main_adhoccommand_inventory_id_b29bba0e_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_adhoccommand main_adhoccommand_unifiedjob_ptr_id_ef80f1dd_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_adhoccommand
    ADD CONSTRAINT main_adhoccommand_unifiedjob_ptr_id_ef80f1dd_fk_main_unif FOREIGN KEY (unifiedjob_ptr_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: _unpartitioned_main_adhoccommandevent main_adhoccommandeve_ad_hoc_command_id_1721f1e2_fk_main_adho; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_adhoccommandevent
    ADD CONSTRAINT main_adhoccommandeve_ad_hoc_command_id_1721f1e2_fk_main_adho FOREIGN KEY (ad_hoc_command_id) REFERENCES public.main_adhoccommand(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: _unpartitioned_main_adhoccommandevent main_adhoccommandevent_host_id_5613e329_fk_main_host_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_adhoccommandevent
    ADD CONSTRAINT main_adhoccommandevent_host_id_5613e329_fk_main_host_id FOREIGN KEY (host_id) REFERENCES public.main_host(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credential main_credential_admin_role_id_6cd7ab86_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_admin_role_id_6cd7ab86_fk_main_rbac_roles_id FOREIGN KEY (admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credential main_credential_created_by_id_237add04_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_created_by_id_237add04_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credential main_credential_credential_type_id_0120654c_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_credential_type_id_0120654c_fk_main_cred FOREIGN KEY (credential_type_id) REFERENCES public.main_credentialtype(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credential main_credential_modified_by_id_c290955a_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_modified_by_id_c290955a_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credential main_credential_organization_id_18d4ae89_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_organization_id_18d4ae89_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credential main_credential_read_role_id_12be41a2_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_read_role_id_12be41a2_fk_main_rbac_roles_id FOREIGN KEY (read_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credential main_credential_use_role_id_122159d4_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credential
    ADD CONSTRAINT main_credential_use_role_id_122159d4_fk_main_rbac_roles_id FOREIGN KEY (use_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credentialinputsource main_credentialinput_created_by_id_d2dc637c_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialinputsource
    ADD CONSTRAINT main_credentialinput_created_by_id_d2dc637c_fk_auth_user FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credentialinputsource main_credentialinput_modified_by_id_e3fd88dd_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialinputsource
    ADD CONSTRAINT main_credentialinput_modified_by_id_e3fd88dd_fk_auth_user FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credentialinputsource main_credentialinput_source_credential_id_868d93af_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialinputsource
    ADD CONSTRAINT main_credentialinput_source_credential_id_868d93af_fk_main_cred FOREIGN KEY (source_credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credentialinputsource main_credentialinput_target_credential_id_4bf0e248_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialinputsource
    ADD CONSTRAINT main_credentialinput_target_credential_id_4bf0e248_fk_main_cred FOREIGN KEY (target_credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credentialtype main_credentialtype_created_by_id_0f8451ed_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialtype
    ADD CONSTRAINT main_credentialtype_created_by_id_0f8451ed_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_credentialtype main_credentialtype_modified_by_id_b425580d_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_credentialtype
    ADD CONSTRAINT main_credentialtype_modified_by_id_b425580d_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_custominventoryscript main_custominventory_created_by_id_45a39526_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_custominventoryscript
    ADD CONSTRAINT main_custominventory_created_by_id_45a39526_fk_auth_user FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_custominventoryscript main_custominventory_modified_by_id_6c74f1d0_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_custominventoryscript
    ADD CONSTRAINT main_custominventory_modified_by_id_6c74f1d0_fk_auth_user FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_executionenvironment main_executionenviro_created_by_id_3808c16f_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_executionenvironment
    ADD CONSTRAINT main_executionenviro_created_by_id_3808c16f_fk_auth_user FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_executionenvironment main_executionenviro_credential_id_e91204b4_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_executionenvironment
    ADD CONSTRAINT main_executionenviro_credential_id_e91204b4_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_executionenvironment main_executionenviro_modified_by_id_fa58a43d_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_executionenvironment
    ADD CONSTRAINT main_executionenviro_modified_by_id_fa58a43d_fk_auth_user FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_executionenvironment main_executionenviro_organization_id_66056df5_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_executionenvironment
    ADD CONSTRAINT main_executionenviro_organization_id_66056df5_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group main_group_created_by_id_326129d5_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group
    ADD CONSTRAINT main_group_created_by_id_326129d5_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group_hosts main_group_hosts_group_id_524c3b29_fk_main_group_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_hosts
    ADD CONSTRAINT main_group_hosts_group_id_524c3b29_fk_main_group_id FOREIGN KEY (group_id) REFERENCES public.main_group(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group_hosts main_group_hosts_host_id_672eaed0_fk_main_host_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_hosts
    ADD CONSTRAINT main_group_hosts_host_id_672eaed0_fk_main_host_id FOREIGN KEY (host_id) REFERENCES public.main_host(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group main_group_inventory_id_f9e83725_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group
    ADD CONSTRAINT main_group_inventory_id_f9e83725_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group_inventory_sources main_group_inventory_inventorysource_id_5da14efc_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_inventory_sources
    ADD CONSTRAINT main_group_inventory_inventorysource_id_5da14efc_fk_main_inve FOREIGN KEY (inventorysource_id) REFERENCES public.main_inventorysource(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group_inventory_sources main_group_inventory_sources_group_id_1be295c4_fk_main_group_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_inventory_sources
    ADD CONSTRAINT main_group_inventory_sources_group_id_1be295c4_fk_main_group_id FOREIGN KEY (group_id) REFERENCES public.main_group(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group main_group_modified_by_id_20a1b654_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group
    ADD CONSTRAINT main_group_modified_by_id_20a1b654_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group_parents main_group_parents_from_group_id_9d63324d_fk_main_group_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_parents
    ADD CONSTRAINT main_group_parents_from_group_id_9d63324d_fk_main_group_id FOREIGN KEY (from_group_id) REFERENCES public.main_group(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_group_parents main_group_parents_to_group_id_851cc1ce_fk_main_group_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_group_parents
    ADD CONSTRAINT main_group_parents_to_group_id_851cc1ce_fk_main_group_id FOREIGN KEY (to_group_id) REFERENCES public.main_group(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_host main_host_created_by_id_2b5e0abe_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host
    ADD CONSTRAINT main_host_created_by_id_2b5e0abe_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_host_inventory_sources main_host_inventory__inventorysource_id_b25d3959_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host_inventory_sources
    ADD CONSTRAINT main_host_inventory__inventorysource_id_b25d3959_fk_main_inve FOREIGN KEY (inventorysource_id) REFERENCES public.main_inventorysource(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_host main_host_inventory_id_e5bcdb08_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host
    ADD CONSTRAINT main_host_inventory_id_e5bcdb08_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_host_inventory_sources main_host_inventory_sources_host_id_03f0dcdc_fk_main_host_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host_inventory_sources
    ADD CONSTRAINT main_host_inventory_sources_host_id_03f0dcdc_fk_main_host_id FOREIGN KEY (host_id) REFERENCES public.main_host(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_host main_host_last_job_host_summar_b8bd727d_fk_main_jobh; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host
    ADD CONSTRAINT main_host_last_job_host_summar_b8bd727d_fk_main_jobh FOREIGN KEY (last_job_host_summary_id) REFERENCES public.main_jobhostsummary(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_host main_host_last_job_id_d247075b_fk_main_job_unifiedjob_ptr_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host
    ADD CONSTRAINT main_host_last_job_id_d247075b_fk_main_job_unifiedjob_ptr_id FOREIGN KEY (last_job_id) REFERENCES public.main_job(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_host main_host_modified_by_id_28b76283_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_host
    ADD CONSTRAINT main_host_modified_by_id_28b76283_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_instancegroup main_instancegroup_admin_role_id_03760535_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup
    ADD CONSTRAINT main_instancegroup_admin_role_id_03760535_fk_main_rbac_roles_id FOREIGN KEY (admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_instancegroup main_instancegroup_credential_id_98351d10_fk_main_credential_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup
    ADD CONSTRAINT main_instancegroup_credential_id_98351d10_fk_main_credential_id FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_instancegroup_instances main_instancegroup_i_instance_id_d41cb05c_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup_instances
    ADD CONSTRAINT main_instancegroup_i_instance_id_d41cb05c_fk_main_inst FOREIGN KEY (instance_id) REFERENCES public.main_instance(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_instancegroup_instances main_instancegroup_i_instancegroup_id_b4b19635_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup_instances
    ADD CONSTRAINT main_instancegroup_i_instancegroup_id_b4b19635_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_instancegroup main_instancegroup_read_role_id_139c801e_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup
    ADD CONSTRAINT main_instancegroup_read_role_id_139c801e_fk_main_rbac_roles_id FOREIGN KEY (read_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_instancegroup main_instancegroup_use_role_id_48ea7ecc_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancegroup
    ADD CONSTRAINT main_instancegroup_use_role_id_48ea7ecc_fk_main_rbac_roles_id FOREIGN KEY (use_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_instancelink main_instancelink_source_id_29f35cad_fk_main_instance_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancelink
    ADD CONSTRAINT main_instancelink_source_id_29f35cad_fk_main_instance_id FOREIGN KEY (source_id) REFERENCES public.main_instance(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_instancelink main_instancelink_target_id_0ee650b4_fk_main_instance_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_instancelink
    ADD CONSTRAINT main_instancelink_target_id_0ee650b4_fk_main_instance_id FOREIGN KEY (target_id) REFERENCES public.main_instance(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory main_inventory_adhoc_role_id_b57042aa_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_adhoc_role_id_b57042aa_fk_main_rbac_roles_id FOREIGN KEY (adhoc_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory main_inventory_admin_role_id_3bb301cb_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_admin_role_id_3bb301cb_fk_main_rbac_roles_id FOREIGN KEY (admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory main_inventory_created_by_id_5d690781_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_created_by_id_5d690781_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory_labels main_inventory_label_inventory_id_3c7ecb7a_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory_labels
    ADD CONSTRAINT main_inventory_label_inventory_id_3c7ecb7a_fk_main_inve FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory_labels main_inventory_labels_label_id_0ab1cd80_fk_main_label_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory_labels
    ADD CONSTRAINT main_inventory_labels_label_id_0ab1cd80_fk_main_label_id FOREIGN KEY (label_id) REFERENCES public.main_label(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory main_inventory_modified_by_id_a4a91734_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_modified_by_id_a4a91734_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory main_inventory_organization_id_3ee77ea9_fk_main_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_organization_id_3ee77ea9_fk_main_organization_id FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory main_inventory_read_role_id_270dd070_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_read_role_id_270dd070_fk_main_rbac_roles_id FOREIGN KEY (read_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory main_inventory_update_role_id_be0903a1_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_update_role_id_be0903a1_fk_main_rbac_roles_id FOREIGN KEY (update_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventory main_inventory_use_role_id_77407b26_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventory
    ADD CONSTRAINT main_inventory_use_role_id_77407b26_fk_main_rbac_roles_id FOREIGN KEY (use_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventoryconstructedinventorymembership main_inventoryconstr_constructed_inventor_7f494472_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryconstructedinventorymembership
    ADD CONSTRAINT main_inventoryconstr_constructed_inventor_7f494472_fk_main_inve FOREIGN KEY (constructed_inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventoryconstructedinventorymembership main_inventoryconstr_input_inventory_id_fc428cbb_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryconstructedinventorymembership
    ADD CONSTRAINT main_inventoryconstr_input_inventory_id_fc428cbb_fk_main_inve FOREIGN KEY (input_inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventoryinstancegroupmembership main_inventoryinstan_instancegroup_id_8c752e87_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryinstancegroupmembership
    ADD CONSTRAINT main_inventoryinstan_instancegroup_id_8c752e87_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventoryinstancegroupmembership main_inventoryinstan_inventory_id_76a877b6_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryinstancegroupmembership
    ADD CONSTRAINT main_inventoryinstan_inventory_id_76a877b6_fk_main_inve FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventorysource main_inventorysource_inventory_id_3c1cac19_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventorysource
    ADD CONSTRAINT main_inventorysource_inventory_id_3c1cac19_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventorysource main_inventorysource_source_project_id_5b9c4374_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventorysource
    ADD CONSTRAINT main_inventorysource_source_project_id_5b9c4374_fk_main_proj FOREIGN KEY (source_project_id) REFERENCES public.main_project(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventorysource main_inventorysource_unifiedjobtemplate_p_6a11d509_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventorysource
    ADD CONSTRAINT main_inventorysource_unifiedjobtemplate_p_6a11d509_fk_main_unif FOREIGN KEY (unifiedjobtemplate_ptr_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventoryupdate main_inventoryupdate_inventory_id_e60f1f2e_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryupdate
    ADD CONSTRAINT main_inventoryupdate_inventory_id_e60f1f2e_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventoryupdate main_inventoryupdate_inventory_source_id_bc4b2567_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryupdate
    ADD CONSTRAINT main_inventoryupdate_inventory_source_id_bc4b2567_fk_main_inve FOREIGN KEY (inventory_source_id) REFERENCES public.main_inventorysource(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: _unpartitioned_main_inventoryupdateevent main_inventoryupdate_inventory_update_id_8974f1f7_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_inventoryupdateevent
    ADD CONSTRAINT main_inventoryupdate_inventory_update_id_8974f1f7_fk_main_inve FOREIGN KEY (inventory_update_id) REFERENCES public.main_inventoryupdate(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventoryupdate main_inventoryupdate_source_project_updat_b896d555_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryupdate
    ADD CONSTRAINT main_inventoryupdate_source_project_updat_b896d555_fk_main_proj FOREIGN KEY (source_project_update_id) REFERENCES public.main_projectupdate(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_inventoryupdate main_inventoryupdate_unifiedjob_ptr_id_a42ff4c2_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_inventoryupdate
    ADD CONSTRAINT main_inventoryupdate_unifiedjob_ptr_id_a42ff4c2_fk_main_unif FOREIGN KEY (unifiedjob_ptr_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_job main_job_inventory_id_1b436658_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_job
    ADD CONSTRAINT main_job_inventory_id_1b436658_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_job main_job_job_template_id_070b0d56_fk_main_jobt; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_job
    ADD CONSTRAINT main_job_job_template_id_070b0d56_fk_main_jobt FOREIGN KEY (job_template_id) REFERENCES public.main_jobtemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_job main_job_project_id_a8f63894_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_job
    ADD CONSTRAINT main_job_project_id_a8f63894_fk_main_proj FOREIGN KEY (project_id) REFERENCES public.main_project(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_job main_job_project_update_id_5adf90ad_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_job
    ADD CONSTRAINT main_job_project_update_id_5adf90ad_fk_main_proj FOREIGN KEY (project_update_id) REFERENCES public.main_projectupdate(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_job main_job_unifiedjob_ptr_id_46108a43_fk_main_unifiedjob_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_job
    ADD CONSTRAINT main_job_unifiedjob_ptr_id_46108a43_fk_main_unifiedjob_id FOREIGN KEY (unifiedjob_ptr_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_job main_job_webhook_credential_id_40ca94fa_fk_main_credential_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_job
    ADD CONSTRAINT main_job_webhook_credential_id_40ca94fa_fk_main_credential_id FOREIGN KEY (webhook_credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: _unpartitioned_main_jobevent main_jobevent_host_id_b03b6059_fk_main_host_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_jobevent
    ADD CONSTRAINT main_jobevent_host_id_b03b6059_fk_main_host_id FOREIGN KEY (host_id) REFERENCES public.main_host(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: _unpartitioned_main_jobevent main_jobevent_job_id_571587e8_fk_main_job_unifiedjob_ptr_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_jobevent
    ADD CONSTRAINT main_jobevent_job_id_571587e8_fk_main_job_unifiedjob_ptr_id FOREIGN KEY (job_id) REFERENCES public.main_job(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobhostsummary main_jobhostsummary_constructed_host_id_8ec8dc05_fk_main_host; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobhostsummary
    ADD CONSTRAINT main_jobhostsummary_constructed_host_id_8ec8dc05_fk_main_host FOREIGN KEY (constructed_host_id) REFERENCES public.main_host(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobhostsummary main_jobhostsummary_host_id_7d9f6bf9_fk_main_host_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobhostsummary
    ADD CONSTRAINT main_jobhostsummary_host_id_7d9f6bf9_fk_main_host_id FOREIGN KEY (host_id) REFERENCES public.main_host(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobhostsummary main_jobhostsummary_job_id_8d60afa0_fk_main_job_; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobhostsummary
    ADD CONSTRAINT main_jobhostsummary_job_id_8d60afa0_fk_main_job_ FOREIGN KEY (job_id) REFERENCES public.main_job(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfig_credentials main_joblaunchconfig_credential_id_2f5c0487_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig_credentials
    ADD CONSTRAINT main_joblaunchconfig_credential_id_2f5c0487_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfig main_joblaunchconfig_execution_environmen_ddf8eeec_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig
    ADD CONSTRAINT main_joblaunchconfig_execution_environmen_ddf8eeec_fk_main_exec FOREIGN KEY (execution_environment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfiginstancegroupmembership main_joblaunchconfig_instancegroup_id_e76ac8f9_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfiginstancegroupmembership
    ADD CONSTRAINT main_joblaunchconfig_instancegroup_id_e76ac8f9_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfig main_joblaunchconfig_inventory_id_f905306d_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig
    ADD CONSTRAINT main_joblaunchconfig_inventory_id_f905306d_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfig main_joblaunchconfig_job_id_6e18fad4_fk_main_unifiedjob_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig
    ADD CONSTRAINT main_joblaunchconfig_job_id_6e18fad4_fk_main_unifiedjob_id FOREIGN KEY (job_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfig_labels main_joblaunchconfig_joblaunchconfig_id_004bb969_fk_main_jobl; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig_labels
    ADD CONSTRAINT main_joblaunchconfig_joblaunchconfig_id_004bb969_fk_main_jobl FOREIGN KEY (joblaunchconfig_id) REFERENCES public.main_joblaunchconfig(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfig_credentials main_joblaunchconfig_joblaunchconfig_id_37dc31b9_fk_main_jobl; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig_credentials
    ADD CONSTRAINT main_joblaunchconfig_joblaunchconfig_id_37dc31b9_fk_main_jobl FOREIGN KEY (joblaunchconfig_id) REFERENCES public.main_joblaunchconfig(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfiginstancegroupmembership main_joblaunchconfig_joblaunchconfig_id_93eb971f_fk_main_jobl; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfiginstancegroupmembership
    ADD CONSTRAINT main_joblaunchconfig_joblaunchconfig_id_93eb971f_fk_main_jobl FOREIGN KEY (joblaunchconfig_id) REFERENCES public.main_joblaunchconfig(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_joblaunchconfig_labels main_joblaunchconfig_labels_label_id_5a9a600e_fk_main_label_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_joblaunchconfig_labels
    ADD CONSTRAINT main_joblaunchconfig_labels_label_id_5a9a600e_fk_main_label_id FOREIGN KEY (label_id) REFERENCES public.main_label(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobtemplate main_jobtemplate_admin_role_id_f9dc66ce_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobtemplate
    ADD CONSTRAINT main_jobtemplate_admin_role_id_f9dc66ce_fk_main_rbac_roles_id FOREIGN KEY (admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobtemplate main_jobtemplate_execute_role_id_c2f0db2c_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobtemplate
    ADD CONSTRAINT main_jobtemplate_execute_role_id_c2f0db2c_fk_main_rbac_roles_id FOREIGN KEY (execute_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobtemplate main_jobtemplate_inventory_id_9b8df646_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobtemplate
    ADD CONSTRAINT main_jobtemplate_inventory_id_9b8df646_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobtemplate main_jobtemplate_project_id_36e80985_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobtemplate
    ADD CONSTRAINT main_jobtemplate_project_id_36e80985_fk_main_proj FOREIGN KEY (project_id) REFERENCES public.main_project(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobtemplate main_jobtemplate_read_role_id_0e489c81_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobtemplate
    ADD CONSTRAINT main_jobtemplate_read_role_id_0e489c81_fk_main_rbac_roles_id FOREIGN KEY (read_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobtemplate main_jobtemplate_unifiedjobtemplate_p_4d0a792f_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobtemplate
    ADD CONSTRAINT main_jobtemplate_unifiedjobtemplate_p_4d0a792f_fk_main_unif FOREIGN KEY (unifiedjobtemplate_ptr_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_jobtemplate main_jobtemplate_webhook_credential_i_eff7fb4b_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_jobtemplate
    ADD CONSTRAINT main_jobtemplate_webhook_credential_i_eff7fb4b_fk_main_cred FOREIGN KEY (webhook_credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_label main_label_created_by_id_201182c0_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_label
    ADD CONSTRAINT main_label_created_by_id_201182c0_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_label main_label_modified_by_id_7f9aac68_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_label
    ADD CONSTRAINT main_label_modified_by_id_7f9aac68_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_label main_label_organization_id_78a1bd27_fk_main_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_label
    ADD CONSTRAINT main_label_organization_id_78a1bd27_fk_main_organization_id FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_notification main_notification_notification_templat_9eed1d65_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_notification
    ADD CONSTRAINT main_notification_notification_templat_9eed1d65_fk_main_noti FOREIGN KEY (notification_template_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_notificationtemplate main_notificationtem_created_by_id_1f77983a_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_notificationtemplate
    ADD CONSTRAINT main_notificationtem_created_by_id_1f77983a_fk_auth_user FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_notificationtemplate main_notificationtem_modified_by_id_83c40510_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_notificationtemplate
    ADD CONSTRAINT main_notificationtem_modified_by_id_83c40510_fk_auth_user FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_notificationtemplate main_notificationtem_organization_id_15933abb_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_notificationtemplate
    ADD CONSTRAINT main_notificationtem_organization_id_15933abb_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_oauth2accesstoken main_oauth2accesstok_application_id_c21bc74c_fk_main_oaut; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2accesstoken
    ADD CONSTRAINT main_oauth2accesstok_application_id_c21bc74c_fk_main_oaut FOREIGN KEY (application_id) REFERENCES public.main_oauth2application(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_oauth2accesstoken main_oauth2accesstok_id_token_id_8a01d0b0_fk_oauth2_pr; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2accesstoken
    ADD CONSTRAINT main_oauth2accesstok_id_token_id_8a01d0b0_fk_oauth2_pr FOREIGN KEY (id_token_id) REFERENCES public.oauth2_provider_idtoken(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_oauth2accesstoken main_oauth2accesstok_source_refresh_token_0a8a7e3b_fk_oauth2_pr; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2accesstoken
    ADD CONSTRAINT main_oauth2accesstok_source_refresh_token_0a8a7e3b_fk_oauth2_pr FOREIGN KEY (source_refresh_token_id) REFERENCES public.oauth2_provider_refreshtoken(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_oauth2accesstoken main_oauth2accesstoken_user_id_71ee5fe6_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2accesstoken
    ADD CONSTRAINT main_oauth2accesstoken_user_id_71ee5fe6_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_oauth2application main_oauth2applicati_organization_id_5e579421_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2application
    ADD CONSTRAINT main_oauth2applicati_organization_id_5e579421_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_oauth2application main_oauth2application_user_id_c4dffdbb_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_oauth2application
    ADD CONSTRAINT main_oauth2application_user_id_c4dffdbb_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_admin_role_id_e3ffdd41_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_admin_role_id_e3ffdd41_fk_main_rbac_roles_id FOREIGN KEY (admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_approval_role_id_14c1d96f_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_approval_role_id_14c1d96f_fk_main_rbac FOREIGN KEY (approval_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_auditor_role_id_f912df0a_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_auditor_role_id_f912df0a_fk_main_rbac FOREIGN KEY (auditor_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_created_by_id_141da798_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_created_by_id_141da798_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_credential_admin_rol_55733eb5_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_credential_admin_rol_55733eb5_fk_main_rbac FOREIGN KEY (credential_admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_default_environment__1696aac2_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_default_environment__1696aac2_fk_main_exec FOREIGN KEY (default_environment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_execute_role_id_76038d3c_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_execute_role_id_76038d3c_fk_main_rbac FOREIGN KEY (execute_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_execution_environmen_f2351549_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_execution_environmen_f2351549_fk_main_rbac FOREIGN KEY (execution_environment_admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_inventory_admin_role_dae5c7e2_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_inventory_admin_role_dae5c7e2_fk_main_rbac FOREIGN KEY (inventory_admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_job_template_admin_r_25a265c4_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_job_template_admin_r_25a265c4_fk_main_rbac FOREIGN KEY (job_template_admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_member_role_id_201ff67a_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_member_role_id_201ff67a_fk_main_rbac_roles_id FOREIGN KEY (member_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_modified_by_id_dec7a500_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_modified_by_id_dec7a500_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization_notification_templates_started main_organization_no_notificationtemplate_1df2f173_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_started
    ADD CONSTRAINT main_organization_no_notificationtemplate_1df2f173_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization_notification_templates_approvals main_organization_no_notificationtemplate_392029b7_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_approvals
    ADD CONSTRAINT main_organization_no_notificationtemplate_392029b7_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization_notification_templates_success main_organization_no_notificationtemplate_4edd98c4_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_success
    ADD CONSTRAINT main_organization_no_notificationtemplate_4edd98c4_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization_notification_templates_error main_organization_no_notificationtemplate_7b1480c0_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_error
    ADD CONSTRAINT main_organization_no_notificationtemplate_7b1480c0_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization_notification_templates_approvals main_organization_no_organization_id_44a19957_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_approvals
    ADD CONSTRAINT main_organization_no_organization_id_44a19957_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization_notification_templates_started main_organization_no_organization_id_48a058ac_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_started
    ADD CONSTRAINT main_organization_no_organization_id_48a058ac_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization_notification_templates_error main_organization_no_organization_id_94b63d49_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_error
    ADD CONSTRAINT main_organization_no_organization_id_94b63d49_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization_notification_templates_success main_organization_no_organization_id_96635cd6_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization_notification_templates_success
    ADD CONSTRAINT main_organization_no_organization_id_96635cd6_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_notification_admin_r_c36d2f0e_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_notification_admin_r_c36d2f0e_fk_main_rbac FOREIGN KEY (notification_admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_project_admin_role_i_442cfebe_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_project_admin_role_i_442cfebe_fk_main_rbac FOREIGN KEY (project_admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_read_role_id_e143c386_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_read_role_id_e143c386_fk_main_rbac_roles_id FOREIGN KEY (read_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organization main_organization_workflow_admin_role__52011cd3_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organization
    ADD CONSTRAINT main_organization_workflow_admin_role__52011cd3_fk_main_rbac FOREIGN KEY (workflow_admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organizationgalaxycredentialmembership main_organizationgal_credential_id_7b6334f3_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organizationgalaxycredentialmembership
    ADD CONSTRAINT main_organizationgal_credential_id_7b6334f3_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organizationgalaxycredentialmembership main_organizationgal_organization_id_0fd9495c_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organizationgalaxycredentialmembership
    ADD CONSTRAINT main_organizationgal_organization_id_0fd9495c_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organizationinstancegroupmembership main_organizationins_instancegroup_id_526173a9_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organizationinstancegroupmembership
    ADD CONSTRAINT main_organizationins_instancegroup_id_526173a9_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_organizationinstancegroupmembership main_organizationins_organization_id_35633383_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_organizationinstancegroupmembership
    ADD CONSTRAINT main_organizationins_organization_id_35633383_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_profile main_profile_user_id_b40d720a_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_profile
    ADD CONSTRAINT main_profile_user_id_b40d720a_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_project main_project_admin_role_id_ba0e70c7_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_admin_role_id_ba0e70c7_fk_main_rbac_roles_id FOREIGN KEY (admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_project main_project_credential_id_370ba2a3_fk_main_credential_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_credential_id_370ba2a3_fk_main_credential_id FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_project main_project_default_environment__01467429_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_default_environment__01467429_fk_main_exec FOREIGN KEY (default_environment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_project main_project_read_role_id_39a01fd4_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_read_role_id_39a01fd4_fk_main_rbac_roles_id FOREIGN KEY (read_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_project main_project_signature_validation_41e77a69_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_signature_validation_41e77a69_fk_main_cred FOREIGN KEY (signature_validation_credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_project main_project_unifiedjobtemplate_p_078080eb_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_unifiedjobtemplate_p_078080eb_fk_main_unif FOREIGN KEY (unifiedjobtemplate_ptr_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_project main_project_update_role_id_36e33c42_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_update_role_id_36e33c42_fk_main_rbac_roles_id FOREIGN KEY (update_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_project main_project_use_role_id_7b6d9148_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_project
    ADD CONSTRAINT main_project_use_role_id_7b6d9148_fk_main_rbac_roles_id FOREIGN KEY (use_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_projectupdate main_projectupdate_credential_id_2f7d826a_fk_main_credential_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_projectupdate
    ADD CONSTRAINT main_projectupdate_credential_id_2f7d826a_fk_main_credential_id FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_projectupdate main_projectupdate_project_id_bdd73efe_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_projectupdate
    ADD CONSTRAINT main_projectupdate_project_id_bdd73efe_fk_main_proj FOREIGN KEY (project_id) REFERENCES public.main_project(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_projectupdate main_projectupdate_unifiedjob_ptr_id_039312cd_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_projectupdate
    ADD CONSTRAINT main_projectupdate_unifiedjob_ptr_id_039312cd_fk_main_unif FOREIGN KEY (unifiedjob_ptr_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: _unpartitioned_main_projectupdateevent main_projectupdateev_project_update_id_9d4358b2_fk_main_proj; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_projectupdateevent
    ADD CONSTRAINT main_projectupdateev_project_update_id_9d4358b2_fk_main_proj FOREIGN KEY (project_update_id) REFERENCES public.main_projectupdate(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_rbac_role_ancestors main_rbac_role_ances_ancestor_id_c6aae106_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_role_ancestors
    ADD CONSTRAINT main_rbac_role_ances_ancestor_id_c6aae106_fk_main_rbac FOREIGN KEY (ancestor_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_rbac_role_ancestors main_rbac_role_ances_descendent_id_23bfc463_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_role_ancestors
    ADD CONSTRAINT main_rbac_role_ances_descendent_id_23bfc463_fk_main_rbac FOREIGN KEY (descendent_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_rbac_roles main_rbac_roles_content_type_id_756d6b30_fk_django_co; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles
    ADD CONSTRAINT main_rbac_roles_content_type_id_756d6b30_fk_django_co FOREIGN KEY (content_type_id) REFERENCES public.django_content_type(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_rbac_roles_members main_rbac_roles_members_role_id_7318b4b7_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles_members
    ADD CONSTRAINT main_rbac_roles_members_role_id_7318b4b7_fk_main_rbac_roles_id FOREIGN KEY (role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_rbac_roles_members main_rbac_roles_members_user_id_f5e05418_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles_members
    ADD CONSTRAINT main_rbac_roles_members_user_id_f5e05418_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_rbac_roles_parents main_rbac_roles_pare_from_role_id_a02db9eb_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles_parents
    ADD CONSTRAINT main_rbac_roles_pare_from_role_id_a02db9eb_fk_main_rbac FOREIGN KEY (from_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_rbac_roles_parents main_rbac_roles_pare_to_role_id_c00b5087_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_rbac_roles_parents
    ADD CONSTRAINT main_rbac_roles_pare_to_role_id_c00b5087_fk_main_rbac FOREIGN KEY (to_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule main_schedule_created_by_id_4e647be2_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule
    ADD CONSTRAINT main_schedule_created_by_id_4e647be2_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule_credentials main_schedule_creden_credential_id_ced5894e_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule_credentials
    ADD CONSTRAINT main_schedule_creden_credential_id_ced5894e_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule_credentials main_schedule_creden_schedule_id_03ecad04_fk_main_sche; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule_credentials
    ADD CONSTRAINT main_schedule_creden_schedule_id_03ecad04_fk_main_sche FOREIGN KEY (schedule_id) REFERENCES public.main_schedule(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule main_schedule_execution_environmen_90eefd45_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule
    ADD CONSTRAINT main_schedule_execution_environmen_90eefd45_fk_main_exec FOREIGN KEY (execution_environment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule main_schedule_inventory_id_43b7b69d_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule
    ADD CONSTRAINT main_schedule_inventory_id_43b7b69d_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule_labels main_schedule_labels_label_id_79a46df6_fk_main_label_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule_labels
    ADD CONSTRAINT main_schedule_labels_label_id_79a46df6_fk_main_label_id FOREIGN KEY (label_id) REFERENCES public.main_label(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule_labels main_schedule_labels_schedule_id_e2f00ec6_fk_main_schedule_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule_labels
    ADD CONSTRAINT main_schedule_labels_schedule_id_e2f00ec6_fk_main_schedule_id FOREIGN KEY (schedule_id) REFERENCES public.main_schedule(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule main_schedule_modified_by_id_3817bc47_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule
    ADD CONSTRAINT main_schedule_modified_by_id_3817bc47_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_schedule main_schedule_unified_job_template_a9d931e2_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_schedule
    ADD CONSTRAINT main_schedule_unified_job_template_a9d931e2_fk_main_unif FOREIGN KEY (unified_job_template_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_scheduleinstancegroupmembership main_scheduleinstanc_instancegroup_id_2d5f236c_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_scheduleinstancegroupmembership
    ADD CONSTRAINT main_scheduleinstanc_instancegroup_id_2d5f236c_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_scheduleinstancegroupmembership main_scheduleinstanc_schedule_id_d8eb2c41_fk_main_sche; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_scheduleinstancegroupmembership
    ADD CONSTRAINT main_scheduleinstanc_schedule_id_d8eb2c41_fk_main_sche FOREIGN KEY (schedule_id) REFERENCES public.main_schedule(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_smartinventorymembership main_smartinventorym_inventory_id_5e13df96_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_smartinventorymembership
    ADD CONSTRAINT main_smartinventorym_inventory_id_5e13df96_fk_main_inve FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_smartinventorymembership main_smartinventorymembership_host_id_c721cb8a_fk_main_host_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_smartinventorymembership
    ADD CONSTRAINT main_smartinventorymembership_host_id_c721cb8a_fk_main_host_id FOREIGN KEY (host_id) REFERENCES public.main_host(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_systemjob main_systemjob_system_job_template__8bba2060_fk_main_syst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_systemjob
    ADD CONSTRAINT main_systemjob_system_job_template__8bba2060_fk_main_syst FOREIGN KEY (system_job_template_id) REFERENCES public.main_systemjobtemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_systemjob main_systemjob_unifiedjob_ptr_id_9517b368_fk_main_unifiedjob_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_systemjob
    ADD CONSTRAINT main_systemjob_unifiedjob_ptr_id_9517b368_fk_main_unifiedjob_id FOREIGN KEY (unifiedjob_ptr_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: _unpartitioned_main_systemjobevent main_systemjobevent_system_job_id_91bbbfc1_fk_main_syst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public._unpartitioned_main_systemjobevent
    ADD CONSTRAINT main_systemjobevent_system_job_id_91bbbfc1_fk_main_syst FOREIGN KEY (system_job_id) REFERENCES public.main_systemjob(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_systemjobtemplate main_systemjobtempla_unifiedjobtemplate_p_60f12f55_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_systemjobtemplate
    ADD CONSTRAINT main_systemjobtempla_unifiedjobtemplate_p_60f12f55_fk_main_unif FOREIGN KEY (unifiedjobtemplate_ptr_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_team main_team_admin_role_id_a9e09a22_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_team
    ADD CONSTRAINT main_team_admin_role_id_a9e09a22_fk_main_rbac_roles_id FOREIGN KEY (admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_team main_team_created_by_id_c370350b_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_team
    ADD CONSTRAINT main_team_created_by_id_c370350b_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_team main_team_member_role_id_a2f93dc9_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_team
    ADD CONSTRAINT main_team_member_role_id_a2f93dc9_fk_main_rbac_roles_id FOREIGN KEY (member_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_team main_team_modified_by_id_9af533cd_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_team
    ADD CONSTRAINT main_team_modified_by_id_9af533cd_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_team main_team_organization_id_8b31bbc1_fk_main_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_team
    ADD CONSTRAINT main_team_organization_id_8b31bbc1_fk_main_organization_id FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_team main_team_read_role_id_ea02761f_fk_main_rbac_roles_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_team
    ADD CONSTRAINT main_team_read_role_id_ea02761f_fk_main_rbac_roles_id FOREIGN KEY (read_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob main_unifiedjob_created_by_id_d2a186ab_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_created_by_id_d2a186ab_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob_credentials main_unifiedjob_cred_credential_id_661c8f49_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_credentials
    ADD CONSTRAINT main_unifiedjob_cred_credential_id_661c8f49_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob_credentials main_unifiedjob_cred_unifiedjob_id_4ed7ff5d_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_credentials
    ADD CONSTRAINT main_unifiedjob_cred_unifiedjob_id_4ed7ff5d_fk_main_unif FOREIGN KEY (unifiedjob_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob_dependent_jobs main_unifiedjob_depe_from_unifiedjob_id_c8d58e88_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_dependent_jobs
    ADD CONSTRAINT main_unifiedjob_depe_from_unifiedjob_id_c8d58e88_fk_main_unif FOREIGN KEY (from_unifiedjob_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob_dependent_jobs main_unifiedjob_depe_to_unifiedjob_id_3f04cbcc_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_dependent_jobs
    ADD CONSTRAINT main_unifiedjob_depe_to_unifiedjob_id_3f04cbcc_fk_main_unif FOREIGN KEY (to_unifiedjob_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob main_unifiedjob_execution_environmen_b2eaf9c0_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_execution_environmen_b2eaf9c0_fk_main_exec FOREIGN KEY (execution_environment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob main_unifiedjob_instance_group_id_f76a06e2_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_instance_group_id_f76a06e2_fk_main_inst FOREIGN KEY (instance_group_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob_labels main_unifiedjob_labe_unifiedjob_id_bd008d37_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_labels
    ADD CONSTRAINT main_unifiedjob_labe_unifiedjob_id_bd008d37_fk_main_unif FOREIGN KEY (unifiedjob_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob_labels main_unifiedjob_labels_label_id_98814bad_fk_main_label_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_labels
    ADD CONSTRAINT main_unifiedjob_labels_label_id_98814bad_fk_main_label_id FOREIGN KEY (label_id) REFERENCES public.main_label(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob main_unifiedjob_modified_by_id_14cbb9bc_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_modified_by_id_14cbb9bc_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob_notifications main_unifiedjob_noti_notification_id_cf3498bf_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_notifications
    ADD CONSTRAINT main_unifiedjob_noti_notification_id_cf3498bf_fk_main_noti FOREIGN KEY (notification_id) REFERENCES public.main_notification(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob_notifications main_unifiedjob_noti_unifiedjob_id_65ab9c3c_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob_notifications
    ADD CONSTRAINT main_unifiedjob_noti_unifiedjob_id_65ab9c3c_fk_main_unif FOREIGN KEY (unifiedjob_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob main_unifiedjob_organization_id_cbfa01d3_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_organization_id_cbfa01d3_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob main_unifiedjob_polymorphic_ctype_id_cb46239b_fk_django_co; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_polymorphic_ctype_id_cb46239b_fk_django_co FOREIGN KEY (polymorphic_ctype_id) REFERENCES public.django_content_type(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob main_unifiedjob_schedule_id_766ca767_fk_main_schedule_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_schedule_id_766ca767_fk_main_schedule_id FOREIGN KEY (schedule_id) REFERENCES public.main_schedule(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjob main_unifiedjob_unified_job_template_a398b197_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjob
    ADD CONSTRAINT main_unifiedjob_unified_job_template_a398b197_fk_main_unif FOREIGN KEY (unified_job_template_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_credentials main_unifiedjobtempl_credential_id_fd216c80_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_credentials
    ADD CONSTRAINT main_unifiedjobtempl_credential_id_fd216c80_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate main_unifiedjobtempl_current_job_id_8f449ab0_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtempl_current_job_id_8f449ab0_fk_main_unif FOREIGN KEY (current_job_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate main_unifiedjobtempl_execution_environmen_bed25866_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtempl_execution_environmen_bed25866_fk_main_exec FOREIGN KEY (execution_environment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplateinstancegroupmembership main_unifiedjobtempl_instancegroup_id_656188b4_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplateinstancegroupmembership
    ADD CONSTRAINT main_unifiedjobtempl_instancegroup_id_656188b4_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_labels main_unifiedjobtempl_label_id_d6a5ee75_fk_main_labe; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_labels
    ADD CONSTRAINT main_unifiedjobtempl_label_id_d6a5ee75_fk_main_labe FOREIGN KEY (label_id) REFERENCES public.main_label(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate main_unifiedjobtempl_last_job_id_7e983743_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtempl_last_job_id_7e983743_fk_main_unif FOREIGN KEY (last_job_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate main_unifiedjobtempl_next_schedule_id_955ff55d_fk_main_sche; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtempl_next_schedule_id_955ff55d_fk_main_sche FOREIGN KEY (next_schedule_id) REFERENCES public.main_schedule(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_notification_templates_success main_unifiedjobtempl_notificationtemplate_9326cdf9_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_success
    ADD CONSTRAINT main_unifiedjobtempl_notificationtemplate_9326cdf9_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_notification_templates_started main_unifiedjobtempl_notificationtemplate_9793a63a_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_started
    ADD CONSTRAINT main_unifiedjobtempl_notificationtemplate_9793a63a_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_notification_templates_error main_unifiedjobtempl_notificationtemplate_b19df8ac_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_error
    ADD CONSTRAINT main_unifiedjobtempl_notificationtemplate_b19df8ac_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate main_unifiedjobtempl_organization_id_c63fa1a4_fk_main_orga; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtempl_organization_id_c63fa1a4_fk_main_orga FOREIGN KEY (organization_id) REFERENCES public.main_organization(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate main_unifiedjobtempl_polymorphic_ctype_id_ce19bb25_fk_django_co; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtempl_polymorphic_ctype_id_ce19bb25_fk_django_co FOREIGN KEY (polymorphic_ctype_id) REFERENCES public.django_content_type(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_notification_templates_error main_unifiedjobtempl_unifiedjobtemplate_i_0ce91b23_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_error
    ADD CONSTRAINT main_unifiedjobtempl_unifiedjobtemplate_i_0ce91b23_fk_main_unif FOREIGN KEY (unifiedjobtemplate_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_notification_templates_success main_unifiedjobtempl_unifiedjobtemplate_i_3934753d_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_success
    ADD CONSTRAINT main_unifiedjobtempl_unifiedjobtemplate_i_3934753d_fk_main_unif FOREIGN KEY (unifiedjobtemplate_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_notification_templates_started main_unifiedjobtempl_unifiedjobtemplate_i_6e21dce4_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_notification_templates_started
    ADD CONSTRAINT main_unifiedjobtempl_unifiedjobtemplate_i_6e21dce4_fk_main_unif FOREIGN KEY (unifiedjobtemplate_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_labels main_unifiedjobtempl_unifiedjobtemplate_i_c9307a9a_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_labels
    ADD CONSTRAINT main_unifiedjobtempl_unifiedjobtemplate_i_c9307a9a_fk_main_unif FOREIGN KEY (unifiedjobtemplate_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate_credentials main_unifiedjobtempl_unifiedjobtemplate_i_d98d7c79_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate_credentials
    ADD CONSTRAINT main_unifiedjobtempl_unifiedjobtemplate_i_d98d7c79_fk_main_unif FOREIGN KEY (unifiedjobtemplate_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplateinstancegroupmembership main_unifiedjobtempl_unifiedjobtemplate_i_e401e3d7_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplateinstancegroupmembership
    ADD CONSTRAINT main_unifiedjobtempl_unifiedjobtemplate_i_e401e3d7_fk_main_unif FOREIGN KEY (unifiedjobtemplate_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate main_unifiedjobtemplate_created_by_id_1f5fadfa_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtemplate_created_by_id_1f5fadfa_fk_auth_user_id FOREIGN KEY (created_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_unifiedjobtemplate main_unifiedjobtemplate_modified_by_id_a8bf1de0_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_unifiedjobtemplate
    ADD CONSTRAINT main_unifiedjobtemplate_modified_by_id_a8bf1de0_fk_auth_user_id FOREIGN KEY (modified_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_usersessionmembership main_usersessionmemb_session_id_fbab60a5_fk_django_se; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_usersessionmembership
    ADD CONSTRAINT main_usersessionmemb_session_id_fbab60a5_fk_django_se FOREIGN KEY (session_id) REFERENCES public.django_session(session_key) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_usersessionmembership main_usersessionmembership_user_id_fe163c98_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_usersessionmembership
    ADD CONSTRAINT main_usersessionmembership_user_id_fe163c98_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowapproval main_workflowapprova_approved_or_denied_b_bb3eae41_fk_auth_user; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowapproval
    ADD CONSTRAINT main_workflowapprova_approved_or_denied_b_bb3eae41_fk_auth_user FOREIGN KEY (approved_or_denied_by_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowapproval main_workflowapprova_unifiedjob_ptr_id_b8cd5385_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowapproval
    ADD CONSTRAINT main_workflowapprova_unifiedjob_ptr_id_b8cd5385_fk_main_unif FOREIGN KEY (unifiedjob_ptr_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowapprovaltemplate main_workflowapprova_unifiedjobtemplate_p_289f0768_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowapprovaltemplate
    ADD CONSTRAINT main_workflowapprova_unifiedjobtemplate_p_289f0768_fk_main_unif FOREIGN KEY (unifiedjobtemplate_ptr_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowapproval main_workflowapprova_workflow_approval_te_b87dda8a_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowapproval
    ADD CONSTRAINT main_workflowapprova_workflow_approval_te_b87dda8a_fk_main_work FOREIGN KEY (workflow_approval_template_id) REFERENCES public.main_workflowapprovaltemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjob main_workflowjob_inventory_id_8c31355b_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjob
    ADD CONSTRAINT main_workflowjob_inventory_id_8c31355b_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjob main_workflowjob_job_template_id_cceff2a3_fk_main_jobt; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjob
    ADD CONSTRAINT main_workflowjob_job_template_id_cceff2a3_fk_main_jobt FOREIGN KEY (job_template_id) REFERENCES public.main_jobtemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjob main_workflowjob_unifiedjob_ptr_id_2ee92d99_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjob
    ADD CONSTRAINT main_workflowjob_unifiedjob_ptr_id_2ee92d99_fk_main_unif FOREIGN KEY (unifiedjob_ptr_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjob main_workflowjob_webhook_credential_i_57c9fece_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjob
    ADD CONSTRAINT main_workflowjob_webhook_credential_i_57c9fece_fk_main_cred FOREIGN KEY (webhook_credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjob main_workflowjob_workflow_job_templat_0d9a93a0_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjob
    ADD CONSTRAINT main_workflowjob_workflow_job_templat_0d9a93a0_fk_main_work FOREIGN KEY (workflow_job_template_id) REFERENCES public.main_workflowjobtemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobinstancegroupmembership main_workflowjobinst_instancegroup_id_00dbe24d_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobinstancegroupmembership
    ADD CONSTRAINT main_workflowjobinst_instancegroup_id_00dbe24d_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobinstancegroupmembership main_workflowjobinst_workflowjobnode_id_e18bb569_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobinstancegroupmembership
    ADD CONSTRAINT main_workflowjobinst_workflowjobnode_id_e18bb569_fk_main_work FOREIGN KEY (workflowjobnode_id) REFERENCES public.main_workflowjob(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_credentials main_workflowjobnode_credential_id_6de5a410_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_credentials
    ADD CONSTRAINT main_workflowjobnode_credential_id_6de5a410_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode main_workflowjobnode_execution_environmen_c593ca11_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode
    ADD CONSTRAINT main_workflowjobnode_execution_environmen_c593ca11_fk_main_exec FOREIGN KEY (execution_environment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_always_nodes main_workflowjobnode_from_workflowjobnode_19edb9d7_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_always_nodes
    ADD CONSTRAINT main_workflowjobnode_from_workflowjobnode_19edb9d7_fk_main_work FOREIGN KEY (from_workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_failure_nodes main_workflowjobnode_from_workflowjobnode_2172a110_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_failure_nodes
    ADD CONSTRAINT main_workflowjobnode_from_workflowjobnode_2172a110_fk_main_work FOREIGN KEY (from_workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_success_nodes main_workflowjobnode_from_workflowjobnode_e04f9991_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_success_nodes
    ADD CONSTRAINT main_workflowjobnode_from_workflowjobnode_e04f9991_fk_main_work FOREIGN KEY (from_workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnodebaseinstancegroupmembership main_workflowjobnode_instancegroup_id_4e4faca5_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnodebaseinstancegroupmembership
    ADD CONSTRAINT main_workflowjobnode_instancegroup_id_4e4faca5_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode main_workflowjobnode_inventory_id_1dac2da9_fk_main_inventory_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode
    ADD CONSTRAINT main_workflowjobnode_inventory_id_1dac2da9_fk_main_inventory_id FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode main_workflowjobnode_job_id_7d2de427_fk_main_unifiedjob_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode
    ADD CONSTRAINT main_workflowjobnode_job_id_7d2de427_fk_main_unifiedjob_id FOREIGN KEY (job_id) REFERENCES public.main_unifiedjob(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_labels main_workflowjobnode_labels_label_id_0e6594a7_fk_main_label_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_labels
    ADD CONSTRAINT main_workflowjobnode_labels_label_id_0e6594a7_fk_main_label_id FOREIGN KEY (label_id) REFERENCES public.main_label(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_always_nodes main_workflowjobnode_to_workflowjobnode_i_0edcda07_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_always_nodes
    ADD CONSTRAINT main_workflowjobnode_to_workflowjobnode_i_0edcda07_fk_main_work FOREIGN KEY (to_workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_failure_nodes main_workflowjobnode_to_workflowjobnode_i_d2e09d9c_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_failure_nodes
    ADD CONSTRAINT main_workflowjobnode_to_workflowjobnode_i_d2e09d9c_fk_main_work FOREIGN KEY (to_workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_success_nodes main_workflowjobnode_to_workflowjobnode_i_e6c8cbb4_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_success_nodes
    ADD CONSTRAINT main_workflowjobnode_to_workflowjobnode_i_e6c8cbb4_fk_main_work FOREIGN KEY (to_workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode main_workflowjobnode_unified_job_template_8a30f93e_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode
    ADD CONSTRAINT main_workflowjobnode_unified_job_template_8a30f93e_fk_main_unif FOREIGN KEY (unified_job_template_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode main_workflowjobnode_workflow_job_id_dcd715c7_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode
    ADD CONSTRAINT main_workflowjobnode_workflow_job_id_dcd715c7_fk_main_work FOREIGN KEY (workflow_job_id) REFERENCES public.main_workflowjob(unifiedjob_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_labels main_workflowjobnode_workflowjobnode_id_14f419e1_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_labels
    ADD CONSTRAINT main_workflowjobnode_workflowjobnode_id_14f419e1_fk_main_work FOREIGN KEY (workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnode_credentials main_workflowjobnode_workflowjobnode_id_31f8c02b_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnode_credentials
    ADD CONSTRAINT main_workflowjobnode_workflowjobnode_id_31f8c02b_fk_main_work FOREIGN KEY (workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobnodebaseinstancegroupmembership main_workflowjobnode_workflowjobnode_id_47a05c0e_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobnodebaseinstancegroupmembership
    ADD CONSTRAINT main_workflowjobnode_workflowjobnode_id_47a05c0e_fk_main_work FOREIGN KEY (workflowjobnode_id) REFERENCES public.main_workflowjobnode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate main_workflowjobtemp_admin_role_id_5675a40e_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate
    ADD CONSTRAINT main_workflowjobtemp_admin_role_id_5675a40e_fk_main_rbac FOREIGN KEY (admin_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate main_workflowjobtemp_approval_role_id_220f0de1_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate
    ADD CONSTRAINT main_workflowjobtemp_approval_role_id_220f0de1_fk_main_rbac FOREIGN KEY (approval_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_credentials main_workflowjobtemp_credential_id_e621c8d1_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_credentials
    ADD CONSTRAINT main_workflowjobtemp_credential_id_e621c8d1_fk_main_cred FOREIGN KEY (credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate main_workflowjobtemp_execute_role_id_ad8970f4_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate
    ADD CONSTRAINT main_workflowjobtemp_execute_role_id_ad8970f4_fk_main_rbac FOREIGN KEY (execute_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode main_workflowjobtemp_execution_environmen_ec5bba6d_fk_main_exec; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode
    ADD CONSTRAINT main_workflowjobtemp_execution_environmen_ec5bba6d_fk_main_exec FOREIGN KEY (execution_environment_id) REFERENCES public.main_executionenvironment(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_always_nodes main_workflowjobtemp_from_workflowjobtemp_8af14c32_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_always_nodes
    ADD CONSTRAINT main_workflowjobtemp_from_workflowjobtemp_8af14c32_fk_main_work FOREIGN KEY (from_workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_success_nodes main_workflowjobtemp_from_workflowjobtemp_9e16f49d_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_success_nodes
    ADD CONSTRAINT main_workflowjobtemp_from_workflowjobtemp_9e16f49d_fk_main_work FOREIGN KEY (from_workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_failure_nodes main_workflowjobtemp_from_workflowjobtemp_fa405230_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_failure_nodes
    ADD CONSTRAINT main_workflowjobtemp_from_workflowjobtemp_fa405230_fk_main_work FOREIGN KEY (from_workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenodebaseinstancegroupmembership main_workflowjobtemp_instancegroup_id_0c59a80a_fk_main_inst; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenodebaseinstancegroupmembership
    ADD CONSTRAINT main_workflowjobtemp_instancegroup_id_0c59a80a_fk_main_inst FOREIGN KEY (instancegroup_id) REFERENCES public.main_instancegroup(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode main_workflowjobtemp_inventory_id_2fab864f_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode
    ADD CONSTRAINT main_workflowjobtemp_inventory_id_2fab864f_fk_main_inve FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate main_workflowjobtemp_inventory_id_99929499_fk_main_inve; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate
    ADD CONSTRAINT main_workflowjobtemp_inventory_id_99929499_fk_main_inve FOREIGN KEY (inventory_id) REFERENCES public.main_inventory(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_labels main_workflowjobtemp_label_id_b3f1a57f_fk_main_labe; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_labels
    ADD CONSTRAINT main_workflowjobtemp_label_id_b3f1a57f_fk_main_labe FOREIGN KEY (label_id) REFERENCES public.main_label(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate_notification_templates_approvals main_workflowjobtemp_notificationtemplate_3811d35e_fk_main_noti; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate_notification_templates_approvals
    ADD CONSTRAINT main_workflowjobtemp_notificationtemplate_3811d35e_fk_main_noti FOREIGN KEY (notificationtemplate_id) REFERENCES public.main_notificationtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate main_workflowjobtemp_read_role_id_acdd95ef_fk_main_rbac; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate
    ADD CONSTRAINT main_workflowjobtemp_read_role_id_acdd95ef_fk_main_rbac FOREIGN KEY (read_role_id) REFERENCES public.main_rbac_roles(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_failure_nodes main_workflowjobtemp_to_workflowjobtempla_2c1db0ae_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_failure_nodes
    ADD CONSTRAINT main_workflowjobtemp_to_workflowjobtempla_2c1db0ae_fk_main_work FOREIGN KEY (to_workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_always_nodes main_workflowjobtemp_to_workflowjobtempla_6fe11708_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_always_nodes
    ADD CONSTRAINT main_workflowjobtemp_to_workflowjobtempla_6fe11708_fk_main_work FOREIGN KEY (to_workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_success_nodes main_workflowjobtemp_to_workflowjobtempla_f16ee478_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_success_nodes
    ADD CONSTRAINT main_workflowjobtemp_to_workflowjobtempla_f16ee478_fk_main_work FOREIGN KEY (to_workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode main_workflowjobtemp_unified_job_template_98b53e6c_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode
    ADD CONSTRAINT main_workflowjobtemp_unified_job_template_98b53e6c_fk_main_unif FOREIGN KEY (unified_job_template_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate main_workflowjobtemp_unifiedjobtemplate_p_3854248b_fk_main_unif; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate
    ADD CONSTRAINT main_workflowjobtemp_unifiedjobtemplate_p_3854248b_fk_main_unif FOREIGN KEY (unifiedjobtemplate_ptr_id) REFERENCES public.main_unifiedjobtemplate(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate main_workflowjobtemp_webhook_credential_i_ced1ad89_fk_main_cred; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate
    ADD CONSTRAINT main_workflowjobtemp_webhook_credential_i_ced1ad89_fk_main_cred FOREIGN KEY (webhook_credential_id) REFERENCES public.main_credential(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode main_workflowjobtemp_workflow_job_templat_2fd591f0_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode
    ADD CONSTRAINT main_workflowjobtemp_workflow_job_templat_2fd591f0_fk_main_work FOREIGN KEY (workflow_job_template_id) REFERENCES public.main_workflowjobtemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplate_notification_templates_approvals main_workflowjobtemp_workflowjobtemplate__ce7a17be_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplate_notification_templates_approvals
    ADD CONSTRAINT main_workflowjobtemp_workflowjobtemplate__ce7a17be_fk_main_work FOREIGN KEY (workflowjobtemplate_id) REFERENCES public.main_workflowjobtemplate(unifiedjobtemplate_ptr_id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_credentials main_workflowjobtemp_workflowjobtemplaten_b91efe86_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_credentials
    ADD CONSTRAINT main_workflowjobtemp_workflowjobtemplaten_b91efe86_fk_main_work FOREIGN KEY (workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenode_labels main_workflowjobtemp_workflowjobtemplaten_f75998d4_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenode_labels
    ADD CONSTRAINT main_workflowjobtemp_workflowjobtemplaten_f75998d4_fk_main_work FOREIGN KEY (workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: main_workflowjobtemplatenodebaseinstancegroupmembership main_workflowjobtemp_workflowjobtemplaten_fa0959c5_fk_main_work; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.main_workflowjobtemplatenodebaseinstancegroupmembership
    ADD CONSTRAINT main_workflowjobtemp_workflowjobtemplaten_fa0959c5_fk_main_work FOREIGN KEY (workflowjobtemplatenode_id) REFERENCES public.main_workflowjobtemplatenode(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: oauth2_provider_grant oauth2_provider_gran_application_id_81923564_fk_main_oaut; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_grant
    ADD CONSTRAINT oauth2_provider_gran_application_id_81923564_fk_main_oaut FOREIGN KEY (application_id) REFERENCES public.main_oauth2application(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: oauth2_provider_grant oauth2_provider_grant_user_id_e8f62af8_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_grant
    ADD CONSTRAINT oauth2_provider_grant_user_id_e8f62af8_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: oauth2_provider_idtoken oauth2_provider_idto_application_id_08c5ff4f_fk_main_oaut; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_idtoken
    ADD CONSTRAINT oauth2_provider_idto_application_id_08c5ff4f_fk_main_oaut FOREIGN KEY (application_id) REFERENCES public.main_oauth2application(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: oauth2_provider_idtoken oauth2_provider_idtoken_user_id_dd512b59_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_idtoken
    ADD CONSTRAINT oauth2_provider_idtoken_user_id_dd512b59_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: oauth2_provider_refreshtoken oauth2_provider_refr_access_token_id_775e84e8_fk_main_oaut; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_refreshtoken
    ADD CONSTRAINT oauth2_provider_refr_access_token_id_775e84e8_fk_main_oaut FOREIGN KEY (access_token_id) REFERENCES public.main_oauth2accesstoken(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: oauth2_provider_refreshtoken oauth2_provider_refr_application_id_2d1c311b_fk_main_oaut; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_refreshtoken
    ADD CONSTRAINT oauth2_provider_refr_application_id_2d1c311b_fk_main_oaut FOREIGN KEY (application_id) REFERENCES public.main_oauth2application(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: oauth2_provider_refreshtoken oauth2_provider_refreshtoken_user_id_da837fce_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.oauth2_provider_refreshtoken
    ADD CONSTRAINT oauth2_provider_refreshtoken_user_id_da837fce_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: social_auth_usersocialauth social_auth_usersocialauth_user_id_17d28448_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.social_auth_usersocialauth
    ADD CONSTRAINT social_auth_usersocialauth_user_id_17d28448_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- Name: sso_userenterpriseauth sso_userenterpriseauth_user_id_5982f0ef_fk_auth_user_id; Type: FK CONSTRAINT; Schema: public; Owner: awx
--

ALTER TABLE ONLY public.sso_userenterpriseauth
    ADD CONSTRAINT sso_userenterpriseauth_user_id_5982f0ef_fk_auth_user_id FOREIGN KEY (user_id) REFERENCES public.auth_user(id) DEFERRABLE INITIALLY DEFERRED;


--
-- PostgreSQL database dump complete
--

