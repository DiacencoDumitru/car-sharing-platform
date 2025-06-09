package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    DisputeRepository disputeRepository;

    private DisputeService disputeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(disputeRepository);
        disputeService = new DisputeService(disputeRepository);
    }

    private Dispute createTestDispute(String disputeDescription, DisputeStatus disputeStatus) {
        return new Dispute(1L, 1L, 2L,  disputeDescription == null ? "Test dispute" : disputeDescription, disputeStatus == null ? DisputeStatus.OPEN : disputeStatus, LocalDateTime.now(), null);
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameDispute()  {
        Dispute dispute = createTestDispute(null, null);
        when(disputeRepository.save(dispute)).thenReturn(dispute);

        Dispute savedDispute = disputeService.save(dispute);

        verify(disputeRepository, times(1)).save(dispute);
        assertSame(dispute, savedDispute);
        assertEquals(dispute.getId(), savedDispute.getId());
        assertEquals(dispute.getBookingId(), savedDispute.getBookingId());
        assertEquals(dispute.getCreationUserId(), savedDispute.getCreationUserId());
        assertEquals(dispute.getDescription(), savedDispute.getDescription());
        assertEquals(dispute.getStatus(), savedDispute.getStatus());
        assertEquals(dispute.getCreatedAt(), savedDispute.getCreatedAt());
        assertEquals(dispute.getResolvedAt(), savedDispute.getResolvedAt());
    }

    @Test
    void save_whenDisputeIsNull_shouldThrowException()  {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> disputeService.save(null));

        assertEquals("Dispute must be non-null", exception.getMessage());
        verify(disputeRepository, never()).save(any());
    }

    @Test
    void findById_whenDisputeIsPresent_shouldReturnDispute() {
        Dispute dispute = createTestDispute(null, null);
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(dispute));

        Optional<Dispute> foundDispute = disputeService.findById(1L);

        verify(disputeRepository, times(1)).findById(1L);
        assertTrue(foundDispute.isPresent());
        assertSame(dispute, foundDispute.get());
        assertEquals(dispute.getId(), foundDispute.get().getId());
        assertEquals(dispute.getBookingId(), foundDispute.get().getBookingId());
        assertEquals(dispute.getStatus(), foundDispute.get().getStatus());
    }

    @Test
    void findById_whenDisputeNotFound_shouldReturnEmpty() {
        when(disputeRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Dispute> foundDispute = disputeService.findById(1L);

        verify(disputeRepository, times(1)).findById(1L);
        assertFalse(foundDispute.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> disputeService.findById(-1L));

        assertEquals("Dispute ID must be non-negative", exception.getMessage());
        verify(disputeRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteDispute() {
        disputeService.deleteById(1L);

        verify(disputeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> disputeService.deleteById(-1L));

        assertEquals("Dispute ID must be non-negative", exception.getMessage());
        verify(disputeRepository, never()).deleteById(any());
    }

    @Test
    void findAll_withMultipleDisputes_shouldReturnAllDisputes() {
        Dispute dispute1 = createTestDispute( null, null);
        Dispute dispute2 = new Dispute(2L, 2L, 3L, "Another dispute", DisputeStatus.OPEN, LocalDateTime.now().minusHours(1), null);
        List<Dispute> disputes = Arrays.asList(dispute1, dispute2);
        when(disputeRepository.findAll()).thenReturn(disputes);

        Iterable<Dispute> result = disputeService.findAll();

        verify(disputeRepository, times(1)).findAll();
        List<Dispute> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(dispute1));
        assertTrue(resultList.contains(dispute2));
        assertEquals(dispute1.getId(), resultList.get(0).getId());
        assertEquals(dispute1.getBookingId(), resultList.get(0).getBookingId());
        assertEquals(dispute1.getStatus(), resultList.get(0).getStatus());
    }

    @Test
    void findAll_withSingleDispute_shouldReturnSingleDispute() {
        Dispute dispute = createTestDispute(null, null);
        List<Dispute> disputes = Collections.singletonList(dispute);
        when(disputeRepository.findAll()).thenReturn(disputes);

        Iterable<Dispute> result = disputeService.findAll();

        verify(disputeRepository, times(1)).findAll();
        List<Dispute> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(1, resultList.size());
        assertSame(dispute, resultList.get(0));
        assertEquals(dispute.getId(), resultList.get(0).getId());
        assertEquals(dispute.getBookingId(), resultList.get(0).getBookingId());
        assertEquals(dispute.getStatus(), resultList.get(0).getStatus());
    }

    @Test
    void findAll_withNoDisputes_shouldReturnEmptyIterable() {
        List<Dispute> disputes = Collections.emptyList();
        when(disputeRepository.findAll()).thenReturn(disputes);

        Iterable<Dispute> result = disputeService.findAll();

        verify(disputeRepository, times(1)).findAll();
        List<Dispute> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(0, resultList.size());
    }

    @Test
    void findDisputesByStatus_withValidStatus_shouldReturnDisputes() throws SQLException {
        Dispute dispute = createTestDispute( null, DisputeStatus.OPEN);
        List<Dispute> disputes = List.of(dispute);
        when(disputeRepository.findByFilter(argThat(filter -> filter != null && filter.test(dispute) && dispute.getStatus().equals(DisputeStatus.OPEN)))).thenReturn(disputes);

        List<Dispute> result = disputeService.findDisputesByStatus(DisputeStatus.OPEN);

        assertEquals(1, result.size());
        assertEquals(dispute, result.get(0));
        verify(disputeRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(dispute) && dispute.getStatus().equals(DisputeStatus.OPEN)));
    }

    @Test
    void findDisputesByBookingId_withValidId_shouldReturnDisputes() throws SQLException {
        Dispute dispute = createTestDispute( null, DisputeStatus.OPEN);
        List<Dispute> disputes = List.of(dispute);
        when(disputeRepository.findByFilter(argThat(filter -> filter != null && filter.test(dispute) && dispute.getBookingId().equals(1L)))).thenReturn(disputes);

        List<Dispute> result = disputeService.findDisputesByBookingId(1L);

        assertEquals(1, result.size());
        assertEquals(dispute, result.get(0));
        verify(disputeRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(dispute) && dispute.getBookingId().equals(1L)));
    }

    @Test
    void resolveDispute_shouldResolveOpenDisputeAndSetResolvedAt()  {
        Dispute dispute = createTestDispute( "Some description", DisputeStatus.OPEN);
        Dispute resolvedDispute = dispute.withStatus(DisputeStatus.RESOLVED).withResolvedAt(LocalDateTime.now());
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(dispute));
        doReturn(resolvedDispute).when(disputeRepository).save(any(Dispute.class));

        Dispute result = disputeService.resolveDispute(1L);

        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, times(1)).save(any(Dispute.class));
        assertEquals(DisputeStatus.RESOLVED, result.getStatus());
        assertNotNull(result.getResolvedAt());
    }

    @Test
    void resolveDispute_withInvalidStatus_shouldThrowException()  {
        Dispute dispute = createTestDispute( "Some description", DisputeStatus.RESOLVED);
        when(disputeRepository.findById(1L)).thenReturn(Optional.of(dispute));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> disputeService.resolveDispute(1L));

        verify(disputeRepository, times(1)).findById(1L);
        verify(disputeRepository, never()).save(any(Dispute.class));
        assertEquals("Can only resolve an OPEN dispute", exception.getMessage());
    }
}