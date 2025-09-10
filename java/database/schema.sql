BEGIN TRANSACTION;

-- Drop All Tables in Safe Order
DROP TABLE IF EXISTS reservations, notifications, sessions, parking_spots, lots, vehicle_model, vehicle_make, user_vehicle, users, valet_invite CASCADE;

-- Users Table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    role VARCHAR(50) NOT NULL,
    full_name VARCHAR(150),
    phone_number VARCHAR(32),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Vehicle Make & Model
CREATE TABLE vehicle_make (
    id SERIAL PRIMARY KEY,
    nhtsa_make_id INT UNIQUE NOT NULL,
    make_name VARCHAR(255) NOT NULL
);

CREATE TABLE vehicle_model (
    id SERIAL PRIMARY KEY,
    nhtsa_make_id INT NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (nhtsa_make_id) REFERENCES vehicle_make(nhtsa_make_id)
);

CREATE TABLE user_vehicle (
    id SERIAL PRIMARY KEY,
    user_id INT NULL REFERENCES users(user_id) ON DELETE SET NULL,
    make_name VARCHAR(255) NOT NULL,
    model_name VARCHAR(255) NOT NULL,
    color VARCHAR(50),
    license_plate VARCHAR(20) NOT NULL,
    vin CHAR(17),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_user_vehicle UNIQUE (license_plate, vin)
);

-- Enforce at most one current vehicle per user
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_vehicle_current_true
    ON user_vehicle(user_id)
    WHERE is_current = TRUE;

-- Lots table
CREATE TABLE lots (
    id SERIAL PRIMARY KEY,
    lot_name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Parking Spots
CREATE TABLE parking_spots (
    id SERIAL PRIMARY KEY,
    spot_number INT NOT NULL,
    lot_id INT NOT NULL REFERENCES lots(id) ON DELETE CASCADE,
    is_available BOOLEAN DEFAULT TRUE,
    is_handicapped BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Enforce uniqueness per lot
CREATE UNIQUE INDEX IF NOT EXISTS ux_parking_spots_lot_spot
    ON parking_spots(lot_id, spot_number);

-- Index for lot-level lookups
CREATE INDEX IF NOT EXISTS idx_parking_spots_lot
    ON parking_spots (lot_id);

-- Seed: One lot
INSERT INTO lots (lot_name, location)
VALUES ('Main Lot', 'Default Location');

-- Seed: 10 Available Parking Spots, all in Main Lot (id = 1)
INSERT INTO parking_spots (spot_number, lot_id, is_available)
SELECT gs, 1, TRUE
FROM generate_series(1, 10) AS gs;

-- Parking Sessions
CREATE TABLE sessions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id),
    spot_id INT NOT NULL REFERENCES parking_spots(id),
    check_in_time TIMESTAMPTZ NOT NULL DEFAULT now(), -- Added default
    check_out_time TIMESTAMPTZ,
    total_charged DECIMAL(10,2),
    created_at TIMESTAMPTZ DEFAULT now(),
    vehicle_id INT NULL REFERENCES user_vehicle(id)
);

-- INDEX: Ensure only one active session per spot at a time
CREATE UNIQUE INDEX IF NOT EXISTS ux_sessions_spot_active
    ON sessions(spot_id)
    WHERE check_out_time IS NULL;

-- INDEX: Ensure only one active session per vehicle
CREATE UNIQUE INDEX IF NOT EXISTS ux_sessions_vehicle_active
    ON sessions(vehicle_id)
    WHERE check_out_time IS NULL;

-- INDEX: Ensure only one active session per user
CREATE UNIQUE INDEX IF NOT EXISTS ux_sessions_user_active
    ON sessions(user_id)
    WHERE check_out_time IS NULL;

-- INDEX: Speed up active session lookups
CREATE INDEX IF NOT EXISTS idx_sessions_active
    ON sessions(vehicle_id, spot_id)
    WHERE check_out_time IS NULL;

-- Trigger + function to auto-update spot availability
CREATE OR REPLACE FUNCTION sync_spot_availability() RETURNS trigger AS $$
DECLARE
    v_spot_id int;
BEGIN
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        v_spot_id := NEW.spot_id;
    ELSE
        v_spot_id := OLD.spot_id;
    END IF;

    UPDATE parking_spots ps
    SET is_available = NOT EXISTS (
        SELECT 1 FROM sessions s
        WHERE s.spot_id = v_spot_id AND s.check_out_time IS NULL
    )
    WHERE ps.id = v_spot_id;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_sessions_sync_spot_availability_ins ON sessions;
DROP TRIGGER IF EXISTS trg_sessions_sync_spot_availability_upd ON sessions;
DROP TRIGGER IF EXISTS trg_sessions_sync_spot_availability_del ON sessions;

CREATE TRIGGER trg_sessions_sync_spot_availability_ins
AFTER INSERT ON sessions
FOR EACH ROW EXECUTE FUNCTION sync_spot_availability();

CREATE TRIGGER trg_sessions_sync_spot_availability_upd
AFTER UPDATE OF check_out_time, spot_id ON sessions
FOR EACH ROW EXECUTE FUNCTION sync_spot_availability();

CREATE TRIGGER trg_sessions_sync_spot_availability_del
AFTER DELETE ON sessions
FOR EACH ROW EXECUTE FUNCTION sync_spot_availability();

-- Notifications
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    session_id INT NOT NULL REFERENCES sessions(id),
    customer_id INT NOT NULL REFERENCES users(user_id),
    requested_time TIMESTAMPTZ NOT NULL,
    picked_up_time TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL,
	notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Constraint for preventing multiple open requests
CREATE UNIQUE INDEX IF NOT EXISTS uniq_notifications_requested_open
	ON notifications(session_id)
	WHERE status='REQUESTED' AND picked_up_time IS NULL;

-- Reservations
CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(user_id),
    spot_id INT NOT NULL REFERENCES parking_spots(id),
    vehicle_id INT NULL REFERENCES user_vehicle(id) ON DELETE SET NULL,
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ,
    duration_mins INTEGER GENERATED ALWAYS AS (
        EXTRACT(EPOCH FROM (end_time - start_time)) / 60
    ) STORED,
    rate_per_hour DECIMAL(8,2) NOT NULL DEFAULT 15.00,
    total_cost DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT now()
);


-- Indexes to speed up valet queries
CREATE INDEX IF NOT EXISTS idx_reservations_status_start_time
    ON reservations (status, start_time DESC);

CREATE INDEX IF NOT EXISTS idx_reservations_spot
    ON reservations (spot_id);

CREATE INDEX IF NOT EXISTS idx_reservations_vehicle
    ON reservations (vehicle_id);

CREATE INDEX IF NOT EXISTS idx_user_vehicle_user_created
    ON user_vehicle (user_id, created_at DESC);

-- Valet Invite
CREATE TABLE valet_invite (
    id SERIAL PRIMARY KEY,
    valet_secret_hash VARCHAR(100) NOT NULL,
    expiration_time TIMESTAMPTZ NOT NULL,
    use_limit INT NOT NULL DEFAULT 1,
    use_count INT NOT NULL DEFAULT 0,
    is_revoked BOOLEAN NOT NULL DEFAULT false,
    creator INT,
    creation_time TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS valet_invite_active_index
    ON valet_invite (expiration_time, is_revoked);

-- Backfill vehicle_id for reservations
UPDATE reservations r
SET vehicle_id = uv.id
FROM user_vehicle uv
WHERE uv.user_id = r.user_id
  AND uv.is_current = TRUE
  AND r.vehicle_id IS NULL;

WITH latest AS (
    SELECT DISTINCT ON (uv.user_id) uv.id, uv.user_id
    FROM user_vehicle uv
    ORDER BY uv.user_id, uv.created_at DESC
)
UPDATE reservations r
SET vehicle_id = l.id
FROM latest l
WHERE l.user_id = r.user_id
  AND r.vehicle_id IS NULL;

COMMIT TRANSACTION;
