package com.dynamiccarsharing.car.controller;

import com.dynamiccarsharing.contracts.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.LocationDto;
import com.dynamiccarsharing.contracts.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.car.service.interfaces.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationService locationService;

    @Test
    @WithMockUser
    void createLocation_withValidData_shouldReturnCreated() throws Exception {
        LocationCreateRequestDto createDto = new LocationCreateRequestDto();
        createDto.setCity("New York");
        createDto.setState("NY");
        createDto.setZipCode("10001");

        LocationDto savedDto = new LocationDto();
        savedDto.setId(1L);
        savedDto.setCity("New York");

        when(locationService.createLocation(any(LocationCreateRequestDto.class))).thenReturn(savedDto);

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
        LocationDto dto = new LocationDto();
        dto.setId(1L);
        when(locationService.findLocationById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/locations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void getAllLocations_shouldReturnList() throws Exception {
        when(locationService.findAllLocations()).thenReturn(List.of(new LocationDto(), new LocationDto()));

        mockMvc.perform(get("/api/v1/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void updateLocation_whenExists_shouldReturnOk() throws Exception {
        LocationUpdateRequestDto updateDto = new LocationUpdateRequestDto();

        updateDto.setCity("Boston");
        updateDto.setState("Massachusetts");
        updateDto.setZipCode("02108");

        LocationDto updatedDto = new LocationDto();
        updatedDto.setId(1L);
        updatedDto.setState("Massachusetts");

        when(locationService.updateLocation(eq(1L), any(LocationUpdateRequestDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/v1/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("Massachusetts"));
    }

    @Test
    @WithMockUser
    void deleteLocation_whenExists_shouldReturnNoContent() throws Exception {
        doNothing().when(locationService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/locations/{id}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

        verify(locationService, times(1)).deleteById(1L);
    }
}