UPDATE letting_agent_access
SET encoded_password = (SELECT encoded_password FROM letting_agent_access WHERE token = '3334abcd-5678-abcd-1234-567abcd2222b')
WHERE token = '3334abcd-5678-abcd-1234-567abcd1111a';
