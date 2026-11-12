-- Change property ownership 6 to an EIC on top of data-mockuser-landlord-with-compliance-actions.sql.
UPDATE property_compliance
SET electrical_cert_type = 1,
    eic_issue_date = DATE '2000-06-01',
    eicr_expiry_date = null
WHERE property_ownership_id = 6;
