package com.dynamiccarsharing.dispute.dao;

import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.filter.DisputeFilter;
import com.dynamiccarsharing.dispute.model.Dispute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class DisputeDaoTest extends DisputeBaseDaoTest {
    @Autowired
    private DisputeDao disputeDao;

    private Long bookingId1;
    private Long bookingId2;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        this.testUserId = 100L;
        this.bookingId1 = 1L;
        this.bookingId2 = 2L;
    }

    private Dispute createUnsavedDispute(String description, DisputeStatus status, Long bookingId, Long creationUserId) {
        return Dispute.builder()
                .bookingId(bookingId)
                .creationUserId(creationUserId)
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
            Dispute dispute = createUnsavedDispute("Test dispute", DisputeStatus.OPEN, bookingId1, testUserId);
            Dispute saved = disputeDao.save(dispute);
            assertNotNull(saved.getId());
            assertEquals(dispute.getBookingId(), saved.getBookingId());
        }

        @Test
        @DisplayName("Should update existing dispute")
        void save_existingDispute_shouldUpdate() {
            Dispute original = disputeDao.save(createUnsavedDispute("Original description", DisputeStatus.OPEN, bookingId1, testUserId));

            original.setStatus(DisputeStatus.RESOLVED);
            original.setResolvedAt(LocalDateTime.now());

            Dispute updated = disputeDao.save(original);
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
            Dispute saved = disputeDao.save(createUnsavedDispute("Find Me", DisputeStatus.OPEN, bookingId1, testUserId));
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
            disputeDao.save(createUnsavedDispute("Dispute 1", DisputeStatus.OPEN, bookingId1, testUserId));
            disputeDao.save(createUnsavedDispute("Dispute 2", DisputeStatus.RESOLVED, bookingId2, testUserId));
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
            Dispute saved = disputeDao.save(createUnsavedDispute("To Be Deleted", DisputeStatus.OPEN, bookingId1, testUserId));
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
            disputeDao.save(createUnsavedDispute("Open dispute", DisputeStatus.OPEN, bookingId1, testUserId));
            disputeDao.save(createUnsavedDispute("Resolved dispute", DisputeStatus.RESOLVED, bookingId2, testUserId));

            DisputeFilter filter = DisputeFilter.ofStatus(DisputeStatus.RESOLVED);
            List<Dispute> results = disputeDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(DisputeStatus.RESOLVED, results.get(0).getStatus());
        }
    }
}