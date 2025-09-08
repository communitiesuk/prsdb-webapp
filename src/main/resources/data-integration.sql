INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao','2024-10-15 00:00:00+00'),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ','2024-10-15 00:00:00+00'),
       ('urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk', '2025-02-19 12:01:07.575927+00'),
       ('urn:fdc:gov.uk:2022:DySqeEXIC4G2xauOirtTDcezwCPLZgQPUQZmQ-aIIMk', '2025-02-26 17:02:19.625996+00'),
       ('urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU','2025-03-06 15:32:59.529898+00'),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI','2025-03-12 17:12:19.833105+00'),
       ('urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU','2025-03-17 10:13:36.388805+00'),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s','2025-03-18 10:13:36.388805+00'),
       ('urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo','2024-10-15 00:00:00+00'),
       ('urn:fdc:gov.uk:2022:sgO5-g7fThIp2MhXMcvFo5N6ObnstGFVNSYFkghMd24','2025-03-06 08:22:41.002251+00'),
       ('urn:fdc:gov.uk:2022:La9gwI6zvuzT3yvKjsKEH2cDbtL88wNbiqAeXQ0plEM','2025-03-06 10:33:22.395944+00'),
       ('urn:fdc:gov.uk:2022:GzFopg--2AyE6XtssVWwQTPELVQFupHJOjpONWS2uz0', '2025-05-01 10:33:22.395944+00') ON CONFLICT DO NOTHING;

INSERT INTO form_context (id, created_date, last_modified_date, journey_type, context, subject_identifier)
VALUES (1, current_date, current_date, 3, '{"lookup-address":{"houseNameOrNumber":"1","postcode":"WC2R 1LA"},"looked-up-addresses":"[{\"singleLineAddress\":\"1, SAVOY COURT, LONDON, WC2R 0EX\",\"localAuthorityId\":318,\"uprn\":100023432931,\"buildingNumber\":\"1\",\"streetName\":\"SAVOY COURT\",\"townName\":\"LONDON\",\"postcode\":\"WC2R 0EX\"}]","select-address":{"address":"1, SAVOY COURT, LONDON, WC2R 0EX"},"property-type":{"customPropertyType":"","propertyType":"DETACHED_HOUSE"}}','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo'),
       (2, '2024-10-15 00:00:00+00', null, 7, '{}','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo'),
       (3, '2025-01-15 00:00:00+00', null, 7, '{}','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo'),
       (4, '2025-01-15 00:00:00+00', null, 7, '{}','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo'),
       (5, '2025-01-15 00:00:00+00', null, 7, '{}','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo') ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('form_context', 'id'), (SELECT MAX(id) FROM form_context));

INSERT INTO local_authority_user (id, created_date, last_modified_date, subject_identifier, is_manager, local_authority_id, email, name, has_accepted_privacy_notice)
VALUES (1,'2024-10-15 00:00:00+00','2024-10-15 00:00:00+00','urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao',true,2,'Team-PRSDB+laadmin@softwire.com','PRSDB LA Admin', true),
       (2,'2024-10-15 00:00:00+00','2025-02-21 16:12:51.530782+00','urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ',false,2,'Team-PRSDB+lauser@softwire.com','PRSDB La User', true),
       (3,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk',true,2,'travis.woodward@communities.gov.uk','Travis Woodward', true),
       (4,'2025-02-26 17:02:19.625996+00',null,'urn:fdc:gov.uk:2022:DySqeEXIC4G2xauOirtTDcezwCPLZgQPUQZmQ-aIIMk',true,2,'travis.woodward@softwire.com','Travis Woodward (Softwire)', true),
       (5,'2025-03-06 15:32:59.529898+00',null,'urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU',true,2,'alexander.read@softwire.com','Alexander Read', true),
       (6,'2025-03-12 17:12:19.833105+00','2025-03-12 17:13:10.020624+00','urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI',true,2,'jasmin.conterio@softwire.com','Jasmin Conterio', true),
       (7,'2025-03-17 10:13:36.388805+00',null,'urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU',true,2,'kiran.randhawakukar@softwire.com','Kiran Fake Name', true),
       (8,'2025-03-18 10:13:36.388805+00',null,'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', true, 2, 'isobel.ibironke@softwire.com','Isobel Ibironke', true) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('local_authority_user', 'id'), (SELECT MAX(id) FROM local_authority_user));

INSERT INTO local_authority_invitation (invited_email, inviting_authority_id, token, created_date)
VALUES ('expired.invitation+1@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11111',  '05/05/2025'),
       ('expired.invitation+2@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11112',  '05/05/2025'),
       ('expired.invitation+3@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11113', '05/05/2025'),
       ('expired.invitation+4@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11114',  '05/05/2025'),
       ('expired.invitation+5@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11115',  '05/05/2025'),
       ('expired.invitation+6@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11116',  '05/05/2025'),
       ('expired.invitation+7@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11117',  '05/05/2025'),
       ('expired.invitation+8@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11118',  '05/05/2025'),
       ('expired.invitation+9@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11119',  '05/05/2025'),
       ('expired.invitation+a@example.com', 2, '1234abcd-5678-abcd-1234-567abcd1111a',  '05/05/2025') ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('local_authority_invitation', 'id'), (SELECT MAX(id) FROM local_authority_invitation));

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1,'2024-10-15 00:00:00+00',2001001001,1),
       (2,'2024-10-15 00:00:00+00',3002001002,1),
       (3,'2025-02-19 08:23:57.267183+00',127959730689,1),
       (4,'2025-02-19 13:41:13.782443+00',116136809177,1),
       (5,'2025-02-19 13:59:18.561124+00',6136283775,1),
       (6,'2025-02-20 11:50:45.723696+00',105757165800,1),
       (7,'2025-02-24 09:29:52.993571+00',116726635893,1),
       (8,'2025-02-24 10:01:14.5196+00',61597584540,1),
       (9,'2025-02-24 10:01:14.5196+00',54697323416,0),
       (10, '2025-01-15 00:00:00+00', 83811499802, 0),
       (11, '2025-01-15 00:00:00+00', 40666195053, 0),
       (12, '2025-01-15 00:00:00+00', 150242309330, 0),
       (13, '2025-01-15 00:00:00+00', 150242309331, 0),
       (14, '2025-01-15 00:00:00+00', 150242309332, 0),
       (15, '2025-01-15 00:00:00+00', 150242309333, 0),
       (16, '2025-01-15 00:00:00+00', 150242309334, 0),
       (17, '2025-07-24 00:00:00+00', 150242309335, 0) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_authority_id)
VALUES (1, '10/15/24', '10/15/24', 2, '1 Fictional Road', 2),
       (2, '2025-01-15 00:00:00+00', null, 100090154792, '5, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 21),
       (3, '2025-01-15 00:00:00+00', null, 100090154788, '1, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 21),
       (4, '2025-01-15 00:00:00+00', null, null, '2, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 21),
       (5, '2025-01-15 00:00:00+00', null, null, '5a, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 21),
       (6, '2025-01-15 00:00:00+00', null, null, '6, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 21),
       (7, '2025-01-15 00:00:00+00', null, null, '7, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 21),
       (8, '2025-01-15 00:00:00+00', null, null, '8, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 21),
       (9, '2025-07-24 00:00:00+00', null, 100090154806, '19, PROVIDENCE WAY, WATERBEACH, CAMBRIDGE, CB25 9QH', 21) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, registration_number_id, address_id, created_date, email, non_england_or_wales_address, is_active, last_modified_date, name, phone_number, subject_identifier, date_of_birth, country_of_residence, is_verified, has_accepted_privacy_notice)
VALUES(1,1,1,'2024-10-15 00:00:00+00','Team-PRSDB+landlord@softwire.com',null,true,'2025-02-25 16:17:18.075473+00','PRSD Landlord','+447123456789','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo','1950-05-13','England or Wales',false, true),
      (2,2,1,'2025-02-19 08:23:57.279777+00','travis.woodward@communities.gov.uk',null,true,null,'LISA S C LOOSELEY','07777777777','urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk','1973-03-14','England or Wales',false, true),
      (3,3,1,'2025-02-19 13:41:13.861504+00','alexander.read@softwire.com',null,true,'2025-03-11 13:38:00.36893+00','KENNETH DECERQUEIRA','07777777777','urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU','1965-07-08','England or Wales',false, true),
      (4,4,1,'2025-02-20 11:50:45.745273+00','kiran.randhawakukar@softwire.com',null,true,'2025-03-06 14:01:33.486684+00','Not Kiran','01234567890','urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU','1965-07-08','England or Wales',false, true),
      (5,5,1,'2025-02-24 09:29:53.079945+00','jasmin.conterio@softwire.com',null,true,'2025-02-27 17:19:52.061638+00','Jasmin Conterio','01223 123 456','urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI','1989-02-02','England or Wales',false, true),
      (6,6,1,'2025-03-06 08:22:41.002251+00','Team-PRSDB+Unverified@softwire.com',null,true,'2025-03-11 13:47:42.800533+00','Unverified Landlord','07777777777','urn:fdc:gov.uk:2022:sgO5-g7fThIp2MhXMcvFo5N6ObnstGFVNSYFkghMd24','1996-03-03','England or Wales',false, true),
      (7,7,1,'2025-03-06 10:33:22.395944+00','team-prsdb+verified@softwire.com',null,true,null,'KENNETH DECERQUEIRA','07777777777','urn:fdc:gov.uk:2022:La9gwI6zvuzT3yvKjsKEH2cDbtL88wNbiqAeXQ0plEM','1965-07-08','England or Wales',true, true),
      (8,8,1,'2025-02-27 13:58:02.81462+00','isobel.ibironke@softwire.com',null,true,null,'Isobel Ibironke','07123456789','urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s','1995-08-4','England or Wales',false, true) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO property (id, status, is_active, property_build_type, address_id, created_date, last_modified_date)
VALUES (1, 1, true, 1, 1, '2024-10-15 00:00:00+00', null),
       (2, 1, true, 1, 2, '2025-01-15 00:00:00+00', null),
       (3, 1, true, 1, 3, '2025-01-15 00:00:00+00', null),
       (4, 1, true, 1, 4, '2025-01-15 00:00:00+00', null),
       (5, 1, true, 1, 5, '2024-10-15 00:00:00+00', null),
       (6, 1, true, 1, 6, '2024-10-15 00:00:00+00', null),
       (7, 1, true, 1, 7, '2024-10-15 00:00:00+00', null),
       (8, 1, true, 1, 8, '2024-10-15 00:00:00+00', null),
       (9, 1, true, 1, 9, '2025-07-24 00:00:00+00', null) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('property', 'id'), (SELECT MAX(id) FROM property));

INSERT INTO property_ownership (id, is_active, occupancy_type, ownership_type, current_num_households, current_num_tenants, registration_number_id, primary_landlord_id, property_id, created_date, last_modified_date, incomplete_compliance_form_id)
VALUES (1, true, 0, 1, 1, 2, 9, 1, 1, '2024-10-15 00:00:00+00', null, 2),
       (2, true, 0, 0, 0, 0, 10, 1, 2,'2025-01-15 00:00:00+00', null, 3),
       (3, true, 0, 0, 0, 0, 11, 1, 3,'2025-01-15 00:00:00+00', null, 4),
       (4, true, 0, 0, 0, 0, 12, 1, 4,'2025-01-15 00:00:00+00', null, 5),
       (5, true, 0, 1, 1, 2, 13, 1, 5, '2024-10-15 00:00:00+00', null, null),
       (6, true, 0, 1, 1, 2, 14, 1, 6, '2024-10-15 00:00:00+00', null, null),
       (7, true, 0, 1, 1, 2, 15, 1, 7, '2024-10-15 00:00:00+00', null, null),
       (8, true, 0, 1, 1, 2, 16, 1, 8, '2024-10-15 00:00:00+00', null, null),
       (9, true, 0, 1, 1, 2, 17, 1, 9, '2025-07-24 00:00:00+00', null, null) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));

INSERT INTO system_operator (id, created_date, last_modified_date, subject_identifier)
VALUES (1,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk'),
       (2,'2025-02-26 17:02:19.625996+00',null,'urn:fdc:gov.uk:2022:DySqeEXIC4G2xauOirtTDcezwCPLZgQPUQZmQ-aIIMk'),
       (3,'2025-03-06 15:32:59.529898+00',null,'urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU'),
       (4,'2025-03-12 17:12:19.833105+00',null,'urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI'),
       (5,'2025-03-17 10:13:36.388805+00',null,'urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU'),
       (6,'2025-03-18 10:13:36.388805+00',null,'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s'),
       (7,'2025-05-01 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:GzFopg--2AyE6XtssVWwQTPELVQFupHJOjpONWS2uz0') ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('system_operator', 'id'), (SELECT MAX(id) FROM system_operator));

INSERT INTO property_compliance (id, property_ownership_id, created_date, last_modified_date,
                                 gas_safety_upload_id, gas_safety_cert_issue_date, gas_safety_cert_engineer_num, gas_safety_cert_exemption_reason, gas_safety_cert_exemption_other_reason,
                                 eicr_id, eicr_issue_date, eicr_exemption_reason, eicr_exemption_other_reason,
                                 epc_url, epc_expiry_date, tenancy_started_before_epc_expiry, epc_energy_rating, epc_exemption_reason, epc_mees_exemption_reason,
                                 has_fire_safety_declaration, has_keep_property_safe_declaration, has_responsibility_to_tenants_declaration)
VALUES (1, 5, '01/01/25', '01/01/25',
        null, null, null, null, null,
        null, null, null, null,
        null, null, null, null, null, null,
        true, true, true),
       (2, 6, '01/01/25', '01/01/25',
        null, '1990-02-28', null, null, null,
        null, '1990-02-28', null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0961-0832', '2021-03-16', false, 'c', null, null,
        true, true, true),
       (3, 7, '01/01/25', '01/01/25',
        null, null, null, 0, null,
        null, null, 0, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-1050-2867', '2031-02-28', null, 'g', null, null,
        true, true, true),
       (4, 8, '01/01/25', '01/01/25',
        null, null, null, 0, null,
        null, null, 0, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0892-1563', '2030-12-03', null, 'c', null, null,
        true, true, true),
       (5, 9, '01/01/25', '01/01/25',
        null, null, null, 0, null,
        null, null, 0, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0892-1563', '2030-12-03', null, 'c', null, null,
        true, true, true) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('property_compliance', 'id'), (SELECT MAX(id) FROM property_compliance));

INSERT INTO passcode (passcode, local_authority_id, created_date, last_modified_date, subject_identifier)
VALUES ('PRSD22', 2, current_date, null, 'urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo'),
       ('PRSD23', 2, current_date, null, 'urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk'),
       ('PRSD24', 2, current_date, null, 'urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU'),
       ('PRSD25', 2, current_date, null, 'urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU'),
       ('PRSD26', 2, current_date, null, 'urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI'),
       ('PRSD27', 2, current_date, null, 'urn:fdc:gov.uk:2022:sgO5-g7fThIp2MhXMcvFo5N6ObnstGFVNSYFkghMd24'),
       ('PRSD29', 2, current_date, null, 'urn:fdc:gov.uk:2022:La9gwI6zvuzT3yvKjsKEH2cDbtL88wNbiqAeXQ0plEM'),
       ('PRSD32', 2, current_date, null, 'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s') ON CONFLICT DO NOTHING;
