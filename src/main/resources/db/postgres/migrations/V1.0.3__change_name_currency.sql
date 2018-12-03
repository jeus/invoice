DROP VIEW invoicing.vu_merchantdebt;
ALTER TABLE invoicing.invoice RENAME currency TO payer_coin;
ALTER TABLE invoicing.invoice ADD COLUMN merchant_coin character varying(4) NOT NULL DEFAULT 'IRR'::character varying;
ALTER TABLE invoicing.invoice ALTER COLUMN amount TYPE numeric (36, 18);
ALTER TABLE invoicing.invoice ADD COLUMN merchant_amount numeric(36, 18) NOT NULL;