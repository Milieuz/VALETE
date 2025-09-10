BEGIN;

-- 0) (Optional but helpful in psql)
-- \set ON_ERROR_STOP on

-- A) Seed matrix (10 unique patrons & cars)
WITH seed(uname, full_name_txt, phone, make, model, color, plate, vin, desired_spot, mins_ago) AS (
  VALUES
    ('patron_ajohnson',  'Alex Johnson',     '502-555-0101', 'Toyota',    'Camry',   'Blue',   'KZT-4921', '1HGCM82633A100001',  1,  7),
    ('patron_bsmith',    'Brianna Smith',    '502-555-0102', 'Honda',     'Civic',   'White',  '8BXW-307', '1HGCM82633A100002',  2, 12),
    ('patron_cmendoza',  'Carlos Mendoza',   '502-555-0103', 'Ford',      'F-150',   'Red',    'LNM-8452', '1HGCM82633A100003',  3, 18),
    ('patron_dlee',      'Dana Lee',         '502-555-0104', 'Tesla',     'Model 3', 'Black',  '3QH-7290', '1HGCM82633A100004',  4, 25),
    ('patron_emiller',   'Evan Miller',      '502-555-0105', 'Chevy', 'Equinox', 'Silver', 'VYA-6138', '1HGCM82633A100005',  5, 33),
    ('patron_nirwin',    'Naomi Irwin',      '502-555-0106', 'Nissan',    'Altima',  'Gray',   '7TRP-442', '1HGCM82633A100006',  6,  9),
    ('patron_hyoung',    'Hannah Young',     '502-555-0107', 'Hyundai',   'Elantra', 'Green',  'GZK-1957', '1HGCM82633A100007',  7, 15),
    ('patron_khughes',   'Kevin Hughes',     '502-555-0108', 'Kia',       'Sorento', 'Maroon', '2MNL-824', '1HGCM82633A100008',  8, 21),
    ('patron_bmurray',   'Beth Murray',      '502-555-0109', 'BMW',       'X5',      'White',  'RHF-5306', '1HGCM82633A100009',  9, 27),
    ('patron_acheng',    'Aiden Cheng',      '502-555-0110', 'Audi',      'A4',      'Blue',   '9JCV-481', '1HGCM82633A100010', 10, 39)
),

-- B) Upsert 10 patrons (password hash = "password" from your seed)
ins_users AS (
  INSERT INTO users (username, password_hash, role, full_name, phone_number)
  SELECT uname, '$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC', 'ROLE_PATRON', full_name_txt, phone
  FROM seed
  ON CONFLICT (username) DO UPDATE
    SET full_name = EXCLUDED.full_name,
        phone_number = EXCLUDED.phone_number
  RETURNING user_id, username
),

-- C) Insert each user's current vehicle; skip if plate+VIN already exist
ins_vehicles AS (
  INSERT INTO user_vehicle (user_id, make_name, model_name, color, license_plate, vin, is_current)
  SELECT iu.user_id, s.make, s.model, s.color, s.plate, s.vin, TRUE
  FROM seed s
  JOIN ins_users iu ON iu.username = s.uname
  ON CONFLICT (license_plate, vin) DO NOTHING
  RETURNING id AS vehicle_id, user_id
),

-- D) Resolve vehicle_id even if it pre-existed
veh AS (
  SELECT
    iu.user_id,
    COALESCE(iv.vehicle_id, uv.id) AS vehicle_id,
    s.desired_spot,
    s.mins_ago
  FROM seed s
  JOIN ins_users iu ON iu.username = s.uname
  LEFT JOIN ins_vehicles iv ON iv.user_id = iu.user_id
  LEFT JOIN user_vehicle uv
    ON uv.user_id = iu.user_id
   AND uv.license_plate = s.plate
   AND uv.vin = s.vin
),

-- E) Choose spots that are *currently free* and match desired_spot
free_targets AS (
  SELECT
    v.user_id,
    v.vehicle_id,
    ps.id       AS spot_id,
    v.mins_ago
  FROM veh v
  JOIN parking_spots ps
    ON ps.spot_number = v.desired_spot
  WHERE NOT EXISTS (
    SELECT 1
    FROM sessions s
    WHERE s.spot_id = ps.id
      AND s.check_out_time IS NULL
  )
    AND NOT EXISTS (
    SELECT 1
    FROM sessions s
    WHERE s.user_id = v.user_id
      AND s.check_out_time IS NULL
  )
)

-- F) Create sessions only where no conflicts exist
INSERT INTO sessions (user_id, spot_id, vehicle_id, check_in_time)
SELECT
  ft.user_id,
  ft.spot_id,
  ft.vehicle_id,
  NOW() - (ft.mins_ago || ' minutes')::interval
FROM free_targets ft;

COMMIT;
