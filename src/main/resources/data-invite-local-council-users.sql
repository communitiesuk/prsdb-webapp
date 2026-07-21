-- Dedicated seed data for the InviteLocalCouncilUsers integration tests.
--
-- These tests assert on the number of "inform admin" emails sent when inviting a new user, which is
-- equal to the number of managers (is_manager = true) in the council. Keeping this data separate from
-- data-local.sql means changes to the shared local seed data do not break the invite tests.
--
-- Local council pages authenticate via the internal-access provider. Its mock user (see
-- MockInternalAccessController) is 'ia-mock-user-12345'. It must be a manager of council 1 so that it
-- can access the manage users and invite user pages.
--
-- Council 1 (BATH AND NORTH EAST SOMERSET COUNCIL) is seeded via Flyway migration reference data.

INSERT INTO prsdb_user (id, created_date)
VALUES ('ia-mock-user-12345', '10/14/24'),
       ('urn:fdc:gov.uk:2022:INVITE_ADMIN_1', '10/14/24'),
       ('urn:fdc:gov.uk:2022:INVITE_ADMIN_2', '10/14/24'),
       ('urn:fdc:gov.uk:2022:INVITE_USER_1', '10/14/24');

INSERT INTO local_council_user (subject_identifier, is_manager, local_council_id, created_date, last_modified_date,
                                name, email, has_accepted_privacy_notice)
VALUES ('ia-mock-user-12345', true, 1, '10/14/24', '10/14/24', 'Mock User IA', 'test@example.com', true),
       ('urn:fdc:gov.uk:2022:INVITE_ADMIN_1', true, 1, '10/14/24', '10/14/24', 'Invite Admin One',
        'invite.admin.one@example.com', true),
       ('urn:fdc:gov.uk:2022:INVITE_ADMIN_2', true, 1, '10/14/24', '10/14/24', 'Invite Admin Two',
        'invite.admin.two@example.com', true),
       ('urn:fdc:gov.uk:2022:INVITE_USER_1', false, 1, '10/14/24', '10/14/24', 'Invite User One',
        'invite.user.one@example.com', true);

SELECT setval(pg_get_serial_sequence('local_council_user', 'id'), (SELECT MAX(id) FROM local_council_user));
