ALTER TABLE property_compliance
    ADD has_keep_property_safe_declaration BOOLEAN DEFAULT TRUE NOT NULL;

ALTER TABLE property_compliance
    ADD has_responsibility_to_tenants_declaration BOOLEAN DEFAULT TRUE NOT NULL;

ALTER TABLE property_compliance
    ADD tenancy_started_before_epc_expiry BOOLEAN;
