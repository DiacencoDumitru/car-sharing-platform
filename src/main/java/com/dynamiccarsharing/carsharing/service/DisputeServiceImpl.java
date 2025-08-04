package com.dynamiccarsharing.carsharing.service;

<<<<<<< HEAD:src/main/java/com/dynamiccarsharing/carsharing/service/DisputeServiceJpaImpl.java
import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.DisputeDto;
=======
>>>>>>> fix/controller-mvc-tests:src/main/java/com/dynamiccarsharing/carsharing/service/DisputeServiceImpl.java
import com.dynamiccarsharing.carsharing.dto.criteria.DisputeSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
import com.dynamiccarsharing.carsharing.filter.DisputeFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
<<<<<<< HEAD:src/main/java/com/dynamiccarsharing/carsharing/service/DisputeServiceJpaImpl.java
import com.dynamiccarsharing.carsharing.mapper.DisputeMapper;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.DisputeService;
import lombok.RequiredArgsConstructor;
=======
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.DisputeService;
>>>>>>> fix/controller-mvc-tests:src/main/java/com/dynamiccarsharing/carsharing/service/DisputeServiceImpl.java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("disputeService")
@Transactional
<<<<<<< HEAD:src/main/java/com/dynamiccarsharing/carsharing/service/DisputeServiceJpaImpl.java
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeMapper disputeMapper;
=======
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;

    public DisputeServiceImpl(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }
>>>>>>> fix/controller-mvc-tests:src/main/java/com/dynamiccarsharing/carsharing/service/DisputeServiceImpl.java

    @Override
    public DisputeDto createDispute(Long bookingId, DisputeCreateRequestDto createDto, Long creationUserId) {
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
    @Transactional(readOnly = true)
    public Iterable<Dispute> findAll() {
        return disputeRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
<<<<<<< HEAD:src/main/java/com/dynamiccarsharing/carsharing/service/DisputeServiceJpaImpl.java
        if (disputeRepository.findById(id).isPresent()) {
            disputeRepository.deleteById(id);
        } else {
=======
        if (disputeRepository.findById(id).isEmpty()) {
>>>>>>> fix/controller-mvc-tests:src/main/java/com/dynamiccarsharing/carsharing/service/DisputeServiceImpl.java
            throw new DisputeNotFoundException("Dispute with ID " + id + " not found.");
        }
    }

    @Override
    public DisputeDto resolveDispute(Long disputeId) {
        Dispute dispute = getDisputeOrThrow(disputeId);
        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new IllegalStateException("Can only resolve an OPEN dispute.");
        }
        Dispute resolvedDispute = disputeRepository.save(dispute.withStatus(DisputeStatus.RESOLVED));
        return disputeMapper.toDto(resolvedDispute);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> searchDisputes(DisputeSearchCriteria criteria) {
        Filter<Dispute> filter = DisputeFilter.of(
                criteria.getBookingId(),
                criteria.getStatus()
        );
        try {
            return disputeRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for dispute failed", e);
        }
    }

    private Dispute getDisputeOrThrow(Long disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException("Dispute with ID " + disputeId + " not found."));
    }
}