package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.criteria.TransactionSearchCriteria;
import com.dynamiccarsharing.booking.dto.TransactionDto;
import com.dynamiccarsharing.booking.service.interfaces.TransactionService;
import com.dynamiccarsharing.util.exception.ResourceNotFoundException;
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

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long id) {
        TransactionDto transactionDto = transactionService.findTransactionById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction with ID " + id + " not found."));
        return ResponseEntity.ok(transactionDto);
    }

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAllTransactions(TransactionSearchCriteria criteria) {
        List<TransactionDto> transactionDtos = criteria.hasAnyFilter()
                ? transactionService.searchTransactions(criteria)
                : transactionService.findAllTransactions();
        return ResponseEntity.ok(transactionDtos);
    }
}