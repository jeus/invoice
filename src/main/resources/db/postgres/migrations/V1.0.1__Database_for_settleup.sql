SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

-- CREATE SCHEMA invoicing;

-- ALTER SCHEMA invoicing OWNER TO jeus;

COMMENT ON SCHEMA invoicing IS 'invoice generate invoice for user. ';

SET default_tablespace = '';

SET default_with_oids = false;

CREATE TABLE invoicing.blockchain (
    id bigint NOT NULL,
    invoice bigint NOT NULL,
    coin character varying(4) NOT NULL,
    cryptoamount character varying(36) NOT NULL,
    address character varying(100) NOT NULL
);

ALTER TABLE invoicing.blockchain OWNER TO jeus;

COMMENT ON TABLE invoicing.blockchain IS 'the blockchain is a table for saving address per coin per invoice [invoice(1)] to [blockchain(o-n)]';

CREATE SEQUENCE invoicing.blockchain_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE invoicing.blockchain_id_seq OWNER TO jeus;

ALTER SEQUENCE invoicing.blockchain_id_seq OWNED BY invoicing.blockchain.id;

CREATE SEQUENCE invoicing.invoice_id_seq
    START WITH 9999
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE invoicing.invoice_id_seq OWNER TO jeus;

CREATE TABLE invoicing.invoice (
    id bigint DEFAULT nextval('invoicing.invoice_id_seq'::regclass) NOT NULL,
    regdatetime timestamp without time zone DEFAULT now() NOT NULL,
    amount bigint NOT NULL,
    userdatetime timestamp without time zone NOT NULL,
    description character varying(1000),
    merchant bigint NOT NULL,
    currency character varying(4) DEFAULT 'IRR'::character varying NOT NULL,
    qr character varying(110) NOT NULL,
    status character varying(8) DEFAULT 'waiting'::character varying NOT NULL,
    category character varying(3) NOT NULL,
    orderid character varying(50)
);

ALTER TABLE invoicing.invoice OWNER TO jeus;

COMMENT ON TABLE invoicing.invoice IS 'Invoice  that created here. ';

COMMENT ON COLUMN invoicing.invoice.regdatetime IS 'date time with zone that get from user. YYYYMMDD HH:MM:SS ';

COMMENT ON COLUMN invoicing.invoice.userdatetime IS 'timestamp that user send to system for create invoice ';

COMMENT ON COLUMN invoicing.invoice.currency IS 'currency for example USD or IRR';

COMMENT ON COLUMN invoicing.invoice.status IS 'status
waiting | archived | success';

CREATE TABLE invoicing.invoicesettle (
    invoice_id bigint NOT NULL,
    settleup bigint NOT NULL
);

ALTER TABLE invoicing.invoicesettle OWNER TO jeus;

COMMENT ON TABLE invoicing.invoicesettle IS 'invoices that settle up ';

CREATE TABLE invoicing.merchant (
    id bigint NOT NULL,
    mobile character varying(11) NOT NULL,
    token character varying(5) NOT NULL,
    push_token text NOT NULL,
    shop_name character varying(100),
    datetime timestamp without time zone DEFAULT now() NOT NULL,
    last_send_token timestamp without time zone DEFAULT now() NOT NULL,
    api_key character varying(64),
    callback character varying(150),
    card_number character varying(16) DEFAULT '6104337645502681'::character varying
);

ALTER TABLE invoicing.merchant OWNER TO jeus;

COMMENT ON TABLE invoicing.merchant IS 'merchant information phone , push id and other things ';

COMMENT ON COLUMN invoicing.merchant.callback IS 'callback URL for redirect UI to this page. ';

COMMENT ON COLUMN invoicing.merchant.card_number IS 'bank access card ';

CREATE SEQUENCE invoicing.merchant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE invoicing.merchant_id_seq OWNER TO jeus;

ALTER SEQUENCE invoicing.merchant_id_seq OWNED BY invoicing.merchant.id;

CREATE TABLE invoicing.payerlog (
    id bigint NOT NULL,
    invoice bigint NOT NULL,
    qrcode text NOT NULL,
    email character varying(200) NOT NULL,
    mobile character varying(14),
    datetime timestamp without time zone DEFAULT now() NOT NULL,
    inform boolean DEFAULT false NOT NULL
);

ALTER TABLE invoicing.payerlog OWNER TO jeus;

COMMENT ON TABLE invoicing.payerlog IS 'this table use for keeping user information of any that pay if pay and not accept return that to user by email or phone number. ';

COMMENT ON COLUMN invoicing.payerlog.inform IS 'do you want to inform you by send an email ? ';

CREATE SEQUENCE invoicing.payerlog_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE invoicing.payerlog_id_seq OWNER TO jeus;

ALTER SEQUENCE invoicing.payerlog_id_seq OWNED BY invoicing.payerlog.id;

CREATE TABLE invoicing.settleup (
    id bigint NOT NULL,
    datetime timestamp without time zone NOT NULL,
    amount bigint NOT NULL,
    origin_card character varying(16) NOT NULL,
    dest_card character varying(16) NOT NULL,
    merchant bigint NOT NULL,
    txid character varying(20) DEFAULT ''::character varying NOT NULL
);

ALTER TABLE invoicing.settleup OWNER TO jeus;

COMMENT ON TABLE invoicing.settleup IS 'information about settle up invoices';

CREATE SEQUENCE invoicing.settleup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE invoicing.settleup_id_seq OWNER TO jeus;

ALTER SEQUENCE invoicing.settleup_id_seq OWNED BY invoicing.settleup.id;

CREATE VIEW invoicing.vu_merchantdebt AS
SELECT
    NULL::bigint AS id,
    NULL::character varying(11) AS mer_mobile,
    NULL::numeric AS balance,
    NULL::text AS symbol,
    NULL::character varying(100) AS name,
    NULL::bigint AS count;

ALTER TABLE invoicing.vu_merchantdebt OWNER TO jeus;

ALTER TABLE ONLY invoicing.blockchain ALTER COLUMN id SET DEFAULT nextval('invoicing.blockchain_id_seq'::regclass);

ALTER TABLE ONLY invoicing.merchant ALTER COLUMN id SET DEFAULT nextval('invoicing.merchant_id_seq'::regclass);

ALTER TABLE ONLY invoicing.payerlog ALTER COLUMN id SET DEFAULT nextval('invoicing.payerlog_id_seq'::regclass);

ALTER TABLE ONLY invoicing.settleup ALTER COLUMN id SET DEFAULT nextval('invoicing.settleup_id_seq'::regclass);

SELECT pg_catalog.setval('invoicing.blockchain_id_seq', 1, false);
SELECT pg_catalog.setval('invoicing.invoice_id_seq', 10934, true);
SELECT pg_catalog.setval('invoicing.merchant_id_seq', 19, true);
SELECT pg_catalog.setval('invoicing.payerlog_id_seq', 130, true);
SELECT pg_catalog.setval('invoicing.settleup_id_seq', 21, true);

ALTER TABLE invoicing.invoice ADD CONSTRAINT amount CHECK ((amount >= 1)) NOT VALID;

COMMENT ON CONSTRAINT amount ON invoicing.invoice IS 'the amount is long and cants less than 1';

ALTER TABLE invoicing.merchant ADD CONSTRAINT "api-key-length" CHECK ((length((api_key)::text) = 64)) NOT VALID;

COMMENT ON CONSTRAINT "api-key-length" ON invoicing.merchant IS 'api ke have to 64 char';

ALTER TABLE ONLY invoicing.settleup ADD CONSTRAINT id_pk PRIMARY KEY (id);

ALTER TABLE ONLY invoicing.blockchain ADD CONSTRAINT invoice_per_coin UNIQUE (invoice, coin);

ALTER TABLE ONLY invoicing.invoice ADD CONSTRAINT invoice_pkey PRIMARY KEY (id);

ALTER TABLE ONLY invoicing.invoicesettle ADD CONSTRAINT invoicesettle_pkey PRIMARY KEY (invoice_id);

ALTER TABLE ONLY invoicing.merchant ADD CONSTRAINT merchant_pkey PRIMARY KEY (id);

ALTER TABLE ONLY invoicing.invoice ADD CONSTRAINT "orderIdPerMerchant" UNIQUE (orderid, merchant);

COMMENT ON CONSTRAINT "orderIdPerMerchant" ON invoicing.invoice IS 'orderIdPermerchant have to unique';

ALTER TABLE ONLY invoicing.payerlog ADD CONSTRAINT payerlog_pkey PRIMARY KEY (id);

ALTER TABLE ONLY invoicing.blockchain ADD CONSTRAINT primary_key_blockchain PRIMARY KEY (id);

CREATE OR REPLACE VIEW invoicing.vu_merchantdebt AS
 SELECT merc.id,
    merc.mobile AS mer_mobile,
    sum(inv.amount) AS balance,
    'IRR'::text AS symbol,
    merc.shop_name AS name,
    count(merc.mobile) AS count
   FROM ((invoicing.merchant merc
     LEFT JOIN invoicing.invoice inv ON ((merc.id = inv.merchant)))
     LEFT JOIN invoicing.invoicesettle invset ON ((inv.id = invset.invoice_id)))
  WHERE (((inv.status)::text = 'success'::text) AND (invset.invoice_id IS NULL))
  GROUP BY merc.id;

ALTER TABLE ONLY invoicing.invoice ADD CONSTRAINT fk_merchantid FOREIGN KEY (merchant) REFERENCES invoicing.merchant(id) ON UPDATE CASCADE ON DELETE CASCADE;

COMMENT ON CONSTRAINT fk_merchantid ON invoicing.invoice IS 'merchant id in this invoices ';

ALTER TABLE ONLY invoicing.invoicesettle ADD CONSTRAINT fk_this_invoice FOREIGN KEY (invoice_id) REFERENCES invoicing.invoice(id);

ALTER TABLE ONLY invoicing.invoicesettle ADD CONSTRAINT fk_this_settle1 FOREIGN KEY (settleup) REFERENCES invoicing.settleup(id) ON UPDATE CASCADE ON DELETE CASCADE;

