package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.filter.DisputeFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.repository.jdbc.DisputeRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.DisputeService;
import com.dynamiccarsharing.carsharing.dto.DisputeSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("disputeService")
@Profile("jdbc")
@Transactional
public class DisputeServiceJdbcImpl implements DisputeService {

    private final DisputeRepositoryJdbcImpl disputeRepositoryJdbcImpl;

    public DisputeServiceJdbcImpl(DisputeRepositoryJdbcImpl disputeRepositoryJdbcImpl) {
        this.disputeRepositoryJdbcImpl = disputeRepositoryJdbcImpl;
    }

    @Override
    public Dispute save(Dispute dispute) {
        return disputeRepositoryJdbcImpl.save(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dispute> findById(Long id) {
        return disputeRepositoryJdbcImpl.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        getDisputeOrThrow(id);
        disputeRepositoryJdbcImpl.deleteById(id);
    }

    @Override
    public Dispute resolveDispute(Long disputeId) {
        Dispute dispute = getDisputeOrThrow(disputeId);
        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new IllegalStateException("Can only resolve an OPEN dispute.");
        }
        return disputeRepositoryJdbcImpl.save(dispute.withStatus(DisputeStatus.RESOLVED));
    }

    @Override
    public Dispute updateDisputeDescription(Long disputeId, String newDescription) {
        Dispute dispute = getDisputeOrThrow(disputeId);
        return disputeRepositoryJdbcImpl.save(dispute.withDescription(newDescription));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> searchDisputes(DisputeSearchCriteria criteria) {
        Filter<Dispute> filter = createFilterFromCriteria(criteria);
        try {
            return disputeRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for disputes failed", e);
        }
    }

    private Filter<Dispute> createFilterFromCriteria(DisputeSearchCriteria criteria) {
        return DisputeFilter.of(
                criteria.getBookingId(),
                criteria.getStatus()
        );
    }

    private Dispute getDisputeOrThrow(Long disputeId) {
        return disputeRepositoryJdbcImpl.findById(disputeId).orElseThrow(() -> new DisputeNotFoundException("Dispute with ID " + disputeId + " not found."));
    }
}