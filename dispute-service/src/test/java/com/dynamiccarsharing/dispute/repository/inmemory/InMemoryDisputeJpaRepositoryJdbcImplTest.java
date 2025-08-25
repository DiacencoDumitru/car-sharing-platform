package com.dynamiccarsharing.dispute.repository.inmemory;

import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.filter.DisputeFilter;
import com.dynamiccarsharing.dispute.model.Dispute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryDisputeJpaRepositoryJdbcImplTest {

    private InMemoryDisputeRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDisputeRepositoryJdbcImpl();
    }

    private Dispute createTestDispute(Long id, DisputeStatus status, Long bookingId, Long creationUserId) {
        LocalDateTime now = LocalDateTime.now();
        return Dispute.builder()
                .id(id)
                .bookingId(bookingId)
                .creationUserId(creationUserId)
                .description("Issue with booking " + bookingId)
                .status(status)
                .createdAt(now)
                .resolvedAt(status == DisputeStatus.RESOLVED ? now : null)
                .build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnDispute() {
            Dispute dispute = createTestDispute(1L, DisputeStatus.OPEN, 10L, 20L);
            Dispute savedDispute = repository.save(dispute);
            assertEquals(dispute, savedDispute);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingDispute_shouldChangeStatus() {
            Dispute original = createTestDispute(1L, DisputeStatus.OPEN, 10L, 20L);
            repository.save(original);

            original.setStatus(DisputeStatus.RESOLVED);
            repository.save(original);

            Optional<Dispute> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals(DisputeStatus.RESOLVED, found.get().getStatus());
        }

        @Test
        void findById_withExistingId_shouldReturnDispute() {
            Dispute dispute = createTestDispute(1L, DisputeStatus.OPEN, 10L, 20L);
            repository.save(dispute);
            Optional<Dispute> foundDispute = repository.findById(1L);
            assertTrue(foundDispute.isPresent());
            assertEquals(dispute, foundDispute.get());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveDispute() {
            Dispute dispute = createTestDispute(1L, DisputeStatus.OPEN, 10L, 20L);
            repository.save(dispute);
            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultipleDisputes_shouldReturnAllDisputes() {
            Dispute dispute1 = createTestDispute(1L, DisputeStatus.OPEN, 10L, 20L);
            Dispute dispute2 = createTestDispute(2L, DisputeStatus.RESOLVED, 11L, 21L);
            repository.save(dispute1);
            repository.save(dispute2);

            Iterable<Dispute> disputesIterable = repository.findAll();

            List<Dispute> disputeList = new ArrayList<>();
            disputesIterable.forEach(disputeList::add);

            assertEquals(2, disputeList.size());
            assertTrue(disputeList.contains(dispute1));
            assertTrue(disputeList.contains(dispute2));
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        @DisplayName("Should find dispute by booking ID")
        void findByBookingId_withMatchingDispute_shouldReturnDispute() {
            Dispute dispute1 = createTestDispute(1L, DisputeStatus.OPEN, 10L, 20L);
            Dispute dispute2 = createTestDispute(2L, DisputeStatus.RESOLVED, 11L, 21L);
            repository.save(dispute1);
            repository.save(dispute2);

            Optional<Dispute> found = repository.findByBookingId(10L);
            assertTrue(found.isPresent());
            assertEquals(dispute1, found.get());
        }

        @Test
        @DisplayName("Should find disputes by status")
        void findByStatus_withMatchingDisputes_shouldReturnMatchingDisputes() {
            Dispute dispute1 = createTestDispute(1L, DisputeStatus.OPEN, 10L, 20L);
            Dispute dispute2 = createTestDispute(2L, DisputeStatus.RESOLVED, 11L, 21L);
            Dispute dispute3 = createTestDispute(3L, DisputeStatus.OPEN, 12L, 22L);
            repository.save(dispute1);
            repository.save(dispute2);
            repository.save(dispute3);

            List<Dispute> openDisputes = repository.findByStatus(DisputeStatus.OPEN);
            assertEquals(2, openDisputes.size());
            assertTrue(openDisputes.contains(dispute1));
            assertTrue(openDisputes.contains(dispute3));
        }

        @Test
        @DisplayName("Should find disputes by filter")
        void findByFilter_withMatchingDisputes_shouldReturnMatchingDisputes() {
            Dispute dispute1 = createTestDispute(1L, DisputeStatus.OPEN, 10L, 20L);
            Dispute dispute2 = createTestDispute(2L, DisputeStatus.RESOLVED, 11L, 21L);
            repository.save(dispute1);
            repository.save(dispute2);

            DisputeFilter filter = DisputeFilter.ofStatus(DisputeStatus.OPEN);
            List<Dispute> filteredDisputes = repository.findByFilter(filter);

            assertEquals(1, filteredDisputes.size());
            assertTrue(filteredDisputes.contains(dispute1));
        }
    }
}