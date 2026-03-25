package com.dynamiccarsharing.notification.fraud;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.notification.analytics.BookingLifecycleAnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimpleAntiFraudService implements AntiFraudService {

    private final BookingLifecycleAnalyticsEventRepository analyticsRepository;

    @Value("${fraud.approve-cancel-window-seconds:3600}")
    private long approveCancelWindowSeconds;

    @Override
    public FraudDecision evaluate(BookingLifecycleEventDto event) {
        if (event == null || event.getBookingStatus() == null || event.getRenterId() == null) {
            return new FraudDecision(0, false);
        }

        TransactionStatus status = event.getBookingStatus();
        Long renterId = event.getRenterId();

        if (status == TransactionStatus.APPROVED && renterId % 2 == 0) {
            return new FraudDecision(80, true);
        }

        if (renterId % 2 != 0) {
            return new FraudDecision(10, false);
        }

        if (status == TransactionStatus.COMPLETED || status == TransactionStatus.CANCELED) {
            return analyticsRepository.findByBookingIdAndBookingStatus(event.getBookingId(), TransactionStatus.APPROVED)
                    .map(approvedAnalytics -> {
                        if (approvedAnalytics.getOccurredAt() == null || event.getOccurredAt() == null) {
                            return new FraudDecision(10, false);
                        }

                        long secondsBetween = Math.abs(event.getOccurredAt().getEpochSecond() - approvedAnalytics.getOccurredAt().getEpochSecond());
                        if (secondsBetween <= approveCancelWindowSeconds) {
                            int risk = status == TransactionStatus.CANCELED ? 85 : 65;
                            return new FraudDecision(risk, true);
                        }

                        return new FraudDecision(10, false);
                    })
                    .orElseGet(() -> new FraudDecision(10, false));
        }

        return new FraudDecision(10, false);
    }
}
