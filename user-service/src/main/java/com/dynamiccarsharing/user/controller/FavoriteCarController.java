package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.user.dto.FavoriteCarsResponseDto;
import com.dynamiccarsharing.user.service.interfaces.FavoriteCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FavoriteCarController {

    private final FavoriteCarService favoriteCarService;

    @GetMapping("/users/{userId}/favorite-cars")
    @PreAuthorize("authentication.name.equals(#userId.toString())")
    public ResponseEntity<FavoriteCarsResponseDto> listFavorites(@PathVariable Long userId) {
        return ResponseEntity.ok(favoriteCarService.listFavoriteCarIds(userId));
    }

    @PutMapping("/users/{userId}/favorite-cars/{carId}")
    @PreAuthorize("authentication.name.equals(#userId.toString())")
    public ResponseEntity<Void> addFavorite(@PathVariable Long userId, @PathVariable Long carId) {
        favoriteCarService.addFavorite(userId, carId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/favorite-cars/{carId}")
    @PreAuthorize("authentication.name.equals(#userId.toString())")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long userId, @PathVariable Long carId) {
        favoriteCarService.removeFavorite(userId, carId);
        return ResponseEntity.noContent().build();
    }
}
