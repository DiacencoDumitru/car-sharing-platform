package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.mapper.ContactInfoMapper;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.service.interfaces.ContactInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/v1/contact-infos")
@RequiredArgsConstructor
public class ContactInfoController {

    private final ContactInfoService contactInfoService;
    private final ContactInfoMapper contactInfoMapper;

    @PostMapping
    public ResponseEntity<ContactInfoDto> createContactInfo(@Valid @RequestBody ContactInfoCreateRequestDto createDto) {
        ContactInfo entityToSave = contactInfoMapper.toEntity(createDto);
        ContactInfo savedEntity = contactInfoService.save(entityToSave);
        return new ResponseEntity<>(contactInfoMapper.toDto(savedEntity), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactInfoDto> getContactInfoById(@PathVariable Long id) {
        return contactInfoService.findById(id)
                .map(contactInfoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ContactInfoDto>> getAllContactInfos() {
        List<ContactInfoDto> dtoList = StreamSupport.stream(contactInfoService.findAll().spliterator(), false)
                .map(contactInfoMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactInfoDto> updateContactInfo(@PathVariable Long id, @Valid @RequestBody ContactInfoUpdateRequestDto updateDto) {
        ContactInfo entityFromDto = contactInfoMapper.toEntity(updateDto);
        ContactInfo updatedEntity = contactInfoService.updateContactInfo(id, entityFromDto);
        return ResponseEntity.ok(contactInfoMapper.toDto(updatedEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContactInfo(@PathVariable Long id) {
        contactInfoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}