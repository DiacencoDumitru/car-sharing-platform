package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.filter.DisputeFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;

class DisputeDaoTest extends BaseDaoTest {
    @Autowired
    private DisputeDao disputeDao;

    private Long bookingId;
    private Long userId;
    private Long carId;
    private Long locationId;


    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Long contactInfoId = createContactInfo("test@example.com", "+123456789", "Test", "User");
        this.userId = createUser(contactInfoId, "RENTER", "ACTIVE");
        this.locationId = createLocation("Test City", "TS", "12345");
        this.carId = createCar("TEST123", "Toyota", "Camry", locationId);
        this.bookingId = createBooking(userId, carId, locationId);
    }

    private Long createBooking(Long userId, Long carId, Long locationId) throws SQLException {
        try (Connection conn = databaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO bookings (renter_id, car_id, start_time, end_time, status, pickup_location_id) VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, carId);
            stmt.setTimestamp(3, Timestamp.valueOf("2025-01-01 10:00:00"));
            stmt.setTimestamp(4, Timestamp.valueOf("2025-01-02 10:00:00"));
            stmt.setString(5, "COMPLETED");
            stmt.setLong(6, locationId);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private Dispute createDispute(String description, DisputeStatus status, Long bookingId) {
        return new Dispute(null, bookingId, userId, description, status, LocalDateTime.now(), null);
    }

    private Dispute createResolvedDispute(Long bookingId) {
        LocalDateTime now = LocalDateTime.now();
        return new Dispute(null, bookingId, userId, "Resolved dispute", DisputeStatus.RESOLVED, now.minusDays(1), now);
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        @DisplayName("Should save new dispute successfully")
        void save_newValidDispute_shouldSave() {
            Dispute dispute = createDispute("Test dispute", DisputeStatus.OPEN, bookingId);

            Dispute saved = disputeDao.save(dispute);

            assertNotNull(saved.getId());
            assertEquals(dispute.getBookingId(), saved.getBookingId());
            assertEquals(dispute.getCreationUserId(), saved.getCreationUserId());
            assertEquals(dispute.getDescription(), saved.getDescription());
            assertEquals(dispute.getStatus(), saved.getStatus());
            assertNotNull(saved.getCreatedAt());
            assertNull(saved.getResolvedAt());
        }

        @Test
        @DisplayName("Should update existing dispute")
        void save_existingDispute_shouldUpdate() {
            Dispute original = disputeDao.save(createDispute("Original description", DisputeStatus.OPEN, bookingId));
            LocalDateTime resolvedTime = LocalDateTime.now();

            Dispute update = new Dispute(
                    original.getId(),
                    bookingId,
                    userId,
                    "Updated description",
                    DisputeStatus.RESOLVED,
                    original.getCreatedAt(),
                    resolvedTime
            );

            Dispute updated = disputeDao.save(update);

            assertEquals(original.getId(), updated.getId());
            assertEquals("Updated description", updated.getDescription());
            assertEquals(DisputeStatus.RESOLVED, updated.getStatus());
            assertEquals(resolvedTime, updated.getResolvedAt());
        }

        @Test
        @DisplayName("Should save dispute with resolved status and timestamp")
        void save_resolvedDispute_shouldSaveWithTimestamp() {
            Dispute dispute = createResolvedDispute(1L);

            Dispute saved = disputeDao.save(dispute);

            assertNotNull(saved.getId());
            assertEquals(DisputeStatus.RESOLVED, saved.getStatus());
            assertNotNull(saved.getResolvedAt());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find dispute by valid ID")
        void findById_validId_shouldReturnDispute() {
            Dispute saved = disputeDao.save(createDispute("Find by ID test", DisputeStatus.OPEN, bookingId));

            Optional<Dispute> found = disputeDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
            assertEquals(saved.getDescription(), found.get().getDescription());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<Dispute> found = disputeDao.findById(999L);

            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should return all disputes")
        void findAll_withData_shouldReturnAll() throws SQLException {

            Long secondBookingId = createBooking(userId, carId, locationId);

            disputeDao.save(createDispute("First dispute", DisputeStatus.OPEN, bookingId));
            disputeDao.save(createDispute("Second dispute", DisputeStatus.RESOLVED, secondBookingId));

            Iterable<Dispute> disputes = disputeDao.findAll();

            assertTrue(disputes.iterator().hasNext());
            long count = 0;
            for (Dispute dispute : disputes) {
                count++;
            }
            assertEquals(2, count);
        }

        @Test
        @DisplayName("Should return empty iterable when no disputes exist")
        void findAll_noData_shouldReturnEmpty() {
            Iterable<Dispute> disputes = disputeDao.findAll();

            assertFalse(disputes.iterator().hasNext());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        @DisplayName("Should delete dispute by ID")
        void deleteById_validId_shouldDelete() {
            Dispute saved = disputeDao.save(createDispute("To be deleted", DisputeStatus.OPEN, 1L));

            disputeDao.deleteById(saved.getId());

            Optional<Dispute> found = disputeDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should not throw exception for non-existent ID")
        void deleteById_nonExistentId_shouldNotThrow() {
            assertDoesNotThrow(() -> disputeDao.deleteById(999L));
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {

        @Test
        @DisplayName("Should find disputes by status filter")
        void findByFilter_statusFilter_shouldReturnMatching() throws SQLException {
            Long secondBookingId = createBooking(userId, carId, locationId);
            Long thirdBookingId = createBooking(userId, carId, locationId);

            disputeDao.save(createDispute("Open dispute 1", DisputeStatus.OPEN, secondBookingId));
            disputeDao.save(createDispute("Open dispute 2", DisputeStatus.OPEN, thirdBookingId));
            disputeDao.save(createResolvedDispute(bookingId));

            DisputeFilter filter = DisputeFilter.ofStatus(DisputeStatus.OPEN);

            List<Dispute> disputes = disputeDao.findByFilter(filter);

            assertEquals(2, disputes.size());
            disputes.forEach(dispute -> assertEquals(DisputeStatus.OPEN, dispute.getStatus()));
        }

        @Test
        @DisplayName("Should find disputes by booking ID filter")
        void findByFilter_bookingIdFilter_shouldReturnMatching() throws SQLException {
            disputeDao.save(createDispute("Dispute for booking", DisputeStatus.OPEN, bookingId));

            Long anotherBookingId = createBooking(userId, carId, locationId);
            Dispute anotherDispute = new Dispute(null, anotherBookingId, userId,
                    "Another dispute", DisputeStatus.OPEN, LocalDateTime.now(), null);
            disputeDao.save(anotherDispute);

            DisputeFilter filter = DisputeFilter.ofBookingId(bookingId);

            List<Dispute> disputes = disputeDao.findByFilter(filter);

            assertEquals(1, disputes.size());
            assertEquals(bookingId, disputes.get(0).getBookingId());
        }

        @Test
        @DisplayName("Should return empty list for non-matching filter")
        void findByFilter_nonMatchingFilter_shouldReturnEmpty() throws SQLException {
            disputeDao.save(createDispute("Open dispute", DisputeStatus.OPEN, bookingId));

            DisputeFilter filter = DisputeFilter.ofStatus(DisputeStatus.RESOLVED);

            List<Dispute> disputes = disputeDao.findByFilter(filter);

            assertTrue(disputes.isEmpty());
        }

        @Test
        @DisplayName("Should handle null filter")
        void findByFilter_nullFilter_shouldReturnAll() throws SQLException {
            disputeDao.save(createDispute("First dispute", DisputeStatus.OPEN, bookingId));

            Long secondBookingId = createBooking(userId, carId, locationId);
            disputeDao.save(createResolvedDispute(secondBookingId));

            List<Dispute> disputes = disputeDao.findByFilter(null);

            assertEquals(2, disputes.size());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle concurrent modifications gracefully")
        void save_concurrentModification_shouldHandle() {
            Dispute dispute = disputeDao.save(createDispute("Concurrent test", DisputeStatus.OPEN, 1L));

            try (Connection conn = databaseUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement( "UPDATE disputes SET description = ? WHERE id = ?")) {
                stmt.setString(1, "Modified externally");
                stmt.setLong(2, dispute.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                fail("Setup failed: " + e.getMessage());
            }

            Dispute updated = new Dispute(dispute.getId(), dispute.getBookingId(), dispute.getCreationUserId(), "Updated by DAO", DisputeStatus.RESOLVED, dispute.getCreatedAt(), LocalDateTime.now());

            assertDoesNotThrow(() -> {
                Dispute result = disputeDao.save(updated);
                assertEquals("Updated by DAO", result.getDescription());
            });
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle very long descriptions")
        void save_longDescription_shouldTruncateOrHandle() {
            String longDescription = "A".repeat(255);
            Dispute dispute = createDispute(longDescription, DisputeStatus.OPEN, bookingId);

            assertDoesNotThrow(() -> {
                Dispute saved = disputeDao.save(dispute);
                assertEquals(longDescription, saved.getDescription());
            });
        }

        @Test
        @DisplayName("Should handle description exactly at character limit")
        void save_descriptionAtLimit_shouldSave() {
            String description = "B".repeat(255);
            Dispute dispute = createDispute(description, DisputeStatus.OPEN, 1L);

            assertDoesNotThrow(() -> {
                Dispute saved = disputeDao.save(dispute);
                assertEquals(description, saved.getDescription());
            });
        }

        @Test
        @DisplayName("Should handle multiple disputes for same booking")
        void save_multipleDisputesPerBooking_shouldHandleConstraints() {
            disputeDao.save(createDispute("First dispute", DisputeStatus.OPEN, bookingId));

            Dispute second = createDispute("Second dispute", DisputeStatus.OPEN, bookingId);

            assertThrows(RuntimeException.class, () -> {
                disputeDao.save(second);
            }, "Should throw exception due to unique constraint on booking_id");
        }
    }
}