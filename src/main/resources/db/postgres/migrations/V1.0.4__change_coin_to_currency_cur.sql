ALTER TABLE invoicing.invoice RENAME amount TO payer_amount;


-- merchant_cur
ALTER TABLE invoicing.invoice RENAME merchant_coin TO merchant_cur;
ALTER TABLE invoicing.invoice ALTER COLUMN merchant_cur TYPE character varying (4) COLLATE pg_catalog."default";
COMMENT ON COLUMN invoicing.invoice.merchant_cur  IS 'merchant currency (normally is FIAT)';
ALTER TABLE invoicing.invoice ALTER COLUMN merchant_amount TYPE numeric (36, 18);


ALTER TABLE invoicing.invoice RENAME payer_coin TO payer_cur;
ALTER TABLE invoicing.invoice ALTER COLUMN payer_cur TYPE character varying (4) COLLATE pg_catalog."default";