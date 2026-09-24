package com.altermax.fordcatalog.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record FordVehicleRequest(
        @NotBlank String model,
        @NotBlank String variant,
        @NotNull
        @Min(1903)
        @Max(2100)
        Integer modelYear,
        @NotBlank String market,
        @NotEmpty List<@Valid FordSpecificationRequest> specifications) {}
