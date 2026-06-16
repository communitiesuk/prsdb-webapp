INSERT INTO prsdb_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:n93slCXHsxJ9rU6-AFM0jFIctYQjYf0KN9YVuJT-cao', '2024-10-15 00:00:00+00'),
       ('urn:fdc:gov.uk:2022:cgVX2oJWKHMwzm8Gzx25CSoVXixVS0rw32Sar4Om8vQ', '2024-10-15 00:00:00+00'),
       ('urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk', '2025-02-19 12:01:07.575927+00'),
       ('urn:fdc:gov.uk:2022:DySqeEXIC4G2xauOirtTDcezwCPLZgQPUQZmQ-aIIMk', '2025-02-26 17:02:19.625996+00'),
       ('urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU', '2025-03-06 15:32:59.529898+00'),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', '2025-03-12 17:12:19.833105+00'),
       ('urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU', '2025-03-17 10:13:36.388805+00'),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', '2025-03-18 10:13:36.388805+00'),
       ('urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', '2024-10-15 00:00:00+00'),
       ('urn:fdc:gov.uk:2022:sgO5-g7fThIp2MhXMcvFo5N6ObnstGFVNSYFkghMd24', '2025-03-06 08:22:41.002251+00'),
       ('urn:fdc:gov.uk:2022:La9gwI6zvuzT3yvKjsKEH2cDbtL88wNbiqAeXQ0plEM', '2025-03-06 10:33:22.395944+00'),
       ('urn:fdc:gov.uk:2022:GzFopg--2AyE6XtssVWwQTPELVQFupHJOjpONWS2uz0', '2025-05-01 10:33:22.395944+00'),
       ('7442a5af6972afba82cb61b66df4d2d2249cfc752af5336320d3e3f8cff9a324', '2026-05-05 00:00:00+00'), -- Bill.Haigh@communities.gov.uk
       ('d3bc128e9145369b00a80ebc9ba8e9a035b91302a98d65ea110dc69f064f8a16', '2026-05-05 00:00:00+00'), -- Jasmin.Conterio@communities.gov.uk
       ('e4ea31a38bb24eae34ac3186218c0084fce639a7fe3d36436f716535f45eafbe', '2026-05-05 00:00:00+00'), -- Thomas.Hanmer@communities.gov.uk
       ('ae24b0d78eda0aa3cf8d51cb56f73ffd6e5678e2ccd44d3ddc4a2e2eb5e2f350', '2026-05-05 00:00:00+00'), -- Rowan.Hill@communities.gov.uk
       ('a8df415dcb0356bd9ea1ac3f368a5603fc609e5ad4654e8f5b1c0415d4f0fb46', '2026-05-05 00:00:00+00'), -- Alexander.Read@communities.gov.uk
       ('a7b19a3c6de8b210be76c44b1d2e3ef3eb59cf19402c20e5983e1ac371d9e696', '2026-05-05 00:00:00+00'), -- Travis.Woodward@communities.gov.uk
       ('cb7d851c94b22400e90d6e6265c9867542e0d39fb22d35ddcc2baee1dcf43225', '2024-10-15 00:00:00+00'), -- lcadmin.prsdb@softwire.com
       ('2488954246d8ffea9e419f3a2db5eb5b694e5859b123a008a533dbe8bf0aa16c', '2024-10-15 00:00:00+00')  -- lcuser.prsdb@softwire.com
    ON CONFLICT DO NOTHING;


INSERT INTO local_council_user (id, created_date, last_modified_date, subject_identifier, is_manager, local_council_id, email, name,
                                has_accepted_privacy_notice)
VALUES (1, '2024-10-15 00:00:00+00', '2024-10-15 00:00:00+00', 'cb7d851c94b22400e90d6e6265c9867542e0d39fb22d35ddcc2baee1dcf43225', true, 2,
        'lcadmin.prsdb@softwire.com', 'PRSDB LA Admin', true),
       (2, '2024-10-15 00:00:00+00', '2025-02-21 16:12:51.530782+00', '2488954246d8ffea9e419f3a2db5eb5b694e5859b123a008a533dbe8bf0aa16c',
        false, 2, 'lcuser.prsdb@softwire.com', 'PRSDB La User', true),
       (3, '2025-02-19 12:01:07.575927+00', null, 'a7b19a3c6de8b210be76c44b1d2e3ef3eb59cf19402c20e5983e1ac371d9e696', true, 2,
        'travis.woodward@communities.gov.uk', 'Travis Woodward', true),
       (9, '2026-05-05 00:00:00+00', null, 'a8df415dcb0356bd9ea1ac3f368a5603fc609e5ad4654e8f5b1c0415d4f0fb46', true, 2,
        'Alexander.Read@communities.gov.uk', 'Alexander Read', true),
       (10, '2026-05-05 00:00:00+00', null, 'd3bc128e9145369b00a80ebc9ba8e9a035b91302a98d65ea110dc69f064f8a16', true, 2,
        'Jasmin.Conterio@communities.gov.uk', 'Jasmin Conterio', true),
       (11, '2026-05-05 00:00:00+00', null, 'ae24b0d78eda0aa3cf8d51cb56f73ffd6e5678e2ccd44d3ddc4a2e2eb5e2f350', true, 2,
        'Rowan.Hill@communities.gov.uk', 'Rowan Hill', true),
       (12, '2026-05-05 00:00:00+00', null, '7442a5af6972afba82cb61b66df4d2d2249cfc752af5336320d3e3f8cff9a324', true, 2,
        'Bill.Haigh@communities.gov.uk', 'Bill Haigh', true),
       (13, '2026-05-05 00:00:00+00', null, 'e4ea31a38bb24eae34ac3186218c0084fce639a7fe3d36436f716535f45eafbe', true, 2,
        'Thomas.Hanmer@communities.gov.uk', 'Thomas Hanmer', true) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('local_council_user', 'id'), (SELECT MAX(id) FROM local_council_user));

INSERT INTO local_council_invitation (invited_email, inviting_council_id, token, created_date)
VALUES ('expired.invitation+1@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11111', '05/05/2025'),
       ('expired.invitation+2@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11112', '05/05/2025'),
       ('expired.invitation+3@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11113', '05/05/2025'),
       ('expired.invitation+4@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11114', '05/05/2025'),
       ('expired.invitation+5@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11115', '05/05/2025'),
       ('expired.invitation+6@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11116', '05/05/2025'),
       ('expired.invitation+7@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11117', '05/05/2025'),
       ('expired.invitation+8@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11118', '05/05/2025'),
       ('expired.invitation+9@example.com', 2, '1234abcd-5678-abcd-1234-567abcd11119', '05/05/2025'),
       ('expired.invitation+a@example.com', 2, '1234abcd-5678-abcd-1234-567abcd1111a', '05/05/2025') ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('local_council_invitation', 'id'), (SELECT MAX(id) FROM local_council_invitation));

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1, '2024-10-15 00:00:00+00', 2001001001, 1),
       (2, '2024-10-15 00:00:00+00', 3002001002, 1),
       (3, '2025-02-19 08:23:57.267183+00', 127959730689, 1),
       (4, '2025-02-19 13:41:13.782443+00', 116136809177, 1),
       (5, '2025-02-19 13:59:18.561124+00', 6136283775, 1),
       (6, '2025-02-20 11:50:45.723696+00', 105757165800, 1),
       (7, '2025-02-24 09:29:52.993571+00', 116726635893, 1),
       (8, '2025-02-24 10:01:14.5196+00', 61597584540, 1),
       (9, '2025-02-24 10:01:14.5196+00', 54697323416, 0),
       (10, '2025-01-15 00:00:00+00', 83811499802, 0),
       (11, '2025-01-15 00:00:00+00', 40666195053, 0),
       (12, '2025-01-15 00:00:00+00', 150242309330, 0),
       (13, '2025-01-15 00:00:00+00', 150242309331, 0),
       (14, '2025-01-15 00:00:00+00', 150242309332, 0),
       (15, '2025-01-15 00:00:00+00', 150242309333, 0),
       (16, '2025-01-15 00:00:00+00', 150242309334, 0),
       (17, '2025-07-24 00:00:00+00', 150242309335, 0),
       (18, '2026-02-27 00:00:00+00', 150242309336, 0),
       (19, '2026-03-02 00:00:00+00', 210000000019, 0),
       (20, '2026-03-02 00:00:00+00', 210000000020, 0),
       (21, '2026-03-02 00:00:00+00', 210000000021, 0),
       (22, '2026-03-02 00:00:00+00', 210000000022, 0),
       (23, '2026-03-02 00:00:00+00', 210000000023, 0),
       (24, '2026-03-02 00:00:00+00', 210000000024, 0),
       (25, '2026-03-02 00:00:00+00', 210000000025, 0),
       (26, '2026-03-02 00:00:00+00', 210000000026, 0),
       (27, '2026-03-02 00:00:00+00', 210000000027, 0),
       (28, '2026-03-02 00:00:00+00', 210000000028, 0),
       (29, '2026-03-02 00:00:00+00', 210000000029, 0),
       (30, '2026-03-02 00:00:00+00', 210000000030, 0),
       (31, '2026-03-02 00:00:00+00', 210000000031, 0),
       (32, '2026-03-02 00:00:00+00', 210000000032, 0),
       (33, '2026-03-02 00:00:00+00', 210000000033, 0),
       (34, '2026-03-02 00:00:00+00', 210000000034, 0),
       (35, '2026-03-02 00:00:00+00', 210000000035, 0),
       (36, '2026-03-02 00:00:00+00', 210000000036, 0),
       (37, '2026-03-02 00:00:00+00', 210000000037, 0),
       (38, '2026-03-02 00:00:00+00', 210000000038, 0),
       (39, '2026-03-02 00:00:00+00', 210000000039, 0),
       (40, '2026-03-02 00:00:00+00', 210000000040, 0),
       (41, '2026-03-02 00:00:00+00', 210000000041, 0),
       (42, '2026-03-02 00:00:00+00', 210000000042, 0),
       (43, '2026-03-02 00:00:00+00', 210000000043, 0),
       (44, '2026-03-02 00:00:00+00', 210000000044, 0),
       (45, '2026-03-02 00:00:00+00', 210000000045, 0),
       (46, '2026-03-02 00:00:00+00', 210000000046, 0),
       (47, '2026-03-02 00:00:00+00', 210000000047, 0),
       (48, '2026-04-14 00:00:00+00', 210000000048, 0),
       (49, '2026-04-14 00:00:00+00', 210000000049, 0),
       (50, '2026-04-14 00:00:00+00', 210000000050, 0),
       (51, '2026-04-14 00:00:00+00', 210000000051, 0),
       (52, '2026-04-14 00:00:00+00', 210000000052, 0),
       (53, '2026-04-14 00:00:00+00', 210000000053, 0),
       (54, '2026-04-14 00:00:00+00', 210000000054, 0),
       (55, '2026-04-14 00:00:00+00', 210000000055, 0),
       (56, '2026-04-14 00:00:00+00', 210000000056, 0) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO landlord (id, registration_number_id, address_id, created_date, email, non_england_or_wales_address, is_active,
                      last_modified_date, name, phone_number, subject_identifier, date_of_birth, country_of_residence, is_verified,
                      has_accepted_privacy_notice)
VALUES (1, 1, 1, '2024-10-15 00:00:00+00', 'Team-PRSDB+landlord@softwire.com', null, true, '2025-02-25 16:17:18.075473+00', 'PRSD Landlord',
        '+447123456789', 'urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo', '1950-05-13', 'England or Wales', false, true),
       (2, 2, 1, '2025-02-19 08:23:57.279777+00', 'travis.woodward@communities.gov.uk', null, true, null, 'LISA S C LOOSELEY',
        '07777777777', 'urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk', '1973-03-14', 'England or Wales', false, true),
       (3, 3, 1, '2025-02-19 13:41:13.861504+00', 'alexander.read@softwire.com', null, true, '2025-03-11 13:38:00.36893+00',
        'KENNETH DECERQUEIRA', '07777777777', 'urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU', '1965-07-08',
        'England or Wales', false, true),
       (4, 4, 1, '2025-02-20 11:50:45.745273+00', 'kiran.randhawakukar@softwire.com', null, true, '2025-03-06 14:01:33.486684+00',
        'Not Kiran', '01234567890', 'urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU', '1965-07-08', 'England or Wales',
        false, true),
       (5, 5, 1, '2025-02-24 09:29:53.079945+00', 'jasmin.conterio@softwire.com', null, true, '2025-02-27 17:19:52.061638+00',
        'Jasmin Conterio', '01223 123 456', 'urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI', '1989-02-02',
        'England or Wales', false, true),
       (6, 6, 1, '2025-03-06 08:22:41.002251+00', 'Team-PRSDB+Unverified@softwire.com', null, true, '2025-03-11 13:47:42.800533+00',
        'Unverified Landlord', '07777777777', 'urn:fdc:gov.uk:2022:sgO5-g7fThIp2MhXMcvFo5N6ObnstGFVNSYFkghMd24', '1996-03-03',
        'England or Wales', false, true),
       (7, 7, 1, '2025-03-06 10:33:22.395944+00', 'team-prsdb+verified@softwire.com', null, true, null, 'KENNETH DECERQUEIRA',
        '07777777777', 'urn:fdc:gov.uk:2022:La9gwI6zvuzT3yvKjsKEH2cDbtL88wNbiqAeXQ0plEM', '1965-07-08', 'England or Wales', true, true),
       (8, 8, 1, '2025-02-27 13:58:02.81462+00', 'isobel.ibironke@softwire.com', null, true, null, 'Isobel Ibironke', '07123456789',
        'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', '1995-08-4', 'England or Wales', false,
        true) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO property_ownership (id, is_active, ownership_type, current_num_households, current_num_tenants, registration_number_id,
                                address_id, created_date, last_modified_date, property_build_type,
                                num_bedrooms, bills_included_list, custom_bills_included, furnished_status, rent_frequency,
                                custom_rent_frequency, rent_amount, custom_property_type)
VALUES (1, true, 1, 1, 2, 9, 1, '2024-10-15 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (2, true, 0, 0, 0, 10, 2, '2025-01-15 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null),
       (3, true, 0, 0, 0, 11, 3, '2025-01-15 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null),
       (4, true, 0, 0, 0, 12, 4, '2025-01-15 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null),
       (5, true, 1, 1, 2, 13, 5, '2024-10-15 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (6, true, 1, 1, 2, 14, 6, '2024-10-15 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (7, true, 1, 1, 2, 15, 7, '2024-10-15 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (8, true, 1, 1, 2, 16, 8, '2024-10-15 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (9, true, 1, 1, 2, 17, 9, '2025-07-24 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (10, true, 1, 1, 2, 18, 10, '2026-02-27 00:00:00+00', null, 4,
        1, null, null, 2, 1, null, 123.12, 'End terrace'),
       (11, true, 1, 1, 2, 19, 7449159, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (12, true, 1, 1, 2, 20, 7449160, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (13, true, 1, 1, 2, 21, 7449165, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (14, true, 1, 1, 2, 22, 7449169, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (15, true, 1, 1, 2, 23, 7449173, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (16, true, 1, 1, 2, 24, 7449174, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (17, true, 1, 1, 2, 25, 7449179, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (18, true, 1, 1, 2, 26, 7449180, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (19, true, 1, 1, 2, 27, 7449185, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (20, true, 1, 1, 2, 28, 7449193, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (21, true, 1, 1, 2, 29, 7449194, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (22, true, 1, 1, 2, 30, 7449198, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (23, true, 1, 1, 2, 31, 7449199, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (24, true, 1, 1, 2, 32, 7449203, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (25, true, 1, 1, 2, 33, 7449206, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (26, true, 1, 1, 2, 34, 7449208, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (27, true, 1, 1, 2, 35, 7449213, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (28, true, 1, 1, 2, 36, 7449214, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (29, true, 1, 1, 2, 37, 7449217, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (30, true, 1, 1, 2, 38, 7449222, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (31, true, 1, 1, 2, 39, 7449226, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (32, true, 1, 1, 2, 40, 7449227, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (33, true, 1, 1, 2, 41, 7449231, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (34, true, 1, 1, 2, 42, 7449235, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (35, true, 1, 1, 2, 43, 7449239, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (36, true, 1, 1, 2, 44, 7449250, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (37, true, 1, 1, 2, 45, 7449252, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (38, true, 1, 1, 2, 46, 7449253, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (39, true, 1, 1, 2, 47, 7449254, '2026-03-02 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (40, true, 1, 1, 2, 48, 7449161, '2026-04-14 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (41, true, 1, 1, 2, 49, 7449162, '2026-04-14 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (42, true, 1, 1, 2, 50, 7449163, '2026-04-14 00:00:00+00', null, 1,
        1, null, null, 2, 1, null, 123.12, null),
       (43, true, 0, 0, 0, 51, 7449166, '2026-04-14 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null),
       (44, true, 0, 0, 0, 52, 7449167, '2026-04-14 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null),
       (45, true, 0, 0, 0, 53, 7449170, '2026-04-14 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null),
       (46, true, 0, 0, 0, 54, 7449175, '2026-04-14 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null),
       (47, true, 0, 0, 0, 55, 7449181, '2026-04-14 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null),
       (48, true, 0, 0, 0, 56, 7449182, '2026-04-14 00:00:00+00', null, 1,
        null, null, null, null, null, null, null, null) ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));

INSERT INTO landlordship_members (landlord_id, landlordship_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 6),
       (1, 7),
       (1, 8),
       (1, 9),
       (1, 10),
       (1, 11),
       (1, 12),
       (1, 13),
       (1, 14),
       (1, 15),
       (1, 16),
       (1, 17),
       (1, 18),
       (1, 19),
       (1, 20),
       (1, 21),
       (1, 22),
       (1, 23),
       (1, 24),
       (1, 25),
       (1, 26),
       (1, 27),
       (1, 28),
       (1, 29),
       (1, 30),
       (1, 31),
       (1, 32),
       (1, 33),
       (1, 34),
       (1, 35),
       (1, 36),
       (1, 37),
       (1, 38),
       (1, 39),
       (1, 40),
       (1, 41),
       (1, 42),
       (1, 43),
       (1, 44),
       (1, 45),
       (1, 46),
       (1, 47),
       (1, 48) ON CONFLICT DO NOTHING;

INSERT INTO system_operator (id, created_date, last_modified_date, subject_identifier)
VALUES (1, '2025-02-19 12:01:07.575927+00', null, 'urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk'),
       (2, '2025-02-26 17:02:19.625996+00', null, 'urn:fdc:gov.uk:2022:DySqeEXIC4G2xauOirtTDcezwCPLZgQPUQZmQ-aIIMk'),
       (3, '2025-03-06 15:32:59.529898+00', null, 'urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU'),
       (4, '2025-03-12 17:12:19.833105+00', null, 'urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI'),
       (5, '2025-03-17 10:13:36.388805+00', null, 'urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU'),
       (6, '2025-03-18 10:13:36.388805+00', null, 'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s'),
       (7, '2025-05-01 12:01:07.575927+00', null, 'urn:fdc:gov.uk:2022:GzFopg--2AyE6XtssVWwQTPELVQFupHJOjpONWS2uz0') ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('system_operator', 'id'), (SELECT MAX(id) FROM system_operator));

INSERT INTO property_compliance (id, property_ownership_id, created_date, last_modified_date, gas_safety_cert_issue_date, has_gas_supply,
                                 electrical_safety_expiry_date, electrical_cert_type, epc_url, epc_expiry_date,
                                 tenancy_started_before_epc_expiry, epc_energy_rating, epc_exemption_reason, epc_mees_exemption_reason,
                                 has_fire_safety_declaration, has_keep_property_safe_declaration, has_responsibility_to_tenants_declaration)
VALUES (1, 5, '01/01/25', '01/01/25', null, true, null, null, null, null, null, null, null, null, true, true, true),
       (2, 6, '01/01/25', '01/01/25', '1990-02-28', true, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0961-0832', '2021-03-16',
        false, 'c', null, null, true, true, true),
       (3, 7, '01/01/25', '01/01/25', null, false, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-1050-2867', '2031-02-28',
        null, 'g', null, null, true, true, true),
       (4, 8, '01/01/25', '01/01/25', null, false, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0892-1563', '2030-12-03',
        null, 'c', null, null, true, true, true),
       (5, 9, '01/01/25', '01/01/25', null, false, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0892-1563', '2030-12-03',
        null, 'c', null, null, true, true, true),
       (6, 40, '2026-04-14', '2026-04-14', '2026-01-15', true, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0892-1563', '2031-06-15',
        null, 'c', null, null, true, true, true),
       (7, 41, '2026-04-14', '2026-04-14', null, false, null, null, null, null, null, null, 0, null, true, true, true),
       (8, 42, '2026-04-14', '2026-04-14', null, false, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-1050-2867', '2031-06-15',
        null, 'f', null, 0, true, true, true),
       (9, 43, '2026-04-14', '2026-04-14', '2026-01-15', true, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0892-1563', '2031-06-15',
        null, 'c', null, null, true, true, true),
       (10, 44, '2026-04-14', '2026-04-14', '2024-06-01', true, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-0961-0832', '2025-04-14',
        false, 'c', null, null, true, true, true),
       (11, 45, '2026-04-14', '2026-04-14', null, false, null, null, null, null, null, null, 0, null, true, true, true),
       (12, 46, '2026-04-14', '2026-04-14', null, false, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-1050-2867', '2031-06-15',
        null, 'f', null, 0, true, true, true),
       (13, 47, '2026-04-14', '2026-04-14', null, true, null, null, null, null, null, null, null, null, true, true, true),
       (14, 48, '2026-04-14', '2026-04-14', null, false, null, null,
        'https://find-energy-certificate-staging.digital.communities.gov.uk/energy-certificate/0000-0000-0000-1050-2867', '2031-06-15',
        null, 'g', null, null, true, true, true),
       (15, 1, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (16, 2, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (17, 3, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (18, 4, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (19, 10, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (20, 11, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (21, 12, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (22, 13, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (23, 14, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (24, 15, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (25, 16, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (26, 17, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (27, 18, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (28, 19, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (29, 20, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (30, 21, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (31, 22, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (32, 23, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (33, 24, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (34, 25, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (35, 26, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (36, 27, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (37, 28, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (38, 29, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (39, 30, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (40, 31, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (41, 32, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (42, 33, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (43, 34, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (44, 35, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (45, 36, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (46, 37, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (47, 38, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true),
       (48, 39, '01/01/25', null, null, null, null, null, null, null, null, null, null, null, true, true, true);

SELECT setval(pg_get_serial_sequence('property_compliance', 'id'), (SELECT MAX(id) FROM property_compliance));

INSERT INTO passcode (passcode, created_date, last_modified_date, subject_identifier)
VALUES ('PRSD22', current_date, null, 'urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo'),
       ('PRSD23', current_date, null, 'urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk'),
       ('PRSD24', current_date, null, 'urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU'),
       ('PRSD25', current_date, null, 'urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU'),
       ('PRSD26', current_date, null, 'urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI'),
       ('PRSD27', current_date, null, 'urn:fdc:gov.uk:2022:sgO5-g7fThIp2MhXMcvFo5N6ObnstGFVNSYFkghMd24'),
       ('PRSD29', current_date, null, 'urn:fdc:gov.uk:2022:La9gwI6zvuzT3yvKjsKEH2cDbtL88wNbiqAeXQ0plEM'),
       ('PRSD32', current_date, null, 'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s') ON CONFLICT DO NOTHING;

-- =============================================================================
-- Metrics test cohort 1: deterministic data (System Operator > Metrics page)
-- =============================================================================
-- Seeds a deterministic cohort so the Metrics percentiles (median / p90 / p95
-- "time to first property") can be verified against known, exact values.
--
-- NOTE: this is written as plain set-based INSERT statements (NOT a procedural DO block),
-- because the Spring `spring.sql.init` script runner splits scripts on ';' and
-- does not understand PostgreSQL dollar-quoting -- a procedural DO block would be
-- shredded into broken fragments and silently skipped (continue-on-error: true).
--
-- All rows use FIXED ids in a high, isolated range (9_000_xxx / 9_001_xxx) plus
-- ON CONFLICT DO NOTHING, so the script is idempotent: because `mode: always`
-- re-runs it on every startup, fixed ids ensure the cohort is inserted once and
-- skipped thereafter (it never duplicates or grows).
--
-- The cohort is anchored in 2030 so it is fully ISOLATED from the rest of the
-- seed data (which lives in 2024-2026). Query the 2030 reporting period and only
-- this cohort is counted.
--
-- For landlord i (1..101):
--   landlord.created_date  = 2030-01-01 09:00:00 (same for all)
--   property.created_date  = landlord.created_date + (i - 1) * 86400 seconds
-- NOTE: the offset is added as absolute SECONDS, not as a `days` interval. In
-- Postgres, `timestamptz + interval 'N days'` is DST-aware under a session whose
-- timezone observes DST (the app's JDBC connection uses Europe/London): an offset
-- that crosses the spring-forward boundary (2030-03-31) lands 1 hour short, so
-- toDays() floors p90/p95 to 89/94. Using absolute seconds (24h * offset) makes the
-- duration exact regardless of session timezone, so the known values stay 50/90/95.
-- so "time to first property" takes the values 0, 1, 2, ..., 100 days. The
-- Metrics service computes percentiles as rank = fraction * (n - 1) with linear
-- interpolation, so for n = 101 this gives exactly:
--   median = 50 days, p90 = 90 days, p95 = 95 days, average = 50 days.
--
-- HOW TO VERIFY: open System Operator > Metrics and submit the date range
--   From: 1 / 1 / 2030    To: 31 / 12 / 2030
-- Expected: 101 landlord registrations, 101 verified, 101 properties,
--   101 landlords with a property, median/p90/p95 = 50/90/95 days.
-- =============================================================================
INSERT INTO prsdb_user (id, created_date)
SELECT 'metrics-test-user-' || i, TIMESTAMPTZ '2030-01-01 09:00:00+00'
FROM generate_series(1, 101) AS s(i)
ON CONFLICT DO NOTHING;

INSERT INTO address (id, created_date, single_line_address, local_council_id, postcode, building_number)
SELECT 9000000 + i, TIMESTAMPTZ '2030-01-01 09:00:00+00',
       i || ' Metrics Landlord Street, MT1 1AA', NULL::integer, 'MT1 1AA', i || ''
FROM generate_series(1, 101) AS s(i)
UNION ALL
SELECT 9001000 + i, TIMESTAMPTZ '2030-01-01 09:00:00+00' + make_interval(secs => (i - 1) * 86400),
       i || ' Metrics Property Street, MT2 2BB', NULL::integer, 'MT2 2BB', i || ''
FROM generate_series(1, 101) AS s(i)
ON CONFLICT DO NOTHING;

INSERT INTO registration_number (id, created_date, number, type)
SELECT 9000000 + i, TIMESTAMPTZ '2030-01-01 09:00:00+00', 900000000000 + i, 1 -- landlord
FROM generate_series(1, 101) AS s(i)
UNION ALL
SELECT 9001000 + i, TIMESTAMPTZ '2030-01-01 09:00:00+00' + make_interval(secs => (i - 1) * 86400), 900000100000 + i, 0 -- property
FROM generate_series(1, 101) AS s(i)
ON CONFLICT DO NOTHING;

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence, is_verified,
                      has_accepted_privacy_notice)
SELECT 9000000 + i, TIMESTAMPTZ '2030-01-01 09:00:00+00', TIMESTAMPTZ '2030-01-01 09:00:00+00',
       9000000 + i, 9000000 + i, DATE '1990-01-01', true, '07111111111', 'metrics-test-user-' || i,
       'Metrics Test Landlord ' || i, 'metrics.landlord.' || i || '@example.com', 'England or Wales', true, true
FROM generate_series(1, 101) AS s(i)
ON CONFLICT DO NOTHING;

INSERT INTO property_ownership (id, is_active, ownership_type, current_num_households, current_num_tenants,
                               registration_number_id, address_id, created_date, last_modified_date, license_id,
                               property_build_type, num_bedrooms, marked_joint_landlord)
SELECT 9001000 + i, true, 1, 1, 2, 9001000 + i, 9001000 + i,
       TIMESTAMPTZ '2030-01-01 09:00:00+00' + make_interval(secs => (i - 1) * 86400),
       TIMESTAMPTZ '2030-01-01 09:00:00+00' + make_interval(secs => (i - 1) * 86400), NULL, 1, 2, false
FROM generate_series(1, 101) AS s(i)
ON CONFLICT DO NOTHING;

INSERT INTO landlordship_members (landlord_id, landlordship_id)
SELECT 9000000 + i, 9001000 + i
FROM generate_series(1, 101) AS s(i)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- Metrics test cohort 2: realistic / randomised data
-- =============================================================================
-- A more lifelike cohort: 150 landlords registering at RANDOM times between
-- 2028-01-01 and 2030-01-01 (the window ends exactly where the deterministic
-- 2030 cohort begins, so the two never overlap). Each landlord:
--   * is verified with ~60% probability
--   * owns a random number of properties (0..4; ~15% own none, 1 is most common)
--   * owns ONLY their own properties (always the registrant), so "time to first
--     property" is always >= 0 -- this cohort never triggers the joint-landlord
--     negative-duration edge case
--   * has a random time to first property spread across MINUTES, HOURS and DAYS,
--     so the Metrics duration formatting (which switches unit at 1 hour / 1 day)
--     is exercised by all three branches
--
-- The values are randomised by random(), so a FRESHLY created database gets a
-- different cohort each time. Because `mode: always` re-runs the script on every
-- startup, the property set is FROZEN after the first load via a NOT EXISTS guard
-- and all rows use fixed ids + ON CONFLICT DO NOTHING, so restarts neither change
-- nor grow the cohort.
--
-- Ids live in an isolated high range: landlords 9_100_xxx, properties 9_110_xxx.
-- Addresses and registration_numbers are created for ALL 600 candidate property
-- slots up front (a few are unused) so that the property and landlordship inserts
-- never hit a missing foreign key regardless of which slots are randomly kept.
--
-- HOW TO VERIFY (random, so query the DB and compare with the Metrics page for a
-- matching range, e.g. From 1/1/2028 To 31/12/2029):
--
--   WITH firsts AS (
--       SELECT l.id,
--              EXTRACT(EPOCH FROM (MIN(po.created_date) - l.created_date)) AS secs
--       FROM landlord l
--       JOIN landlordship_members lm ON lm.landlord_id = l.id
--       JOIN property_ownership po ON po.id = lm.landlordship_id
--       WHERE l.subject_identifier LIKE 'metrics-rand-user-%'
--       GROUP BY l.id, l.created_date)
--   SELECT count(*)                                                  AS landlords_with_a_property,
--          percentile_cont(0.5)  WITHIN GROUP (ORDER BY secs) / 86400 AS median_days,
--          percentile_cont(0.9)  WITHIN GROUP (ORDER BY secs) / 86400 AS p90_days,
--          percentile_cont(0.95) WITHIN GROUP (ORDER BY secs) / 86400 AS p95_days
--   FROM firsts;
--
-- (percentile_cont uses the same rank = fraction * (n - 1) linear interpolation
-- as the Metrics service, so its result matches the dashboard exactly.)
-- =============================================================================
INSERT INTO prsdb_user (id, created_date)
SELECT 'metrics-rand-user-' || s, TIMESTAMPTZ '2028-01-01 00:00:00+00'
FROM generate_series(1, 150) AS g(s)
ON CONFLICT DO NOTHING;

INSERT INTO address (id, created_date, single_line_address, local_council_id, postcode, building_number)
SELECT 9100000 + s, TIMESTAMPTZ '2028-01-01 00:00:00+00',
       s || ' Random Landlord Way, RD1 1AA', NULL::integer, 'RD1 1AA', s || ''
FROM generate_series(1, 150) AS g(s)
UNION ALL
SELECT 9110000 + ((s - 1) * 4 + k), TIMESTAMPTZ '2028-01-01 00:00:00+00',
       s || '/' || k || ' Random Property Road, RD2 2BB', NULL::integer, 'RD2 2BB', s || ''
FROM generate_series(1, 150) AS g(s) CROSS JOIN generate_series(1, 4) AS h(k)
ON CONFLICT DO NOTHING;

INSERT INTO registration_number (id, created_date, number, type)
SELECT 9100000 + s, TIMESTAMPTZ '2028-01-01 00:00:00+00', 900001000000 + s, 1 -- landlord
FROM generate_series(1, 150) AS g(s)
UNION ALL
SELECT 9110000 + ((s - 1) * 4 + k), TIMESTAMPTZ '2028-01-01 00:00:00+00',
       900001100000 + ((s - 1) * 4 + k), 0 -- property
FROM generate_series(1, 150) AS g(s) CROSS JOIN generate_series(1, 4) AS h(k)
ON CONFLICT DO NOTHING;

INSERT INTO landlord (id, created_date, last_modified_date, registration_number_id, address_id, date_of_birth,
                      is_active, phone_number, subject_identifier, name, email, country_of_residence, is_verified,
                      has_accepted_privacy_notice)
SELECT 9100000 + s,
       TIMESTAMPTZ '2028-01-01 00:00:00+00'
           + (random() * (TIMESTAMPTZ '2030-01-01 00:00:00+00' - TIMESTAMPTZ '2028-01-01 00:00:00+00')),
       NULL, 9100000 + s, 9100000 + s, DATE '1985-06-15', true, '07222222222',
       'metrics-rand-user-' || s, 'Random Test Landlord ' || s, 'metrics.random.' || s || '@example.com',
       'England or Wales', random() < 0.6, true
FROM generate_series(1, 150) AS g(s)
ON CONFLICT DO NOTHING;

INSERT INTO property_ownership (id, is_active, ownership_type, current_num_households, current_num_tenants,
                               registration_number_id, address_id, created_date, last_modified_date, license_id,
                               property_build_type, num_bedrooms, marked_joint_landlord)
SELECT 9110000 + base.g, true,
       (random() * 2.999)::int, (random() * 3.999)::int, (random() * 5.999)::int,
       9110000 + base.g, 9110000 + base.g, base.created, base.created, NULL,
       1 + (random() * 2.999)::int, 1 + (random() * 4.999)::int, false
FROM (
    -- MATERIALIZED so the volatile random() expressions are evaluated exactly once
    -- per (landlord, slot) row; otherwise the planner can collapse them to a single
    -- constant and every landlord ends up with an identical time to first property.
    WITH candidates AS MATERIALIZED (
        SELECT
            (s - 1) * 4 + k AS g,
            l.created_date
                + CASE
                      WHEN k > 1 THEN make_interval(days => 30 * (k - 1) + (random() * 60)::int)
                      WHEN random() < 0.10 THEN make_interval(mins => 1 + (random() * 58)::int)
                      WHEN random() < 0.17 THEN make_interval(hours => 1 + (random() * 22)::int)
                      ELSE make_interval(days => 1 + (random() * 299)::int)
                               + make_interval(secs => (random() * 86399)::int)
                  END AS created,
            CASE k
                WHEN 1 THEN random() < 0.85
                WHEN 2 THEN random() < 0.35
                WHEN 3 THEN random() < 0.12
                ELSE random() < 0.04
            END AS keep
        FROM generate_series(1, 150) AS g0(s)
        CROSS JOIN generate_series(1, 4) AS h(k)
        JOIN landlord l ON l.id = 9100000 + s
    )
    SELECT g, created
    FROM candidates
    WHERE keep
      AND created < TIMESTAMPTZ '2030-01-01 00:00:00+00'
) base
WHERE NOT EXISTS (SELECT 1 FROM property_ownership WHERE id BETWEEN 9110001 AND 9110600)
ON CONFLICT DO NOTHING;

INSERT INTO landlordship_members (landlord_id, landlordship_id)
SELECT 9100000 + ((po.id - 9110000 + 3) / 4), po.id
FROM property_ownership po
WHERE po.id BETWEEN 9110001 AND 9110600
ON CONFLICT DO NOTHING;
