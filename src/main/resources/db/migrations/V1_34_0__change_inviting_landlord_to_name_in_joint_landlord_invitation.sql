ALTER TABLE joint_landlord_invitation
    ADD COLUMN inviting_landlord_name VARCHAR(255);

UPDATE joint_landlord_invitation jli
SET inviting_landlord_name = l.name
FROM landlord l
WHERE jli.inviting_landlord_id = l.id;

ALTER TABLE joint_landlord_invitation
    DROP COLUMN inviting_landlord_id;

ALTER TABLE joint_landlord_invitation
    ALTER COLUMN inviting_landlord_name SET NOT NULL;
