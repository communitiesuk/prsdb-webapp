ALTER TABLE property_compliance RENAME COLUMN electrical_safety_expiry_date TO eicr_expiry_date;

ALTER TABLE property_compliance ADD COLUMN eic_issue_date DATE;

-- electrical_cert_type is stored as the CertificateType ordinal: Eicr = 0, Eic = 1
UPDATE property_compliance
SET eic_issue_date   = (eicr_expiry_date - INTERVAL '5 years')::date,
    eicr_expiry_date = NULL
WHERE electrical_cert_type = 1
  AND eicr_expiry_date IS NOT NULL;
