package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.QuoteRequestDto;
import com.dynamiccarsharing.booking.dto.QuoteResponseDto;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.loyalty.LoyaltyService;
import com.dynamiccarsharing.booking.pricing.PriceComponents;
import com.dynamiccarsharing.booking.pricing.PricingContext;
import com.dynamiccarsharing.booking.pricing.PricingService;
import com.dynamiccarsharing.booking.service.interfaces.QuoteService;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteServiceImpl implements QuoteService {

    private static final String CURRENCY = "USD";
    private static final long QUOTE_TTL_MINUTES = 15L;

    private final CarIntegrationClient carIntegrationClient;
    private final PricingService pricingService;
    private final LoyaltyService loyaltyService;

    @Override
    public QuoteResponseDto calculateQuote(QuoteRequestDto requestDto) {
        if (!requestDto.getStartTime().isBefore(requestDto.getEndTime())) {
            throw new ValidationException("Validation failed: The end time must be after the start time.");
        }

        carIntegrationClient.assertCarAvailable(requestDto.getCarId());

        PricingContext pricingContext = new PricingContext(
                null,
                requestDto.getRenterId(),
                requestDto.getCarId(),
                null,
                requestDto.getPickupLocationId(),
                requestDto.getStartTime(),
                requestDto.getEndTime(),
                requestDto.getPromoCode()
        );

        PriceComponents components = pricingService.calculatePriceComponents(pricingContext);
        BigDecimal loyaltyAmount = calculateLoyaltyDiscount(
                requestDto.getRenterId(),
                requestDto.getLoyaltyPointsToUse(),
                components.total()
        );
        BigDecimal total = components.total().subtract(loyaltyAmount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return QuoteResponseDto.builder()
                .renterId(requestDto.getRenterId())
                .carId(requestDto.getCarId())
                .baseAmount(components.basePrice())
                .dynamicMarkupAmount(components.dynamicMarkup())
                .discountAmount(components.discounts())
                .loyaltyAmount(loyaltyAmount)
                .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                .currency(CURRENCY)
                .expiresAt(LocalDateTime.now().plusMinutes(QUOTE_TTL_MINUTES))
                .build();
    }

    private BigDecimal calculateLoyaltyDiscount(Long renterId, BigDecimal requestedPoints, BigDecimal amountBeforeLoyalty) {
        if (requestedPoints == null || requestedPoints.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal redeemedAmount = loyaltyService.previewRedeemAmount(renterId, requestedPoints, amountBeforeLoyalty);
        return redeemedAmount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
