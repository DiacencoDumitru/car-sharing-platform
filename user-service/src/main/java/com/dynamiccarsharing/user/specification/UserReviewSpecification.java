package com.dynamiccarsharing.user.specification;

import com.dynamiccarsharing.user.model.UserReview;
import org.springframework.data.jpa.domain.Specification;

public class UserReviewSpecification {

    private UserReviewSpecification() {
    }

    public static Specification<UserReview> hasUserId(Long userId) {
        return (root, query, cb) -> userId != null ? cb.equal(root.get("user").get("id"), userId) : null;
    }

    public static Specification<UserReview> hasReviewerId(Long reviewerId) {
        return (root, query, cb) -> reviewerId != null ? cb.equal(root.get("reviewer").get("id"), reviewerId) : null;
    }

    public static Specification<UserReview> commentContains(String text) {
        return (root, query, cb) -> text != null ? cb.like(cb.lower(root.get("comment")), "%" + text.toLowerCase() + "%") : null;
    }

    public static Specification<UserReview> withCriteria(Long userId, Long reviewerId) {
        return Specification
                .where(hasUserId(userId))
                .and(hasReviewerId(reviewerId));
    }
}