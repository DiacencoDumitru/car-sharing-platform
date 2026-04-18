package dto;

import com.dynamiccarsharing.car.dto.CarReviewCreateRequestDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CarReviewCreateRequestDtoTest {

    private CarReviewCreateRequestDto createDto() {
        CarReviewCreateRequestDto dto = new CarReviewCreateRequestDto();
        dto.setCarId(1L);
        dto.setReviewerId(2L);
        dto.setBookingId(9L);
        dto.setRating(5);
        dto.setComment("This is a great car, 10/10.");
        return dto;
    }

    @Test
    void testGettersAndSetters() {
        CarReviewCreateRequestDto dto = new CarReviewCreateRequestDto();
        assertNull(dto.getCarId());

        dto.setCarId(1L);
        dto.setReviewerId(2L);
        dto.setComment("Test comment");

        assertEquals(1L, dto.getCarId());
        assertEquals(2L, dto.getReviewerId());
        assertEquals("Test comment", dto.getComment());
    }

    @Test
    void testToString() {
        CarReviewCreateRequestDto dto = createDto();
        String s = dto.toString();
        assertTrue(s.contains("carId=1"));
        assertTrue(s.contains("reviewerId=2"));
        assertTrue(s.contains("comment=This is a great car, 10/10."));
    }

    @Test
    void testEqualsAndHashCode_BranchCoverage() {
        CarReviewCreateRequestDto dto1 = createDto();
        CarReviewCreateRequestDto dto2 = createDto();
        
        assertEquals(dto1, dto1);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());

        assertNotEquals(dto1, null);

        assertNotEquals(dto1, new Object());

        CarReviewCreateRequestDto dto3;

        dto3 = createDto();
        dto3.setCarId(99L);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        dto3 = createDto();
        dto3.setReviewerId(99L);
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setComment("Different comment");
        assertNotEquals(dto1, dto3);
    }
}