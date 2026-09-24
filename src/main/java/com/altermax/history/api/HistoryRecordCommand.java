package com.altermax.history.api;

import java.time.Instant;
import java.util.List;

public record HistoryRecordCommand(
        Instant createdAt,
        Long requestedByUserId,
        Long fordVehicleId,
        String competitorExternalId,
        String competitorDisplayName,
        List<String> requestedAttributes,
        String resultSnapshotJson) {}
