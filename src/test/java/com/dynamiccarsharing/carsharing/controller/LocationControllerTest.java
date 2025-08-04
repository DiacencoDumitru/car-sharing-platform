package com.dynamiccarsharing.carsharing.controller;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.carsharing.service.interfaces.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
=======
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
>>>>>>> fix/controller-mvc-tests
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
<<<<<<< HEAD
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
=======
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
>>>>>>> fix/controller-mvc-tests
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
<<<<<<< HEAD
    private LocationService locationService;
=======
    private LocationRepository locationRepository;
>>>>>>> fix/controller-mvc-tests

    @Test
    @WithMockUser
    void createLocation_withValidData_shouldReturnCreated() throws Exception {
        LocationCreateRequestDto createDto = new LocationCreateRequestDto();
        createDto.setCity("New York");
        createDto.setState("NY");
        createDto.setZipCode("10001");

<<<<<<< HEAD
        LocationDto savedDto = new LocationDto();
        savedDto.setId(1L);
        savedDto.setCity("New York");

        when(locationService.createLocation(any(LocationCreateRequestDto.class))).thenReturn(savedDto);
=======
        Location savedLocation = Location.builder()
                .id(1L).city("New York").state("NY").zipCode("10001").build();

        when(locationRepository.save(any(Location.class))).thenReturn(savedLocation);
>>>>>>> fix/controller-mvc-tests

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
<<<<<<< HEAD
        LocationDto dto = new LocationDto();
        dto.setId(1L);
        when(locationService.findLocationById(1L)).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/locations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
=======
        Location location = Location.builder().id(1L).city("Los Angeles").state("CA").zipCode("90001").build();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        mockMvc.perform(get("/api/v1/locations/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.city").value("Los Angeles"));
>>>>>>> fix/controller-mvc-tests
    }

    @Test
    @WithMockUser
    void getAllLocations_shouldReturnList() throws Exception {
<<<<<<< HEAD
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
=======
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
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(put("/api/v1/locations/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .with(csrf()))
                .andExpect(status().isOk())
<<<<<<< HEAD
=======
                .andExpect(jsonPath("$.id").value(1L))
>>>>>>> fix/controller-mvc-tests
                .andExpect(jsonPath("$.state").value("Massachusetts"));
    }

    @Test
    @WithMockUser
    void deleteLocation_whenExists_shouldReturnNoContent() throws Exception {
<<<<<<< HEAD
        doNothing().when(locationService).deleteById(1L);
=======
        when(locationRepository.findById(1L)).thenReturn(Optional.of(Location.builder().id(1L).build()));
        doNothing().when(locationRepository).deleteById(1L);
>>>>>>> fix/controller-mvc-tests

        mockMvc.perform(delete("/api/v1/locations/{id}", 1L).with(csrf()))
                .andExpect(status().isNoContent());

<<<<<<< HEAD
        verify(locationService, times(1)).deleteById(1L);
=======
        verify(locationRepository, times(1)).deleteById(1L);
>>>>>>> fix/controller-mvc-tests
    }
}