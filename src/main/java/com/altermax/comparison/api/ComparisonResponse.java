package com.altermax.comparison.api;

import java.time.Instant;
import java.util.List;

public record ComparisonResponse(
        Long comparisonId,
        Instant createdAt,
        List<String> attributes,
        List<ComparisonVehicle> vehicles) {}
