package com.altermax.shared.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

public record ApiError(
        @Schema(description = "Instante UTC em que o erro foi produzido") Instant timestamp,
        @Schema(example = "400") int status,
        @Schema(example = "Bad Request") String error,
        @Schema(example = "VALIDATION_ERROR") String code,
        @Schema(example = "A requisicao possui campos invalidos.") String message,
        @Schema(example = "/api/comparisons") String path,
        @Schema(example = "[\"attributes: must not be empty\"]") List<String> details) {}
