package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.mapper.LocationMapper;
import com.dynamiccarsharing.carsharing.model.Location;
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.service.interfaces.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
<<<<<<< HEAD
=======
import java.util.stream.StreamSupport;
>>>>>>> fix/controller-mvc-tests

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
<<<<<<< HEAD

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@Valid @RequestBody LocationCreateRequestDto createDto) {
        LocationDto savedDto = locationService.createLocation(createDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
=======
    private final LocationMapper locationMapper;

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@Valid @RequestBody LocationCreateRequestDto createDto) {
        Location locationToSave = locationMapper.toEntity(createDto);
        Location savedLocation = locationService.createLocation(locationToSave);
        return new ResponseEntity<>(locationMapper.toDto(savedLocation), HttpStatus.CREATED);
>>>>>>> fix/controller-mvc-tests
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long id) {
<<<<<<< HEAD
        return locationService.findLocationById(id)
=======
        return locationService.findById(id)
                .map(locationMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
<<<<<<< HEAD
        List<LocationDto> locationDtos = locationService.findAllLocations();
=======
        List<LocationDto> locationDtos = StreamSupport.stream(locationService.findAll().spliterator(), false)
                .map(locationMapper::toDto)
                .toList();
>>>>>>> fix/controller-mvc-tests
        return ResponseEntity.ok(locationDtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationUpdateRequestDto updateDto) {
<<<<<<< HEAD
        LocationDto updatedDto = locationService.updateLocation(id, updateDto);
        return ResponseEntity.ok(updatedDto);
=======
        Location locationDetails = locationMapper.toEntity(updateDto);
        Location updatedLocation = locationService.updateLocation(id, locationDetails);
        return ResponseEntity.ok(locationMapper.toDto(updatedLocation));
>>>>>>> fix/controller-mvc-tests
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}