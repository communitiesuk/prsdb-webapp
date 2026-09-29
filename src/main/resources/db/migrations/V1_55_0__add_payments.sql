CREATE TABLE payment
(
    payment_id                                            VARCHAR(255)                NOT NULL,
    last_modified_date                                    TIMESTAMP WITHOUT TIME ZONE,
    created_date                                          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    amount_in_pence                                       INTEGER                     NOT NULL,
    reference                                             VARCHAR(255)                NOT NULL,
    payment_created_at                                    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    for_period_ending                                     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status                                                SMALLINT                    NOT NULL,
    paying_user_id                                        VARCHAR(255),
    associated_property_id                                BIGINT,
    associated_incomplete_property_user_id                VARCHAR(255),
    associated_incomplete_property_saved_journey_state_id BIGINT,
    CONSTRAINT pk_payment PRIMARY KEY (payment_id)
);

ALTER TABLE payment
   ADD CONSTRAINT EXACTLY_ONE_ASSOCIATED_PROPERTY
        CHECK ((associated_property_id IS NULL) <> (associated_incomplete_property_user_id IS NULL));

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_ASSOCIATEDPROPERTY
    FOREIGN KEY (associated_property_id)
    REFERENCES property_ownership (id)
    ON DELETE CASCADE;

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_ASSOCIATEDINCOMPLETEPROPERTY
    FOREIGN KEY (associated_incomplete_property_user_id, associated_incomplete_property_saved_journey_state_id)
    REFERENCES landlord_incomplete_properties (user_id, saved_journey_state_id)
    MATCH FULL
    ON DELETE CASCADE;

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_PAYINGUSER FOREIGN KEY (paying_user_id) REFERENCES prsdb_user (id) ON DELETE SET NULL;
