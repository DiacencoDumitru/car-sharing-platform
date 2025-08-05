package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.DisputeDto;
import com.dynamiccarsharing.carsharing.service.interfaces.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping("/bookings/{bookingId}/dispute")
    public ResponseEntity<DisputeDto> createDispute(@PathVariable Long bookingId, @Valid @RequestBody DisputeCreateRequestDto createDto, @AuthenticationPrincipal UserDetails userDetails) {
        Long creationUserId = Long.parseLong(userDetails.getUsername());
        DisputeDto savedDto = disputeService.createDispute(bookingId, createDto, creationUserId);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
    }

    @GetMapping("/admin/disputes")
    public ResponseEntity<List<DisputeDto>> getAllDisputes() {
        List<DisputeDto> dtoList = disputeService.findAllDisputes();
        return ResponseEntity.ok(dtoList);
    }


    @GetMapping("/admin/disputes/{disputeId}")
    public ResponseEntity<DisputeDto> getDisputeById(@PathVariable Long disputeId) {
        return disputeService.findDisputeById(disputeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PatchMapping("/admin/disputes/{disputeId}/resolve")
    public ResponseEntity<DisputeDto> resolveDispute(@PathVariable Long disputeId) {
        DisputeDto resolvedDto = disputeService.resolveDispute(disputeId);
        return ResponseEntity.ok(resolvedDto);
    }

    @DeleteMapping("/admin/disputes/{disputeId}")
    public ResponseEntity<Void> deleteDispute(@PathVariable Long disputeId) {
        disputeService.deleteById(disputeId);
        return ResponseEntity.noContent().build();
    }
}