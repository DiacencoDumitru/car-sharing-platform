## Users of Car Sharing Platform
* `Renter:` A user who wants to rent a car.
* `Car Owner:` A user who lists their car for rent.
* `Admin:` A system administrator who manages the platform.
* `Guest:` A non-registered user exploring the platform.

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
------------------------
**Car Owner User Stories**
* As a car owner, I want to list my car on the platform so that I can earn money by renting it out.
* As a car owner, I want to set the availability and pricing for my car so that I can control when and how it’s rented.
* As a car owner, I want to review renter profiles before approving a booking so that I can ensure my car is in safe hands.
* As a car owner, I want to receive notifications about booking requests so that I can respond promptly.
* As a car owner, I want to track my earnings and booking history so that I can manage my rental business effectively.
* As a car owner, I want to rate and review renters after a trip so that I can provide feedback for future owners.
------------------------
**Admin User Stories**
* As an admin, I want to verify car owner profiles and vehicles so that I can ensure the platform’s safety and reliability.
* As an admin, I want to resolve disputes between renters and owners so that I can maintain a fair platform.
* As an admin, I want to monitor platform activity so that I can detect and prevent fraudulent behavior.
* As an admin, I want to manage user accounts (e.g., suspend or ban) so that I can enforce platform rules.
------------------------
**Guest User Stories**
* As a guest, I want to view available cars without registering so that I can explore the platform before committing.
* As a guest, I want to sign up for an account so that I can start renting or listing cars.

### OOD Class Diagram, Divide and conquer
1. `Cars Catalog:` Car, CarRepository, CarService _(🔵Blue border color)_
2. `Users Management:` User, UserRepository, UserService _(🟢Green border color)_
3. `Booking Management:` Booking, Payment, Review, BookingRepository, PaymentRepository, ReviewRepository, BookingService, PaymentService, ReviewService _(🔴Red border color)_