DROP VIEW invoicing.vu_merchantdebt;
ALTER TABLE invoicing.invoice RENAME currency TO payer_coin;
ALTER TABLE invoicing.invoice ADD COLUMN merchant_coin character varying(4) NOT NULL DEFAULT 'IRR'::character varying;
ALTER TABLE invoicing.invoice ALTER COLUMN amount TYPE numeric (36, 18);
ALTER TABLE invoicing.invoice ADD COLUMN merchant_amount numeric(36, 18) NOT NULL DEFAULT 0::numeric;

UPDATE  invoicing.invoice set merchant_amount = amount;

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