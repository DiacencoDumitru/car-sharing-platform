package com.dynamiccarsharing.user.service.interfaces;

import java.util.List;

public interface FavoriteCarService {

    /**
     * For {@code GET /api/v1/users/me/favorite-cars}: most recently added first.
     */
    List<Long> listMyFavorites(Long userId);

    /**
     * For {@code GET /api/v1/users/{userId}/favorite-cars}: sorted by car ID ascending.
     */
    List<Long> listFavoritesForUserProfile(Long userId);

    void addFavorite(Long userId, Long carId);

    void removeFavorite(Long userId, Long carId);
}
