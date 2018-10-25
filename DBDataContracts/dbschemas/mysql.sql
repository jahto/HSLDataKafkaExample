CREATE TABLE calendar_dates (
    service_id VARCHAR(320),
    exception_date DATE,
    exception_type TINYINT NOT NULL,
    PRIMARY KEY (service_id, exception_date)
);

CREATE TABLE calendars (
    service_id VARCHAR(320),
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
    trip_id VARCHAR(128),
    start_time INTEGER,
    end_time INTEGER NOT NULL,
    headway_secs SMALLINT NOT NULL,
    exact_times TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (trip_id, start_time)
);

CREATE TABLE routes (
    route_id VARCHAR(64),
    short_name VARCHAR(64) NOT NULL,
    long_name VARCHAR(256) NOT NULL,
    route_type SMALLINT NOT NULL,
    description VARCHAR(192),
    url VARCHAR(128),
    PRIMARY KEY (route_id)
);

CREATE TABLE stop_times (
    trip_id VARCHAR(128),
    stop_id VARCHAR(64),
    arrival INTEGER,
    stop_sequence INTEGER,
    departure INTEGER NOT NULL,
    pickup_type TINYINT NOT NULL DEFAULT 0,
    dropoff_type TINYINT NOT NULL DEFAULT 0,
    timepoint TINYINT NOT NULL DEFAULT 1,
    headsign VARCHAR(128), -- !!! Not yet checked!
    dist_traveled REAL,
    PRIMARY KEY (trip_id, stop_id, arrival)
);

CREATE TABLE stops (
    stop_id VARCHAR(64),
    stop_name VARCHAR(192) NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    location_type TINYINT NOT NULL DEFAULT 0,
    wheelchair_boarding TINYINT NOT NULL DEFAULT 0,
    stop_code VARCHAR(64),
    stop_description VARCHAR(64),
    url VARCHAR(128),
    parent_station VARCHAR(64),
    PRIMARY KEY (stop_id)
);

CREATE TABLE trips (
    trip_id VARCHAR(128),
    route_id VARCHAR(64) NOT NULL,
    service_id VARCHAR(320) NOT NULL,
    direction TINYINT NOT NULL, -- Nullable in GTFS specs!
    bikes_allowed TINYINT NOT NULL DEFAULT 0,
    wheelchair_accessible TINYINT NOT NULL DEFAULT 0,
    headsign VARCHAR(128),
    short_name VARCHAR(128), -- !!! Not yet checked!
    block_id VARCHAR(128), -- !!! Not yet checked!
    shape_id VARCHAR(64),
    PRIMARY KEY (trip_id)
);