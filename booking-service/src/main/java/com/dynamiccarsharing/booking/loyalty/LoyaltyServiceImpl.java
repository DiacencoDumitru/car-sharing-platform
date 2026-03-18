package com.dynamiccarsharing.booking.loyalty;

import com.dynamiccarsharing.booking.model.LoyaltyAccount;
import com.dynamiccarsharing.booking.model.LoyaltyTransaction;
import com.dynamiccarsharing.booking.repository.LoyaltyAccountRepository;
import com.dynamiccarsharing.booking.repository.LoyaltyTransactionRepository;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class LoyaltyServiceImpl implements LoyaltyService {

    private static final BigDecimal EARN_RATE = new BigDecimal("0.05");

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    @Override
    public BigDecimal redeemPoints(Long renterId, Long paymentId, BigDecimal requestedPoints, BigDecimal initialAmount) {
        if (requestedPoints == null || requestedPoints.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        LoyaltyAccount account = accountRepository.findByRenterId(renterId)
                .orElseThrow(() -> new ValidationException("Loyalty account not found for renter " + renterId));

        if (account.getBalance().compareTo(requestedPoints) < 0) {
            throw new ValidationException("Insufficient loyalty points for renter " + renterId);
        }

        BigDecimal discount = requestedPoints.min(initialAmount);
        account.setBalance(account.getBalance().subtract(discount));
        accountRepository.save(account);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .account(account)
                .amount(discount)
                .earn(false)
                .paymentId(paymentId)
                .build();
        transactionRepository.save(transaction);

        return discount;
    }

    @Override
    public void earnPoints(Long renterId, Long paymentId, BigDecimal paidAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal points = paidAmount.multiply(EARN_RATE);

        LoyaltyAccount account = accountRepository.findByRenterId(renterId)
                .orElseGet(() -> LoyaltyAccount.builder()
                        .renterId(renterId)
                        .balance(BigDecimal.ZERO)
                        .build());

        account.setBalance(account.getBalance().add(points));
        account = accountRepository.save(account);

        LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                .account(account)
                .amount(points)
                .earn(true)
                .paymentId(paymentId)
                .build();
        transactionRepository.save(transaction);
    }
}

