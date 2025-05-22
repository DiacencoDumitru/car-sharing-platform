package com.dynamiccarsharing.carsharing;

import java.sql.*;

public class App {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/app_db1";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "password";

    public static void main(String[] args) {
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
        String sql = "SELECT u.id, c.email FROM \"user\" u JOIN contact_info c ON u.contact_info_id = c.id WHERE u.role = ?::user_role";
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