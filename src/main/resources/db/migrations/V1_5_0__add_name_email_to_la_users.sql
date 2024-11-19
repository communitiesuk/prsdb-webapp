ALTER TABLE local_authority_user
    ADD email VARCHAR(255);

ALTER TABLE local_authority_user
    ADD name VARCHAR(255);

ALTER TABLE local_authority_user
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE local_authority_user
    ALTER COLUMN name SET NOT NULL;