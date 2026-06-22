ALTER TABLE joint_landlord_invitation
    ADD COLUMN inviting_landlord_id BIGINT;

UPDATE joint_landlord_invitation jli
SET inviting_landlord_id = po.primary_landlord_id
FROM property_ownership po
WHERE jli.registered_propertyid = po.id;

ALTER TABLE joint_landlord_invitation
    ALTER COLUMN inviting_landlord_id SET NOT NULL,
    ADD CONSTRAINT fk_joint_landlord_invitation_inviting_landlord
        FOREIGN KEY (inviting_landlord_id) REFERENCES landlord(id);
