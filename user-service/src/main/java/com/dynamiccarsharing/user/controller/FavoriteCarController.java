package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.user.dto.FavoriteCarsResponseDto;
import com.dynamiccarsharing.user.service.interfaces.FavoriteCarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FavoriteCarController {

    private final FavoriteCarService favoriteCarService;

    @GetMapping("/users/me/favorite-cars")
    public ResponseEntity<List<Long>> listMine(@AuthenticationPrincipal Object principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(favoriteCarService.listMyFavorites(userId));
    }

    @PutMapping("/users/me/favorite-cars/{carId}")
    public ResponseEntity<Void> addMine(@AuthenticationPrincipal Object principal, @PathVariable Long carId) {
        favoriteCarService.addFavorite(resolveUserId(principal), carId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/me/favorite-cars/{carId}")
    public ResponseEntity<Void> removeMine(@AuthenticationPrincipal Object principal, @PathVariable Long carId) {
        favoriteCarService.removeFavorite(resolveUserId(principal), carId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}/favorite-cars")
    @PreAuthorize("authentication.name.equals(#userId.toString())")
    public ResponseEntity<FavoriteCarsResponseDto> listForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(new FavoriteCarsResponseDto(favoriteCarService.listFavoritesForUserProfile(userId)));
    }

    @PutMapping("/users/{userId}/favorite-cars/{carId}")
    @PreAuthorize("authentication.name.equals(#userId.toString())")
    public ResponseEntity<Void> addForUser(@PathVariable Long userId, @PathVariable Long carId) {
        favoriteCarService.addFavorite(userId, carId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}/favorite-cars/{carId}")
    @PreAuthorize("authentication.name.equals(#userId.toString())")
    public ResponseEntity<Void> removeForUser(@PathVariable Long userId, @PathVariable Long carId) {
        favoriteCarService.removeFavorite(userId, carId);
        return ResponseEntity.noContent().build();
    }

    private static Long resolveUserId(Object principal) {
        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
    }
}
