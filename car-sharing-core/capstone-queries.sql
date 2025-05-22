-- create
insert into car (registration_number, make, model, status, location_id, price_per_day, "type", verification_status) values
('GLC777', 'Mercedes', 'GLC', 'AVAILABLE', 2, 95, 'COUPE', 'VERIFIED');

-- read
select c.make, l.city, l.state, l.zip_code
from car c
join "location" l on c.location_id = l.id;

-- update
update car
set price_per_day = 130.00, status = 'AVAILABLE', verification_status = 'VERIFIED'
where id = 4;

-- delete
delete from car where id = 4

-- filter
select c.id, c.make, c.model, c.price_per_day, c.type, l.city, l.state
from car c
join location l on c.location_id = c.id
where c.status = 'AVAILABLE'
and c.verification_status = 'VERIFIED'
and ('SUV' is null or c.type = 'SUV')
and (c.price_per_day between 30 and 100)
and ('New York' is null or l.city ilike 'New York')
order by c.price_per_day
limit 2 offset 0;

-- search with joined data
select b.id, b.start_time, b.end_time, b.status,
	   concat(ci.first_name, ' ', ci.last_name) as renter_fullname,
	   c.make, c.model, l.city as pickup_city
from booking b
join "user" u on b.renter_id = u.id
join contact_info ci on u.contact_info_id = ci.id
join car c on b.car_id = c.id
join "location" l on b.pickup_location_id = l.id
where b.status = 'COMPLETED'
order by b.start_time;

-- statistic query
select l.city, l.state, sum(p.amount) as total_revenue_per_city
from "location" l
join booking b on b.pickup_location_id = l.id
join payment p on p.booking_id = b.id
where p.status = 'COMPLETED'
group by l.id, l.city, l.state
order by total_revenue_per_city desc;

-- top something query
select c.make, c.model, count(b.id) as booking_count
from car c
left join booking b on c.id = b.car_id
where c.verification_status = 'VERIFIED'
group by c.id, c.make, c.model
order by booking_count desc
limit 2;