DROP VIEW IF EXISTS local_authority_user_or_invitation;

ALTER TABLE local_authority RENAME TO local_council;
ALTER TABLE local_authority_invitation RENAME TO local_council_invitation;
ALTER TABLE local_authority_user RENAME TO local_council_user;

ALTER TABLE address RENAME COLUMN local_authority_id TO local_council_id;

ALTER TABLE passcode RENAME COLUMN local_authority_id TO local_council_id;

ALTER TABLE local_council_invitation RENAME COLUMN inviting_authority_id TO inviting_council_id;

ALTER TABLE local_council_user RENAME COLUMN local_authority_id TO local_council_id;

CREATE VIEW local_council_user_or_invitation AS
    SELECT u.id,
           u.name,
           u.is_manager,
           u.local_council_id,
           'local_council_user' AS entity_type
    FROM local_council_user u
UNION ALL
    SELECT i.id,
           i.invited_email AS name,
           i.invited_as_admin AS is_manager,
           i.inviting_council_id AS local_council_id,
           'local_council_invitation' AS entity_type
    FROM local_council_invitation i;


