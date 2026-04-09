package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.user.service.interfaces.FavoriteCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/favorite-cars")
@RequiredArgsConstructor
public class FavoriteCarController {

    private final FavoriteCarService favoriteCarService;

    @GetMapping
    public ResponseEntity<List<Long>> listFavorites(@AuthenticationPrincipal Object principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(favoriteCarService.listFavoriteCarIds(userId));
    }

    @PutMapping("/{carId}")
    public ResponseEntity<Void> addFavorite(@AuthenticationPrincipal Object principal, @PathVariable Long carId) {
        Long userId = resolveUserId(principal);
        favoriteCarService.addFavorite(userId, carId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{carId}")
    public ResponseEntity<Void> removeFavorite(@AuthenticationPrincipal Object principal, @PathVariable Long carId) {
        Long userId = resolveUserId(principal);
        favoriteCarService.removeFavorite(userId, carId);
        return ResponseEntity.noContent().build();
    }

    private static Long resolveUserId(Object principal) {
        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
    }
}
