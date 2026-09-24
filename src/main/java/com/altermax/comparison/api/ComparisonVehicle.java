package com.altermax.comparison.api;

import java.util.Map;

public record ComparisonVehicle(
        String source,
        String brand,
        String model,
        String variant,
        Map<String, NormalizedSpecification> specifications) {}
