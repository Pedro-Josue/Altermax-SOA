package com.altermax.competitor.api;

public record CompetitorSummary(
        String externalId,
        String brand,
        String model,
        String generation,
        String variant,
        Integer year) {}
