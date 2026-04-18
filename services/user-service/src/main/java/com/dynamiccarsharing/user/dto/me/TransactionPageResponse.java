package com.dynamiccarsharing.user.dto.me;

import lombok.Data;

import java.util.List;

@Data
public class TransactionPageResponse {
    private List<TransactionItemDto> content;
    private int totalPages;
    private long totalElements;
}
