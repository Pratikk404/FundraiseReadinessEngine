package com.fundraise.engine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank(message = "Plan is required")
    private String plan; // "pro" or "advisor"
}
