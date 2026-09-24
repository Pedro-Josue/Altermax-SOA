package com.altermax.fordcatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "ford_vehicle_specifications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"ford_vehicle_id", "attribute_key"}))
public class FordVehicleSpecification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ford_vehicle_id")
    private FordVehicle vehicle;

    @Column(name = "attribute_key", nullable = false)
    private String attributeKey;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String category;

    @Column(name = "spec_value", nullable = false)
    private String value;

    private String unit;

    protected FordVehicleSpecification() {
    }

    public FordVehicleSpecification(
            String key, String label, String category, String value, String unit) {
        this.attributeKey = key;
        this.label = label;
        this.category = category;
        this.value = value;
        this.unit = unit;
    }

    void attach(FordVehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getAttributeKey() {
        return attributeKey;
    }

    public String getLabel() {
        return label;
    }

    public String getCategory() {
        return category;
    }

    public String getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }
}
