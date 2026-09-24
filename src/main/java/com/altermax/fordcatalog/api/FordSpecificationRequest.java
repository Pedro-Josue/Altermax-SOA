package com.altermax.fordcatalog.api;

import jakarta.validation.constraints.NotBlank;

public record FordSpecificationRequest(
        @NotBlank String key,
        @NotBlank String label,
        @NotBlank String category,
        @NotBlank String value,
        String unit) {}
