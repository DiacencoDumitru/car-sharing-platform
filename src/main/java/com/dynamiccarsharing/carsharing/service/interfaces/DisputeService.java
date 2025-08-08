package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.DisputeDto;
import com.dynamiccarsharing.carsharing.dto.criteria.DisputeSearchCriteria;
import com.dynamiccarsharing.carsharing.model.Dispute;

import java.util.List;
import java.util.Optional;

public interface DisputeService {
    DisputeDto createDispute(Long bookingId, DisputeCreateRequestDto createDto, Long creationUserId);

    Optional<DisputeDto> findDisputeById(Long id);

    List<DisputeDto> findAllDisputes();

    void deleteById(Long id);

    DisputeDto resolveDispute(Long disputeId);

    List<Dispute> searchDisputes(DisputeSearchCriteria criteria);
}