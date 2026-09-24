package com.altermax.fordcatalog.infrastructure;

import com.altermax.fordcatalog.domain.FordVehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FordCatalogPersistence {
    private final FordVehicleRepository repository;

    public FordCatalogPersistence(FordVehicleRepository repository) {
        this.repository = repository;
    }

    public List<FordVehicle> findAll() {
        return repository.findAll();
    }

    public Optional<FordVehicle> findById(Long id) {
        return repository.findById(id);
    }

    public FordVehicle save(FordVehicle vehicle) {
        return repository.save(vehicle);
    }

    public FordVehicle saveAndFlush(FordVehicle vehicle) {
        return repository.saveAndFlush(vehicle);
    }

    public void delete(FordVehicle vehicle) {
        repository.delete(vehicle);
    }

    public boolean duplicate(String model, String variant, Integer modelYear, String market) {
        return repository.existsByModelAndVariantAndModelYearAndMarket(
                model, variant, modelYear, market);
    }

    public long count() {
        return repository.count();
    }
}
