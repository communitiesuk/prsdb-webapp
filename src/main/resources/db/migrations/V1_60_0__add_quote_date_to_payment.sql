ALTER TABLE payment
    ADD COLUMN quote_date DATE;

UPDATE payment
SET quote_date = (payment_created_at AT TIME ZONE 'Europe/London')::date;

ALTER TABLE payment
    ALTER COLUMN quote_date SET NOT NULL;
