ALTER TABLE landlord_incomplete_properties
    ADD COLUMN user_id VARCHAR(255);

UPDATE landlord_incomplete_properties lip
SET user_id = sjs.subject_identifier
FROM saved_journey_state sjs
WHERE sjs.id = lip.saved_journey_state_id;

ALTER TABLE landlord_incomplete_properties
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE landlord_incomplete_properties
    DROP CONSTRAINT fk_lanincpro_on_landlord;

ALTER TABLE landlord_incomplete_properties
    DROP CONSTRAINT pk_landlord_incomplete_properties;

ALTER TABLE landlord_incomplete_properties
    DROP COLUMN landlord_id;

ALTER TABLE landlord_incomplete_properties
    ADD CONSTRAINT pk_landlord_incomplete_properties PRIMARY KEY (user_id, saved_journey_state_id);

ALTER TABLE landlord_incomplete_properties
    ADD CONSTRAINT fk_lanincpro_on_user FOREIGN KEY (user_id) REFERENCES prsdb_user (id);
