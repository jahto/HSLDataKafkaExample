CREATE TABLE calendar_dates (
    service_id VARCHAR,
    exception_date DATE,
    exception_type TINYINT NOT NULL,
    -- The real primary key would be only service_id and exception_date
    -- but it's probably faster to add exception_type too in it, so there's
    -- not any need even to go to the table to fetch that one missing column.
    -- Could lead to duplicate entries if the incoming data does not behave
    -- according to the specs... Perhaps add an otherwise quite useless unique
    -- index to guard against that possibility.
    PRIMARY KEY (service_id, exception_date, exception_type)
);

CREATE TABLE calendars (
    service_id VARCHAR,
    valid_from DATE NOT NULL,
    valid_until DATE NOT NULL,
    monday TINYINT NOT NULL,
    tuesday TINYINT NOT NULL,
    wednesday TINYINT NOT NULL,
    thursday TINYINT NOT NULL,
    friday TINYINT NOT NULL,
    saturday TINYINT NOT NULL,
    sunday TINYINT NOT NULL,
    PRIMARY KEY (service_id)
);

CREATE TABLE frequencies (
    trip_id VARCHAR,
    start_time INTEGER,
    end_time INTEGER NOT NULL,
    headway_secs SMALLINT NOT NULL,
    exact_times TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (trip_id, start_time)
);

CREATE TABLE routes (
    route_id VARCHAR,
    short_name VARCHAR NOT NULL,
    long_name VARCHAR NOT NULL,
    route_type SMALLINT NOT NULL,
    description VARCHAR,
    url VARCHAR,
    PRIMARY KEY (route_id)
);

CREATE TABLE stop_times (
    trip_id VARCHAR,
    stop_id VARCHAR,
    arrival INTEGER,
    stop_sequence INTEGER,
    departure INTEGER NOT NULL,
    pickup_type TINYINT NOT NULL DEFAULT 0,
    dropoff_type TINYINT NOT NULL DEFAULT 0,
    timepoint TINYINT NOT NULL DEFAULT 1,
    headsign VARCHAR,
    dist_traveled REAL,
    PRIMARY KEY (trip_id, stop_id, arrival, stop_sequence)
);

CREATE TABLE stops (
    stop_id VARCHAR,
    stop_name VARCHAR NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    location_type TINYINT NOT NULL DEFAULT 0,
    wheelchair_boarding TINYINT NOT NULL DEFAULT 0,
    stop_code VARCHAR,
    stop_description VARCHAR,
    url VARCHAR,
    parent_station VARCHAR,
    PRIMARY KEY (stop_id)
);

CREATE TABLE trips (
    trip_id VARCHAR,
    route_id VARCHAR NOT NULL,
    service_id VARCHAR NOT NULL,
    direction TINYINT NOT NULL, -- Nullable in GTFS specs!
    bikes_allowed TINYINT NOT NULL DEFAULT 0,
    wheelchair_accessible TINYINT NOT NULL DEFAULT 0,
    headsign VARCHAR,
    short_name VARCHAR,
    block_id VARCHAR,
    shape_id VARCHAR,
    PRIMARY KEY (trip_id)
);