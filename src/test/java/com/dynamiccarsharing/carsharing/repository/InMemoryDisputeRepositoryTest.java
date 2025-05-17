package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.filter.DisputeFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryDisputeRepositoryTest {

    private InMemoryDisputeRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDisputeRepository();
        repository.findAll().forEach(dispute -> repository.deleteById(dispute.getId()));
    }

    private Dispute createTestDispute(Long id, DisputeStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new Dispute(id, 1L, 2L, "Issue", status, now, status == DisputeStatus.RESOLVED ? now : null);
    }

    @Test
    void save_shouldSaveAndReturnDispute() {
        Dispute dispute = createTestDispute(1L, DisputeStatus.OPEN);

        Dispute savedDispute = repository.save(dispute);

        assertEquals(dispute, savedDispute);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(dispute, repository.findById(1L).get());
    }

    @Test
    void save_withNullDispute_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnDispute() {
        Dispute dispute = createTestDispute(1L, DisputeStatus.OPEN);
        repository.save(dispute);

        Optional<Dispute> foundDispute = repository.findById(1L);

        assertTrue(foundDispute.isPresent());
        assertEquals(dispute, foundDispute.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<Dispute> foundDispute = repository.findById(1L);

        assertFalse(foundDispute.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveDispute() {
        Dispute dispute = createTestDispute(1L, DisputeStatus.OPEN);
        repository.save(dispute);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultipleDisputes_shouldReturnAllDisputes() {
        Dispute dispute1 = createTestDispute(1L, DisputeStatus.OPEN);
        Dispute dispute2 = createTestDispute(2L, DisputeStatus.RESOLVED);
        repository.save(dispute1);
        repository.save(dispute2);

        Iterable<Dispute> disputes = repository.findAll();
        List<Dispute> disputeList = new ArrayList<>();
        disputes.forEach(disputeList::add);

        assertEquals(2, disputeList.size());
        assertTrue(disputeList.contains(dispute1));
        assertTrue(disputeList.contains(dispute2));
    }

    @Test
    void findAll_withSingleDispute_shouldReturnSingleDispute() {
        Dispute dispute = createTestDispute(1L, DisputeStatus.OPEN);
        repository.save(dispute);

        Iterable<Dispute> disputes = repository.findAll();
        List<Dispute> disputeList = new ArrayList<>();
        disputes.forEach(disputeList::add);

        assertEquals(1, disputeList.size());
        assertEquals(dispute, disputeList.get(0));
    }

    @Test
    void findAll_withNoDisputes_shouldReturnEmptyIterable() {
        Iterable<Dispute> disputes = repository.findAll();
        List<Dispute> disputeList = new ArrayList<>();
        disputes.forEach(disputeList::add);

        assertEquals(0, disputeList.size());
    }

    @Test
    void findByFilter_withMatchingDisputes_shouldReturnMatchingDisputes() {
        Dispute dispute1 = createTestDispute(1L, DisputeStatus.OPEN);
        Dispute dispute2 = createTestDispute(2L, DisputeStatus.RESOLVED);
        Dispute dispute3 = createTestDispute(3L, DisputeStatus.OPEN);
        repository.save(dispute1);
        repository.save(dispute2);
        repository.save(dispute3);
        DisputeFilter filter = mock(DisputeFilter.class);
        when(filter.test(any(Dispute.class))).thenAnswer(invocation -> {
            Dispute dispute = invocation.getArgument(0);
            return dispute.getStatus() == DisputeStatus.OPEN;
        });

        List<Dispute> filteredDisputes = repository.findByFilter(filter);

        assertEquals(2, filteredDisputes.size());
        assertTrue(filteredDisputes.contains(dispute1));
        assertTrue(filteredDisputes.contains(dispute3));
        assertFalse(filteredDisputes.contains(dispute2));
    }

    @Test
    void findByFilter_withNoMatchingDisputes_shouldReturnEmptyList() {
        Dispute dispute = createTestDispute(1L, DisputeStatus.OPEN);
        repository.save(dispute);
        DisputeFilter filter = mock(DisputeFilter.class);
        when(filter.test(any(Dispute.class))).thenReturn(false);

        List<Dispute> filteredDisputes = repository.findByFilter(filter);

        assertEquals(0, filteredDisputes.size());
    }
}