CREATE TABLE calendar_dates (
    service_num BIGINT NOT NULL,
    exception_date DATE NOT NULL,
    exception_type SMALLINT NOT NULL,
    PRIMARY KEY (service_num, exception_date)
);

CREATE TABLE calendars (
    service_num BIGSERIAL,
    service_id VARCHAR(320) NOT NULL,
    valid_from DATE NOT NULL,
    valid_until DATE NOT NULL,
    monday BOOLEAN NOT NULL,
    tuesday BOOLEAN NOT NULL,
    wednesday BOOLEAN NOT NULL,
    thursday BOOLEAN NOT NULL,
    friday BOOLEAN NOT NULL,
    saturday BOOLEAN NOT NULL,
    sunday BOOLEAN NOT NULL,
    PRIMARY KEY (service_num)
);

CREATE TABLE frequencies (
    trip_num BIGINT NOT NULL,
    start_time INTEGER NOT NULL,
    end_time INTEGER NOT NULL,
    headway_secs SMALLINT NOT NULL,
    exact_times SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY (trip_num, start_time)
);

CREATE TABLE routes (
    route_num BIGSERIAL,
    route_id VARCHAR(64) NOT NULL,
    short_name VARCHAR(96) NOT NULL,
    long_name VARCHAR(224) NOT NULL,
    route_type VARCHAR(64) NOT NULL,
    description VARCHAR(160),
    url VARCHAR(96),
    PRIMARY KEY (route_num)
);

CREATE TABLE stop_times (
    trip_num BIGINT NOT NULL,
    stop_num BIGINT NOT NULL,
    arrival INTEGER NOT NULL,
    stop_sequence INTEGER NOT NULL,
    departure INTEGER NOT NULL,
    pickup_type SMALLINT NOT NULL DEFAULT 0,
    dropoff_type SMALLINT NOT NULL DEFAULT 0,
    timepoint SMALLINT NOT NULL DEFAULT 1,
    headsign VARCHAR(64),
    dist_traveled REAL,
    PRIMARY KEY (trip_num, arrival, stop_sequence)
);

CREATE TABLE stops (
    stop_num BIGSERIAL,
    stop_id VARCHAR(32) NOT NULL,
    stop_name VARCHAR(160) NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    location_type SMALLINT NOT NULL DEFAULT 0,
    wheelchair_boarding SMALLINT NOT NULL DEFAULT 0,
    stop_code VARCHAR(32),
    stop_description VARCHAR(64),
    url VARCHAR(96),
    parent_station VARCHAR(32),
    platform_code VARCHAR(64),
    PRIMARY KEY (stop_num)
);

CREATE TABLE trips (
    trip_num BIGSERIAL,
    trip_id VARCHAR(96) NOT NULL,
    route_num BIGINT NOT NULL,
    service_num BIGINT NOT NULL,
    direction SMALLINT NOT NULL, -- Nullable in GTFS specs!
    start_time INTEGER NOT NULL,
    bikes_allowed SMALLINT NOT NULL DEFAULT 0,
    wheelchair_accessible SMALLINT NOT NULL DEFAULT 0,
    headsign VARCHAR(96),
    block_id VARCHAR(64),
    shape_id VARCHAR(64),
    short_name VARCHAR(96),
    PRIMARY KEY (trip_num)
);

CREATE INDEX calendar_dates_exception_date_exception_type_idx ON calendar_dates (exception_date, exception_type);

CREATE UNIQUE INDEX calendars_service_id_inc_num_idx ON calendars (service_id) INCLUDE (service_num);
-- CREATE INDEX calendars_service_id_num_idx ON calendars (service_id, service_num);

CREATE INDEX calendars_valid_from_idx ON calendars (valid_from);
CREATE INDEX calendars_valid_until_idx ON calendars (valid_until);

CREATE UNIQUE INDEX routes_route_id_inc_num_idx ON routes (route_id) INCLUDE (route_num);
-- CREATE INDEX routes_route_id_num_idx ON routes (route_id, route_num);

CREATE INDEX stop_times_arrival_idx ON stop_times (arrival);
CREATE INDEX stop_times_stop_num_idx ON stop_times (stop_num);
CREATE INDEX stop_times_stop_sequence_idx ON stop_times (stop_sequence);
CREATE INDEX stop_times_trip_num_idx ON stop_times (trip_num);

CREATE UNIQUE INDEX stops_stop_id_inc_num_idx ON stops (stop_id) INCLUDE (stop_num);
-- CREATE INDEX stops_stop_id_num_idx ON stops (stop_id, stop_num);

CREATE INDEX trips_block_id_idx ON trips (block_id);
CREATE INDEX trips_route_num_idx ON trips (route_num);
CREATE INDEX trips_service_num_idx ON trips (service_num);
CREATE INDEX trips_start_time_idx ON trips (start_time);

CREATE UNIQUE INDEX trips_trip_id_inc_num_idx ON trips (trip_id) INCLUDE (trip_num);
-- CREATE INDEX trips_trip_id_num_idx ON trips (trip_id, trip_num);

ALTER TABLE calendar_dates ADD CONSTRAINT calendar_dates_service_num_fkey
    FOREIGN KEY (service_num) REFERENCES calendars(service_num) ON DELETE CASCADE;

ALTER TABLE stop_times ADD CONSTRAINT stop_times_stop_num_fkey
    FOREIGN KEY (stop_num) REFERENCES stops(stop_num) ON DELETE CASCADE;
ALTER TABLE stop_times ADD CONSTRAINT stop_times_trip_num_fkey
    FOREIGN KEY (trip_num) REFERENCES trips(trip_num) ON DELETE CASCADE;

ALTER TABLE trips ADD CONSTRAINT trips_route_num_fkey
    FOREIGN KEY (route_num) REFERENCES routes(route_num) ON DELETE CASCADE;
ALTER TABLE trips ADD CONSTRAINT trips_service_num_fkey
    FOREIGN KEY (service_num) REFERENCES calendars(service_num) ON DELETE CASCADE;

ALTER TABLE frequencies ADD CONSTRAINT frequencies_trip_num_fkey
    FOREIGN KEY (trip_num) REFERENCES trips(trip_num) ON DELETE CASCADE;
