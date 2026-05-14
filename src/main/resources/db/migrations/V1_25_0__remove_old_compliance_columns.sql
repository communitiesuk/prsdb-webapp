ALTER TABLE property_compliance DROP COLUMN IF EXISTS gas_safety_upload_id;
ALTER TABLE property_compliance DROP COLUMN IF EXISTS eicr_upload_id;
ALTER TABLE property_compliance DROP COLUMN IF EXISTS eicr_issue_date;
ALTER TABLE property_compliance DROP COLUMN IF EXISTS gas_safety_cert_engineer_num;
ALTER TABLE property_compliance DROP COLUMN IF EXISTS gas_safety_cert_exemption_reason;
ALTER TABLE property_compliance DROP COLUMN IF EXISTS gas_safety_cert_exemption_other_reason;
ALTER TABLE property_compliance DROP COLUMN IF EXISTS eicr_exemption_reason;
ALTER TABLE property_compliance DROP COLUMN IF EXISTS eicr_exemption_other_reason;
