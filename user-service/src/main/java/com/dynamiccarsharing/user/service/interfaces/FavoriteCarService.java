package com.dynamiccarsharing.user.service.interfaces;

import com.dynamiccarsharing.user.dto.FavoriteCarsResponseDto;

public interface FavoriteCarService {

    FavoriteCarsResponseDto listFavoriteCarIds(Long userId);

    void addFavorite(Long userId, Long carId);

    void removeFavorite(Long userId, Long carId);
}
