-- Parking Spots 
CREATE TABLE parking_spots (
	id SERIAL,
	spot_number INT UNIQUE NOT NULL,
	status BOOLEAN DEFAULT false,
	-- is_handicapped BOOLEAN DEFAULT false
	CONSTRAINT PK_parking_spots_id PRIMARY KEY (id)
);

-- Parking session
CREATE TABLE sessions (
	id SERIAL,
	user_id SERIAL NOT NULL,
	spot_id SERIAL NOT NULL, 
	check_in_time TIMESTAMPTZ NOT NULL,
	check_out_time TIMESTAMPTZ,
	total_charged DECIMAL(10,2),
	CONSTRAINT PK_sessions_id PRIMARY KEY (id),
	CONSTRAINT FK_sessions_users FOREIGN KEY (user_id) REFERENCES users(user_id),
	CONSTRAINT FK_sessions_parking_spots FOREIGN KEY (spot_id) REFERENCES parking_spots (id)
);

-- Notification
CREATE TABLE notifications (
	id SERIAL,
	session_id SERIAL NOT NULL,
	customer_id SERIAL NOT NULL,
	requested_time TIMESTAMPTZ NOT NULL,
	picked_up_time TIMESTAMPTZ,
	status VARCHAR(30) NOT NULL,
	CONSTRAINT PK_notifications PRIMARY KEY (id),
	CONSTRAINT FK_notifications_sessions FOREIGN KEY (session_id) REFERENCES sessions(id),
	CONSTRAINT FK_notifications_parking_spots FOREIGN KEY (customer_id) REFERENCES users(user_id)
);

-- 1 vehicle dropoff = 1 valet parking reservation = 1 valet parking pick-up
CREATE TABLE reservations (
	id SERIAL,
	user_id INT,
	spot_id INT,
	start_time TIMESTAMPTZ NOT NULL,
	end_time TIMESTAMPTZ,
	duration_mins INTEGER GENERATED ALWAYS AS (EXTRACT(EPOCH FROM (end_time - start_time)) / 60) STORED,
	rate_per_hour DECIMAL(8,2) NOT NULL,
	total_cost DECIMAL(10,2) NOT NULL,
	CONSTRAINT PK_reservations_id PRIMARY KEY (id),
	CONSTRAINT FK_reservations_users FOREIGN KEY (user_id) REFERENCES users(user_id),
	CONSTRAINT FK_reservations_parking_spots FOREIGN KEY (spot_id) REFERENCES parking_spots(id)
);