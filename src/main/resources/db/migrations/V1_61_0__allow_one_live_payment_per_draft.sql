-- Payment status ordinals: 0 = CREATED, 1 = CAPTURABLE (no longer stored), 6 = CANCELLING.
-- Capturable payments, and all but the newest created payment for each draft, are marked for cancellation.
UPDATE payment
SET status = 6
WHERE status = 1;

UPDATE payment older
SET status = 6
WHERE status = 0
  AND EXISTS (SELECT 1
              FROM payment newer
              WHERE newer.status = 0
                AND newer.associated_incomplete_property_user_id = older.associated_incomplete_property_user_id
                AND newer.associated_incomplete_property_saved_journey_state_id = older.associated_incomplete_property_saved_journey_state_id
                AND newer.payment_created_at > older.payment_created_at);

CREATE UNIQUE INDEX uc_payment_one_created_per_incomplete_property
    ON payment (associated_incomplete_property_user_id, associated_incomplete_property_saved_journey_state_id)
    WHERE status = 0;

ALTER TABLE payment
    DROP CONSTRAINT FK_PAYMENT_ON_ASSOCIATEDINCOMPLETEPROPERTY;

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_ASSOCIATEDINCOMPLETEPROPERTY
        FOREIGN KEY (associated_incomplete_property_user_id, associated_incomplete_property_saved_journey_state_id)
            REFERENCES landlord_incomplete_properties (user_id, saved_journey_state_id)
            MATCH FULL
            ON DELETE SET NULL;

ALTER TABLE payment
    DROP CONSTRAINT EXACTLY_ONE_ASSOCIATED_PROPERTY;

ALTER TABLE payment
    ADD CONSTRAINT AT_MOST_ONE_ASSOCIATED_PROPERTY
        CHECK (associated_property_id IS NULL OR associated_incomplete_property_user_id IS NULL);
