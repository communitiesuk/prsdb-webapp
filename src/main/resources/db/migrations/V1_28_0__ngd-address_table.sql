CREATE TABLE ngd_address
(
    uprn                 INTEGER         NOT NULL,
    last_modified_date   TIMESTAMPTZ(6),
    created_date         TIMESTAMPTZ(6),
    organisation_name    VARCHAR(100),
    subname              VARCHAR(500),
    name                 VARCHAR(110),
    number               VARCHAR(13),
    street_name          VARCHAR(100)    NOT NULL,
    locality             VARCHAR(35),
    town_name            VARCHAR(35),
    postcode             VARCHAR(8)      NOT NULL,
    full_address         VARCHAR(1000)   NOT NULL,
    local_custodian_code INTEGER         NOT NULL,
    CONSTRAINT pk_ngdaddress PRIMARY KEY (uprn)
);

COMMENT ON TABLE ngd_address IS 'dataPackageVersionId=';