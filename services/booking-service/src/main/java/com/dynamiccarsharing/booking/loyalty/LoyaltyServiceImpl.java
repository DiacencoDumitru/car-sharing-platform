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
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoyaltyServiceImpl implements LoyaltyService {

    private static final BigDecimal EARN_RATE = new BigDecimal("0.05");

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    @Override
    public BigDecimal redeemPoints(Long renterId, Long paymentId, BigDecimal requestedPoints, BigDecimal initialAmount) {
        LoyaltyAccount account = findAccountForRedemption(renterId, requestedPoints);
        BigDecimal discount = requestedPoints.min(initialAmount).max(BigDecimal.ZERO);
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
    @Transactional(readOnly = true)
    public BigDecimal previewRedeemAmount(Long renterId, BigDecimal requestedPoints, BigDecimal initialAmount) {
        if (requestedPoints == null || requestedPoints.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        LoyaltyAccount account = findAccountForRedemption(renterId, requestedPoints);
        return requestedPoints.min(initialAmount).max(BigDecimal.ZERO);
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

    @Override
    public void reverseLoyaltyForPayment(Long renterId, Long paymentId) {
        List<LoyaltyTransaction> transactions = transactionRepository.findByPaymentId(paymentId);
        if (transactions.isEmpty()) {
            return;
        }

        LoyaltyAccount account = null;
        BigDecimal balanceDelta = BigDecimal.ZERO;

        for (LoyaltyTransaction transaction : transactions) {
            LoyaltyAccount txAccount = transaction.getAccount();
            if (!txAccount.getRenterId().equals(renterId)) {
                throw new ValidationException("Loyalty transaction does not belong to renter " + renterId);
            }
            if (account == null) {
                account = txAccount;
            } else if (!account.getId().equals(txAccount.getId())) {
                throw new ValidationException("Loyalty transactions for payment " + paymentId + " span multiple accounts");
            }

            if (Boolean.TRUE.equals(transaction.getEarn())) {
                balanceDelta = balanceDelta.subtract(transaction.getAmount());
            } else {
                balanceDelta = balanceDelta.add(transaction.getAmount());
            }
        }

        BigDecimal newBalance = account.getBalance().add(balanceDelta);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Insufficient loyalty balance to reverse transactions for payment " + paymentId);
        }

        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    private LoyaltyAccount findAccountForRedemption(Long renterId, BigDecimal requestedPoints) {
        LoyaltyAccount account = accountRepository.findByRenterId(renterId)
                .orElseThrow(() -> new ValidationException("Loyalty account not found for renter " + renterId));
        if (account.getBalance().compareTo(requestedPoints) < 0) {
            throw new ValidationException("Insufficient loyalty points for renter " + renterId);
        }
        return account;
    }
}

