\echo 'Populating locations...'
INSERT INTO locations (id, city, state, zip_code)
SELECT
    s,
    'City' || s,
    'State' || (s % 50),
    (10000 + s)::text
FROM generate_series(1, 10000) s ON CONFLICT (id) DO NOTHING;

\echo 'Populating contact_infos...'
INSERT INTO contact_infos (id, first_name, last_name, email, phone_number)
SELECT
    s,
    'FirstName' || s,
    'LastName' || s,
    'user' || s || '@example.com',
    '555-' || lpad(s::text, 7, '0')
FROM generate_series(1, 50000) s ON CONFLICT (id) DO NOTHING;

\echo 'Populating users...'
INSERT INTO users (id, contact_info_id, role, status)
SELECT
    s,
    s,
    CASE WHEN s % 10 = 0 THEN 'CAR_OWNER' ELSE 'RENTER' END,
    'ACTIVE'
FROM generate_series(1, 50000) s ON CONFLICT (id) DO NOTHING;

\echo 'Populating cars...'
INSERT INTO cars (id, registration_number, make, model, status, location_id, price_per_day, type, verification_status)
SELECT
    s,
    'CAR-' || s,
    'Make' || (s % 100),
    'Model' || (s % 1000),
    'AVAILABLE',
    (1 + (random() * 9999))::integer,
    (50 + random() * 100)::decimal(10, 2),
    'SEDAN',
    'VERIFIED'
FROM generate_series(1, 50000) s ON CONFLICT (id) DO NOTHING;

\echo 'Populating bookings (this will take a few minutes)...'
TRUNCATE TABLE bookings CASCADE;
INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id)
SELECT
    (1 + (random() * 49999))::integer,
    (1 + (random() * 49999))::integer,
    timestamp '2023-01-01 00:00:00' + random() * (timestamp '2025-01-01 00:00:00' - timestamp '2023-01-01 00:00:00'),
    timestamp '2023-01-02 00:00:00' + random() * (timestamp '2025-01-02 00:00:00' - timestamp '2023-01-01 00:00:00'),
    'COMPLETED',
    (1 + (random() * 9999))::integer
FROM generate_series(1, 2000000);

\echo 'Data population complete.'