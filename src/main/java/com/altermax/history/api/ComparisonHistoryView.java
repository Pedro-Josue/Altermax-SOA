package com.altermax.history.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.List;

public record ComparisonHistoryView(
        Long id,
        Instant createdAt,
        Long fordVehicleId,
        String competitorExternalId,
        String competitorDisplayName,
        List<String> requestedAttributes,
        JsonNode resultSnapshot) {}
