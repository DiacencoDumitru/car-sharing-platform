package com.dynamiccarsharing.carsharing.controller;

import com.dynamiccarsharing.carsharing.dto.TransactionDto;
import com.dynamiccarsharing.carsharing.mapper.TransactionMapper;
import com.dynamiccarsharing.carsharing.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        return transactionService.findById(id)
                .map(transactionMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAllTransactions() {
        List<TransactionDto> transactionDtos = transactionService.findAll().stream()
                .map(transactionMapper::toDto)
                .toList();
        return ResponseEntity.ok(transactionDtos);
    }
}