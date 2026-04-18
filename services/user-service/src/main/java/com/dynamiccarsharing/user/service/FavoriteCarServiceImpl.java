package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.user.exception.UserNotFoundException;
import com.dynamiccarsharing.user.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.user.model.FavoriteCar;
import com.dynamiccarsharing.user.repository.UserRepository;
import com.dynamiccarsharing.user.repository.jpa.FavoriteCarJpaRepository;
import com.dynamiccarsharing.user.service.interfaces.FavoriteCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteCarServiceImpl implements FavoriteCarService {

    private final UserRepository userRepository;
    private final FavoriteCarJpaRepository favoriteCarJpaRepository;
    private final CarIntegrationClient carIntegrationClient;

    @Override
    @Transactional(readOnly = true)
    public List<Long> listMyFavorites(Long userId) {
        assertUserExists(userId);
        return favoriteCarJpaRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FavoriteCar::getCarId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> listFavoritesForUserProfile(Long userId) {
        assertUserExists(userId);
        return favoriteCarJpaRepository.findByUserIdOrderByCarIdAsc(userId).stream()
                .map(FavoriteCar::getCarId)
                .toList();
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long carId) {
        assertUserExists(userId);
        carIntegrationClient.assertCarExists(carId);
        if (favoriteCarJpaRepository.findByUserIdAndCarId(userId, carId).isPresent()) {
            return;
        }
        favoriteCarJpaRepository.save(FavoriteCar.builder().userId(userId).carId(carId).build());
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long carId) {
        assertUserExists(userId);
        favoriteCarJpaRepository.deleteByUserIdAndCarId(userId, carId);
    }

    private void assertUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " was not found."));
    }
}
