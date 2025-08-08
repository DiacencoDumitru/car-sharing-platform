package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.TransactionDto;
import com.dynamiccarsharing.carsharing.mapper.TransactionMapper;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionRepository, transactionMapper);
    }

    @Test
    void findTransactionById_whenExists_shouldMapAndReturnDto() {
        Long id = 1L;
        Transaction transactionEntity = Transaction.builder().id(id).build();
        TransactionDto expectedDto = new TransactionDto();
        expectedDto.setId(id);

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transactionEntity));
        when(transactionMapper.toDto(transactionEntity)).thenReturn(expectedDto);

        Optional<TransactionDto> result = transactionService.findTransactionById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findTransactionById_whenNotExists_shouldReturnEmptyOptional() {
        Long id = 1L;
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        Optional<TransactionDto> result = transactionService.findTransactionById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void findAllTransactions_shouldMapAndReturnDtoList() {
        Transaction entity = Transaction.builder().id(1L).build();
        TransactionDto dto = new TransactionDto();
        dto.setId(1L);

        when(transactionRepository.findAll()).thenReturn(Collections.singletonList(entity));
        when(transactionMapper.toDto(entity)).thenReturn(dto);

        List<TransactionDto> results = transactionService.findAllTransactions();

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
    }

    @Test
    void findTransactionsByBookingId_shouldReturnEntityList() {
        Long bookingId = 1L;
        Transaction transaction = Transaction.builder()
                .booking(Booking.builder().id(bookingId).build()).build();
        when(transactionRepository.findByBookingId(bookingId)).thenReturn(Collections.singletonList(transaction));

        List<Transaction> results = transactionService.findTransactionsByBookingId(bookingId);

        assertEquals(1, results.size());
        assertEquals(bookingId, results.get(0).getBooking().getId());
    }
}