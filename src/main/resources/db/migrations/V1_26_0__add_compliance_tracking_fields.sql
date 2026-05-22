ALTER TABLE property_ownership ADD COLUMN last_occupied_date DATE;

UPDATE property_ownership
SET last_occupied_date = created_date::date
WHERE current_num_tenants > 0;

ALTER TABLE property_compliance ADD COLUMN gas_safety_cert_provide_later BOOLEAN;
ALTER TABLE property_compliance ADD COLUMN electrical_safety_cert_provide_later BOOLEAN;
ALTER TABLE property_compliance ADD COLUMN epc_provide_later BOOLEAN;

UPDATE property_compliance
SET gas_safety_cert_provide_later = false,
    electrical_safety_cert_provide_later = false,
    epc_provide_later = false;
