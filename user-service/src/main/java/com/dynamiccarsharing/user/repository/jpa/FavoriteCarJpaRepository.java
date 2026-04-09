package com.dynamiccarsharing.user.repository.jpa;

import com.dynamiccarsharing.user.model.FavoriteCar;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface FavoriteCarJpaRepository extends JpaRepository<FavoriteCar, Long> {

    List<FavoriteCar> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<FavoriteCar> findByUserIdAndCarId(Long userId, Long carId);

    void deleteByUserIdAndCarId(Long userId, Long carId);
}
