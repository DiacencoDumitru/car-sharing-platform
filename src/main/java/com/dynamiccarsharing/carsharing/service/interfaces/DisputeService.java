package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.dto.DisputeSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface DisputeService {
    Dispute save(Dispute dispute);

    Optional<Dispute> findById(Long id);

    void deleteById(Long id);

    Dispute resolveDispute(Long disputeId);

    Dispute updateDisputeDescription(Long disputeId, String newDescription);

    List<Dispute> searchDisputes(DisputeSearchCriteria criteria);
}