## Users of Car Sharing Platform
* `Renter:` A user who wants to rent a car.
* `Car Owner:` A user who lists their car for rent.
* `Admin:` A system administrator who manages the platform.
* `Guest:` A non-registered user exploring the platform.
-------
### User stories
**Format:** `As a [type of user], I want to [perform an action] so that [benefit or goal].`

------------------------
**Renter User Stories**
* As a renter, I want to browse available cars in my area so that I can choose one that fits my needs.
* As a renter, I want to filter cars by price, location, and type so that I can find the most suitable car quickly.
* As a renter, I want to book a car for a specific time period so that I can use it for my trip.
* As a renter, I want to view the car owner’s profile and ratings so that I can trust the person I’m renting from.
* As a renter, I want to make a secure payment through the platform so that I can complete my booking safely.
* As a renter, I want to receive a booking confirmation and car pickup details so that I know the process for accessing the car.
* As a renter, I want to rate and review the car and owner after my trip so that I can provide feedback for future users.
* As a renter, I want to filter cars by verification status so that I can ensure the car is trusted and reliable.
* As a renter, I want to file a dispute if there’s an issue with the car or booking so that I can seek resolution from the platform, with immutable dispute records.
* As a renter, I want to track my earnings, booking, and transaction history so that I can manage my rental activity and expenses.
------------------------
**Car Owner User Stories**
* As a car owner, I want to list my car on the platform so that I can earn money by renting it out.
* As a car owner, I want to set the availability and pricing for my car so that I can control when and how it’s rented.
* As a car owner, I want to review renter profiles before approving a booking so that I can ensure my car is in safe hands.
* As a car owner, I want to receive notifications about booking requests so that I can respond promptly.
* As a car owner, I want to track my earnings, booking, and transaction history so that I can manage my rental business effectively.
* As a car owner, I want to rate and review renters after a trip so that I can provide feedback for future owners.
* As a car owner, I want to submit my car for verification so that it can be listed as trusted on the platform.
* As a car owner, I want to manage my list of cars (add/remove) through the platform so that I can update my offerings, with updates handled by the system to maintain immutability.
* As a car owner, I want to update my contact information so that renters and the platform can reach me easily, with immutable contact records.
------------------------
**Admin User Stories**
* As an admin, I want to verify car owner profiles and vehicles so that I can ensure the platform’s safety and reliability.
* As an admin, I want to resolve disputes between renters and owners so that I can maintain a fair platform.
* As an admin, I want to monitor platform activity so that I can detect and prevent fraudulent behavior.
* As an admin, I want to manage user accounts (e.g., suspend or ban) so that I can enforce platform rules.
* As an admin, I want to view and manage reviews to ensure they meet platform guidelines so that users receive reliable feedback, with immutable review data.
* As an admin, I want to access detailed user and car data for reports so that I can improve platform operations.
------------------------
**Guest User Stories**
* As a guest, I want to view available cars without registering so that I can explore the platform before committing.
* As a guest, I want to sign up for an account so that I can start renting or listing cars.
* As a guest, I want to view car details, including type, price, and location, so that I can assess the platform’s offerings.
-------
### OOD Class Diagram, Divide and conquer
1. `Cars Catalog:` Car, Location, CarRepository, LocationRepository, CarService, LocationService _(🔵Blue border color)_
2. `Users Management:` User, ContactInfo, UserRepository, ContactInfoRepository, UserService, ContactInfoService _(🟢Green border color)_
3. `Booking Management:` Booking, Payment, Transaction, Dispute, Review, CarReview, UserReview, BookingRepository, PaymentRepository, CarReviewRepository, UserReviewRepository, BookingService, PaymentService, CarReviewService, UserReviewService _(🔴Red border color)_
---------
## Prerequisites
1. Docker and Docker Compose installed.
2. Optional: DBeaver/pgAdmin for GUI-based database management.

### Setup Instructions
- Clone the Repository: `git clone git@gitlab.griddynamics.net:DynamicCarSharing.git`
- The SQL scripts in `initdb.sql` will create the schema if the database is empty.

### Edit .env if needed:
* DB_NAME=name_db
* DB_USER=username_db
* DB_PASSWORD=password_db
* DB_PORT=5432
* PGA_EMAIL=example@gmail.com
* PGA_PASSWORD=pgadmin_password
* PGA_PORT=5050

#### Start the Database, run Docker Compose: 
`docker-compose up -d`

#### Check the container is running:
`docker ps`

------------
### Connect using a database client (e.g., DBeaver):
#### Example with DBeaver:
* Host: localhost
* Port: 5432 (or DB_PORT from .env)
* Database: exampledb
* Username: exampleuser
* Password: examplepassword
--------
#### Example with pgAdmin
* URL: http://localhost:5050
* Email: example@gmail.com
* Password: root

**Add a server with:**
* Host: db (server name)
* Port: 5432
* Database: name-db
* Username: user
* Password: password
------
#### Stop the DatabaseTo stop without deleting data:
`docker-compose down`

#### To remove data:
`docker-compose down -v`

### Troubleshooting
Port Conflict: Check if 5432 is in use

--------------------------
1. Update DB_PORT in .env if needed.
2. Connection Issues: Verify container status (docker ps) and .env values.
3. Permission Errors: Ensure Docker is running, and you have access (sudo systemctl start docker).