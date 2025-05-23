### Process
**Setup Docker Environment:**
* Installed Docker and Docker Compose on the local machine (Ubuntu/macOS/Windows).
* Verified installations with `docker --version` and `docker-compose --version.`

**Repository and Configuration:**
* Created .env with variables (DB_USER, DB_PASSWORD, DB_NAME, DB_PORT) and added .env to .gitignore.
* Created .env.example for reference.

**Docker Compose Implementation:**
* Wrote docker-compose.yml for a PostgreSQL service with a persistent volume (db_data).
* Configured environment variables to override default PostgreSQL credentials.
* Ran docker-compose up -d to start the container.

**Testing and Verification:**
* Verified container status with `docker ps.`
* Connected to the database using DBeaver: 
  * (localhost:5432, exampledb, exampleuser, examplepassword)
* Connected to the database using pgAdmin: 
  * (localhost:5050, example@gmail.com, examplepassword)
  * Add a server with: Host: example_db (server name), port: 5432, database: examplename, username: user, examplepassword: password)
* Tested data persistence by stopping and restarting the container.

**Documentation:**
* Updated README.md with step-by-step setup instructions.
* Ensured instructions were reproducible by testing on a clean environment.

**Key Insights:**
* Environment Variables: Using .env files simplifies configuration and enhances security by keeping sensitive data out of version control.
* Persistent Storage: Named volumes (db_data) are critical for preserving database data across container restarts.
* Documentation: Clear, step-by-step instructions in README.md ensure team members can replicate the setup easily.
* Testing: Validating connectivity with multiple tools (DBeaver, pgAdmin) confirms robustness.
* Reproducibility: Testing instructions on a clean machine is essential to verify documentation.

### Mentor Discussion Points
Implementation Review:
Confirm the `docker-compose.yml` setup (volume, environment variables) is optimal.
Discuss the decision to exclude pgAdmin (based on requirements).

Optimizations:
* Should we add a health check (e.g., pg_isready) to ensure the database is ready?

Documentation:
* Is the README.md clear and sufficient for new developers?
* Any additional sections needed (e.g., backup/restore instructions)?

Future Improvements:
* Explore adding pgAdmin or other services if needed. 
* Experience with Docker Compose, PostgreSQL, and environment variable management. 