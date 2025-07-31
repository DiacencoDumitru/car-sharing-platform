package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.DisputeFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("jdbc")
class DisputeDaoTest extends BaseDaoTest {
    @Autowired
    private DisputeDao disputeDao;

    private Booking booking1;
    private Booking booking2;
    private User testUser;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Location location = createLocation("Test City", "TS", "12345");
        ContactInfo contactInfo = createContactInfo("test@example.com", "+123456789", "Test", "User");
        this.testUser = createUser(contactInfo, UserRole.RENTER, UserStatus.ACTIVE);
        Car testCar = createCar("TEST123", "Toyota", "Camry", location);
        this.booking1 = createBooking(testUser, testCar, location, TransactionStatus.COMPLETED);
        this.booking2 = createBooking(testUser, testCar, location, TransactionStatus.COMPLETED);
    }

    private Dispute createUnsavedDispute(String description, DisputeStatus status, Booking booking) {
        return Dispute.builder()
                .booking(booking)
                .creationUser(testUser)
                .description(description)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save new dispute successfully")
        void save_newValidDispute_shouldSave() {
            Dispute dispute = createUnsavedDispute("Test dispute", DisputeStatus.OPEN, booking1);
            Dispute saved = disputeDao.save(dispute);
            assertNotNull(saved.getId());
            assertEquals(dispute.getBooking().getId(), saved.getBooking().getId());
        }

        @Test
        @DisplayName("Should update existing dispute")
        void save_existingDispute_shouldUpdate() {
            Dispute original = disputeDao.save(createUnsavedDispute("Original description", DisputeStatus.OPEN, booking1));
            Dispute toUpdate = original.withStatus(DisputeStatus.RESOLVED).withResolvedAt(LocalDateTime.now());
            Dispute updated = disputeDao.save(toUpdate);
            assertEquals(original.getId(), updated.getId());
            assertEquals(DisputeStatus.RESOLVED, updated.getStatus());
            assertNotNull(updated.getResolvedAt());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find dispute by valid ID")
        void findById_validId_shouldReturnDispute() {
            Dispute saved = disputeDao.save(createUnsavedDispute("Find Me", DisputeStatus.OPEN, booking1));
            Optional<Dispute> found = disputeDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<Dispute> found = disputeDao.findById(999L);
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should find all disputes")
        void findAll_withData_shouldReturnAll() {
            disputeDao.save(createUnsavedDispute("Dispute 1", DisputeStatus.OPEN, booking1));
            disputeDao.save(createUnsavedDispute("Dispute 2", DisputeStatus.RESOLVED, booking2));
            List<Dispute> results = (List<Dispute>) disputeDao.findAll();
            assertEquals(2, results.size());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete dispute by ID")
        void deleteById_validId_shouldDelete() {
            Dispute saved = disputeDao.save(createUnsavedDispute("To Be Deleted", DisputeStatus.OPEN, booking1));
            disputeDao.deleteById(saved.getId());
            Optional<Dispute> found = disputeDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @Test
        @DisplayName("Should find disputes by status filter")
        void findByFilter_byStatus_shouldReturnMatching() throws SQLException {
            disputeDao.save(createUnsavedDispute("Open dispute", DisputeStatus.OPEN, booking1));
            disputeDao.save(createUnsavedDispute("Resolved dispute", DisputeStatus.RESOLVED, booking2));

            DisputeFilter filter = DisputeFilter.ofStatus(DisputeStatus.RESOLVED);
            List<Dispute> results = disputeDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(DisputeStatus.RESOLVED, results.get(0).getStatus());
        }
    }
}