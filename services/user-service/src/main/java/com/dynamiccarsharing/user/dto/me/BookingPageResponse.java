package com.dynamiccarsharing.user.dto.me;

import com.dynamiccarsharing.contracts.dto.BookingDto;
import lombok.Data;

import java.util.List;

@Data
public class BookingPageResponse {
    private List<BookingDto> content;
    private int totalPages;
    private long totalElements;
}
