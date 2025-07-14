package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    private DisputeService disputeService;

    @BeforeEach
    void setUp() {
        disputeService = new DisputeService(disputeRepository);
    }

    private Dispute createTestDispute(UUID id, DisputeStatus status) {
        return Dispute.builder()
                .id(id)
                .booking(Booking.builder().id(UUID.randomUUID()).build())
                .creationUser(User.builder().id(UUID.randomUUID()).build())
                .description("Test description")
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnDispute() {
        Dispute disputeToSave = createTestDispute(null, DisputeStatus.OPEN);
        Dispute savedDispute = createTestDispute(UUID.randomUUID(), DisputeStatus.OPEN);
        when(disputeRepository.save(disputeToSave)).thenReturn(savedDispute);

        Dispute result = disputeService.save(disputeToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        verify(disputeRepository).save(disputeToSave);
    }

    @Test
    void findById_whenDisputeExists_shouldReturnOptionalOfDispute() {
        UUID disputeId = UUID.randomUUID();
        Dispute testDispute = createTestDispute(disputeId, DisputeStatus.OPEN);
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(testDispute));

        Optional<Dispute> result = disputeService.findById(disputeId);

        assertTrue(result.isPresent());
        assertEquals(disputeId, result.get().getId());
    }

    @Test
    void findAll_shouldReturnListOfDisputes() {
        when(disputeRepository.findAll()).thenReturn(List.of(createTestDispute(UUID.randomUUID(), DisputeStatus.OPEN)));

        List<Dispute> results = disputeService.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void deleteById_whenDisputeExists_shouldSucceed() {
        UUID disputeId = UUID.randomUUID();
        when(disputeRepository.existsById(disputeId)).thenReturn(true);
        doNothing().when(disputeRepository).deleteById(disputeId);

        disputeService.deleteById(disputeId);

        verify(disputeRepository).deleteById(disputeId);
    }

    @Test
    void deleteById_whenDisputeDoesNotExist_shouldThrowDisputeNotFoundException() {
        UUID disputeId = UUID.randomUUID();
        when(disputeRepository.existsById(disputeId)).thenReturn(false);

        assertThrows(DisputeNotFoundException.class, () -> {
            disputeService.deleteById(disputeId);
        });
    }

    @Test
    void findDisputesByStatus_shouldCallRepository() {
        DisputeStatus status = DisputeStatus.OPEN;
        when(disputeRepository.findByStatus(status)).thenReturn(List.of(createTestDispute(UUID.randomUUID(), status)));

        disputeService.findDisputesByStatus(status);

        verify(disputeRepository).findByStatus(status);
    }

    @Test
    void findDisputeByBookingId_shouldCallRepository() {
        UUID bookingId = UUID.randomUUID();
        when(disputeRepository.findByBookingId(bookingId)).thenReturn(Optional.of(createTestDispute(UUID.randomUUID(), DisputeStatus.OPEN)));

        disputeService.findDisputeByBookingId(bookingId);

        verify(disputeRepository).findByBookingId(bookingId);
    }

    @Test
    void resolveDispute_withOpenDispute_shouldSucceedAndSetResolvedAt() {
        UUID disputeId = UUID.randomUUID();
        Dispute openDispute = createTestDispute(disputeId, DisputeStatus.OPEN);
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(openDispute));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dispute resolvedDispute = disputeService.resolveDispute(disputeId);

        assertEquals(DisputeStatus.RESOLVED, resolvedDispute.getStatus());
        assertNotNull(resolvedDispute.getResolvedAt());
        verify(disputeRepository).save(any(Dispute.class));
    }

    @Test
    void resolveDispute_withResolvedDispute_shouldThrowInvalidDisputeStatusException() {
        UUID disputeId = UUID.randomUUID();
        Dispute resolvedDispute = createTestDispute(disputeId, DisputeStatus.RESOLVED);
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(resolvedDispute));

        assertThrows(InvalidDisputeStatusException.class, () -> {
            disputeService.resolveDispute(disputeId);
        });
        verify(disputeRepository, never()).save(any());
    }

    @Test
    void searchDisputes_withCriteria_shouldCallRepositoryWithSpecification() {
        UUID bookingId = UUID.randomUUID();
        when(disputeRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestDispute(UUID.randomUUID(), DisputeStatus.OPEN)));

        List<Dispute> results = disputeService.searchDisputes(bookingId, null);

        assertFalse(results.isEmpty());
        verify(disputeRepository, times(1)).findAll(any(Specification.class));
    }
}