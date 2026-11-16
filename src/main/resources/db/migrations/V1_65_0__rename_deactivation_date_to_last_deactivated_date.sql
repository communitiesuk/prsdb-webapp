ALTER TABLE landlord RENAME COLUMN deactivation_date TO last_deactivated_date;

ALTER TABLE property_ownership RENAME COLUMN deactivation_date TO last_deactivated_date;
