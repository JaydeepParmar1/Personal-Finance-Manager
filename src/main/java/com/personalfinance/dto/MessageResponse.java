package com.personalfinance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response DTO carrying a single confirmation or status message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    /** The message text. */
    private String message;
}
