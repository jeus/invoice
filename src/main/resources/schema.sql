--
-- PostgreSQL database dump
--

-- Dumped from database version 10.4
-- Dumped by pg_dump version 10.4

-- Started on 2018-11-12 10:24:29 +0330

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 7 (class 2615 OID 84594)
-- Name: invoicing; Type: SCHEMA; Schema: -; Owner: jeus
--

DROP SCHEMA IF EXISTS old_invoice3 cascade;

ALTER SCHEMA IF EXISTS name old_invoice2 TO old_invoice3
ALTER SCHEMA IF EXISTS name old_invoice1 TO old_invoice2
ALTER SCHEMA IF EXISTS name invoicing TO old_invoice1

CREATE SCHEMA invoicing;


ALTER SCHEMA invoicing OWNER TO jeus;

--
-- TOC entry 2564 (class 0 OID 0)
-- Dependencies: 7
-- Name: SCHEMA invoicing; Type: COMMENT; Schema: -; Owner: jeus
--

COMMENT ON SCHEMA invoicing IS 'invoice generate invoice for user. ';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 217 (class 1259 OID 84595)
-- Name: blockchain; Type: TABLE; Schema: invoicing; Owner: jeus
--

CREATE TABLE invoicing.blockchain (
    id bigint NOT NULL,
    invoice bigint NOT NULL,
    coin character varying(4) NOT NULL,
    cryptoamount character varying(36) NOT NULL,
    address character varying(100) NOT NULL
);


ALTER TABLE invoicing.blockchain OWNER TO jeus;

--
-- TOC entry 2565 (class 0 OID 0)
-- Dependencies: 217
-- Name: TABLE blockchain; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON TABLE invoicing.blockchain IS 'the blockchain is a table for saving address per coin per invoice [invoice(1)] to [blockchain(o-n)]';


--
-- TOC entry 218 (class 1259 OID 84598)
-- Name: blockchain_id_seq; Type: SEQUENCE; Schema: invoicing; Owner: jeus
--

CREATE SEQUENCE invoicing.blockchain_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE invoicing.blockchain_id_seq OWNER TO jeus;

--
-- TOC entry 2566 (class 0 OID 0)
-- Dependencies: 218
-- Name: blockchain_id_seq; Type: SEQUENCE OWNED BY; Schema: invoicing; Owner: jeus
--

ALTER SEQUENCE invoicing.blockchain_id_seq OWNED BY invoicing.blockchain.id;


--
-- TOC entry 219 (class 1259 OID 84600)
-- Name: invoice_id_seq; Type: SEQUENCE; Schema: invoicing; Owner: jeus
--

CREATE SEQUENCE invoicing.invoice_id_seq
    START WITH 9999
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE invoicing.invoice_id_seq OWNER TO jeus;

--
-- TOC entry 220 (class 1259 OID 84602)
-- Name: invoice; Type: TABLE; Schema: invoicing; Owner: jeus
--

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

--
-- TOC entry 2567 (class 0 OID 0)
-- Dependencies: 220
-- Name: TABLE invoice; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON TABLE invoicing.invoice IS 'Invoice  that created here. ';


--
-- TOC entry 2568 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN invoice.regdatetime; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON COLUMN invoicing.invoice.regdatetime IS 'date time with zone that get from user. YYYYMMDD HH:MM:SS ';


--
-- TOC entry 2569 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN invoice.userdatetime; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON COLUMN invoicing.invoice.userdatetime IS 'timestamp that user send to system for create invoice ';


--
-- TOC entry 2570 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN invoice.currency; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON COLUMN invoicing.invoice.currency IS 'currency for example USD or IRR';


--
-- TOC entry 2571 (class 0 OID 0)
-- Dependencies: 220
-- Name: COLUMN invoice.status; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON COLUMN invoicing.invoice.status IS 'status
waiting | archived | success';


--
-- TOC entry 221 (class 1259 OID 84612)
-- Name: invoicesettle; Type: TABLE; Schema: invoicing; Owner: jeus
--

CREATE TABLE invoicing.invoicesettle (
    invoice_id bigint NOT NULL,
    settleup bigint NOT NULL
);


ALTER TABLE invoicing.invoicesettle OWNER TO jeus;

--
-- TOC entry 2572 (class 0 OID 0)
-- Dependencies: 221
-- Name: TABLE invoicesettle; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON TABLE invoicing.invoicesettle IS 'invoices that settle up ';


--
-- TOC entry 222 (class 1259 OID 84615)
-- Name: merchant; Type: TABLE; Schema: invoicing; Owner: jeus
--

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

--
-- TOC entry 2573 (class 0 OID 0)
-- Dependencies: 222
-- Name: TABLE merchant; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON TABLE invoicing.merchant IS 'merchant information phone , push id and other things ';


--
-- TOC entry 2574 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN merchant.callback; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON COLUMN invoicing.merchant.callback IS 'callback URL for redirect UI to this page. ';


--
-- TOC entry 2575 (class 0 OID 0)
-- Dependencies: 222
-- Name: COLUMN merchant.card_number; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON COLUMN invoicing.merchant.card_number IS 'bank access card ';


--
-- TOC entry 223 (class 1259 OID 84624)
-- Name: merchant_id_seq; Type: SEQUENCE; Schema: invoicing; Owner: jeus
--

CREATE SEQUENCE invoicing.merchant_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE invoicing.merchant_id_seq OWNER TO jeus;

--
-- TOC entry 2576 (class 0 OID 0)
-- Dependencies: 223
-- Name: merchant_id_seq; Type: SEQUENCE OWNED BY; Schema: invoicing; Owner: jeus
--

ALTER SEQUENCE invoicing.merchant_id_seq OWNED BY invoicing.merchant.id;


--
-- TOC entry 224 (class 1259 OID 84626)
-- Name: payerlog; Type: TABLE; Schema: invoicing; Owner: jeus
--

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

--
-- TOC entry 2577 (class 0 OID 0)
-- Dependencies: 224
-- Name: TABLE payerlog; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON TABLE invoicing.payerlog IS 'this table use for keeping user information of any that pay if pay and not accept return that to user by email or phone number. ';


--
-- TOC entry 2578 (class 0 OID 0)
-- Dependencies: 224
-- Name: COLUMN payerlog.inform; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON COLUMN invoicing.payerlog.inform IS 'do you want to inform you by send an email ? ';


--
-- TOC entry 225 (class 1259 OID 84634)
-- Name: payerlog_id_seq; Type: SEQUENCE; Schema: invoicing; Owner: jeus
--

CREATE SEQUENCE invoicing.payerlog_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE invoicing.payerlog_id_seq OWNER TO jeus;

--
-- TOC entry 2579 (class 0 OID 0)
-- Dependencies: 225
-- Name: payerlog_id_seq; Type: SEQUENCE OWNED BY; Schema: invoicing; Owner: jeus
--

ALTER SEQUENCE invoicing.payerlog_id_seq OWNED BY invoicing.payerlog.id;


--
-- TOC entry 226 (class 1259 OID 84636)
-- Name: settleup; Type: TABLE; Schema: invoicing; Owner: jeus
--

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

--
-- TOC entry 2580 (class 0 OID 0)
-- Dependencies: 226
-- Name: TABLE settleup; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON TABLE invoicing.settleup IS 'information about settle up invoices';


--
-- TOC entry 227 (class 1259 OID 84640)
-- Name: settleup_id_seq; Type: SEQUENCE; Schema: invoicing; Owner: jeus
--

CREATE SEQUENCE invoicing.settleup_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE invoicing.settleup_id_seq OWNER TO jeus;

--
-- TOC entry 2581 (class 0 OID 0)
-- Dependencies: 227
-- Name: settleup_id_seq; Type: SEQUENCE OWNED BY; Schema: invoicing; Owner: jeus
--

ALTER SEQUENCE invoicing.settleup_id_seq OWNED BY invoicing.settleup.id;


--
-- TOC entry 228 (class 1259 OID 84642)
-- Name: vu_merchantdebt; Type: VIEW; Schema: invoicing; Owner: jeus
--

CREATE VIEW invoicing.vu_merchantdebt AS
SELECT
    NULL::bigint AS id,
    NULL::character varying(11) AS mer_mobile,
    NULL::numeric AS balance,
    NULL::text AS symbol,
    NULL::character varying(100) AS name,
    NULL::bigint AS count;


ALTER TABLE invoicing.vu_merchantdebt OWNER TO jeus;

--
-- TOC entry 2390 (class 2604 OID 84646)
-- Name: blockchain id; Type: DEFAULT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.blockchain ALTER COLUMN id SET DEFAULT nextval('invoicing.blockchain_id_seq'::regclass);


--
-- TOC entry 2399 (class 2604 OID 84647)
-- Name: merchant id; Type: DEFAULT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.merchant ALTER COLUMN id SET DEFAULT nextval('invoicing.merchant_id_seq'::regclass);


--
-- TOC entry 2403 (class 2604 OID 84648)
-- Name: payerlog id; Type: DEFAULT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.payerlog ALTER COLUMN id SET DEFAULT nextval('invoicing.payerlog_id_seq'::regclass);


--
-- TOC entry 2405 (class 2604 OID 84649)
-- Name: settleup id; Type: DEFAULT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.settleup ALTER COLUMN id SET DEFAULT nextval('invoicing.settleup_id_seq'::regclass);


--
-- TOC entry 2548 (class 0 OID 84595)
-- Dependencies: 217
-- Data for Name: blockchain; Type: TABLE DATA; Schema: invoicing; Owner: jeus
--



--
-- TOC entry 2551 (class 0 OID 84602)
-- Dependencies: 220
-- Data for Name: invoice; Type: TABLE DATA; Schema: invoicing; Owner: jeus
--

INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10020, '2018-09-24 11:35:47.646217', 800000000, '2018-09-24 11:35:47.423', 'NEW PAYMANET', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.80262244', 'failed', 'POS', '10020');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10022, '2018-09-24 13:01:28.804361', 800000000, '2018-09-24 13:01:28.592', 'NEW PAYMANET', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.78447545', 'failed', 'POS', '10022');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10023, '2018-09-24 14:45:41.133482', 800000000, '2018-09-24 14:45:40.868', 'NEW PAYMANET', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.78552765', 'failed', 'POS', '10023');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10024, '2018-09-24 15:56:56.981649', 400000, '2018-09-24 15:56:56.727', 'NEW PAYMANET', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00038917', 'failed', 'POS', '10024');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10026, '2018-09-24 16:13:32.560349', 500000, '2018-09-24 16:13:32.166', 'NEW PAYMANET', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00048366', 'failed', 'POS', '10026');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10933, '2018-10-25 18:10:07.55892', 84773, '2018-10-25 18:10:07.504', 'test Description', 19, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00009544', 'success', 'POS', 'OksCIUbPMuKuubA');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10169, '2018-10-16 15:09:26.443929', 59000, '2018-10-16 15:09:26.399', 'TEST NEW INVOICE CREATE', 11, 'IRR', '', 'failed', 'POS', 'TESTEMAAIL55');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10205, '2018-10-16 21:26:31.391265', 59000, '2018-10-16 21:26:31.39', 'TEST NEW INVOICE CREATE', 11, 'IRR', '', 'failed', 'POS', 'lkjhgjkhvgj');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10209, '2018-10-16 21:55:49.066813', 59000, '2018-10-16 21:55:49.022', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'failed', 'POS', 'LLKKKKS');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10210, '2018-10-16 21:59:13.254412', 59000, '2018-10-16 21:59:13.253', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'failed', 'POS', 'KDJSKJD');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10213, '2018-10-16 22:17:18.092556', 59000, '2018-10-16 22:17:18.027', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'failed', 'POS', 'PPP');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10166, '2018-10-15 17:39:10.378969', 59000, '2018-10-15 17:39:10.377', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00006205', 'success', 'POS', 'id is not unique');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10021, '2018-09-24 12:41:36.102393', 800000000, '2018-09-24 12:41:35.957', 'NEW PAYMANET', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.77737045', 'success', 'POS', '10021');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10025, '2018-09-24 16:08:15.034164', 500000, '2018-09-24 16:08:14.699', 'NEW PAYMANET', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.000483014', 'success', 'POS', '10025');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10910, '2018-10-18 16:05:36.534517', 590400, '2018-10-18 16:05:36.468', 'TEST NEW INVOICE CREATE', 11, 'IRR', '', 'failed', 'POS', 'UUdUUAdAfAA');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10905, '2018-10-17 12:33:01.71766', 59000, '2018-10-17 12:33:01.676', 'TEST NEW INVOICE CREATE', 11, 'IRR', '', 'success', 'POS', 'jkadk12');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10914, '2018-10-24 10:56:38.933654', 300000, '2018-10-24 10:56:38.866', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'waiting', 'POS', 'TEST123');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10027, '2018-09-24 16:17:03.364632', 500000, '2018-09-24 16:17:03.069', 'NEW PAYMANET', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00048336', 'success', 'POS', '10027');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10167, '2018-10-15 19:50:35.453901', 59000, '2018-10-15 19:50:35.378', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00006201', 'failed', 'POS', 'TESTEMAIL---');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10211, '2018-10-16 22:04:03.350355', 59000, '2018-10-16 22:04:03.295', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'failed', 'POS', 'vbgfhgdsdvcx');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10902, '2018-10-17 11:51:33.653101', 59000, '2018-10-17 11:51:33.57', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.001969430235742485', 'failed', 'POS', 'PPPPP');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10903, '2018-10-17 11:54:55.663916', 59000, '2018-10-17 11:54:55.662', 'TEST NEW INVOICE CREATE', 11, 'IRR', '', 'failed', 'POS', 'DDDDD');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10906, '2018-10-17 14:40:36.378179', 59000, '2018-10-17 14:40:36.284', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.002015092067026741', 'success', 'POS', 'UUUUAAAA');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10912, '2018-10-24 10:11:14.753959', 590400, '2018-10-24 10:11:14.682', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066117', 'waiting', 'POS', 'UUdUUAAdAfAA');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10918, '2018-10-24 12:31:34.249703', 1000006, '2018-10-24 12:31:34.248', 'TEST NEW INVOICE CREATE', 18, 'IRR', '', 'waiting', 'POS', 'TEST12325');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10919, '2018-10-24 12:31:50.193516', 5000000, '2018-10-24 12:31:50.191', 'TEST NEW INVOICE CREATE', 18, 'IRR', '', 'waiting', 'POS', 'BASKJJJ');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10915, '2018-10-24 12:31:16.623292', 300000, '2018-10-24 12:31:16.621', 'TEST NEW INVOICE CREATE', 18, 'IRR', '', 'success', 'POS', 'TEST123');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10917, '2018-10-24 12:31:27.471377', 100000, '2018-10-24 12:31:27.47', 'TEST NEW INVOICE CREATE', 18, 'IRR', '', 'success', 'POS', 'TEST1232');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10930, '2018-10-25 18:02:34.226845', 322632, '2018-10-25 18:02:34.165', 'test Description', 19, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00036450', 'success', 'POS', 'TtkGkhkGUxtHeva');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10931, '2018-10-25 18:04:09.276', 427652, '2018-10-25 18:04:09.274', 'test Description', 19, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00048327', 'success', 'POS', 'cWwrfNPHOMRuCDI');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10934, '2018-10-25 18:12:22.010675', 104923, '2018-10-25 18:12:21.927', 'test Description', 19, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00011866', 'success', 'POS', 'GuEaHnQrpyNCUdT');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10164, '2018-10-15 13:46:39.457164', 59000, '2018-10-15 13:46:39.38', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:15fZp3nC68C39QHsAjtnT5WV1SCBLXL8LU?amount=0.00006241', 'failed', 'POS', 'TESTNETTEST1');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10168, '2018-10-15 21:21:31.468092', 59000, '2018-10-15 21:21:31.412', 'TEST NEW INVOICE CREATE', 11, 'IRR', '', 'failed', 'POS', 'TESTEMAIL55');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10208, '2018-10-16 21:53:37.860048', 59000, '2018-10-16 21:53:37.859', 'TEST NEW INVOICE CREATE', 11, 'IRR', '', 'failed', 'POS', 'KJSAKJAS');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10212, '2018-10-16 22:12:24.221741', 59000, '2018-10-16 22:12:24.161', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'failed', 'POS', 'LKJKL');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10904, '2018-10-17 12:24:24.901103', 59000, '2018-10-17 12:24:24.854', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.001977483155201415', 'failed', 'POS', 'LLKKA');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10907, '2018-10-17 14:46:02.492498', 59000, '2018-10-17 14:46:02.443', 'TEST NEW INVOICE CREATE', 11, 'IRR', '', 'failed', 'POS', 'UUdUUAAAA');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10909, '2018-10-17 14:48:35.85825', 590400, '2018-10-17 14:48:35.857', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.020164582311398745', 'failed', 'POS', 'UUdUUAdAAA');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10913, '2018-10-24 10:21:35.538375', 590400, '2018-10-24 10:21:35.482', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066051', 'waiting', 'POS', 'TESTNEWINVOICE');
INSERT INTO invoicing.invoice (id, regdatetime, amount, userdatetime, description, merchant, currency, qr, status, category, orderid) VALUES (10163, '2018-10-14 15:48:28.557878', 59000, '2018-10-14 15:48:28.482', 'TEST NEW INVOICE CREATE', 11, 'IRR', 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00007019', 'success', 'POS', 'TEST10');


--
-- TOC entry 2552 (class 0 OID 84612)
-- Dependencies: 221
-- Data for Name: invoicesettle; Type: TABLE DATA; Schema: invoicing; Owner: jeus
--

INSERT INTO invoicing.invoicesettle (invoice_id, settleup) VALUES (10021, 2);
INSERT INTO invoicing.invoicesettle (invoice_id, settleup) VALUES (10027, 2);


--
-- TOC entry 2553 (class 0 OID 84615)
-- Dependencies: 222
-- Data for Name: merchant; Type: TABLE DATA; Schema: invoicing; Owner: jeus
--

INSERT INTO invoicing.merchant (id, mobile, token, push_token, shop_name, datetime, last_send_token, api_key, callback, card_number) VALUES (18, '09192792373', '27332', 'UNDEFINED', 'Becopay', '2018-10-16 15:55:32.242', '2018-10-16 15:55:32.242', 'a5c1f417591ba3ebaf65dea182ba3bc5b2e4c90ec2355a855a068edd4cdc7042', 'https://sandbox.becopay.com/ordeid=${orderId}', '6104337645502681');
INSERT INTO invoicing.merchant (id, mobile, token, push_token, shop_name, datetime, last_send_token, api_key, callback, card_number) VALUES (11, '09120453931', '81409', 'pushrokewnebbew', 'mehdi-berger', '2018-09-08 11:59:40.519', '2018-10-11 15:54:23.59', 'd089b7cad4b1f425b35ab943ac34c6e88514afeed56e13e161c4a521e9e50dc6', 'http://www.2charkhe.com/invoice', '6104337645502681');
INSERT INTO invoicing.merchant (id, mobile, token, push_token, shop_name, datetime, last_send_token, api_key, callback, card_number) VALUES (19, '09120779807', '29120', 'UNDEFINED', 'Test Shop', '2018-10-25 12:45:06.158', '2018-10-25 12:45:06.158', '9494cae811f7b93091a083a8cdf985cf8836f61d38de3a2068baf110e65bcf80', 'www.becopay.com/ordeid=${orderId}', '1234567887654321');


--
-- TOC entry 2555 (class 0 OID 84626)
-- Dependencies: 224
-- Data for Name: payerlog; Type: TABLE DATA; Schema: invoicing; Owner: jeus
--

INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (65, 10166, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00006203', 'alkhandani@gmail.com', '09120454941', '2018-10-15 17:49:10.38375', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (66, 10166, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.001957765192148197', 'alkhandani@gmail.com', '09120454941', '2018-10-15 17:57:33.508498', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (67, 10166, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.001957765192148197', 'alkhandani+test2@gmail.com', '09120454941', '2018-10-15 17:57:47.827729', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (68, 10166, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00006205', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-15 17:57:56.229034', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (69, 10167, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00006201', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-15 19:50:47.534077', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (70, 10209, 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-16 21:56:41.414375', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (71, 10210, 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-16 21:59:25.993783', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (72, 10210, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.001965407410000873', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-16 22:01:13.000349', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (73, 10210, 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-16 22:01:48.18815', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (74, 10211, 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-16 22:09:49.060981', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (75, 10212, 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-16 22:15:43.701674', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (76, 10213, 'bitcoin:mzueNijLVLwzf9UscyKJyQkFzfCyL11ZTV?amount=0.00006224', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-16 22:18:26.901854', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (77, 10902, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00006270', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-17 11:51:57.005016', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (78, 10902, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00006270', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-17 11:52:53.871439', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (79, 10902, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.001969430235742485', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-17 11:54:14.671234', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (80, 10904, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.001977483155201415', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-17 12:25:18.680061', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (81, 10906, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.002015092067026741', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-17 14:40:48.542515', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (82, 10909, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.020164582311398745', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-17 14:48:50.690534', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (83, 10912, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.020858696065291675', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:11:42.365994', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (84, 10912, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.020858696065291675', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:12:02.511485', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (85, 10912, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.020858696065291675', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:12:06.621271', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (86, 10912, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066117', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:12:19.869769', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (87, 10912, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066117', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:12:27.950054', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (88, 10912, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066117', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:12:41.036593', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (89, 10912, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066117', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:12:44.180944', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (90, 10912, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066117', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:13:24.441956', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (91, 10912, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066117', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:13:26.221837', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (92, 10913, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.020867037130584327', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:21:53.441163', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (93, 10913, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066051', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:22:20.762256', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (94, 10913, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066051', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:22:23.934829', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (95, 10913, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066051', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:22:25.567855', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (96, 10913, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00066051', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:22:30.546062', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (97, 10914, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.010609897884239682', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:56:49.69122', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (98, 10914, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.010609897884239682', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:56:53.055298', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (99, 10914, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.010609897884239682', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:57:05.97278', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (100, 10914, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.010609897884239682', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:57:08.50253', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (101, 10914, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.010609897884239682', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:57:09.968524', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (102, 10914, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.010609897884239682', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:57:11.609939', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (103, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:57:21.908352', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (104, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:57:23.920347', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (105, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:57:25.334953', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (106, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:57:27.306668', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (107, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:27.044511', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (108, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:28.409597', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (109, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:29.536146', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (110, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:30.644288', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (111, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:31.745836', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (112, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:32.847974', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (113, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:33.968133', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (114, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:35.269883', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (115, 10914, 'ethereum:0x8592528c4b0782347a7552b043682dde2799810e?amount=0.010609897884239682', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:58:54.256595', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (116, 10914, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033602', 'alkhandani+test3@gmail.com', '09120454941', '2018-10-24 10:59:24.572451', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (117, 10921, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00051320', '', '09120779807', '2018-10-25 16:31:52.961143', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (118, 10922, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00015565', '', '09120779807', '2018-10-25 16:42:10.751431', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (119, 10923, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00033636', '', '09120779807', '2018-10-25 16:52:03.752191', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (120, 10924, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00029140', '', '09120779807', '2018-10-25 16:52:26.413861', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (121, 10925, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00090499', '', '09120779807', '2018-10-25 16:53:35.693445', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (122, 10926, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00010674', '', '09120779807', '2018-10-25 16:58:04.513763', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (123, 10927, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00110657', '', '09120779807', '2018-10-25 17:03:16.689475', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (124, 10928, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00031134', '', '09120779807', '2018-10-25 17:05:46.183301', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (125, 10929, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00084727', '', '09120779807', '2018-10-25 17:09:03.383522', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (126, 10930, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00036450', '', '09120779807', '2018-10-25 18:02:35.491389', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (127, 10931, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00048327', '', '09120779807', '2018-10-25 18:04:09.905233', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (128, 10932, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00067584', '', '09120779807', '2018-10-25 18:07:55.60076', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (129, 10933, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00009544', '', '09120779807', '2018-10-25 18:10:08.87985', true);
INSERT INTO invoicing.payerlog (id, invoice, qrcode, email, mobile, datetime, inform) VALUES (130, 10934, 'bitcoin:1JyQeEFa6NTobQeNrQKzVCY1dqdBC2LLRt?amount=0.00011866', '', '09120779807', '2018-10-25 18:12:23.148039', true);


--
-- TOC entry 2557 (class 0 OID 84636)
-- Dependencies: 226
-- Data for Name: settleup; Type: TABLE DATA; Schema: invoicing; Owner: jeus
--

INSERT INTO invoicing.settleup (id, datetime, amount, origin_card, dest_card, merchant, txid) VALUES (2, '2018-10-23 16:14:51', 800000000, '123456787654321', '876543212345678', 11, '123123');


--
-- TOC entry 2582 (class 0 OID 0)
-- Dependencies: 218
-- Name: blockchain_id_seq; Type: SEQUENCE SET; Schema: invoicing; Owner: jeus
--

SELECT pg_catalog.setval('invoicing.blockchain_id_seq', 1, false);


--
-- TOC entry 2583 (class 0 OID 0)
-- Dependencies: 219
-- Name: invoice_id_seq; Type: SEQUENCE SET; Schema: invoicing; Owner: jeus
--

SELECT pg_catalog.setval('invoicing.invoice_id_seq', 10934, true);


--
-- TOC entry 2584 (class 0 OID 0)
-- Dependencies: 223
-- Name: merchant_id_seq; Type: SEQUENCE SET; Schema: invoicing; Owner: jeus
--

SELECT pg_catalog.setval('invoicing.merchant_id_seq', 19, true);


--
-- TOC entry 2585 (class 0 OID 0)
-- Dependencies: 225
-- Name: payerlog_id_seq; Type: SEQUENCE SET; Schema: invoicing; Owner: jeus
--

SELECT pg_catalog.setval('invoicing.payerlog_id_seq', 130, true);


--
-- TOC entry 2586 (class 0 OID 0)
-- Dependencies: 227
-- Name: settleup_id_seq; Type: SEQUENCE SET; Schema: invoicing; Owner: jeus
--

SELECT pg_catalog.setval('invoicing.settleup_id_seq', 21, true);


--
-- TOC entry 2395 (class 2606 OID 84650)
-- Name: invoice amount; Type: CHECK CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE invoicing.invoice
    ADD CONSTRAINT amount CHECK ((amount >= 1)) NOT VALID;


--
-- TOC entry 2587 (class 0 OID 0)
-- Dependencies: 2395
-- Name: CONSTRAINT amount ON invoice; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON CONSTRAINT amount ON invoicing.invoice IS 'the amount is long and cants less than 1';


--
-- TOC entry 2400 (class 2606 OID 84651)
-- Name: merchant api-key-length; Type: CHECK CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE invoicing.merchant
    ADD CONSTRAINT "api-key-length" CHECK ((length((api_key)::text) = 64)) NOT VALID;


--
-- TOC entry 2588 (class 0 OID 0)
-- Dependencies: 2400
-- Name: CONSTRAINT "api-key-length" ON merchant; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON CONSTRAINT "api-key-length" ON invoicing.merchant IS 'api ke have to 64 char';


--
-- TOC entry 2421 (class 2606 OID 84653)
-- Name: settleup id_pk; Type: CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.settleup
    ADD CONSTRAINT id_pk PRIMARY KEY (id);


--
-- TOC entry 2407 (class 2606 OID 84655)
-- Name: blockchain invoice_per_coin; Type: CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.blockchain
    ADD CONSTRAINT invoice_per_coin UNIQUE (invoice, coin);


--
-- TOC entry 2411 (class 2606 OID 84657)
-- Name: invoice invoice_pkey; Type: CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.invoice
    ADD CONSTRAINT invoice_pkey PRIMARY KEY (id);


--
-- TOC entry 2415 (class 2606 OID 84659)
-- Name: invoicesettle invoicesettle_pkey; Type: CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.invoicesettle
    ADD CONSTRAINT invoicesettle_pkey PRIMARY KEY (invoice_id);


--
-- TOC entry 2417 (class 2606 OID 84661)
-- Name: merchant merchant_pkey; Type: CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.merchant
    ADD CONSTRAINT merchant_pkey PRIMARY KEY (id);


--
-- TOC entry 2413 (class 2606 OID 84663)
-- Name: invoice orderIdPerMerchant; Type: CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.invoice
    ADD CONSTRAINT "orderIdPerMerchant" UNIQUE (orderid, merchant);


--
-- TOC entry 2589 (class 0 OID 0)
-- Dependencies: 2413
-- Name: CONSTRAINT "orderIdPerMerchant" ON invoice; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON CONSTRAINT "orderIdPerMerchant" ON invoicing.invoice IS 'orderIdPermerchant have to unique';


--
-- TOC entry 2419 (class 2606 OID 84665)
-- Name: payerlog payerlog_pkey; Type: CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.payerlog
    ADD CONSTRAINT payerlog_pkey PRIMARY KEY (id);


--
-- TOC entry 2409 (class 2606 OID 84667)
-- Name: blockchain primary_key_blockchain; Type: CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.blockchain
    ADD CONSTRAINT primary_key_blockchain PRIMARY KEY (id);


--
-- TOC entry 2547 (class 2618 OID 84645)
-- Name: vu_merchantdebt _RETURN; Type: RULE; Schema: invoicing; Owner: jeus
--

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


--
-- TOC entry 2422 (class 2606 OID 84669)
-- Name: invoice fk_merchantid; Type: FK CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.invoice
    ADD CONSTRAINT fk_merchantid FOREIGN KEY (merchant) REFERENCES invoicing.merchant(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2590 (class 0 OID 0)
-- Dependencies: 2422
-- Name: CONSTRAINT fk_merchantid ON invoice; Type: COMMENT; Schema: invoicing; Owner: jeus
--

COMMENT ON CONSTRAINT fk_merchantid ON invoicing.invoice IS 'merchant id in this invoices ';


--
-- TOC entry 2423 (class 2606 OID 84674)
-- Name: invoicesettle fk_this_invoice; Type: FK CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.invoicesettle
    ADD CONSTRAINT fk_this_invoice FOREIGN KEY (invoice_id) REFERENCES invoicing.invoice(id);


--
-- TOC entry 2424 (class 2606 OID 84679)
-- Name: invoicesettle fk_this_settle1; Type: FK CONSTRAINT; Schema: invoicing; Owner: jeus
--

ALTER TABLE ONLY invoicing.invoicesettle
    ADD CONSTRAINT fk_this_settle1 FOREIGN KEY (settleup) REFERENCES invoicing.settleup(id) ON UPDATE CASCADE ON DELETE CASCADE;


-- Completed on 2018-11-12 10:24:29 +0330

--
-- PostgreSQL database dump complete
--

