package com.dynamiccarsharing.dispute.service.interfaces;

import com.dynamiccarsharing.contracts.dto.DisputeDto;
import com.dynamiccarsharing.dispute.criteria.DisputeSearchCriteria;
import com.dynamiccarsharing.dispute.dto.DisputeCreateRequestDto;

import java.util.List;
import java.util.Optional;

public interface DisputeService {
    DisputeDto createDispute(Long bookingId, DisputeCreateRequestDto createDto, Long creationUserId);

    Optional<DisputeDto> findDisputeById(Long id);

    List<DisputeDto> findAllDisputes();

    void deleteById(Long id);

    DisputeDto resolveDispute(Long disputeId);

    List<DisputeDto> searchDisputes(DisputeSearchCriteria criteria);
}