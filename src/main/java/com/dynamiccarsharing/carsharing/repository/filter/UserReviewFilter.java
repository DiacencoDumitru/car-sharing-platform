package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.UserReview;
import lombok.Getter;

@Getter
public class UserReviewFilter implements Filter<UserReview> {
    private final Long id;
    private final Long userId;
    private final Long reviewerId;
    private final String comment;

    private UserReviewFilter(Long id, Long userId, Long reviewerId, String comment) {
        this.id = id;
        this.userId = userId;
        this.reviewerId = reviewerId;
        this.comment = comment;
    }

    public static UserReviewFilter of(Long id, Long userId, Long reviewerId, String comment) {
        return new UserReviewFilter(id, userId, reviewerId, comment);
    }

    public static UserReviewFilter ofId(Long id) {
        return new UserReviewFilter(id, null, null, null);
    }

    public static UserReviewFilter ofUserId(Long userId) {
        return new UserReviewFilter(null, userId, null, null);
    }

    public static UserReviewFilter ofReviewerId(Long reviewerId) {
        return new UserReviewFilter(null, null, reviewerId, null);
    }

    public static UserReviewFilter ofComment(String comment) {
        return new UserReviewFilter(null, null, null, comment);
    }

    @Override
    public boolean test(UserReview review) {
        boolean matches = true;
        if (id != null) matches &= review.getId().equals(id);
        if (userId != null) matches &= review.getUserId().equals(userId);
        if (reviewerId != null) matches &= review.getReviewerId().equals(reviewerId);
        if (comment != null) matches &= review.getComment().equals(comment);
        return matches;
    }
}