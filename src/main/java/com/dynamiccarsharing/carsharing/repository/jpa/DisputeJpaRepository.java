package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface DisputeJpaRepository extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute> {

    Optional<Dispute> findByBookingId(Long bookingId);

    List<Dispute> findByStatus(DisputeStatus status);
}