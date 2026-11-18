ALTER TABLE property_ownership ADD COLUMN paid_for_until DATE;

-- Backfill with the period covered by each property's latest successful payment (status 2 = PaymentStatus.SUCCEEDED).
WITH latest_successful_payment AS (
    SELECT DISTINCT ON (p.associated_property_id)
           p.associated_property_id AS property_ownership_id,
           p.for_period_ending
    FROM payment p
    WHERE p.status = 2
      AND p.associated_property_id IS NOT NULL
    ORDER BY p.associated_property_id, p.payment_created_at DESC, p.created_date DESC
)
UPDATE property_ownership po
SET paid_for_until = lsp.for_period_ending
FROM latest_successful_payment lsp
WHERE lsp.property_ownership_id = po.id;

-- Properties without a successful payment (e.g. registered before payments existed) are treated as paid up to their renewal date.
UPDATE property_ownership
SET paid_for_until = renewal_date
WHERE paid_for_until IS NULL;

ALTER TABLE property_ownership ALTER COLUMN paid_for_until SET NOT NULL;
