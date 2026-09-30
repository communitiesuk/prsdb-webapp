INSERT INTO prsdb_user (id, created_date)
VALUES ('test-base-user-id', current_timestamp);

INSERT INTO reminder_email_sent (id, last_reminder_email_sent_date)
VALUES (1, current_timestamp);

INSERT INTO saved_journey_state (
    id,
    created_date,
    journey_id,
    serialized_state,
    subject_identifier,
    reminder_email_sent_id
)
VALUES (
    1,
    current_timestamp - INTERVAL '29 days',
    'old-incomplete-property',
    '{}',
    'test-base-user-id',
    1
);

INSERT INTO landlord_incomplete_properties (user_id, saved_journey_state_id)
VALUES ('test-base-user-id', 1);
