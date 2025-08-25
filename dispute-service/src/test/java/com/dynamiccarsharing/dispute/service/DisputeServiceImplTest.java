package com.dynamiccarsharing.dispute.service;

import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.dto.DisputeDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.dispute.exception.DisputeNotFoundException;
import com.dynamiccarsharing.dispute.mapper.DisputeMapper;
import com.dynamiccarsharing.dispute.model.Dispute;
import com.dynamiccarsharing.dispute.repository.DisputeRepository;
import com.dynamiccarsharing.util.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceImplTest {

    @Mock
    private WebClient userWebClient;
    @Mock
    private WebClient bookingWebClient;
    @Mock
    private DisputeRepository disputeRepository;
    @Mock
    private DisputeMapper disputeMapper;
    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private DisputeServiceImpl disputeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(disputeService, "userWebClient", userWebClient);
        ReflectionTestUtils.setField(disputeService, "bookingWebClient", bookingWebClient);
    }

    private Dispute createTestDispute(Long id, DisputeStatus status) {
        return Dispute.builder()
                .id(id)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("createDispute() should succeed when user and booking exist")
    void createDispute_shouldMapAndSaveAndReturnDto() {
        Long bookingId = 1L;
        Long creationUserId = 2L;
        DisputeCreateRequestDto createDto = new DisputeCreateRequestDto();
        Dispute disputeEntity = createTestDispute(null, DisputeStatus.OPEN);
        Dispute savedEntity = createTestDispute(1L, DisputeStatus.OPEN);
        DisputeDto expectedDto = new DisputeDto();
        expectedDto.setId(1L);

        mockWebClient(userWebClient, creationUserId, UserDto.class, new UserDto());
        mockWebClient(bookingWebClient, bookingId, BookingDto.class, new BookingDto());

        when(disputeMapper.toEntity(createDto, bookingId, creationUserId)).thenReturn(disputeEntity);
        when(disputeRepository.save(disputeEntity)).thenReturn(savedEntity);
        when(disputeMapper.toDto(savedEntity)).thenReturn(expectedDto);

        DisputeDto result = disputeService.createDispute(bookingId, createDto, creationUserId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userWebClient.get()).uri("/" + creationUserId);
        verify(bookingWebClient.get()).uri("/" + bookingId);
    }

    @Test
    @DisplayName("createDispute() should throw exception when user does not exist")
    void createDispute_whenUserNotFound_shouldThrowException() {
        Long bookingId = 1L;
        Long creationUserId = 2L;
        DisputeCreateRequestDto createDto = new DisputeCreateRequestDto();

        mockWebClient(userWebClient, creationUserId, UserDto.class, null);

        assertThrows(ValidationException.class, () -> disputeService.createDispute(bookingId, createDto, creationUserId));
    }

    @Test
    @DisplayName("createDispute() should throw exception when booking does not exist")
    void createDispute_whenBookingNotFound_shouldThrowException() {
        Long bookingId = 1L;
        Long creationUserId = 2L;
        DisputeCreateRequestDto createDto = new DisputeCreateRequestDto();

        mockWebClient(userWebClient, creationUserId, UserDto.class, new UserDto());
        mockWebClient(bookingWebClient, bookingId, BookingDto.class, null);

        assertThrows(ValidationException.class, () -> disputeService.createDispute(bookingId, createDto, creationUserId));
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
        DisputeDto expectedDto = new DisputeDto();
        expectedDto.setStatus(DisputeStatus.RESOLVED);

        when(disputeRepository.findById(disputeId)).thenReturn(Optional.of(openDispute));
        when(disputeRepository.save(any(Dispute.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(disputeMapper.toDto(any(Dispute.class))).thenReturn(expectedDto);

        DisputeDto result = disputeService.resolveDispute(disputeId);

        assertNotNull(result);
        assertEquals(DisputeStatus.RESOLVED, result.getStatus());

        verify(disputeRepository).save(argThat(dispute -> dispute.getStatus() == DisputeStatus.RESOLVED));
    }

    @Test
    void resolveDispute_whenNotFound_shouldThrowException() {
        Long disputeId = 1L;
        when(disputeRepository.findById(disputeId)).thenReturn(Optional.empty());

        assertThrows(DisputeNotFoundException.class, () -> disputeService.resolveDispute(disputeId));
    }

    private <T> void mockWebClient(WebClient webClient, Long id, Class<T> responseClass, T responseDto) {
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono<T> mono = mock(Mono.class);

        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri("/" + id)).thenReturn(requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.bodyToMono(any(Class.class))).thenReturn(mono);

        if (responseDto != null) {
            lenient().when(mono.block()).thenReturn(responseDto);
        } else {
            lenient().when(mono.block()).thenThrow(new RuntimeException("Simulated 404 Not Found"));
        }
    }
}