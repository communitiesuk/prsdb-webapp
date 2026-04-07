CREATE TABLE gas_safety_uploads
(
    property_compliance_id     BIGINT NOT NULL,
    gas_safety_file_uploads_id BIGINT NOT NULL
);

ALTER TABLE gas_safety_uploads
    ADD CONSTRAINT uc_gas_safety_uploads_gassafetyfileuploads UNIQUE (gas_safety_file_uploads_id);

ALTER TABLE gas_safety_uploads
    ADD CONSTRAINT fk_gassafupl_on_file_upload FOREIGN KEY (gas_safety_file_uploads_id) REFERENCES file_upload (id);

ALTER TABLE gas_safety_uploads
    ADD CONSTRAINT fk_gassafupl_on_property_compliance FOREIGN KEY (property_compliance_id) REFERENCES property_compliance (id);
