package com.dynamiccarsharing.carsharing;


import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.*;
import com.dynamiccarsharing.carsharing.service.CarService;
import com.dynamiccarsharing.carsharing.service.PaymentService;
import com.dynamiccarsharing.carsharing.service.UserService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class App {
    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        CarRepository carRepository = new InMemoryCarRepository();
        PaymentRepository paymentRepository = new InMemoryPaymentRepository();

        UserService userService = new UserService(userRepository);
        CarService carService = new CarService(carRepository);
        PaymentService paymentService = new PaymentService(paymentRepository);

        System.out.println("=== Car Sharing Demo ===");

        // === Create ===
        System.out.println("\n1. Creating a new user (Renter)");
        ContactInfo contactInfo = new ContactInfo(1L, "Dumitru", "Diacenco", "dd.prodev@gmail.com", "37367773888");
        User renter = userService.signUp("dd.prodev@gmail.com", "password123", contactInfo, UserRole.RENTER);
        System.out.println("Created user: " + renter);

        System.out.println("\n2. Adding a car");
        Location location = new Location(1L, "New York", "New York", "10001");
        Car car = new Car(null, "ABC123", "Toyota", "Camry", CarStatus.AVAILABLE, location, 50.0, CarType.SEDAN, VerificationStatus.VERIFIED);
        car = carService.save(car);
        System.out.println("Created car: " + car);

        System.out.println("\n3. Creating a payment");
        Payment payment = new Payment(null, 1L, 50.0, TransactionStatus.PENDING, PaymentType.CREDIT_CARD, LocalDateTime.now(), LocalDateTime.now());
        payment = paymentService.save(payment);
        System.out.println("Created payment: " + payment);

        // === Read ===
        System.out.println("\n4. Reading user by ID");
        Optional<User> foundUser = userService.findById(renter.getId());
        System.out.println("Found user: " + foundUser.orElse(null));

        System.out.println("\n5. Reading car by ID");
        Optional<Car> foundCar = carService.findById(car.getId());
        System.out.println("Found car: " + foundCar.orElse(null));

        System.out.println("\n6. Reading payment by ID");
        Optional<Payment> foundPayment = paymentService.findById(payment.getId());
        System.out.println("Found payment: " + foundPayment.orElse(null));

        // === Update ===
        System.out.println("\n7. Updating user contact info");
        ContactInfo updatedContactInfo = new ContactInfo(2L, "John", "Doe", "john.doe@gmail.com", "37367773999");
        User updatedUser = userService.updateContactInfo(renter.getId(), updatedContactInfo);
        System.out.println("Updated user: " + updatedUser);

        System.out.println("\n8. Updating car price");
        Car updatedCar = carService.updatePrice(car.getId(), 75.0);
        System.out.println("Updated car: " + updatedCar);

        // === Filter ===
        System.out.println("\n10. Filtering users by role (RENTER)");
        List<User> renters = userService.findUsersByRole(UserRole.RENTER);
        System.out.println("Renters: " + renters);

        System.out.println("\n11. Filtering cars by status (AVAILABLE)");
        List<Car> availableCars = carService.findCarsByCarStatus(CarStatus.AVAILABLE);
        System.out.println("Available cars: " + availableCars);

        System.out.println("\n12. Filtering cars by location (New York)");
        List<Car> carsInNY = carService.findCarsByLocation(location);
        System.out.println("Cars in New York: " + carsInNY);

        // === Delete ===
        System.out.println("\n13. Deleting user");
        userService.deleteById(renter.getId());
        System.out.println("User deleted. Find by ID: " + userService.findById(renter.getId()).orElse(null));

        System.out.println("\n14. Deleting car");
        carService.deleteById(car.getId());
        System.out.println("Car deleted. Find by ID: " + carService.findById(car.getId()).orElse(null));

        System.out.println("\n15. Deleting payment");
        paymentService.deleteById(payment.getId());
        System.out.println("Payment deleted. Find by ID: " + paymentService.findById(payment.getId()).orElse(null));
    }
}