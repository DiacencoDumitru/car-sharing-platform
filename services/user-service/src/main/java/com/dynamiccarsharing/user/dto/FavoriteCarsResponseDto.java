package com.dynamiccarsharing.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCarsResponseDto {
    private List<Long> carIds;
}
