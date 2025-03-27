INSERT INTO one_login_user (id, created_date)
VALUES ('urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk', '2025-02-19 12:01:07.575927+00'),
       ('urn:fdc:gov.uk:2022:DySqeEXIC4G2xauOirtTDcezwCPLZgQPUQZmQ-aIIMk', '2025-02-26 17:02:19.625996+00'),
       ('urn:fdc:gov.uk:2022:e4eN4Mujv2NMVut4mIPzRlwpyBL3sBE304GgGlxgd-Q', '2025-02-27 14:13:11.876928+00'),
       ('urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU','2025-03-06 15:32:59.529898+00'),
       ('urn:fdc:gov.uk:2022:RR3fauA1ZgHYuLjW9824VtLzegGG9NXfdHJrtg2hIAE','2025-03-11 11:28:52.837278+00'),
       ('urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI','2025-03-12 17:12:19.833105+00'),
       ('urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU','2025-03-17 10:13:36.388805+00'),
       ('urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo','2024-10-15 00:00:00+00'),
       ('urn:fdc:gov.uk:2022:ea8XwChQkjezm4MgGJIzI_HRm7l8IPPTIMT705UQXjI','2025-02-27 13:56:15.745135+00'),
       ('urn:fdc:gov.uk:2022:kob7zYIuzdrUxKTYq7160l_6Tj2ScXTPJ876jZVvAFA','2025-02-27 13:58:02.81462+00'),
       ('urn:fdc:gov.uk:2022:sgO5-g7fThIp2MhXMcvFo5N6ObnstGFVNSYFkghMd24','2025-03-06 08:22:41.002251+00'),
       ('urn:fdc:gov.uk:2022:La9gwI6zvuzT3yvKjsKEH2cDbtL88wNbiqAeXQ0plEM','2025-03-06 10:33:22.395944+00'),
       ('urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s','2025-03-18 10:13:36.388805+00');

SELECT setval(pg_get_serial_sequence('local_authority', 'id'), (SELECT MAX(id) FROM local_authority));

INSERT INTO local_authority_user (id, created_date, last_modified_date, subject_identifier, is_manager, local_authority_id, email, name)
VALUES (1,'2025-02-19 12:01:07.575927+00',null,'urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk',false,1,'travis.woodward@communities.gov.uk','Travis Woodward'),
       (2,'2025-02-26 17:02:19.625996+00',null,'urn:fdc:gov.uk:2022:DySqeEXIC4G2xauOirtTDcezwCPLZgQPUQZmQ-aIIMk',false,1,'travis.woodward@softwire.com','Travis Woodward (Softwire)'),
       (3,'2025-02-27 14:13:11.876928+00',null,'urn:fdc:gov.uk:2022:e4eN4Mujv2NMVut4mIPzRlwpyBL3sBE304GgGlxgd-Q',false,1,'simon.greensmith@communities.gov.uk','Simon Greensmith'),
       (4,'2025-03-06 15:32:59.529898+00',null,'urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU',false,1,'alexander.read@softwire.com','Alexander Read'),
       (5,'2025-03-11 11:28:52.837278+00','2025-03-11 11:31:28.061279+00','urn:fdc:gov.uk:2022:RR3fauA1ZgHYuLjW9824VtLzegGG9NXfdHJrtg2hIAE',true,1,'niamh.rafferty@communities.gov.uk','Niamh Rafferty'),
       (6,'2025-03-18 10:13:36.388805+00',null,'urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s', true, 1, 'isobel.ibironke@softwire.com','Isobel Ibironke'),
       (7,'2025-03-12 17:12:19.833105+00','2025-03-12 17:13:10.020624+00','urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI',true,1,'jasmin.conterio@softwire.com','Jasmin Conterio'),
       (8,'2025-03-17 10:13:36.388805+00',null,'urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU',false,1,'kiran.randhawakukar@softwire.com','Kiran Fake Name');

SELECT setval(pg_get_serial_sequence('local_authority_user', 'id'), (SELECT MAX(id) FROM local_authority_user));

INSERT INTO registration_number (id, created_date, number, type)
VALUES (1,'2024-10-15 00:00:00+00',2001001001,1),
       (2,'2024-10-15 00:00:00+00',3002001002,1),
       (3,'2025-02-19 08:23:57.267183+00',127959730689,1),
       (4,'2025-02-19 13:41:13.782443+00',116136809177,1),
       (5,'2025-02-19 13:59:18.561124+00',6136283775,1),
       (6,'2025-02-20 11:50:45.723696+00',105757165800,1),
       (7,'2025-02-24 09:29:52.993571+00',116726635893,1),
       (8,'2025-02-24 10:01:14.5196+00',61597584540,1),
       (9,'2025-02-27 13:54:35.212499+00',52836094838,1),
       (10,'2025-02-27 13:55:19.931954+00',49873217784,1),
       (11,'2025-02-27 13:56:15.743199+00',80551586002,1),
       (12,'2025-02-27 13:58:02.810321+00',72697323406,1),
       (13,'2025-02-27 13:58:02.810321+00',54697323416,1);

SELECT setval(pg_get_serial_sequence('registration_number', 'id'), (SELECT MAX(id) FROM registration_number));

INSERT INTO address (id, created_date, last_modified_date, uprn, single_line_address, local_authority_id)
VALUES (1, '10/15/24', '10/15/24', 2, '1 Fictional Road', 1);

SELECT setval(pg_get_serial_sequence('address', 'id'), (SELECT MAX(id) FROM address));

INSERT INTO landlord (id, registration_number_id, address_id, created_date, email, non_england_or_wales_address, is_active, last_modified_date, name, phone_number, subject_identifier, date_of_birth, country_of_residence, is_verified)
VALUES(1,1,1,'2024-10-15 00:00:00+00','Team-PRSDB+landlord@softwire.com',null,true,'2025-02-25 16:17:18.075473+00','PRSD Landlord','+447123456789','urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo','1950-05-13','England or Wales',false),
      (2,2,1,'2025-02-19 08:23:57.279777+00','travis.woodward@communities.gov.uk',null,true,null,'LISA S C LOOSELEY','07777777777','urn:fdc:gov.uk:2022:_RNZomOzEjxF4o2NzxWskS062b7hTVWLFI8TYsmoWAk','1973-03-14','England or Wales',false),
      (3,3,1,'2025-02-19 13:41:13.861504+00','alexander.read@softwire.com',null,true,'2025-03-11 13:38:00.36893+00','KENNETH DECERQUEIRA','07777777777','urn:fdc:gov.uk:2022:A9B5GpzhlOrNoGQM65oUESHL5i3O9fp0wjizEFVcCrU','1965-07-08','England or Wales',false),
      (4,4,1,'2025-02-20 11:50:45.745273+00','kiran.randhawakukar@softwire.com',null,true,'2025-03-06 14:01:33.486684+00','Not Kiran','01234567890','urn:fdc:gov.uk:2022:ListhqO1Hu6G90tyF_Rozj4F0YkLHreBnCQZ3JQSiEU','1965-07-08','England or Wales',false),
      (5,5,1,'2025-02-24 09:29:53.079945+00','jasmin.conterio@softwire.com',null,true,'2025-02-27 17:19:52.061638+00','Jasmin Conterio','01223 123 456','urn:fdc:gov.uk:2022:07lXHJeQwE0k5PZO7w_PQF425vT8T7e63MrvyPYNSoI','1989-02-02','England or Wales',false),
      (6,6,1,'2025-02-27 13:55:19.93409+00','simon.greensmith@communities.gov.uk',null,true,'2025-02-27 14:35:38.905229+00','bob','+447944323732','urn:fdc:gov.uk:2022:e4eN4Mujv2NMVut4mIPzRlwpyBL3sBE304GgGlxgd-Q','1973-03-14','England or Wales',true),
      (7,7,1,'2025-02-27 13:56:15.745135+00','geetika.kejriwal@communities.gov.uk',null,true,'2025-02-27 14:34:33.323661+00','LISA S C LOOSELEY','+447765617009','urn:fdc:gov.uk:2022:ea8XwChQkjezm4MgGJIzI_HRm7l8IPPTIMT705UQXjI','1973-03-14','England or Wales',true),
      (8,8,1,'2025-02-27 13:58:02.81462+00','isobel.ibironke@softwire.com',null,true,null,'Isobel Ibironke','07123456789','urn:fdc:gov.uk:2022:mwfvbb5GgiDh0acjz9EDDQ7zwskWZzUSnWfavL70f6s','1995-08-4','England or Wales',false),
      (9,9,1,'2025-02-27 13:58:02.81462+00','catherine.graham2@communities.gov.uk',null,true,null,'LISA S C LOOSELEY','+447432569695','urn:fdc:gov.uk:2022:kob7zYIuzdrUxKTYq7160l_6Tj2ScXTPJ876jZVvAFA','1973-03-14','England or Wales',true),
      (10,10,1,'2025-02-27 14:13:54.057882+00','niamh.rafferty@communities.gov.uk','Blah blah',true,null,'LISA S C LOOSELEY','07552039585','urn:fdc:gov.uk:2022:RR3fauA1ZgHYuLjW9824VtLzegGG9NXfdHJrtg2hIAE','1973-03-14','Barbados',true),
      (11,11,1,'2025-03-06 08:22:41.002251+00','Team-PRSDB+Unverified@softwire.com',null,true,'2025-03-11 13:47:42.800533+00','Unverified Landlord','07777777777','urn:fdc:gov.uk:2022:sgO5-g7fThIp2MhXMcvFo5N6ObnstGFVNSYFkghMd24','1996-03-03','England or Wales',false),
      (12,12,1,'2025-03-06 10:33:22.395944+00','team-prsdb+verified@softwire.com',null,true,null,'KENNETH DECERQUEIRA','07777777777','urn:fdc:gov.uk:2022:La9gwI6zvuzT3yvKjsKEH2cDbtL88wNbiqAeXQ0plEM','1965-07-08','England or Wales',true);

SELECT setval(pg_get_serial_sequence('landlord', 'id'), (SELECT MAX(id) FROM landlord));

INSERT INTO property (id, status, is_active, property_build_type, address_id)
VALUES (1, 1, true, 1, 1);

SELECT setval(pg_get_serial_sequence('property', 'id'), (SELECT MAX(id) FROM property));

INSERT INTO property_ownership (id, is_active, occupancy_type, ownership_type, current_num_households, current_num_tenants, registration_number_id, primary_landlord_id, property_id)
VALUES (1, true, 0, 1, 1, 2, 12, 1, 1);

SELECT setval(pg_get_serial_sequence('property_ownership', 'id'), (SELECT MAX(id) FROM property_ownership));