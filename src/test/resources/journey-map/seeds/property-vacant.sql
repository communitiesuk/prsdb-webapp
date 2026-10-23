UPDATE property_ownership
SET is_occupied = false,
    current_num_households = 0,
    current_num_tenants = 0,
    bills_included_list = NULL,
    custom_bills_included = NULL,
    furnished_status = NULL,
    rent_frequency = NULL,
    custom_rent_frequency = NULL,
    rent_amount = NULL,
    tenancy_provide_later = NULL
WHERE id = 1;

UPDATE property_compliance SET tenancy_started_before_epc_expiry = NULL WHERE property_ownership_id = 1;
