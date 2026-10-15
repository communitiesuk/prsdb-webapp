INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:ORG02', '07/23/26'),
       ('urn:fdc:gov.uk:2022:ORG03', '07/23/26');

INSERT INTO organisational_landlord_user (organisation_landlord_id, subject_identifier, name, email, role, created_date)
VALUES (36, 'urn:fdc:gov.uk:2022:ORG02', 'Beth Admin', 'beth.admin@example.com', 0, '07/23/26'),
       (36, 'urn:fdc:gov.uk:2022:ORG03', 'Carl Editor', 'carl.editor@example.com', 1, '07/23/26');

INSERT INTO organisational_landlord_invitation (created_date, last_modified_date, token, invited_email, organisation_landlord_id,
                                               role, invitation_expired_email_sent, is_hidden)
VALUES (current_date - 5, current_date - 5, '1234abcd-5678-abcd-1234-567abcd2222e', 'hidden-org-invite-e@example.com', 36,
        0, false, true),
       (current_date - 70, current_date - 70, '1234abcd-5678-abcd-1234-567abcd2222f', 'expired-org-invite-f@example.com', 36,
        0, false, false);
