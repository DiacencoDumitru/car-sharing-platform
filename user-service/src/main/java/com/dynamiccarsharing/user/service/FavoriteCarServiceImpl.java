package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.user.dto.FavoriteCarsResponseDto;
import com.dynamiccarsharing.user.exception.UserNotFoundException;
import com.dynamiccarsharing.user.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.model.UserFavoriteCar;
import com.dynamiccarsharing.user.repository.UserRepository;
import com.dynamiccarsharing.user.repository.jpa.UserFavoriteCarJpaRepository;
import com.dynamiccarsharing.user.service.interfaces.FavoriteCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteCarServiceImpl implements FavoriteCarService {

    private final UserRepository userRepository;
    private final UserFavoriteCarJpaRepository favoriteCarRepository;
    private final CarIntegrationClient carIntegrationClient;

    @Override
    @Transactional(readOnly = true)
    public FavoriteCarsResponseDto listFavoriteCarIds(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " was not found."));
        List<Long> ids = favoriteCarRepository.findByUser_IdOrderByCarIdAsc(userId).stream()
                .map(UserFavoriteCar::getCarId)
                .toList();
        return new FavoriteCarsResponseDto(ids);
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long carId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " was not found."));
        carIntegrationClient.assertCarExists(carId);
        if (favoriteCarRepository.existsByUser_IdAndCarId(userId, carId)) {
            return;
        }
        favoriteCarRepository.save(UserFavoriteCar.builder().user(user).carId(carId).build());
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long carId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " was not found."));
        favoriteCarRepository.deleteByUser_IdAndCarId(userId, carId);
    }
}
