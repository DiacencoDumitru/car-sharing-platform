package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.InMemoryDisputeRepository;
import com.dynamiccarsharing.carsharing.repository.filter.DisputeFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class DisputeService {
    private final InMemoryDisputeRepository inMemoryDisputeRepository;

    public DisputeService(InMemoryDisputeRepository inMemoryDisputeRepository) {
        this.inMemoryDisputeRepository = inMemoryDisputeRepository;
    }

    public Dispute save(Dispute dispute) {
        Validator.validateNonNull(dispute, "Dispute");
        return inMemoryDisputeRepository.save(dispute);
    }

    public Optional<Dispute> findById(Long id) {
        Validator.validateId(id, "ID");
        return inMemoryDisputeRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "ID");
        inMemoryDisputeRepository.deleteById(id);
    }

    public Iterable<Dispute> findAll() {
        return inMemoryDisputeRepository.findAll();
    }

    public List<Dispute> findDisputesByStatus(DisputeStatus status) {
        Validator.validateNonNull(status, "Dispute Status");
        DisputeFilter filter = new DisputeFilter().setStatus(status);
        return (List<Dispute>) inMemoryDisputeRepository.findByFilter(filter);
    }
}
