ALTER TABLE property_ownership
    ADD incomplete_compliance_form_id BIGINT;

ALTER TABLE property_ownership
    ADD CONSTRAINT uc_propertyownership_incomplete_compliance_form UNIQUE (incomplete_compliance_form_id);

ALTER TABLE property_ownership
    ADD CONSTRAINT FK_PROPERTY_OWNERSHIP_INCOMPLETE_COMPLIANCE_FORM FOREIGN KEY (incomplete_compliance_form_id) REFERENCES form_context (id);