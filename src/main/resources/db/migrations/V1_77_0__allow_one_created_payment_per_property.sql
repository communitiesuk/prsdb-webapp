-- Payment status ordinal 0 = CREATED.
-- A property can have at most one CREATED payment, so a landlord starting a reactivation payment must cancel
-- any other CREATED payment for the property first ("newest payment wins").
-- Registration payments are only linked to a property once they have succeeded, so no existing rows conflict.
CREATE UNIQUE INDEX uc_payment_one_created_per_property
    ON payment (associated_property_id)
    WHERE status = 0;
