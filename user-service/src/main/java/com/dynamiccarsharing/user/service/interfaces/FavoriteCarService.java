package com.dynamiccarsharing.user.service.interfaces;

import java.util.List;

public interface FavoriteCarService {

    List<Long> listFavoriteCarIds(Long userId);

    void addFavorite(Long userId, Long carId);

    void removeFavorite(Long userId, Long carId);
}
