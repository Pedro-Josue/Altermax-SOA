package com.altermax.history.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "comparison_history")
public class ComparisonHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Long requestedByUserId;

    @Column(nullable = false)
    private Long fordVehicleId;

    @Column(nullable = false)
    private String competitorExternalId;

    @Column(nullable = false)
    private String competitorDisplayName;

    @Lob
    @Column(nullable = false)
    private String requestedAttributesJson;

    @Lob
    @Column(nullable = false)
    private String resultSnapshotJson;

    protected ComparisonHistory() {
    }

    public ComparisonHistory(
            Instant createdAt,
            Long requestedByUserId,
            Long fordVehicleId,
            String competitorExternalId,
            String competitorDisplayName,
            String requestedAttributesJson,
            String resultSnapshotJson) {
        this.createdAt = createdAt;
        this.requestedByUserId = requestedByUserId;
        this.fordVehicleId = fordVehicleId;
        this.competitorExternalId = competitorExternalId;
        this.competitorDisplayName = competitorDisplayName;
        this.requestedAttributesJson = requestedAttributesJson;
        this.resultSnapshotJson = resultSnapshotJson;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getRequestedByUserId() {
        return requestedByUserId;
    }

    public Long getFordVehicleId() {
        return fordVehicleId;
    }

    public String getCompetitorExternalId() {
        return competitorExternalId;
    }

    public String getCompetitorDisplayName() {
        return competitorDisplayName;
    }

    public String getRequestedAttributesJson() {
        return requestedAttributesJson;
    }

    public String getResultSnapshotJson() {
        return resultSnapshotJson;
    }
}
