package com.dynamiccarsharing.dispute.repository.jpa;

import com.dynamiccarsharing.dispute.config.JpaConfig;
import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.model.Dispute;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@Import(JpaConfig.class)
class DisputeJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DisputeJpaRepository disputeRepository;

    @Test
    void findByFilter_withCriteria_returnsMatchingDispute() {
        Dispute dispute1 = Dispute.builder().bookingId(1L).status(DisputeStatus.OPEN).description("Test").creationUserId(100L).createdAt(LocalDateTime.now()).build();
        Dispute dispute2 = Dispute.builder().bookingId(2L).status(DisputeStatus.RESOLVED).description("Another").creationUserId(101L).createdAt(LocalDateTime.now()).build();
        entityManager.persist(dispute1);
        entityManager.persist(dispute2);
        entityManager.flush();

        Specification<Dispute> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), DisputeStatus.RESOLVED);

        List<Dispute> results = disputeRepository.findAll(spec);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(DisputeStatus.RESOLVED, results.get(0).getStatus());
        assertEquals(2L, results.get(0).getBookingId());
    }
}