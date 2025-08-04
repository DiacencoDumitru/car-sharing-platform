package dto.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.CarReviewSearchCriteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CarReviewSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        Long carId = 1L;
        Long reviewerId = 2L;

        CarReviewSearchCriteria criteria = CarReviewSearchCriteria.builder()
                .carId(carId)
                .reviewerId(reviewerId)
                .build();

        assertNotNull(criteria);
        assertEquals(carId, criteria.getCarId());
        assertEquals(reviewerId, criteria.getReviewerId());
    }
}