CREATE TABLE passcode
(
    passcode                 VARCHAR(255),
    created_date       TIMESTAMPTZ(6),
    last_modified_date TIMESTAMPTZ(6),
    local_authority_id INT NOT NULL,
    subject_identifier VARCHAR(255) UNIQUE,
    PRIMARY KEY (passcode)
);

ALTER TABLE IF EXISTS passcode
    ADD CONSTRAINT FK_PASSCODE_LA FOREIGN KEY (local_authority_id) REFERENCES local_authority;
ALTER TABLE IF EXISTS passcode
    ADD CONSTRAINT FK_PASSCODE_1L_USER FOREIGN KEY (subject_identifier) REFERENCES one_login_user;
