package com.dynamiccarsharing.util.dao.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlFilterTest {

    @Test
    @DisplayName("Record constructor and accessors work correctly")
    void constructorAndAccessors() {
        String query = " AND name = ?";
        List<Object> params = List.of("test");
        SqlFilter sqlFilter = new SqlFilter(query, params);

        assertEquals(query, sqlFilter.filterQuery());
        assertEquals(params, sqlFilter.parameters());
    }

    @Test
    @DisplayName("empty() static factory creates empty filter")
    void empty_createsEmptyFilter() {
        SqlFilter emptyFilter = SqlFilter.empty();

        assertEquals("", emptyFilter.filterQuery());
        assertEquals(Collections.emptyList(), emptyFilter.parameters());
        assertNotNull(emptyFilter.parameters());
    }

    @Test
    @DisplayName("parametersArray() returns correct array for non-empty list")
    void parametersArray_nonEmptyList_returnsCorrectArray() {
        String query = " AND age > ? AND city = ?";
        List<Object> params = List.of(25, "New York");
        SqlFilter sqlFilter = new SqlFilter(query, params);

        Object[] expectedArray = {25, "New York"};
        assertArrayEquals(expectedArray, sqlFilter.parametersArray());
    }

    @Test
    @DisplayName("parametersArray() returns empty array for empty list")
    void parametersArray_emptyList_returnsEmptyArray() {
        SqlFilter emptyFilter = SqlFilter.empty();
        Object[] expectedArray = {};
        assertArrayEquals(expectedArray, emptyFilter.parametersArray());
    }

    @Test
    @DisplayName("null parameters list is normalized to an empty list")
    void nullParameters_normalizedToEmptyList() {
        SqlFilter filter = new SqlFilter("query", null);
        assertEquals(Collections.emptyList(), filter.parameters());
        assertArrayEquals(new Object[0], filter.parametersArray());
    }

     @Test
    @DisplayName("Test Record's equals, hashCode, and toString")
    void recordMethods_workAsExpected() {
        String query = " AND id = ?";
        List<Object> params1 = List.of(1L);
        List<Object> params2 = List.of(1L);
        List<Object> params3 = List.of(2L);

        SqlFilter filter1 = new SqlFilter(query, params1);
        SqlFilter filter2 = new SqlFilter(query, params2);
        SqlFilter filter3 = new SqlFilter(query, params3);
        SqlFilter filter4 = new SqlFilter(" different", params1);
        SqlFilter emptyFilter = SqlFilter.empty();

        assertEquals(filter1, filter2);
        assertNotEquals(filter1, filter3);
        assertNotEquals(filter1, filter4);
        assertNotEquals(filter1, emptyFilter);
        assertNotEquals(null, filter1);
        assertNotEquals(new Object(), filter1);

        assertEquals(filter1.hashCode(), filter2.hashCode());
        assertNotEquals(filter1.hashCode(), filter3.hashCode());
        assertNotEquals(filter1.hashCode(), filter4.hashCode());

        String toStringResult = filter1.toString();
        assertTrue(toStringResult.contains(query));
        assertTrue(toStringResult.contains(params1.toString()));
        assertNotNull(emptyFilter.toString());
    }
}