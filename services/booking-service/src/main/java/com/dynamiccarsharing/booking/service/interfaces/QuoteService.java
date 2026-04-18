package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.dto.QuoteRequestDto;
import com.dynamiccarsharing.booking.dto.QuoteResponseDto;

public interface QuoteService {
    QuoteResponseDto calculateQuote(QuoteRequestDto requestDto);
}
