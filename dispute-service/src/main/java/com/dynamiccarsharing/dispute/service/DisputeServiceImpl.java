package com.dynamiccarsharing.dispute.service;

import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.dto.DisputeDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.criteria.DisputeSearchCriteria;
import com.dynamiccarsharing.dispute.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.dispute.exception.DisputeNotFoundException;
import com.dynamiccarsharing.dispute.filter.DisputeFilter;
import com.dynamiccarsharing.dispute.mapper.DisputeMapper;
import com.dynamiccarsharing.dispute.model.Dispute;
import com.dynamiccarsharing.dispute.repository.DisputeRepository;
import com.dynamiccarsharing.dispute.service.interfaces.DisputeService;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.filter.Filter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service("disputeService")
@Transactional
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeMapper disputeMapper;
    private final WebClient.Builder webClientBuilder;

    private WebClient userWebClient;
    private WebClient bookingWebClient;

    @PostConstruct
    public void init() {
        this.userWebClient = webClientBuilder.baseUrl("http://user-service").build();
        this.bookingWebClient = webClientBuilder.baseUrl("http://booking-service").build();
    }


    @Override
    public DisputeDto createDispute(Long bookingId, DisputeCreateRequestDto createDto, Long creationUserId) {
        validateUserExists(creationUserId);
        validateBookingExists(bookingId);

        Dispute dispute = disputeMapper.toEntity(createDto, bookingId, creationUserId);
        Dispute savedDispute = disputeRepository.save(dispute);
        return disputeMapper.toDto(savedDispute);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DisputeDto> findDisputeById(Long id) {
        return disputeRepository.findById(id).map(disputeMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisputeDto> findAllDisputes() {
        return disputeRepository.findAll().stream()
                .map(disputeMapper::toDto)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        if (disputeRepository.findById(id).isPresent()) {
            disputeRepository.deleteById(id);
        } else {
            throw new DisputeNotFoundException("Dispute with ID " + id + " not found.");
        }
    }

    @Override
    public DisputeDto resolveDispute(Long disputeId) {
        Dispute dispute = getDisputeOrThrow(disputeId);
        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new IllegalStateException("Can only resolve an OPEN dispute.");
        }

        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolvedAt(LocalDateTime.now());
        Dispute resolvedDispute = disputeRepository.save(dispute);

        return disputeMapper.toDto(resolvedDispute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisputeDto> searchDisputes(DisputeSearchCriteria criteria) {
        Filter<Dispute> filter = DisputeFilter.of(
                criteria.getBookingId(),
                criteria.getStatus()
        );
        try {
            return disputeRepository.findByFilter(filter).stream().map(disputeMapper::toDto).toList();
        } catch (SQLException e) {
            throw new ServiceException("Search for dispute failed", e);
        }
    }

    private Dispute getDisputeOrThrow(Long disputeId) {
        return disputeRepository.findById(disputeId).orElseThrow(() -> new DisputeNotFoundException("Dispute with ID " + disputeId + " not found."));
    }

    private void validateUserExists(Long userId) {
        try {
            if (userWebClient == null) {
                throw new IllegalStateException("User WebClient not initialized");
            }

            userWebClient.get()
                    .uri("/" + userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (Exception e) {
            throw new ValidationException("User with ID " + userId + " does not exist.");
        }
    }

    private void validateBookingExists(Long bookingId) {
        try {
            bookingWebClient.get()
                    .uri("/" + bookingId)
                    .retrieve()
                    .bodyToMono(BookingDto.class)
                    .block();
        } catch (Exception e) {
            throw new ValidationException("Booking with ID " + bookingId + " does not exist.");
        }
    }
}