package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.DisputeDto;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.mapper.DisputeMapper;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
=======
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jpa.DisputeJpaRepository;
import com.dynamiccarsharing.carsharing.dto.criteria.DisputeSearchCriteria;
>>>>>>> fix/controller-mvc-tests
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

<<<<<<< HEAD
import java.util.Collections;
=======
import java.sql.SQLException;
import java.time.LocalDateTime;
>>>>>>> fix/controller-mvc-tests
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceImplTest {

    @Mock
    private DisputeJpaRepository disputeJpaRepository;

<<<<<<< HEAD
    @Mock
    private DisputeMapper disputeMapper;

=======
>>>>>>> fix/controller-mvc-tests
    private DisputeServiceImpl disputeService;

    @BeforeEach
    void setUp() {
<<<<<<< HEAD
        disputeService = new DisputeServiceImpl(disputeRepository, disputeMapper);
=======
        disputeService = new DisputeServiceImpl(disputeJpaRepository);
>>>>>>> fix/controller-mvc-tests
    }

    private Dispute createTestDispute(Long id, DisputeStatus status) {
        return Dispute.builder()
                .id(id)
                .status(status)
                .build();
    }

    @Test
<<<<<<< HEAD
    void createDispute_shouldMapAndSaveAndReturnDto() {
        Long bookingId = 1L;
        Long creationUserId = 2L;
        DisputeCreateRequestDto createDto = new DisputeCreateRequestDto();
        Dispute disputeEntity = createTestDispute(null, DisputeStatus.OPEN);
        Dispute savedEntity = createTestDispute(1L, DisputeStatus.OPEN);
        DisputeDto expectedDto = new DisputeDto();
        expectedDto.setId(1L);
=======
    void save_shouldCallRepositoryAndReturnDispute() {
        Dispute disputeToSave = createTestDispute(null, DisputeStatus.OPEN);
        Dispute savedDispute = createTestDispute(1L, DisputeStatus.OPEN);
        when(disputeJpaRepository.save(disputeToSave)).thenReturn(savedDispute);
>>>>>>> fix/controller-mvc-tests

        when(disputeMapper.toEntity(createDto, bookingId, creationUserId)).thenReturn(disputeEntity);
        when(disputeRepository.save(disputeEntity)).thenReturn(savedEntity);
        when(disputeMapper.toDto(savedEntity)).thenReturn(expectedDto);

        DisputeDto result = disputeService.createDispute(bookingId, createDto, creationUserId);

        assertNotNull(result);
<<<<<<< HEAD
        assertEquals(1L, result.getId());
=======
        assertNotNull(result.getId());
        verify(disputeJpaRepository).save(disputeToSave);
>>>>>>> fix/controller-mvc-tests
    }

    @Test
    void findDisputeById_whenExists_shouldMapAndReturnDto() {
        Long disputeId = 1L;
<<<<<<< HEAD
        Dispute disputeEntity = createTestDispute(disputeId, DisputeStatus.OPEN);
        DisputeDto expectedDto = new DisputeDto();
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(disputeEntity));
        when(disputeMapper.toDto(disputeEntity)).thenReturn(expectedDto);
=======
        Dispute testDispute = createTestDispute(disputeId, DisputeStatus.OPEN);
        when(disputeJpaRepository.findById(disputeId)).thenReturn(Optional.of(testDispute));
>>>>>>> fix/controller-mvc-tests

        Optional<DisputeDto> result = disputeService.findDisputeById(disputeId);

        assertTrue(result.isPresent());
    }

    @Test
    void findAllDisputes_shouldMapAndReturnDtoList() {
        Dispute disputeEntity = createTestDispute(1L, DisputeStatus.OPEN);
        when(disputeRepository.findAll()).thenReturn(Collections.singletonList(disputeEntity));
        when(disputeMapper.toDto(disputeEntity)).thenReturn(new DisputeDto());

        List<DisputeDto> result = disputeService.findAllDisputes();

        assertEquals(1, result.size());
    }

    @Test
    void deleteById_whenDisputeExists_shouldSucceed() {
        Long disputeId = 1L;
<<<<<<< HEAD
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(Dispute.builder().build()));
        doNothing().when(disputeRepository).deleteById(disputeId);
=======
        when(disputeJpaRepository.findById(disputeId)).thenReturn(Optional.of(createTestDispute(disputeId, DisputeStatus.OPEN)));
        doNothing().when(disputeJpaRepository).deleteById(disputeId);
>>>>>>> fix/controller-mvc-tests

        disputeService.deleteById(disputeId);

        verify(disputeJpaRepository).deleteById(disputeId);
    }

    @Test
<<<<<<< HEAD
    void resolveDispute_withOpenDispute_shouldSucceedAndReturnDto() {
        Long disputeId = 1L;
        Dispute openDispute = createTestDispute(disputeId, DisputeStatus.OPEN);
        Dispute resolvedEntity = openDispute.withStatus(DisputeStatus.RESOLVED);
        DisputeDto expectedDto = new DisputeDto();
        expectedDto.setStatus(DisputeStatus.RESOLVED);

        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(openDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(resolvedEntity);
        when(disputeMapper.toDto(resolvedEntity)).thenReturn(expectedDto);
=======
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
>>>>>>> fix/controller-mvc-tests

        DisputeDto result = disputeService.resolveDispute(disputeId);

<<<<<<< HEAD
        assertNotNull(result);
        assertEquals(DisputeStatus.RESOLVED, result.getStatus());
=======
        assertEquals(DisputeStatus.RESOLVED, resolvedDispute.getStatus());
        verify(disputeJpaRepository).save(any(Dispute.class));
>>>>>>> fix/controller-mvc-tests
    }

    @Test
    void resolveDispute_whenNotFound_shouldThrowException() {
        Long disputeId = 1L;
<<<<<<< HEAD
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.empty());

        assertThrows(DisputeNotFoundException.class, () -> disputeService.resolveDispute(disputeId));
=======
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
>>>>>>> fix/controller-mvc-tests
    }
}