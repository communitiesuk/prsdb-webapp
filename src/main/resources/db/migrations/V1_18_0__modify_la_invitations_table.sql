ALTER TABLE local_authority_invitation
    ADD COLUMN invited_as_admin BOOLEAN;

DROP VIEW local_authority_user_or_invitation;

CREATE VIEW local_authority_user_or_invitation AS
SELECT  u.id,
        olu.name,
        u.is_manager,
        u.local_authority_id,
        'local_authority_user' AS entity_type
FROM local_authority_user u
         JOIN one_login_user olu ON olu.id = u.subject_identifier
UNION ALL
SELECT  i.id,
        i.invited_email AS name,
        i.invited_as_admin AS is_manager,
        i.inviting_authority_id AS local_authority_id,
        'local_authority_invitation' AS entity_type
FROM local_authority_invitation i