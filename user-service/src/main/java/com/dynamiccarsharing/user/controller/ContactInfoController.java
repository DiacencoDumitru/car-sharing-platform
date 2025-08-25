package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.dto.ContactInfoDto;
import com.dynamiccarsharing.user.dto.ContactInfoCreateRequestDto;
import com.dynamiccarsharing.user.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.service.interfaces.ContactInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contact-infos")
@RequiredArgsConstructor
public class ContactInfoController {

    private final ContactInfoService contactInfoService;

    @PostMapping
    public ResponseEntity<ContactInfoDto> createContactInfo(@Valid @RequestBody ContactInfoCreateRequestDto createDto) {
        ContactInfoDto savedDto = contactInfoService.createContactInfo(createDto);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactInfoDto> getContactInfoById(@PathVariable Long id) {
        return contactInfoService.findContactInfoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<List<ContactInfoDto>> getAllContactInfos() {
        List<ContactInfoDto> dtoList = contactInfoService.findAllContactInfos();
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactInfoDto> updateContactInfo(@PathVariable Long id, @Valid @RequestBody ContactInfoUpdateRequestDto updateDto) {
        ContactInfoDto updatedDto = contactInfoService.updateContactInfo(id, updateDto);
        return ResponseEntity.ok(updatedDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContactInfo(@PathVariable Long id) {
        contactInfoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}