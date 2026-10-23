UPDATE organisational_landlord_invitation
SET created_date = current_date - 60, last_modified_date = current_date - 60
WHERE token = '1234abcd-5678-abcd-1234-567abcd2222a';
