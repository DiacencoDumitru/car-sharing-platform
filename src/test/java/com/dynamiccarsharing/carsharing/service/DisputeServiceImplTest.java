package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jpa.DisputeJpaRepository;
import com.dynamiccarsharing.carsharing.dto.criteria.DisputeSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceImplTest {

    @Mock
    private DisputeJpaRepository disputeJpaRepository;

    private DisputeServiceImpl disputeService;

    @BeforeEach
    void setUp() {
        disputeService = new DisputeServiceImpl(disputeJpaRepository);
    }

    private Dispute createTestDispute(Long id, DisputeStatus status) {
        return Dispute.builder()
                .id(id)
                .booking(Booking.builder().id(1L).build())
                .creationUser(User.builder().id(1L).build())
                .description("Test description")
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnDispute() {
        Dispute disputeToSave = createTestDispute(null, DisputeStatus.OPEN);
        Dispute savedDispute = createTestDispute(1L, DisputeStatus.OPEN);
        when(disputeJpaRepository.save(disputeToSave)).thenReturn(savedDispute);

        Dispute result = disputeService.save(disputeToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        verify(disputeJpaRepository).save(disputeToSave);
    }

    @Test
    void findById_whenDisputeExists_shouldReturnOptionalOfDispute() {
        Long disputeId = 1L;
        Dispute testDispute = createTestDispute(disputeId, DisputeStatus.OPEN);
        when(disputeJpaRepository.findById(disputeId)).thenReturn(Optional.of(testDispute));

        Optional<Dispute> result = disputeService.findById(disputeId);

        assertTrue(result.isPresent());
        assertEquals(disputeId, result.get().getId());
    }

    @Test
    void deleteById_whenDisputeExists_shouldSucceed() {
        Long disputeId = 1L;
        when(disputeJpaRepository.findById(disputeId)).thenReturn(Optional.of(createTestDispute(disputeId, DisputeStatus.OPEN)));
        doNothing().when(disputeJpaRepository).deleteById(disputeId);

        disputeService.deleteById(disputeId);

        verify(disputeJpaRepository).deleteById(disputeId);
    }

    @Test
    void deleteById_whenDisputeDoesNotExist_shouldThrowDisputeNotFoundException() {
        Long disputeId = 1L;
        when(disputeJpaRepository.findById(disputeId)).thenReturn(Optional.empty());

        assertThrows(DisputeNotFoundException.class, () -> disputeService.deleteById(disputeId));
    }

    @Test
    void resolveDispute_withOpenDispute_shouldSucceedAndSetStatusToResolved() {
        Long disputeId = 1L;
        Dispute openDispute = createTestDispute(disputeId, DisputeStatus.OPEN);
        when(disputeJpaRepository.findById(disputeId)).thenReturn(Optional.of(openDispute));
        when(disputeJpaRepository.save(any(Dispute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Dispute resolvedDispute = disputeService.resolveDispute(disputeId);

        assertEquals(DisputeStatus.RESOLVED, resolvedDispute.getStatus());
        verify(disputeJpaRepository).save(any(Dispute.class));
    }

    @Test
    void resolveDispute_withResolvedDispute_shouldThrowInvalidDisputeStatusException() {
        Long disputeId = 1L;
        Dispute resolvedDispute = createTestDispute(disputeId, DisputeStatus.RESOLVED);
        when(disputeJpaRepository.findById(disputeId)).thenReturn(Optional.of(resolvedDispute));

        assertThrows(IllegalStateException.class, () -> disputeService.resolveDispute(disputeId));
        verify(disputeJpaRepository, never()).save(any());
    }

    @Test
    void searchDisputes_withCriteria_shouldCallRepositoryWithSpecification() throws SQLException {
        Long bookingId = 1L;
        DisputeSearchCriteria criteria = DisputeSearchCriteria.builder().bookingId(bookingId).build();
        when(disputeJpaRepository.findByFilter(any(Filter.class))).thenReturn(List.of(createTestDispute(1L, DisputeStatus.OPEN)));

        List<Dispute> results = disputeService.searchDisputes(criteria);

        assertFalse(results.isEmpty());
        verify(disputeJpaRepository, times(1)).findByFilter(any(Filter.class));
    }
}