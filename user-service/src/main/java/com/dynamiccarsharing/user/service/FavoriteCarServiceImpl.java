package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.user.model.FavoriteCar;
import com.dynamiccarsharing.user.repository.jpa.FavoriteCarJpaRepository;
import com.dynamiccarsharing.user.service.interfaces.FavoriteCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile("jpa")
@Service
@RequiredArgsConstructor
public class FavoriteCarServiceImpl implements FavoriteCarService {

    private final FavoriteCarJpaRepository favoriteCarJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Long> listFavoriteCarIds(Long userId) {
        return favoriteCarJpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FavoriteCar::getCarId)
                .toList();
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long carId) {
        if (favoriteCarJpaRepository.findByUserIdAndCarId(userId, carId).isPresent()) {
            return;
        }
        favoriteCarJpaRepository.save(FavoriteCar.builder().userId(userId).carId(carId).build());
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long carId) {
        favoriteCarJpaRepository.deleteByUserIdAndCarId(userId, carId);
    }
}
