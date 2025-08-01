package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.TestApplication;
import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationRepository locationRepository;

    @Test
    @WithMockUser
    void createLocation_withValidData_shouldReturnCreated() throws Exception {
        LocationCreateRequestDto createDto = new LocationCreateRequestDto();
        createDto.setCity("New York");
        createDto.setState("NY");
        createDto.setZipCode("10001");

        Location savedLocation = Location.builder()
                .id(1L).city("New York").state("NY").zipCode("10001").build();

        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);

        mockMvc.perform(post("/api/v1/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.city").value("New York"));
    }

    @Test
    @WithMockUser
    void getLocationById_whenExists_shouldReturnOk() throws Exception {
        Location location = Location.builder().id(1L).city("Los Angeles").state("CA").zipCode("90001").build();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        mockMvc.perform(get("/api/v1/locations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.city").value("Los Angeles"));
    }

    @Test
    @WithMockUser
    void getAllLocations_shouldReturnList() throws Exception {
        Location loc1 = Location.builder().id(1L).city("Chicago").build();
        Location loc2 = Location.builder().id(2L).city("Houston").build();
        when(locationRepository.findAll()).thenReturn(List.of(loc1, loc2));

        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].city").value("Chicago"));
    }
    
    @Test
    @WithMockUser
    void updateLocation_whenExists_shouldReturnOk() throws Exception {
        Location existingLocation = Location.builder().id(1L).city("Boston").state("MA").zipCode("02101").build();
        
        LocationUpdateRequestDto updateDto = new LocationUpdateRequestDto();
        updateDto.setCity("Boston");
        updateDto.setState("Massachusetts"); // Updated field
        updateDto.setZipCode("02101");

        Location updatedLocation = Location.builder().id(1L).city("Boston").state("Massachusetts").zipCode("02101").build();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(updatedLocation);

        mockMvc.perform(put("/api/v1/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.state").value("Massachusetts"));
    }

    @Test
    @WithMockUser
    void deleteLocation_whenExists_shouldReturnNoContent() throws Exception {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(Location.builder().id(1L).build()));
        doNothing().when(locationRepository).deleteById(1L);

        mockMvc.perform(delete("/api/v1/locations/{id}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(locationRepository, times(1)).deleteById(1L);
    }
}