package com.dynamiccarsharing.carsharing;

import com.dynamiccarsharing.carsharing.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/dynamiccarsharing_db";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) throws SQLException, InterruptedException {
        DatabaseUtil dbUtil = new DatabaseUtil(DB_URL, DB_USER, DB_PASSWORD);

        setupInitialData(dbUtil);

        System.out.println("--- Part 1: ACID Consistency Demonstration ---");
        demonstrateConsistency(dbUtil);

        System.out.println("\n--- Part 2: Transaction Isolation Demonstration ---");
        demonstrateIsolationLevels(dbUtil);
    }

    private static void setupInitialData(DatabaseUtil dbUtil) {
        System.out.println("Setting up initial data for demos...");
        dbUtil.execute("INSERT INTO contact_infos (id, email, phone_number, first_name, last_name) VALUES (1, 'demo@user.com', '000', 'Demo', 'User') ON CONFLICT (id) DO NOTHING;");
        dbUtil.execute("INSERT INTO users (id, contact_info_id, role, status) VALUES (1, 1, 'RENTER', 'ACTIVE') ON CONFLICT (id) DO NOTHING;");
        dbUtil.execute("INSERT INTO locations (id, city, state, zip_code) VALUES (1, 'DemoCity', 'DemoState', '12345') ON CONFLICT (id) DO NOTHING;");
        dbUtil.execute("INSERT INTO cars (id, registration_number, make, model, status, location_id, price_per_day, type, verification_status) VALUES (1, 'DEMO-001', 'Test', 'Car', 'AVAILABLE', 1, 50.00, 'SEDAN', 'VERIFIED') ON CONFLICT (id) DO UPDATE SET status = 'AVAILABLE';");
        dbUtil.execute("INSERT INTO cars (id, registration_number, make, model, status, location_id, price_per_day, type, verification_status) VALUES (2, 'ISO-TEST', 'Test', 'Car', 'AVAILABLE', 1, 100.00, 'SEDAN', 'VERIFIED') ON CONFLICT (id) DO UPDATE SET price_per_day = 100.00;");
        System.out.println("Setup complete.");
    }

    public static void demonstrateConsistency(DatabaseUtil dbUtil) throws SQLException {
        System.out.println("\nRunning WITHOUT transaction...");
        try {
            // 1: Insert the booking directly. (this will auto-commit immediately)
            dbUtil.execute("INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id) VALUES (1, 1, NOW(), NOW() + INTERVAL '1 day', 'APPROVED', 1)");
            System.out.println("Step 1: Booking created.");

            // 2: Simulate a crash (before the car status is updated)
            throw new RuntimeException("CRASH! System failed before updating car status.");

        } catch (Exception e) {
            System.out.println("Step 2: " + e.getMessage());
        }

        checkBookingAndCarState(dbUtil, "Result WITHOUT transaction", 1L);

        dbUtil.execute("DELETE FROM bookings WHERE car_id = 1");


        System.out.println("\nRunning WITH transaction...");
        try (Connection connection = dbUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                // 1: Insert booking (within the transaction)
                try (PreparedStatement insertBooking = connection.prepareStatement("INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id) VALUES (1, 1, NOW(), NOW() + INTERVAL '1 day', 'APPROVED', 1)")) {
                    insertBooking.executeUpdate();
                    System.out.println("Step 1: Booking created within transaction.");
                }

                // 2: Simulate a crash (before the user is created)
                throw new RuntimeException("CRASH! System failed before updating car status.");

                // The following lines are never reached
                // try(PreparedStatement updateCar = connection.prepareStatement("UPDATE cars SET status = 'RENTED' WHERE id = 1")){
                //    updateCar.executeUpdate();
                // }
                // connection.commit();

            } catch (Exception e) {
                System.out.println("Step 2: " + e.getMessage());
                System.out.println("Rolling back transaction...");
                connection.rollback(); // atomically (undo all changes within the transaction)
            }
        }

        checkBookingAndCarState(dbUtil, "Result WITH transaction", 1L);
    }

    private static void checkBookingAndCarState(DatabaseUtil dbUtil, String context, long carId) {
        String carStatus = dbUtil.findOne("SELECT status FROM cars WHERE id = ?", rs -> rs.getString(1), carId);
        Long bookingCount = dbUtil.findOne("SELECT COUNT(*) FROM bookings WHERE car_id = ?", rs -> rs.getLong(1), carId);
        System.out.printf("[%s] Car Status: %s, Booking Count: %d\n", context, carStatus, bookingCount);
    }

    public static void demonstrateIsolationLevels(DatabaseUtil dbUtil) throws InterruptedException {
        final long carToUpdateId = 2L;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        System.out.println("\nRunning with default isolation (READ COMMITTED)...");
        dbUtil.execute("UPDATE cars SET price_per_day = 100.00 WHERE id = ?", carToUpdateId);

        executor.submit(() -> runUpdateTransaction(dbUtil, carToUpdateId));
        Thread.sleep(200);
        executor.submit(() -> runReadTransaction(dbUtil, Connection.TRANSACTION_READ_COMMITTED, "Default (Read Committed)", carToUpdateId));

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);


        System.out.println("\nRunning with low isolation (READ UNCOMMITTED)...");
        executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> runUpdateTransaction(dbUtil, carToUpdateId));
        Thread.sleep(200);
        executor.submit(() -> runReadTransaction(dbUtil, Connection.TRANSACTION_READ_UNCOMMITTED, "Low (Read Uncommitted)", carToUpdateId));

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    // Transaction A (Updates a price but rolls back)
    private static void runUpdateTransaction(DatabaseUtil dbUtil, long carId) {
        try (Connection connection = dbUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updatePrice = connection.prepareStatement("UPDATE cars SET price_per_day = 999.99 WHERE id = ?")) {
                updatePrice.setLong(1, carId);
                System.out.println("[Tx A] Updating price to 999.99 (but not committing)...");
                updatePrice.executeUpdate();
                Thread.sleep(1500);
            }
            System.out.println("[Tx A] Rolling back update.");
            connection.rollback();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Transaction B (Reads a price with a specific isolation level)
    private static void runReadTransaction(DatabaseUtil dbUtil, int isolationLevel, String context, long carId) {
        try (Connection connection = dbUtil.getConnection()) {
            connection.setTransactionIsolation(isolationLevel);
            try (PreparedStatement selectPrice = connection.prepareStatement("SELECT price_per_day FROM cars WHERE id = ?")) {
                selectPrice.setLong(1, carId);
                ResultSet rs = selectPrice.executeQuery();
                rs.next();
                double price = rs.getDouble(1);
                System.out.printf("[Tx B - %s] Read car price: %.2f\n", context, price);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
