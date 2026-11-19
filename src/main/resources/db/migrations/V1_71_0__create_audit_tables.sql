CREATE TABLE landlord_aud
(
    id                                      BIGINT NOT NULL,
    rev                                     BIGINT NOT NULL,
    revtype                                 SMALLINT,
    landlord_type                           INTEGER,
    created_date                            TIMESTAMPTZ(6),
    registration_number_id                  BIGINT,
    anniversary_day                         INTEGER,
    anniversary_month                       INTEGER,
    is_active                               BOOLEAN,
    has_registered_property                 BOOLEAN,
    last_deactivated_date                   DATE,
    individual_subject_identifier           VARCHAR(255),
    individual_name                         VARCHAR(255),
    individual_email                        VARCHAR(255),
    individual_phone_number                 VARCHAR(255),
    individual_address_id                   BIGINT,
    individual_country_of_residence         VARCHAR(255),
    individual_non_england_or_wales_address VARCHAR(1000),
    individual_date_of_birth                DATE,
    individual_is_verified                  BOOLEAN,
    individual_has_accepted_privacy_notice  BOOLEAN,
    organisation_landlord_name              VARCHAR(255),
    organisation_address_id                 BIGINT,
    organisation_email                      VARCHAR(255),
    organisation_phone_number               VARCHAR(255),
    organisation_registrant_name            VARCHAR(255),
    organisation_registrant_date_of_birth   DATE,
    organisation_registrant_email           VARCHAR(255),
    organisation_registrant_phone_number    VARCHAR(255),
    organisation_is_company                 BOOLEAN,
    organisation_is_charity                 BOOLEAN,
    organisation_is_trust                   BOOLEAN,
    organisation_company_number             VARCHAR(255),
    organisation_charity_registered_with    SMALLINT,
    organisation_charity_number             VARCHAR(255),
    organisation_lead_trustee_name          VARCHAR(255),
    organisation_lead_trustee_date_of_birth DATE,
    organisation_lead_trustee_email         VARCHAR(255),
    organisation_lead_trustee_phone         VARCHAR(255),
    organisation_lead_trustee_address_id    BIGINT,
    organisation_main_contact_name          VARCHAR(255),
    organisation_main_contact_email         VARCHAR(255),
    organisation_main_contact_phone         VARCHAR(255),
    CONSTRAINT pk_landlord_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_landlord_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE organisation_governing_body_member_aud
(
    id                       BIGINT NOT NULL,
    rev                      BIGINT NOT NULL,
    revtype                  SMALLINT,
    created_date             TIMESTAMPTZ(6),
    organisation_landlord_id BIGINT,
    type                     SMALLINT,
    name                     VARCHAR(255),
    date_of_birth            DATE,
    address_id               BIGINT,
    CONSTRAINT pk_organisation_governing_body_member_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_organisation_governing_body_member_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE organisational_landlord_user_aud
(
    id                       BIGINT NOT NULL,
    rev                      BIGINT NOT NULL,
    revtype                  SMALLINT,
    created_date             TIMESTAMPTZ(6),
    organisation_landlord_id BIGINT,
    subject_identifier       VARCHAR(255),
    name                     VARCHAR(255),
    email                    VARCHAR(255),
    role                     SMALLINT,
    CONSTRAINT pk_organisational_landlord_user_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_organisational_landlord_user_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE organisational_landlord_invitation_aud
(
    id                            BIGINT NOT NULL,
    rev                           BIGINT NOT NULL,
    revtype                       SMALLINT,
    created_date                  TIMESTAMPTZ(6),
    invited_email                 VARCHAR(255),
    organisation_landlord_id      BIGINT,
    role                          SMALLINT,
    invitation_expired_email_sent BOOLEAN,
    is_hidden                     BOOLEAN,
    CONSTRAINT pk_organisational_landlord_invitation_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_organisational_landlord_invitation_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE ownership_link_aud
(
    id                 BIGINT NOT NULL,
    rev                BIGINT NOT NULL,
    revtype            SMALLINT,
    created_date       TIMESTAMPTZ(6),
    landlord_id        BIGINT,
    landlordship_id    BIGINT,
    CONSTRAINT pk_ownership_link_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_ownership_link_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE property_ownership_aud
(
    id                            BIGINT NOT NULL,
    rev                           BIGINT NOT NULL,
    revtype                       SMALLINT,
    created_date                  TIMESTAMPTZ(6),
    is_active                     BOOLEAN,
    ownership_type                SMALLINT,
    ownership_type_before_pdjb955 SMALLINT,
    current_num_households        INTEGER,
    current_num_tenants           INTEGER,
    is_occupied                   BOOLEAN,
    registration_number_id        BIGINT,
    property_build_type           SMALLINT,
    address_id                    BIGINT,
    correspondence_email          VARCHAR(255),
    correspondence_address_id     BIGINT,
    license_id                    BIGINT,
    property_manager_id           BIGINT,
    num_bedrooms                  INTEGER,
    bills_included_list           VARCHAR,
    custom_bills_included         VARCHAR(255),
    furnished_status              SMALLINT,
    rent_frequency                SMALLINT,
    custom_rent_frequency         VARCHAR(255),
    custom_property_type          VARCHAR(255),
    rent_amount                   NUMERIC(9, 2),
    marked_joint_landlord         BOOLEAN,
    last_occupied_date            DATE,
    license_provide_later         BOOLEAN,
    tenancy_provide_later         BOOLEAN,
    renewal_date                  DATE,
    last_deactivated_date         DATE,
    paid_for_until                DATE,
    CONSTRAINT pk_property_ownership_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_property_ownership_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE property_compliance_aud
(
    id                                        BIGINT NOT NULL,
    rev                                       BIGINT NOT NULL,
    revtype                                   SMALLINT,
    created_date                              TIMESTAMPTZ(6),
    property_ownership_id                     BIGINT,
    gas_safety_cert_issue_date                DATE,
    has_gas_supply                            BOOLEAN,
    gas_safety_cert_provide_later             BOOLEAN,
    eicr_expiry_date                          DATE,
    eic_issue_date                            DATE,
    electrical_cert_type                      SMALLINT,
    electrical_safety_cert_provide_later      BOOLEAN,
    epc_url                                   VARCHAR(255),
    epc_expiry_date                           DATE,
    tenancy_started_before_epc_expiry         BOOLEAN,
    epc_energy_rating                         VARCHAR(255),
    epc_exemption_reason                      SMALLINT,
    epc_mees_exemption_reason                 SMALLINT,
    epc_provide_later                         BOOLEAN,
    has_fire_safety_declaration               BOOLEAN,
    has_keep_property_safe_declaration        BOOLEAN,
    has_responsibility_to_tenants_declaration BOOLEAN,
    CONSTRAINT pk_property_compliance_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_property_compliance_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE gas_safety_uploads_aud
(
    rev                        BIGINT NOT NULL,
    revtype                    SMALLINT,
    property_compliance_id     BIGINT NOT NULL,
    gas_safety_file_uploads_id BIGINT NOT NULL,
    CONSTRAINT pk_gas_safety_uploads_aud PRIMARY KEY (rev, property_compliance_id, gas_safety_file_uploads_id),
    CONSTRAINT fk_gas_safety_uploads_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE electrical_safety_uploads_aud
(
    rev                               BIGINT NOT NULL,
    revtype                           SMALLINT,
    property_compliance_id            BIGINT NOT NULL,
    electrical_safety_file_uploads_id BIGINT NOT NULL,
    CONSTRAINT pk_electrical_safety_uploads_aud PRIMARY KEY (rev, property_compliance_id, electrical_safety_file_uploads_id),
    CONSTRAINT fk_electrical_safety_uploads_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE file_upload_aud
(
    id                 BIGINT NOT NULL,
    rev                BIGINT NOT NULL,
    revtype            SMALLINT,
    created_date       TIMESTAMPTZ(6),
    status             SMALLINT,
    object_key         VARCHAR(255),
    e_tag              VARCHAR(255),
    version_id         VARCHAR(255),
    extension          VARCHAR(255),
    file_name          VARCHAR(255),
    CONSTRAINT pk_file_upload_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_file_upload_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE license_aud
(
    id                 BIGINT NOT NULL,
    rev                BIGINT NOT NULL,
    revtype            SMALLINT,
    created_date       TIMESTAMPTZ(6),
    license_type       SMALLINT,
    license_number     VARCHAR(255),
    CONSTRAINT pk_license_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_license_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE freeholder_aud
(
    id                    BIGINT NOT NULL,
    rev                   BIGINT NOT NULL,
    revtype               SMALLINT,
    created_date          TIMESTAMPTZ(6),
    name                  VARCHAR(255),
    email                 VARCHAR(255),
    property_ownership_id BIGINT,
    CONSTRAINT pk_freeholder_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_freeholder_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE superior_landlord_aud
(
    id                    BIGINT NOT NULL,
    rev                   BIGINT NOT NULL,
    revtype               SMALLINT,
    created_date          TIMESTAMPTZ(6),
    name                  VARCHAR(255),
    email                 VARCHAR(255),
    property_ownership_id BIGINT,
    CONSTRAINT pk_superior_landlord_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_superior_landlord_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE property_manager_aud
(
    id                 BIGINT NOT NULL,
    rev                BIGINT NOT NULL,
    revtype            SMALLINT,
    created_date       TIMESTAMPTZ(6),
    name               VARCHAR(255),
    email              VARCHAR(255),
    CONSTRAINT pk_property_manager_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_property_manager_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE joint_landlord_invitation_aud
(
    id                            BIGINT NOT NULL,
    rev                           BIGINT NOT NULL,
    revtype                       SMALLINT,
    created_date                  TIMESTAMPTZ(6),
    invited_email                 VARCHAR(255),
    registered_propertyid         BIGINT,
    inviting_landlord_name        VARCHAR(255),
    invitation_expired_email_sent BOOLEAN,
    is_hidden                     BOOLEAN,
    CONSTRAINT pk_joint_landlord_invitation_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_joint_landlord_invitation_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);

CREATE TABLE letting_agent_access_aud
(
    id                    BIGINT NOT NULL,
    rev                   BIGINT NOT NULL,
    revtype               SMALLINT,
    created_date          TIMESTAMPTZ(6),
    invited_email         VARCHAR(255),
    property_ownership_id BIGINT,
    CONSTRAINT pk_letting_agent_access_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_letting_agent_access_aud_on_audit_revision FOREIGN KEY (rev) REFERENCES audit_revision (id)
);
