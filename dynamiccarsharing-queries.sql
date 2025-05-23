-- CREATE
INSERT INTO car (registration_number, make, model, status, location_id, price_per_day, "type", verification_status) VALUES ('GLC777', 'Mercedes', 'GLC', 'AVAILABLE', 2, 95, 'COUPE', 'VERIFIED');

-- READ
SELECT c.make, l.city, l.state, l.zip_code
FROM car c
JOIN "location" l ON c.location_id = l.id;

-- UPDATE
UPDATE car
SET price_per_day = 130.00, status = 'AVAILABLE', verification_status = 'VERIFIED'
WHERE id = 4;

-- DELETE
DELETE FROM car
WHERE id = 4;

-- FILTER
SELECT c.id, c.make, c.model, c.price_per_day, c.type, l.city, l.state
FROM car c
JOIN location l ON c.location_id = c.id
WHERE c.status = 'AVAILABLE'
  AND c.verification_status = 'VERIFIED'
  AND ('SUV' IS NULL OR c.type = 'SUV')
  AND (c.price_per_day BETWEEN 30 AND 100)
  AND ('New York' IS NULL OR l.city ILIKE 'New York')
ORDER BY c.price_per_day
LIMIT 2 OFFSET 0;

-- SEARCH WITH JOINED DATA
SELECT b.id, b.start_time, b.end_time, b.status,
       CONCAT(ci.first_name, ' ', ci.last_name) AS renter_fullname,
       c.make, c.model, l.city AS pickup_city
FROM booking b
JOIN "user" u ON b.renter_id = u.id
JOIN contact_info ci ON u.contact_info_id = ci.id
JOIN car c ON b.car_id = c.id
JOIN "location" l ON b.pickup_location_id = l.id
WHERE b.status = 'COMPLETED'
ORDER BY b.start_time;

-- STATISTIC QUERY
SELECT l.city, l.state, SUM(p.amount) AS total_revenue_per_city
FROM "location" l
JOIN booking b ON b.pickup_location_id = l.id
JOIN payment p ON p.booking_id = b.id
WHERE p.status = 'COMPLETED'
GROUP BY l.id, l.city, l.state
ORDER BY total_revenue_per_city DESC;

-- TOP SOMETHING QUERY
SELECT c.make, c.model, COUNT(b.id) AS booking_count
FROM car c
LEFT JOIN booking b ON c.id = b.car_id
WHERE c.verification_status = 'VERIFIED'
GROUP BY c.id, c.make, c.model
ORDER BY booking_count DESC
LIMIT 2;