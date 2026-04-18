package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {

    Optional<PromoCode> findByCodeAndActiveIsTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            String code,
            LocalDateTime startAt,
            LocalDateTime endAt
    );
}

