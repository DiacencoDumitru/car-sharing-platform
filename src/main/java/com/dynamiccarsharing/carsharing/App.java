package com.dynamiccarsharing.carsharing;

import com.dynamiccarsharing.carsharing.dao.CarDao;
import com.dynamiccarsharing.carsharing.dao.PaymentDao;
import com.dynamiccarsharing.carsharing.enums.*;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.*;
import com.dynamiccarsharing.carsharing.service.CarService;
import com.dynamiccarsharing.carsharing.service.PaymentService;
import com.dynamiccarsharing.carsharing.service.UserService;
import com.dynamiccarsharing.carsharing.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class App {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/dynamiccarsharing_db";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) throws SQLException {
        // CRUD operations
        demoCrudInMemory();

        // Comparing Statement vs Prepared Statement
        demoSqlInjection();

        // Comparing Execute() methods: a,b
        demoDbExecuteDifference();
    }

    private static void demoCrudInMemory() throws SQLException {
        DatabaseUtil databaseUtil = new DatabaseUtil(DB_URL, DB_USER, DB_PASSWORD);

        UserRepository userRepository = new InMemoryUserRepository();
        PaymentRepository paymentRepository = new PaymentDao(databaseUtil);
        CarDao carRepository = new CarDao(databaseUtil);

        UserService userService = new UserService(userRepository);
        PaymentService paymentService = new PaymentService(paymentRepository);
        CarService carService = new CarService(carRepository);

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

    private static void demoDbExecuteDifference() {
        // Method A: Simple execute with direct parameter passing. Ideal for straightforward queries where parameters are known and simple to set.
        // Method B: Execute with a lambda for custom PreparedStatement configuration. Offers flexibility for complex scenarios like setting null values, batch operations, or array parameters.

        try {
            DatabaseUtil db = new DatabaseUtil(DB_URL, DB_USER, DB_PASSWORD);

            // Method A
            db.execute(
                    "INSERT INTO contact_info(email, phone_number, first_name, last_name) VALUES (?, ?, ?, ?)",
                    "dd.prodev@gmail.com", "+37367773888", "Dumitru", "Diacenco"
            );

            // Method B
            db.execute(
                    "UPDATE \"user\" SET status = CAST(? AS user_status), contact_info_id = ? WHERE id = ?",
                    stmt -> {
                        try {
                            stmt.setString(1, UserStatus.SUSPENDED.name());
                            stmt.setNull(2, Types.INTEGER);
                            stmt.setLong(3, 1L);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            // Method B: batch with multiple cars
            Location loc = new Location(1L, "New York", "NY", "10001");
            List<Car> cars = List.of(
                    new Car(null, "DDP999", "Ford", "Fiesta", CarStatus.AVAILABLE, loc, 30.0, CarType.HATCHBACK, VerificationStatus.PENDING),
                    new Car(null, "DDP898", "Tesla", "Model 3", CarStatus.AVAILABLE, loc, 100.0, CarType.SEDAN, VerificationStatus.PENDING)
            );
            db.execute(
                    "INSERT INTO car(registration_number, make, model, status, location_id, price_per_day, type, verification_status) VALUES (?, ?, ?, CAST(? AS car_status), ?, ?, CAST(? AS car_type), CAST(? AS verification_status))",
                    stmt -> {
                        try {
                            for (Car c : cars) {
                                stmt.setString(1, c.getRegistrationNumber());
                                stmt.setString(2, c.getMake());
                                stmt.setString(3, c.getModel());
                                stmt.setString(4, c.getStatus().name());
                                stmt.setLong(5, c.getLocation().getId());
                                stmt.setDouble(6, c.getPrice());
                                stmt.setString(7, c.getType().name());
                                stmt.setString(8, c.getVerificationStatus().name());
                                stmt.addBatch();
                            }
                            stmt.executeBatch();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            // Method B: array parameter example
            db.execute(
                    "INSERT INTO dispute(booking_id, description, status, tags) VALUES (?, ?, ?, ?)",
                    stmt -> {
                        try {
                            stmt.setLong(1, 1L);
                            stmt.setString(2, "Late return");
                            stmt.setString(3, DisputeStatus.OPEN.name());
                            Array tags = stmt.getConnection().createArrayOf("text", new String[]{"late", "penalty"});
                            stmt.setArray(4, tags);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            System.out.println("All DatabaseUtil calls succeeded.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void demoSqlInjection() {
        String safeInput = "RENTER";
        String maliciousInput = "RENTER' OR '1'='1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("=== Vulnerable Statement ===");

            System.out.println("\ntest safeInput:");
            testStatement(conn, safeInput);
            System.out.println("\n" + "test maliciousInput:");
            testStatement(conn, maliciousInput);

            System.out.println("\n=== Secure PreparedStatement ===");
            System.out.println("\ntest safeInput:");
            testPreparedStatement(conn, safeInput);
            System.out.println("\n" + "test maliciousInput:");
            testPreparedStatement(conn, maliciousInput);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void testStatement(Connection conn, String input) {
        String sql = "SELECT u.id, c.email FROM \"user\" u JOIN contact_info c ON u.contact_info_id = c.id WHERE u.role = '" + input + "'";
        System.out.println("Query: " + sql);

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("User ID: " + rs.getInt("id") + ", Email: " + rs.getString("email"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void testPreparedStatement(Connection conn, String input) {
        String sql = "SELECT u.id, c.email FROM \"user\" u JOIN contact_info c ON u.contact_info_id = c.id WHERE u.role = ?";
        System.out.println("Query: " + sql + " [param: " + input + "]");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, input);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println("User ID: " + rs.getInt("id") + ", Email: " + rs.getString("email"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}