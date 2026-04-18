package dto;

import com.dynamiccarsharing.car.dto.LocationDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocationDtoTest {

    private LocationDto createDto() {
        LocationDto dto = new LocationDto();
        dto.setId(1L);
        dto.setCity("New York");
        dto.setState("NY");
        dto.setZipCode("10001");
        return dto;
    }

    @Test
    void testGettersAndSetters() {
        LocationDto dto = new LocationDto();
        assertNull(dto.getId());

        dto.setId(1L);
        dto.setCity("New York");
        dto.setState("NY");
        dto.setZipCode("10001");

        assertEquals(1L, dto.getId());
        assertEquals("New York", dto.getCity());
        assertEquals("NY", dto.getState());
        assertEquals("10001", dto.getZipCode());
    }

    @Test
    void testToString() {
        LocationDto dto = createDto();
        String s = dto.toString();
        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("city=New York"));
        assertTrue(s.contains("state=NY"));
        assertTrue(s.contains("zipCode=10001"));
    }

    @Test
    void testEqualsAndHashCode_BranchCoverage() {
        LocationDto dto1 = createDto();
        LocationDto dto2 = createDto();

        assertEquals(dto1, dto1);
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, null);
        assertNotEquals(dto1, new Object());

        LocationDto dto3;

        dto3 = createDto();
        dto3.setId(2L);
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());

        dto3 = createDto();
        dto3.setCity("Boston");
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setState("MA");
        assertNotEquals(dto1, dto3);

        dto3 = createDto();
        dto3.setZipCode("02101");
        assertNotEquals(dto1, dto3);
    }
}