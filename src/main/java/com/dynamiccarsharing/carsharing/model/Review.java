package com.dynamiccarsharing.carsharing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Review {
    private Long id;
    private Long reviewerId;
    private Long targetId; // carId or userId
    private String type; // like: "car" or "user"
    private String comment;
}
