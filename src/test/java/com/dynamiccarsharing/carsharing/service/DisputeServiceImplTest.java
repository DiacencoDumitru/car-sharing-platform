package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.DisputeDto;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
import com.dynamiccarsharing.carsharing.mapper.DisputeMapper;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceImplTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private DisputeMapper disputeMapper;

    private DisputeServiceImpl disputeService;

    @BeforeEach
    void setUp() {
        disputeService = new DisputeServiceImpl(disputeRepository, disputeMapper);
    }

    private Dispute createTestDispute(Long id, DisputeStatus status) {
        return Dispute.builder()
                .id(id)
                .status(status)
                .build();
    }

    @Test
    void createDispute_shouldMapAndSaveAndReturnDto() {
        Long bookingId = 1L;
        Long creationUserId = 2L;
        DisputeCreateRequestDto createDto = new DisputeCreateRequestDto();
        Dispute disputeEntity = createTestDispute(null, DisputeStatus.OPEN);
        Dispute savedEntity = createTestDispute(1L, DisputeStatus.OPEN);
        DisputeDto expectedDto = new DisputeDto();
        expectedDto.setId(1L);

        when(disputeMapper.toEntity(createDto, bookingId, creationUserId)).thenReturn(disputeEntity);
        when(disputeRepository.save(disputeEntity)).thenReturn(savedEntity);
        when(disputeMapper.toDto(savedEntity)).thenReturn(expectedDto);

        DisputeDto result = disputeService.createDispute(bookingId, createDto, creationUserId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findDisputeById_whenExists_shouldMapAndReturnDto() {
        Long disputeId = 1L;
        Dispute disputeEntity = createTestDispute(disputeId, DisputeStatus.OPEN);
        DisputeDto expectedDto = new DisputeDto();
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(disputeEntity));
        when(disputeMapper.toDto(disputeEntity)).thenReturn(expectedDto);

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
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(Dispute.builder().build()));
        doNothing().when(disputeRepository).deleteById(disputeId);

        disputeService.deleteById(disputeId);

        verify(disputeRepository).deleteById(disputeId);
    }

    @Test
    void resolveDispute_withOpenDispute_shouldSucceedAndReturnDto() {
        Long disputeId = 1L;
        Dispute openDispute = createTestDispute(disputeId, DisputeStatus.OPEN);
        Dispute resolvedEntity = openDispute.withStatus(DisputeStatus.RESOLVED);
        DisputeDto expectedDto = new DisputeDto();
        expectedDto.setStatus(DisputeStatus.RESOLVED);

        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(openDispute));
        when(disputeRepository.save(any(Dispute.class))).thenReturn(resolvedEntity);
        when(disputeMapper.toDto(resolvedEntity)).thenReturn(expectedDto);

        DisputeDto result = disputeService.resolveDispute(disputeId);

        assertNotNull(result);
        assertEquals(DisputeStatus.RESOLVED, result.getStatus());
    }

    @Test
    void resolveDispute_whenNotFound_shouldThrowException() {
        Long disputeId = 1L;
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.empty());

        assertThrows(DisputeNotFoundException.class, () -> disputeService.resolveDispute(disputeId));
    }
}