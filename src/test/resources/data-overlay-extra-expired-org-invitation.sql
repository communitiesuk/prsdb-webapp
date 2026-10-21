-- Adds a second expired organisational landlord invitation for the local organisation landlord (id 36).
-- Apply on top of data-local.sql.
-- Used by tests that need more than one expired invitation, e.g. to check that removing one leaves the others.

INSERT INTO organisational_landlord_invitation (created_date, last_modified_date, token, invited_email, organisation_landlord_id,
                                               role, invitation_expired_email_sent, is_hidden)
VALUES (current_date - 70, current_date - 70, '1234abcd-5678-abcd-1234-567abcd2222f', 'expired-org-invite-f@example.com', 36,
        0, false, false);
