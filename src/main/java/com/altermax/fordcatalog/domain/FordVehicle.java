package com.altermax.fordcatalog.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "ford_vehicles",
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"model", "variant", "model_year", "market"}))
public class FordVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String variant;

    @Column(name = "model_year", nullable = false)
    private Integer modelYear;

    @Column(nullable = false)
    private String market;

    @OneToMany(
            mappedBy = "vehicle",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<FordVehicleSpecification> specifications = new ArrayList<>();

    protected FordVehicle() {
    }

    public FordVehicle(String model, String variant, Integer modelYear, String market) {
        update(model, variant, modelYear, market);
    }

    public void update(String model, String variant, Integer modelYear, String market) {
        this.model = model;
        this.variant = variant;
        this.modelYear = modelYear;
        this.market = market;
    }

    public void replaceSpecifications(List<FordVehicleSpecification> specs) {
        specifications.clear();
        specs.forEach(this::addSpecification);
    }

    public void addSpecification(FordVehicleSpecification spec) {
        spec.attach(this);
        specifications.add(spec);
    }

    public Long getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getVariant() {
        return variant;
    }

    public Integer getModelYear() {
        return modelYear;
    }

    public String getMarket() {
        return market;
    }

    public List<FordVehicleSpecification> getSpecifications() {
        return List.copyOf(specifications);
    }
}
