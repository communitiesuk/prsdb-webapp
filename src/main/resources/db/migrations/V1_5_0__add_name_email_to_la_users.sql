ALTER TABLE local_authority_user
    ADD email VARCHAR(255);

ALTER TABLE local_authority_user
    ADD name VARCHAR(255);

ALTER TABLE local_authority_user
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE local_authority_user
    ALTER COLUMN name SET NOT NULL;

DROP VIEW local_authority_user_or_invitation;

CREATE VIEW local_authority_user_or_invitation AS
    SELECT  u.id,
            u.name,
            u.is_manager,
            u.local_authority_id,
            'local_authority_user' AS entity_type
    FROM local_authority_user u
    UNION ALL
    SELECT  i.id,
            i.invited_email AS name,
            false AS is_manager,
            i.inviting_authority_id AS local_authority_id,
            'local_authority_invitation' AS entity_type
    FROM local_authority_invitation i;
