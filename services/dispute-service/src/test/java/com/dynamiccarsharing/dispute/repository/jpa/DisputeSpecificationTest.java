package com.dynamiccarsharing.dispute.repository.jpa;

import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.model.Dispute;
import com.dynamiccarsharing.dispute.specification.DisputeSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DisputeSpecificationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DisputeJpaRepository disputeRepository;

    @BeforeEach
    void setUp() {
        entityManager.persist(Dispute.builder().bookingId(101L).status(DisputeStatus.OPEN).description("Open dispute").creationUserId(100L).createdAt(LocalDateTime.now()).build());
        entityManager.persist(Dispute.builder().bookingId(102L).status(DisputeStatus.RESOLVED).description("Resolved case").creationUserId(101L).createdAt(LocalDateTime.now()).build());
        entityManager.persist(Dispute.builder().bookingId(103L).status(DisputeStatus.OPEN).description("Another open one").creationUserId(102L).createdAt(LocalDateTime.now()).build());
        entityManager.flush();
    }

    @Test
    void hasStatus_withMatchingStatus_returnsMatchingDisputes() {
        Specification<Dispute> spec = DisputeSpecification.hasStatus(DisputeStatus.OPEN);
        List<Dispute> results = disputeRepository.findAll(spec);
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(d -> d.getStatus() == DisputeStatus.OPEN);
    }

    @Test
    void hasBookingId_withMatchingId_returnsMatchingDispute() {
        Specification<Dispute> spec = DisputeSpecification.hasBookingId(102L);
        List<Dispute> results = disputeRepository.findAll(spec);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBookingId()).isEqualTo(102L);
    }

    @Test
    void withCriteria_withAllFields_returnsMatchingDispute() {
        Specification<Dispute> spec = DisputeSpecification.withCriteria(101L, DisputeStatus.OPEN);
        List<Dispute> results = disputeRepository.findAll(spec);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getBookingId()).isEqualTo(101L);
        assertThat(results.get(0).getStatus()).isEqualTo(DisputeStatus.OPEN);
    }
}