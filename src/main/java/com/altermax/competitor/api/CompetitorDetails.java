package com.altermax.competitor.api;

import java.util.List;

public record CompetitorDetails(
        String externalId,
        String brand,
        String model,
        String generation,
        String variant,
        Integer year,
        List<CompetitorSpecification> specifications) {}
