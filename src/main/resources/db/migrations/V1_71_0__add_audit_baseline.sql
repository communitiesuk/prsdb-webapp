DO
$$
    DECLARE
        baseline_rev BIGINT;
    BEGIN
        IF NOT (EXISTS (SELECT 1 FROM landlord)
            OR EXISTS (SELECT 1 FROM organisation_governing_body_member)
            OR EXISTS (SELECT 1 FROM organisational_landlord_user)
            OR EXISTS (SELECT 1 FROM organisational_landlord_invitation)
            OR EXISTS (SELECT 1 FROM ownership_link)
            OR EXISTS (SELECT 1 FROM property_ownership)
            OR EXISTS (SELECT 1 FROM property_compliance)
            OR EXISTS (SELECT 1 FROM file_upload)
            OR EXISTS (SELECT 1 FROM license)
            OR EXISTS (SELECT 1 FROM freeholder)
            OR EXISTS (SELECT 1 FROM superior_landlord)
            OR EXISTS (SELECT 1 FROM property_manager)
            OR EXISTS (SELECT 1 FROM joint_landlord_invitation)
            OR EXISTS (SELECT 1 FROM letting_agent_access)) THEN
            RETURN;
        END IF;

        INSERT INTO audit_revision (created_date) VALUES (current_timestamp) RETURNING id INTO baseline_rev;

        INSERT INTO landlord_aud (id, rev, revtype, landlord_type, created_date,
                                  registration_number_id, anniversary_day, anniversary_month, is_active,
                                  has_registered_property, last_deactivated_date, individual_subject_identifier,
                                  individual_name, individual_email, individual_phone_number, individual_address_id,
                                  individual_country_of_residence, individual_non_england_or_wales_address,
                                  individual_date_of_birth, individual_is_verified,
                                  individual_has_accepted_privacy_notice, organisation_landlord_name,
                                  organisation_address_id, organisation_email, organisation_phone_number,
                                  organisation_registrant_name, organisation_registrant_date_of_birth,
                                  organisation_registrant_email, organisation_registrant_phone_number,
                                  organisation_is_company, organisation_is_charity, organisation_is_trust,
                                  organisation_company_number, organisation_charity_registered_with,
                                  organisation_charity_number, organisation_lead_trustee_name,
                                  organisation_lead_trustee_date_of_birth, organisation_lead_trustee_email,
                                  organisation_lead_trustee_phone, organisation_lead_trustee_address_id,
                                  organisation_main_contact_name, organisation_main_contact_email,
                                  organisation_main_contact_phone)
        SELECT id, baseline_rev, 0, landlord_type, created_date,
               registration_number_id, anniversary_day, anniversary_month, is_active,
               has_registered_property, last_deactivated_date, individual_subject_identifier,
               individual_name, individual_email, individual_phone_number, individual_address_id,
               individual_country_of_residence, individual_non_england_or_wales_address,
               individual_date_of_birth, individual_is_verified,
               individual_has_accepted_privacy_notice, organisation_landlord_name,
               organisation_address_id, organisation_email, organisation_phone_number,
               organisation_registrant_name, organisation_registrant_date_of_birth,
               organisation_registrant_email, organisation_registrant_phone_number,
               organisation_is_company, organisation_is_charity, organisation_is_trust,
               organisation_company_number, organisation_charity_registered_with,
               organisation_charity_number, organisation_lead_trustee_name,
               organisation_lead_trustee_date_of_birth, organisation_lead_trustee_email,
               organisation_lead_trustee_phone, organisation_lead_trustee_address_id,
               organisation_main_contact_name, organisation_main_contact_email,
               organisation_main_contact_phone
        FROM landlord;

        INSERT INTO organisation_governing_body_member_aud (id, rev, revtype, created_date,
                                                            organisation_landlord_id, type, name, date_of_birth,
                                                            address_id)
        SELECT id, baseline_rev, 0, created_date, organisation_landlord_id, type, name,
               date_of_birth, address_id
        FROM organisation_governing_body_member;

        INSERT INTO organisational_landlord_user_aud (id, rev, revtype, created_date,
                                                      organisation_landlord_id, subject_identifier, name, email, role)
        SELECT id, baseline_rev, 0, created_date, organisation_landlord_id, subject_identifier,
               name, email, role
        FROM organisational_landlord_user;

        INSERT INTO organisational_landlord_invitation_aud (id, rev, revtype, created_date,
                                                            invited_email, organisation_landlord_id, role,
                                                            invitation_expired_email_sent, is_hidden)
        SELECT id, baseline_rev, 0, created_date, invited_email, organisation_landlord_id, role,
               invitation_expired_email_sent, is_hidden
        FROM organisational_landlord_invitation;

        INSERT INTO ownership_link_aud (id, rev, revtype, created_date, landlord_id, landlordship_id)
        SELECT id, baseline_rev, 0, created_date, landlord_id, landlordship_id
        FROM ownership_link;

        INSERT INTO property_ownership_aud (id, rev, revtype, created_date, is_active,
                                            ownership_type, ownership_type_before_pdjb955, current_num_households,
                                            current_num_tenants, is_occupied, registration_number_id,
                                            property_build_type, address_id, correspondence_email,
                                            correspondence_address_id, license_id, property_manager_id, num_bedrooms,
                                            bills_included_list, custom_bills_included, furnished_status,
                                            rent_frequency, custom_rent_frequency, custom_property_type, rent_amount,
                                            marked_joint_landlord, last_occupied_date, license_provide_later,
                                            tenancy_provide_later, renewal_date, last_deactivated_date, paid_for_until)
        SELECT id, baseline_rev, 0, created_date, is_active,
               ownership_type, ownership_type_before_pdjb955, current_num_households,
               current_num_tenants, is_occupied, registration_number_id,
               property_build_type, address_id, correspondence_email,
               correspondence_address_id, license_id, property_manager_id, num_bedrooms,
               bills_included_list, custom_bills_included, furnished_status,
               rent_frequency, custom_rent_frequency, custom_property_type, rent_amount,
               marked_joint_landlord, last_occupied_date, license_provide_later,
               tenancy_provide_later, renewal_date, last_deactivated_date, paid_for_until
        FROM property_ownership;

        INSERT INTO property_compliance_aud (id, rev, revtype, created_date, property_ownership_id,
                                             gas_safety_cert_issue_date, has_gas_supply, gas_safety_cert_provide_later,
                                             eicr_expiry_date, eic_issue_date, electrical_cert_type,
                                             electrical_safety_cert_provide_later, epc_url, epc_expiry_date,
                                             tenancy_started_before_epc_expiry, epc_energy_rating,
                                             epc_exemption_reason, epc_mees_exemption_reason, epc_provide_later,
                                             has_fire_safety_declaration, has_keep_property_safe_declaration,
                                             has_responsibility_to_tenants_declaration)
        SELECT id, baseline_rev, 0, created_date, property_ownership_id,
               gas_safety_cert_issue_date, has_gas_supply, gas_safety_cert_provide_later,
               eicr_expiry_date, eic_issue_date, electrical_cert_type,
               electrical_safety_cert_provide_later, epc_url, epc_expiry_date,
               tenancy_started_before_epc_expiry, epc_energy_rating,
               epc_exemption_reason, epc_mees_exemption_reason, epc_provide_later,
               has_fire_safety_declaration, has_keep_property_safe_declaration,
               has_responsibility_to_tenants_declaration
        FROM property_compliance;

        INSERT INTO gas_safety_uploads_aud (rev, revtype, property_compliance_id, gas_safety_file_uploads_id)
        SELECT baseline_rev, 0, property_compliance_id, gas_safety_file_uploads_id
        FROM gas_safety_uploads;

        INSERT INTO electrical_safety_uploads_aud (rev, revtype, property_compliance_id, electrical_safety_file_uploads_id)
        SELECT baseline_rev, 0, property_compliance_id, electrical_safety_file_uploads_id
        FROM electrical_safety_uploads;

        INSERT INTO file_upload_aud (id, rev, revtype, created_date, status, object_key, e_tag,
                                     version_id, extension, file_name)
        SELECT id, baseline_rev, 0, created_date, status, object_key, e_tag, version_id,
               extension, file_name
        FROM file_upload;

        INSERT INTO license_aud (id, rev, revtype, created_date, license_type, license_number)
        SELECT id, baseline_rev, 0, created_date, license_type, license_number
        FROM license;

        INSERT INTO freeholder_aud (id, rev, revtype, created_date, name, email,
                                    property_ownership_id)
        SELECT id, baseline_rev, 0, created_date, name, email, property_ownership_id
        FROM freeholder;

        INSERT INTO superior_landlord_aud (id, rev, revtype, created_date, name, email,
                                           property_ownership_id)
        SELECT id, baseline_rev, 0, created_date, name, email, property_ownership_id
        FROM superior_landlord;

        INSERT INTO property_manager_aud (id, rev, revtype, created_date, name, email)
        SELECT id, baseline_rev, 0, created_date, name, email
        FROM property_manager;

        INSERT INTO joint_landlord_invitation_aud (id, rev, revtype, created_date, invited_email,
                                                   registered_propertyid, inviting_landlord_name,
                                                   invitation_expired_email_sent, is_hidden)
        SELECT id, baseline_rev, 0, created_date, invited_email, registered_propertyid,
               inviting_landlord_name, invitation_expired_email_sent, is_hidden
        FROM joint_landlord_invitation;

        INSERT INTO letting_agent_access_aud (id, rev, revtype, created_date, invited_email,
                                              property_ownership_id)
        SELECT id, baseline_rev, 0, created_date, invited_email, property_ownership_id
        FROM letting_agent_access;
    END
$$;
