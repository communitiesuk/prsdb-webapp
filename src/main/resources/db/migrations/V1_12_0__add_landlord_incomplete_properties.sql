CREATE TABLE landlord_incomplete_properties
(
    landlord_id            BIGINT NOT NULL,
    saved_journey_state_id BIGINT NOT NULL,
    CONSTRAINT pk_landlord_incomplete_properties PRIMARY KEY (landlord_id, saved_journey_state_id)
);

ALTER TABLE landlord_incomplete_properties
    ADD CONSTRAINT uc_landlord_incomplete_properties_saved_journey_state UNIQUE (saved_journey_state_id);

ALTER TABLE landlord_incomplete_properties
    ADD CONSTRAINT fk_lanincpro_on_landlord FOREIGN KEY (landlord_id) REFERENCES landlord (id);

ALTER TABLE landlord_incomplete_properties
    ADD CONSTRAINT fk_lanincpro_on_saved_journey_state FOREIGN KEY (saved_journey_state_id) REFERENCES saved_journey_state (id);
