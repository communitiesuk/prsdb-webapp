-- Adds landlord 2 as a joint landlord on the mock user's inactive property 6 (which has no details to provide).
-- Properties with joint landlords can't be deactivated manually, so this also makes property 6 look like it was
-- deactivated for non-payment: it expired 60 days ago and was deactivated 28 days after that.
-- Apply on top of data-local.sql.

INSERT INTO ownership_link (landlord_id, landlordship_id, created_date)
VALUES (2, 6, '2025-01-15');

UPDATE property_ownership
SET renewal_date          = current_date - 60,
    paid_for_until        = current_date - 60,
    last_deactivated_date = current_date - 60 + 28
WHERE id = 6;
