ALTER TABLE property_ownership
    DROP CONSTRAINT IF EXISTS property_ownership_incomplete_compliance_form_id_fkey,
    DROP COLUMN IF EXISTS incomplete_compliance_form_id;

DROP TABLE IF EXISTS form_context;
