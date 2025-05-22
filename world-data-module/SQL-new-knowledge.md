### What new I learned:
- Solve conflict when docker container is running `virtual-db` and postgresql@version service is running `local-db` on the same localhost port. (ex: 5432:5432)
- What is `Create User` and `Create Role` in Postgresql?
- Work with several databases on one docker container
- Writing queries with plugin `Database Navigator`
- Writing queries with pgAdmin `Query Tool`
- Types of subqueries
- Pagination: `OFFSET`
- PostgreSQL:`ILIKE`
- Use: `::float`, when we want to avoid division by zero, ex: `coalesce(count(distinct pc.person_id), 0)::float / c.area as density`
---------------

#### Solution for conflict from first point of this file:
1. Stop the local PostgreSQL instance if it’s not needed.
2. Reconfigure the Docker container to use a different host port (ex: 5433) while keeping the container’s internal port as 5432.
3. Alternative: Consolidate database management into a single Docker container by migrating any necessary local-db data to virtual-db, stopping the local PostgreSQL service, and using the container exclusively.

* Helper command to check running services: `brew services list`