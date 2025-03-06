
ALTER TABLE users ADD COLUMN locked_at TIMESTAMP DEFAULT null;
ALTER TABLE users ADD COLUMN failed_login_attempts_amount smallint default 0;
ALTER TABLE users ADD COLUMN last_failed_login_attempt_date TIMESTAMP;

comment on column users.locked_at is 'If this field is different from null , it indicates the login functionality has been blocked for the user due to a number of failed login attempts';
