-- Sets the gas safety certificate for 2 Fake Way (property ownership 1) to have been issued 13 months ago.
-- Apply on top of data-mockuser-landlord-with-compliance-actions.sql.
-- Used by tests that check the certificate is expired under 12 month gas expiry but valid under 14 month gas expiry.
-- TODO: PDJB-1926: Remove this

UPDATE property_compliance
SET gas_safety_cert_issue_date = (current_date - interval '13 months')::date
WHERE property_ownership_id = 1;
