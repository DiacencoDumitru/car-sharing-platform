package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.model.UserReview;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

public class UserReviewSpecification {

    public static Specification<UserReview> hasUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<UserReview> hasReviewerId(UUID reviewerId) {
        return (root, query, cb) -> cb.equal(root.get("reviewer").get("id"), reviewerId);
    }

    public static Specification<UserReview> commentContains(String text) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("comment")), "%" + text.toLowerCase() + "%");
    }
}