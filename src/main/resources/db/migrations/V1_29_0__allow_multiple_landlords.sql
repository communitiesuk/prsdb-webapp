ALTER TABLE property_ownership
DROP
CONSTRAINT property_ownership_primary_landlord_id_fkey;

CREATE TABLE landlordship_members
(
    landlord_id     BIGINT NOT NULL,
    landlordship_id BIGINT NOT NULL,
    CONSTRAINT pk_landlordship_members PRIMARY KEY (landlord_id, landlordship_id)
);

ALTER TABLE landlordship_members
    ADD CONSTRAINT fk_lanmem_on_landlord FOREIGN KEY (landlord_id) REFERENCES landlord (id) ON DELETE CASCADE ;

ALTER TABLE landlordship_members
    ADD CONSTRAINT fk_lanmem_on_property_ownership FOREIGN KEY (landlordship_id) REFERENCES property_ownership (id) ON DELETE CASCADE ;

INSERT INTO landlordship_members (landlord_id, landlordship_id) SELECT p.primary_landlord_id, p.id FROM property_ownership p;

ALTER TABLE property_ownership
DROP
COLUMN primary_landlord_id;
