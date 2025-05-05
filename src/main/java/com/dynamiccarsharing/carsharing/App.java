package com.dynamiccarsharing.carsharing;

import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.service.*;

import java.time.LocalDateTime;

public class App {
    public static void main(String[] args) {

        UserService userService = new UserService();
        CarService carService = new CarService();
        BookingService bookingService = new BookingService();
        PaymentService paymentService = new PaymentService();
        ReviewService reviewService = new ReviewService();

        // Create a CarOwner and a Renter
        User carOwner = new User(1L, "John Doe", "owner@example.com", "+37367773888", "CarOwner", "active");
        User renter = new User(2L, "Jane Smith", "renter@example.com", "+37367773999", "Renter", "active");
        userService.saveUser(carOwner);
        userService.saveUser(renter);
        System.out.println("Created CarOwner: " + userService.findUserById(1L));
        System.out.println("Created Renter: " + userService.findUserById(2L));
        System.out.println();

        // Create a Car and add to CarOwner's list
        Car car1 = new Car(1L, "BLB017", "Toyota Camry", "available", "New York", 50.0, "sedan", "verified");
        carService.saveCar(car1);
        carOwner.getCarList().add(car1);
        userService.updateUser(carOwner);
        System.out.println("Car added to CarOwner: " + userService.findUserById(1L));
        System.out.println();

        // Create a Booking for the Renter
        Booking booking1 = new Booking(1L, 2L, 1L, LocalDateTime.now(), LocalDateTime.now().plusDays(2), "pending", "Downtown NYC", null, null);
        bookingService.saveBooking(booking1);
        System.out.println("Created Booking: " + bookingService.findBookingById(1L));
        System.out.println();

        // Create a Payment for the Booking
        Payment payment1 = new Payment(1L, 1L, 100.0, "pending", "credit card", "TXN12345");
        paymentService.savePayment(payment1);
        System.out.println("Created Payment: " + paymentService.findPaymentById(1L));
        System.out.println();

        // Create a Review for the Car by the Renter
        Review review1 = new Review(1L, 2L, 1L, "car", "Great car, very clean!");
        reviewService.saveReview(review1);
        System.out.println("Created Review: " + reviewService.findReviewById(1L));
        System.out.println();

        // Find by ID or Alternate Field
        System.out.println("User by ID: " + userService.findUserById(1L));
        System.out.println("User by Email: " + userService.findUserByEmail("owner@example.com"));
        System.out.println("Car by ID: " + carService.findCarById(1L));
        System.out.println("Car by Registration: " + carService.findCarByRegistration("BLB017"));
        System.out.println("Booking by ID: " + bookingService.findBookingById(1L));
        System.out.println("Booking by Renter ID: " + bookingService.findBookingByRenterId(2L));
        System.out.println("Payment by ID: " + paymentService.findPaymentById(1L));
        System.out.println("Payment by Booking ID: " + paymentService.findPaymentByBookingId(1L));
        System.out.println("Review by ID: " + reviewService.findReviewById(1L));
        System.out.println("Review by Reviewer ID: " + reviewService.findReviewByReviewerId(2L));
        System.out.println();

        // FindAll
        System.out.println("All Users: " + userService.findAllUsers());
        System.out.println("All Cars: " + carService.findAllCars());
        System.out.println("All Bookings: " + bookingService.findAllBookings());
        System.out.println("All Payments: " + paymentService.findAllPayments());
        System.out.println("All Reviews: " + reviewService.findAllReviews());
        System.out.println();

        // Update
        carOwner.setName("John Smith");
        userService.updateUser(carOwner);
        System.out.println("Updated CarOwner: " + userService.findUserById(1L));
        car1.setStatus("rented");
        carService.updateCar(car1);
        System.out.println("Updated Car: " + carService.findCarById(1L));
        booking1.setStatus("approved");
        bookingService.updateBooking(booking1);
        System.out.println("Updated Booking: " + bookingService.findBookingById(1L));
        payment1.setStatus("completed");
        paymentService.updatePayment(payment1);
        System.out.println("Updated Payment: " + paymentService.findPaymentById(1L));
        review1.setComment("Great car, very clean and reliable!");
        reviewService.updateReview(review1);
        System.out.println("Updated Review: " + reviewService.findReviewById(1L));
        System.out.println();

        // Find with filter field
        System.out.println("Users with role 'CarOwner': " + userService.findUsersByFilter("role", "CarOwner"));
        System.out.println("Users with status 'active': " + userService.findUsersByFilter("status", "active"));
        System.out.println("Cars with status 'rented': " + carService.findCarsByFilter("status", "rented"));
        System.out.println("Cars with location 'New York': " + carService.findCarsByFilter("location", "New York"));
        System.out.println("Bookings with status 'approved': " + bookingService.findBookingsByFilter("status", "approved"));
        System.out.println("Payments with status 'completed': " + paymentService.findPaymentsByFilter("status", "completed"));
        System.out.println("Reviews for type 'car': " + reviewService.findReviewsByFilter("type", "car"));
        System.out.println();

        // Delete
        carOwner.getCarList().remove(car1);
        userService.updateUser(carOwner);
        bookingService.deleteBooking(1L);
        paymentService.deletePayment(1L);
        reviewService.deleteReview(1L);
        carService.deleteCar(1L);
        userService.deleteUser(1L);
        userService.deleteUser(2L);
        System.out.println("After delete, Booking by ID: " + bookingService.findBookingById(1L));
        System.out.println("After delete, Payment by ID: " + paymentService.findPaymentById(1L));
        System.out.println("After delete, Review by ID: " + reviewService.findReviewById(1L));
        System.out.println("After delete, Car by ID: " + carService.findCarById(1L));
        System.out.println("After delete, User by ID (CarOwner): " + userService.findUserById(1L));
        System.out.println("After delete, User by ID (Renter): " + userService.findUserById(2L));
    }
}