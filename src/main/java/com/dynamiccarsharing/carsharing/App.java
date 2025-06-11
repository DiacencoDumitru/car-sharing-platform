package com.dynamiccarsharing.carsharing;

import com.dynamiccarsharing.carsharing.util.DatabaseUtil;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class App {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/dynamiccarsharing_db";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "password";
    private static final int NUM_THREADS = 20;
    private static final int SLEEP_MILLISECONDS = 1000;

    public static void main(String[] args) throws InterruptedException {
        // --- Test with a Single Connection (Cached) ---
        System.out.println("--- Running with a single-connection DataSource ---");

        DataSource singleConnectionDs = createSingleConnectionDataSource();
        DatabaseUtil dbUtilWithoutPool = new DatabaseUtil(singleConnectionDs);
        runTest(dbUtilWithoutPool.getDataSource());


        System.out.println("\n---------------------------------------------\n");

        // --- Test with HikariCP Connection Pool ---
        System.out.println("--- Running with HikariCP Connection Pool ---");

        DatabaseUtil dbUtilWithPool = new DatabaseUtil(DB_URL, DB_USER, DB_PASSWORD);
        runTest(dbUtilWithPool.getDataSource());
    }

    private static DataSource createSingleConnectionDataSource() {
        return new DataSource() {
            private Connection cachedConnection;

            @Override
            public synchronized Connection getConnection() throws SQLException {
                if (cachedConnection == null || cachedConnection.isClosed()) {
                    cachedConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                }
                return cachedConnection;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return getConnection();
            }

            @Override public PrintWriter getLogWriter() throws SQLException { throw new UnsupportedOperationException(); }
            @Override public void setLogWriter(PrintWriter out) throws SQLException { throw new UnsupportedOperationException(); }
            @Override public void setLoginTimeout(int seconds) throws SQLException { throw new UnsupportedOperationException(); }
            @Override public int getLoginTimeout() throws SQLException { throw new UnsupportedOperationException(); }
            @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
            @Override public <T> T unwrap(Class<T> iface) throws SQLException { throw new UnsupportedOperationException(); }
            @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
        };
    }

    private static void runTest(DataSource dataSource) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_THREADS; i++) {
            int threadNum = i + 1;
            executor.submit(() -> {
                System.out.println("Thread " + threadNum + " starting.");
                try (Connection connection = dataSource.getConnection();
                     PreparedStatement ps = connection.prepareStatement("SELECT 1")) {

                    System.out.println("Thread " + threadNum + " got connection.");
                    ps.execute();

                    System.out.println("Thread " + threadNum + " starting long task...");
                    Thread.sleep(SLEEP_MILLISECONDS);
                    System.out.println("Thread " + threadNum + " finished task.");

                } catch (SQLException | InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            });
        }

        executor.shutdown();
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            System.err.println("Threads did not finish in 30 seconds!");
            executor.shutdownNow();
        }

        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0;
        System.out.println("\nTotal execution time: " + duration + " seconds.");
    }
}