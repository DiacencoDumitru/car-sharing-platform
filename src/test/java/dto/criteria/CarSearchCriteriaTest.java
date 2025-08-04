package dto.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CarSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        String make = "Honda";
        String model = "Civic";
        CarStatus status = CarStatus.AVAILABLE;
        Location location = Location.builder().id(5L).build();
        CarType type = CarType.SEDAN;
        VerificationStatus verificationStatus = VerificationStatus.VERIFIED;

        CarSearchCriteria criteria = CarSearchCriteria.builder()
                .make(make)
                .model(model)
                .status(status)
                .location(location)
                .type(type)
                .verificationStatus(verificationStatus)
                .build();

        assertNotNull(criteria);
        assertEquals(make, criteria.getMake());
        assertEquals(model, criteria.getModel());
        assertEquals(status, criteria.getStatus());
        assertEquals(location, criteria.getLocation());
        assertEquals(type, criteria.getType());
        assertEquals(verificationStatus, criteria.getVerificationStatus());
    }
}