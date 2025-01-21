CREATE TABLE tmp
(
    AUTH_CODE              VARCHAR(255),
    ACCOUNT_NAME           VARCHAR(255),
    ACCOUNT_TYPE_NAME      VARCHAR(255),
    ADDRESS_SUBMITTER_FLAG CHAR,
    STREET_SUBMITTER_FLAG  CHAR,
    SUBMITTER_FLAG         CHAR
);

COPY tmp
    FROM '/data/addressbase-local-custodian-codes.csv'
    WITH (FORMAT CSV, HEADER);

INSERT INTO local_authority(custodian_code, name)
SELECT AUTH_CODE, ACCOUNT_NAME
FROM tmp;

DROP TABLE tmp;