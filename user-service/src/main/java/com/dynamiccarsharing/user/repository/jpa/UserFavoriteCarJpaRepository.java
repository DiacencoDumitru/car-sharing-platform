package com.dynamiccarsharing.user.repository.jpa;

import com.dynamiccarsharing.user.model.UserFavoriteCar;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Profile("jpa")
@Repository
public interface UserFavoriteCarJpaRepository extends JpaRepository<UserFavoriteCar, Long> {

    List<UserFavoriteCar> findByUser_IdOrderByCarIdAsc(Long userId);

    boolean existsByUser_IdAndCarId(Long userId, Long carId);

    void deleteByUser_IdAndCarId(Long userId, Long carId);
}
