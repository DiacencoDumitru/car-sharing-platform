-- ============================================================================================================================
-- 3. Write SQL queries to find the following data about countries:
-- ============================================================================================================================
--☑️ Country with the biggest population (id and name of the country)
select c.id, c.name as country
from country c
join person_country pc on c.id = pc.country_id
group by c.id, c.name
having count(distinct person_id) = (
	select max(person_count)
    from (
		select country_id, count(distinct person_id) as person_count
		from person_country
		group by country_id
	) as sub
);

--☑️ Top 10 countries with the lowest population density (names of the countries)
select c.name,
		coalesce(count(distinct pc.person_id), 0) as population,
		c.area,
		coalesce(count(distinct pc.person_id), 0)::float / c.area as density
from country c
left join person_country pc on c.id = pc.country_id
group by c.id, c.name, c.area
order by density asc
limit 10;

--☑️ Countries with population density higher than average across all countries
select c.name
from country c
left join person_country pc on c.id = pc.country_id
group by c.id, c.name, c.area
having count(distinct pc.person_id) / c.area > (
	select avg(density)
	from (
		select count(distinct pc2.person_id) / c2.area as density
		from country c2
		left join person_country pc2 on c2.id = pc2.country_id
		group by c2.id, c2.area
	) as sub
);

--☑️ Country with the longest name (if several countries have name of the same length, show all of them)
select name
from country
where length(name) = (
    select max(length(name))
    from country
);

--☑️ All countries with name containing letter “F”, sorted in alphabetical order
select name
from country
where name like '%F%'
order by name;

--☑️ Country which has a population, closest to the average population of all countries
with country_population as (
	select c.id, c.name, count(pc.person_id) as population
	from country c
	left join person_country pc on c.id = pc.country_id
	group by c.id, c.name
),

avg_pop as (
	select avg(population) as avg_population
	from country_population
)

select cp.name, cp.population
from country_population cp, avg_pop
order by abs(cp.population - avg_pop.avg_population)
limit 1;

-- ============================================================================================================================
-- 4. Write SQL queries to find the following data about countries and continents:
-- ============================================================================================================================
--☑️ Count of countries for each continent
select ct.name, count(c.continent_id) as countries
from country c
join continent ct on c.continent_id = ct.id
group by ct.name;

--☑️ Total area for each continent (print continent name and total area), sorted by area from biggest to smallest
select ct.name as continent, sum(c.area)  as total_area
from continent ct
join country c on ct.id = c.continent_id
group by ct.name
order by total_area desc;

--☑️ Average population density per continent
select ct.name as continent_name, count(distinct pc.person_id) / sum(c.area) as avg_pop_density
from continent ct
left join country c on ct.id = c.continent_id
left join person_country pc on c.id = pc.country_id
group by ct.id, ct.name;

--☑️ For each continent, find a country with the smallest area (print continent name, country name and area)
select ct.name as continent, c.name as country, c.area
from country c
join continent ct on c.continent_id = ct.id
where c.area = (
    select min(c2.area)
    from country c2
	where c2.continent_id = ct.id
)
order by c.area;

--☑️ Find all continents, which have average country population less than 20 million
with country_population as (
	select c.id as country_id, c.continent_id, count(pc.person_id) as population
	from country c
	left join person_country pc on c.id = pc.country_id
	group by c.id, c.continent_id
),

continent_avg_pop as (
	select ct.name as continent_name,
	avg(cp.population) as avg_country_population
	from country_population cp
	join continent ct on cp.continent_id = ct.id
	group by ct.id, ct.name
)

select continent_name, round(avg_country_population, 3)
from continent_avg_pop
where avg_country_population < 20000000;

-- ============================================================================================================================
-- 5. Write SQL queries to find the following data about people
-- ============================================================================================================================
--☑️ Person with the biggest number of citizenship's
select p.name, count(distinct(pc.country_id)) as citizensihps
from person p
join person_country pc on p.id = person_id
group by p.name
order by citizensihps desc
limit 1;

--☑️ All people who have no citizenship (solved with anti-join)
select p.name, p.id
from person p
left join person_country pc on p.id = person_id
where pc.country_id is null;

--☑️ Country with the least people in People table
select c.name as country, count(distinct pc.person_id) as people_per_country
from country c
join person_country pc on c.id = pc.country_id
group by c.name, c.name
having count(distinct pc.person_id) = (
    select min(people_count)
    from (
        select country_id, count(distinct person_id) as people_count
        from person_country
        group by country_id
    ) as sub
);

--☑️ Continent with the most people in People table
select ct.name as continent
from continent ct
join country c on ct.id = c.continent_id
join person_country pc on c.id = pc.country_id
group by ct.name
having count(pc.person_id) = (
    select max(people_count)
    from (
        select ct_inner.name as continent_name, count(pc_inner.person_id) as people_count
        from continent ct_inner
        join country c_inner on ct_inner.id = c_inner.continent_id
        join person_country pc_inner on c_inner.id = pc_inner.country_id
        group by ct_inner.name
    ) as sub
);

--☑️ Find pairs of people with the same name - print 2 ids and the name (solved with self-join)
select p1.id as id1, p2.id as id2, p2.name
from person p1
join person p2 on p1.name = p2.name and p1.id < p2.id;