ALTER TABLE property_ownership ADD COLUMN last_occupied_date DATE;

UPDATE property_ownership
SET last_occupied_date = created_date::date
WHERE current_num_tenants > 0
  AND current_num_households > 0
  AND num_bedrooms IS NOT NULL AND num_bedrooms > 0
  AND furnished_status IS NOT NULL
  AND rent_frequency IS NOT NULL
  AND rent_amount IS NOT NULL;

ALTER TABLE property_compliance ADD COLUMN gas_safety_cert_provide_later BOOLEAN;
ALTER TABLE property_compliance ADD COLUMN electrical_safety_cert_provide_later BOOLEAN;
ALTER TABLE property_compliance ADD COLUMN epc_provide_later BOOLEAN;

UPDATE property_compliance
SET gas_safety_cert_provide_later = false,
    electrical_safety_cert_provide_later = false,
    epc_provide_later = false;
