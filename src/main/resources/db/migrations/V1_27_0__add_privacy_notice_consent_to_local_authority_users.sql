ALTER TABLE local_authority_user
    ADD has_accepted_privacy_notice BOOLEAN;

UPDATE local_authority_user
    SET has_accepted_privacy_notice = FALSE
    WHERE has_accepted_privacy_notice IS NULL;

ALTER TABLE local_authority_user
    ALTER COLUMN has_accepted_privacy_notice SET NOT NULL;