CREATE TABLE contact_infos (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    contact_info_id UUID UNIQUE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('RENTER', 'CAR_OWNER', 'ADMIN', 'GUEST')),
    status VARCHAR(50) NOT NULL CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED')),
    CONSTRAINT fk_users_on_contact_infos FOREIGN KEY (contact_info_id) REFERENCES contact_infos(id) ON DELETE CASCADE
);

CREATE TABLE locations (
    id UUID PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20) NOT NULL
);

CREATE TABLE cars (
    id UUID PRIMARY KEY,
    registration_number VARCHAR(20) UNIQUE NOT NULL,
    make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('AVAILABLE', 'RENTED', 'MAINTENANCE')),
    location_id UUID NOT NULL,
    price_per_day DECIMAL(10, 2) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('SEDAN', 'SUV', 'HATCHBACK', 'COUPE', 'TRUCK')),
    verification_status VARCHAR(50) NOT NULL CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED')),
    CONSTRAINT fk_cars_on_locations FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE
);

CREATE TABLE user_cars (
    user_id UUID NOT NULL,
    car_id UUID NOT NULL,
    PRIMARY KEY (user_id, car_id),
    CONSTRAINT fk_user_cars_on_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_cars_on_cars FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    renter_id UUID NOT NULL,
    car_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'COMPLETED', 'CANCELED')),
    pickup_location_id UUID NOT NULL,
    dispute_description VARCHAR(255),
    dispute_status VARCHAR(50) CHECK (dispute_status IN ('OPEN', 'RESOLVED')),
    CONSTRAINT fk_bookings_on_users FOREIGN KEY (renter_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_on_cars FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_on_locations FOREIGN KEY (pickup_location_id) REFERENCES locations(id) ON DELETE CASCADE
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    booking_id UUID UNIQUE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'COMPLETED', 'CANCELED')),
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'APPLE_PAY', 'GOOGLE_PAY', 'BANK_TRANSFER', 'CASH', 'CRYPTO', 'MOBILE_MONEY')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_payments_on_bookings FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE disputes (
    id UUID PRIMARY KEY,
    booking_id UUID UNIQUE NOT NULL,
    creation_user_id UUID NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('OPEN', 'RESOLVED')),
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    CONSTRAINT fk_disputes_on_bookings FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_disputes_on_users FOREIGN KEY (creation_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'COMPLETED', 'CANCELED')),
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'APPLE_PAY', 'GOOGLE_PAY', 'BANK_TRANSFER', 'CASH', 'CRYPTO', 'MOBILE_MONEY')),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_transactions_on_bookings FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE car_reviews (
    id UUID PRIMARY KEY,
    car_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    comment VARCHAR(255) NOT NULL,
    CONSTRAINT fk_car_reviews_on_cars FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    CONSTRAINT fk_car_reviews_on_users FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_reviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    reviewer_id UUID NOT NULL,
    comment VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_reviews_on_users_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_reviews_on_users_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE
);