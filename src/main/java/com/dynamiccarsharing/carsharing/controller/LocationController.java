package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.carsharing.mapper.LocationMapper;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.service.interfaces.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final LocationMapper locationMapper;

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@Valid @RequestBody LocationCreateRequestDto createDto) {
        Location locationToSave = locationMapper.toEntity(createDto);
        Location savedLocation = locationService.createLocation(locationToSave);
        return new ResponseEntity<>(locationMapper.toDto(savedLocation), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(locationMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations() {
        List<LocationDto> locationDtos = StreamSupport.stream(locationService.findAll().spliterator(), false)
                .map(locationMapper::toDto)
                .toList();
        return ResponseEntity.ok(locationDtos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocation(@PathVariable Long id, @Valid @RequestBody LocationUpdateRequestDto updateDto) {
        Location locationDetails = locationMapper.toEntity(updateDto);
        Location updatedLocation = locationService.updateLocation(id, locationDetails);
        return ResponseEntity.ok(locationMapper.toDto(updatedLocation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        locationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}