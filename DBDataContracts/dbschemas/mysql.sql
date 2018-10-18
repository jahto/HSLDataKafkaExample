CREATE TABLE calendar_dates (
    service_id VARCHAR,
    PRIMARY KEY (service_id)
)

CREATE TABLE calendars (
    service_id VARCHAR,
    PRIMARY KEY (service_id)
)

CREATE TABLE frequencies (
    trip_id VARCHAR
    PRIMARY KEY (trip_id)
)

CREATE TABLE routes (
    route_id VARCHAR,
    PRIMARY KEY (route_id)
)

CREATE TABLE stop_times (
    trip_id VARCHAR,
    stop_id VARCHAR,
    arrival INTEGER,
    stop_sequence INTEGER
    PRIMARY KEY (trip_id, stop_id, arrival, stop_sequence)
)

CREATE TABLE stops (
    stop_id VARCHAR,
    PRIMARY KEY (stop_id)
)

CREATE TABLE trips (
    trip_id VARCHAR,
    PRIMARY KEY (trip_id)
)