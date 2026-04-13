CREATE TABLE electrical_safety_uploads
(
    property_compliance_id              BIGINT NOT NULL,
    electrical_safety_file_uploads_id   BIGINT NOT NULL
);

ALTER TABLE electrical_safety_uploads
    ADD CONSTRAINT uc_electrical_safety_uploads_electricalsafetyfileuploads UNIQUE (electrical_safety_file_uploads_id);

ALTER TABLE electrical_safety_uploads
    ADD CONSTRAINT fk_elesafupl_on_file_upload FOREIGN KEY (electrical_safety_file_uploads_id) REFERENCES file_upload (id);

ALTER TABLE electrical_safety_uploads
    ADD CONSTRAINT fk_elesafupl_on_property_compliance FOREIGN KEY (property_compliance_id) REFERENCES property_compliance (id);
