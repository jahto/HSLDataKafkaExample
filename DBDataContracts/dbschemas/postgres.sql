CREATE TABLE calendar_dates (
    generated_id BIGSERIAL NOT NULL,
    PRIMARY KEY (generated_id)
)

CREATE TABLE calendars (
    generated_id BIGSERIAL NOT NULL,
    PRIMARY KEY (generated_id)
)

CREATE TABLE frequencies (
    generated_id BIGSERIAL NOT NULL,
    PRIMARY KEY (generated_id)
)

CREATE TABLE routes (
    generated_id BIGSERIAL NOT NULL,
    PRIMARY KEY (generated_id)
)

CREATE TABLE stop_times (
    generated_id BIGSERIAL NOT NULL,
    PRIMARY KEY (generated_id)
)

CREATE TABLE stops (
    generated_id BIGSERIAL NOT NULL,
    PRIMARY KEY (generated_id)
)

CREATE TABLE trips (
    generated_id BIGSERIAL NOT NULL,
    PRIMARY KEY (generated_id)
)