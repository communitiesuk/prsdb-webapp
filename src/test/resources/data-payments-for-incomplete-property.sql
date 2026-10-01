INSERT INTO payment (payment_id, created_date, amount_in_pence, reference, payment_created_at, for_period_ending, status,
                     paying_user_id, associated_property_id, associated_incomplete_property_user_id,
                     associated_incomplete_property_saved_journey_state_id)
VALUES ('created-payment', current_date, 1000, 'reference-1', current_date, current_date, 0, 'urn:fdc:gov.uk:2022:UVWXY', null, 'urn:fdc:gov.uk:2022:UVWXY', 1),
       ('capturable-payment', current_date, 1000, 'reference-2', current_date, current_date, 1, 'urn:fdc:gov.uk:2022:UVWXY', null, 'urn:fdc:gov.uk:2022:UVWXY', 1),
       ('succeeded-payment', current_date, 1000, 'reference-3', current_date, current_date, 2, 'urn:fdc:gov.uk:2022:UVWXY', 1, null, null),
       ('failed-payment', current_date, 1000, 'reference-4', current_date, current_date, 3, 'urn:fdc:gov.uk:2022:UVWXY', null, 'urn:fdc:gov.uk:2022:UVWXY', 1),
       ('cancelled-payment', current_date, 1000, 'reference-5', current_date, current_date, 4, 'urn:fdc:gov.uk:2022:UVWXY', null, 'urn:fdc:gov.uk:2022:UVWXY', 1);
