CREATE TABLE calendar_dates (
    generated_id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (generated_id)
)

CREATE TABLE calendars (
    generated_id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (generated_id)
)

CREATE TABLE frequencies (
    generated_id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (generated_id)
)

CREATE TABLE routes (
    generated_id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (generated_id)
)

CREATE TABLE stop_times (
    generated_id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (generated_id)
)

CREATE TABLE stops (
    generated_id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (generated_id)
)

CREATE TABLE trips (
    generated_id BIGINT NOT NULL AUTO_INCREMENT,
    PRIMARY KEY (generated_id)
)