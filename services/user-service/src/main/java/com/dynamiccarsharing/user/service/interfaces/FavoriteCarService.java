package com.dynamiccarsharing.user.service.interfaces;

import java.util.List;

public interface FavoriteCarService {

    List<Long> listMyFavorites(Long userId);

    List<Long> listFavoritesForUserProfile(Long userId);

    List<Long> listUserIdsByFavoriteCarId(Long carId);

    void addFavorite(Long userId, Long carId);

    void removeFavorite(Long userId, Long carId);
}
