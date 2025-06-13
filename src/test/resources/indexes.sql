\timing on

-- Single-Column Index Test -- 

--  1: Drop the index if it exists, to ensure a clean test.
DROP INDEX IF EXISTS idx_bookings_renter_id;
VACUUM ANALYZE bookings;

-- 2: Test WITHOUT the index.
EXPLAIN ANALYZE SELECT * FROM bookings WHERE renter_id = 456;

-- 3: Create the index.
CREATE INDEX idx_bookings_renter_id ON bookings(renter_id);
VACUUM ANALYZE bookings;

-- 4: Test WITH the index.
EXPLAIN ANALYZE SELECT * FROM bookings WHERE renter_id = 456;


-- Compound Index Test --

-- 1: Drop index for a clean test.
DROP INDEX IF EXISTS idx_bookings_car_id_start_time;
VACUUM ANALYZE bookings;

-- 2: Create the compound index.
CREATE INDEX idx_bookings_car_id_start_time ON bookings(car_id, start_time);
VACUUM ANALYZE bookings;

-- 3: Test the FULL index prefix (car_id and start_time).
EXPLAIN ANALYZE SELECT * FROM bookings WHERE car_id = 12345 AND start_time > '2024-06-01';

-- 4: Test only the LEFT-MOST part of the index (car_id).
EXPLAIN ANALYZE SELECT * FROM bookings WHERE car_id = 12345;

-- 5: Test a column that is NOT the left-most part of the index (start_time only).
EXPLAIN ANALYZE SELECT * FROM bookings WHERE start_time > '2024-06-01';

\timing off