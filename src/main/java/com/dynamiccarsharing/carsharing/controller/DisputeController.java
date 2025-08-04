package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.DisputeCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.DisputeDto;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.mapper.DisputeMapper;
import com.dynamiccarsharing.carsharing.model.Dispute;
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.service.interfaces.DisputeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
=======
import java.util.ArrayList;
>>>>>>> fix/controller-mvc-tests
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;
<<<<<<< HEAD

    @PostMapping("/bookings/{bookingId}/dispute")
    public ResponseEntity<DisputeDto> createDispute(@PathVariable Long bookingId, @Valid @RequestBody DisputeCreateRequestDto createDto, @AuthenticationPrincipal UserDetails userDetails) {
        Long creationUserId = Long.parseLong(userDetails.getUsername());
        DisputeDto savedDto = disputeService.createDispute(bookingId, createDto, creationUserId);
        return new ResponseEntity<>(savedDto, HttpStatus.CREATED);
=======
    private final DisputeMapper disputeMapper;

    @PostMapping("/bookings/{bookingId}/dispute")
    public ResponseEntity<DisputeDto> createDispute(
            @PathVariable Long bookingId,
            @Valid @RequestBody DisputeCreateRequestDto createDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long creationUserId = Long.parseLong(userDetails.getUsername());

        Dispute disputeToSave = disputeMapper.toEntity(createDto, bookingId, creationUserId);
        Dispute savedDispute = disputeService.save(disputeToSave);
        return new ResponseEntity<>(disputeMapper.toDto(savedDispute), HttpStatus.CREATED);
>>>>>>> fix/controller-mvc-tests
    }

    @GetMapping("/admin/disputes")
    public ResponseEntity<List<DisputeDto>> getAllDisputes() {
<<<<<<< HEAD
        List<DisputeDto> dtoList = disputeService.findAllDisputes();
=======
        Iterable<Dispute> disputes = disputeService.findAll();

        List<DisputeDto> dtoList = new ArrayList<>();
        for (Dispute dispute : disputes) {
            dtoList.add(disputeMapper.toDto(dispute));
        }

>>>>>>> fix/controller-mvc-tests
        return ResponseEntity.ok(dtoList);
    }


    @GetMapping("/admin/disputes/{disputeId}")
    public ResponseEntity<DisputeDto> getDisputeById(@PathVariable Long disputeId) {
<<<<<<< HEAD
        return disputeService.findDisputeById(disputeId)
=======
        return disputeService.findById(disputeId)
                .map(disputeMapper::toDto)
>>>>>>> fix/controller-mvc-tests
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/admin/disputes/{disputeId}/resolve")
    public ResponseEntity<DisputeDto> resolveDispute(@PathVariable Long disputeId) {
<<<<<<< HEAD
        DisputeDto resolvedDto = disputeService.resolveDispute(disputeId);
        return ResponseEntity.ok(resolvedDto);
=======
        Dispute resolvedDispute = disputeService.resolveDispute(disputeId);
        return ResponseEntity.ok(disputeMapper.toDto(resolvedDispute));
>>>>>>> fix/controller-mvc-tests
    }

    @DeleteMapping("/admin/disputes/{disputeId}")
    public ResponseEntity<Void> deleteDispute(@PathVariable Long disputeId) {
        disputeService.deleteById(disputeId);
        return ResponseEntity.noContent().build();
    }
}