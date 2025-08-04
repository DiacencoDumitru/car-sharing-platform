package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoDto;
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.mapper.ContactInfoMapper;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.service.interfaces.ContactInfoService;
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
@RequestMapping("/api/v1/contact-infos")
@RequiredArgsConstructor
public class ContactInfoController {

    private final ContactInfoService contactInfoService;
<<<<<<< HEAD

    @PostMapping
    public ResponseEntity<ContactInfoDto> createContactInfo(@Valid @RequestBody ContactInfoCreateRequestDto createDto) {
        ContactInfoDto savedDto = contactInfoService.createContactInfo(createDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
=======
    private final ContactInfoMapper contactInfoMapper;

    @PostMapping
    public ResponseEntity<ContactInfoDto> createContactInfo(@Valid @RequestBody ContactInfoCreateRequestDto createDto) {
        ContactInfo entityToSave = contactInfoMapper.toEntity(createDto);
        ContactInfo savedEntity = contactInfoService.save(entityToSave);
        return new ResponseEntity<>(contactInfoMapper.toDto(savedEntity), HttpStatus.CREATED);
>>>>>>> fix/controller-mvc-tests
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactInfoDto> getContactInfoById(@PathVariable Long id) {
<<<<<<< HEAD
        return contactInfoService.findContactInfoById(id)
=======
        return contactInfoService.findById(id)
                .map(contactInfoMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ContactInfoDto>> getAllContactInfos() {
<<<<<<< HEAD
        List<ContactInfoDto> dtoList = contactInfoService.findAllContactInfos();
=======
        List<ContactInfoDto> dtoList = StreamSupport.stream(contactInfoService.findAll().spliterator(), false)
                .map(contactInfoMapper::toDto)
                .toList();
>>>>>>> fix/controller-mvc-tests
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactInfoDto> updateContactInfo(@PathVariable Long id, @Valid @RequestBody ContactInfoUpdateRequestDto updateDto) {
<<<<<<< HEAD
        ContactInfoDto updatedDto = contactInfoService.updateContactInfo(id, updateDto);
        return ResponseEntity.ok(updatedDto);
=======
        ContactInfo entityFromDto = contactInfoMapper.toEntity(updateDto);
        ContactInfo updatedEntity = contactInfoService.updateContactInfo(id, entityFromDto);
        return ResponseEntity.ok(contactInfoMapper.toDto(updatedEntity));
>>>>>>> fix/controller-mvc-tests
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContactInfo(@PathVariable Long id) {
        contactInfoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}