package com.dynamiccarsharing.car.search.es;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.util.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.dynamiccarsharing.car.config.SecurityConfig;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarSearchController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = { "eureka.instance.instance-id=car-service:unknown" })
class CarSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarSearchService carSearchService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    private CarDto carDto;
    private Page<CarDto> carPage;

    @BeforeEach
    void setUp() {
        carDto = new CarDto();
        carDto.setId(1L);
        carDto.setMake("Test Make");

        carPage = new PageImpl<>(List.of(carDto));
    }

    @Test
    @DisplayName("Should perform search and set instanceId")
    void testSearch() throws Exception {
        when(carSearchService.search(eq("test"), any(CarSearchCriteria.class), any(Pageable.class))).thenReturn(carPage);

        mockMvc.perform(get("/api/v1/cars/search")
                        .param("q", "test")
                        .param("make", "Test Make")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].make").value("Test Make"))
                .andExpect(jsonPath("$.content[0].instanceId").value("car-service:unknown"));
    }
    
    @Test
    @DisplayName("Should perform search with no query param")
    void testSearchNoQuery() throws Exception {
        when(carSearchService.search(eq(null), any(CarSearchCriteria.class), any(Pageable.class))).thenReturn(carPage);

        mockMvc.perform(get("/api/v1/cars/search")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }
}