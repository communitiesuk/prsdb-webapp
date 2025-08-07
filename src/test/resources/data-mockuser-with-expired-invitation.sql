INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:UVWXY', '10/14/24');

INSERT INTO local_authority_invitation (invited_email, inviting_authority_id, token, invited_as_admin, created_date)
VALUES ('expired.invitation+a@example.com', 2, '1234abcd-5678-abcd-1234-567abcd1111a', false, current_timestamp - interval '49 hour');