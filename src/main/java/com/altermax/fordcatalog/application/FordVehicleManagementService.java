package com.altermax.fordcatalog.application;

import com.altermax.fordcatalog.api.FordVehicleRequest;
import com.altermax.fordcatalog.api.FordVehicleView;
import com.altermax.fordcatalog.domain.FordVehicle;
import com.altermax.fordcatalog.infrastructure.FordCatalogPersistence;
import com.altermax.shared.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FordVehicleManagementService {
    private final FordCatalogPersistence persistence;
    private final FordSpecificationService specs;
    private final FordVehicleQueryService queries;

    public FordVehicleManagementService(
            FordCatalogPersistence persistence,
            FordSpecificationService specs,
            FordVehicleQueryService queries) {
        this.persistence = persistence;
        this.specs = specs;
        this.queries = queries;
    }

    @Transactional
    public FordVehicleView create(FordVehicleRequest request) {
        rejectDuplicate(request);
        var vehicle =
                new FordVehicle(
                        request.model().trim(),
                        request.variant().trim(),
                        request.modelYear(),
                        request.market().trim());
        specs.replace(vehicle, request.specifications());
        return queries.view(persistence.save(vehicle));
    }

    @Transactional
    public FordVehicleView update(Long id, FordVehicleRequest request) {
        var vehicle =
                persistence.findById(id).orElseThrow(() -> FordVehicleQueryService.notFound(id));
        boolean identityChanged =
                !vehicle.getModel().equals(request.model().trim())
                        || !vehicle.getVariant().equals(request.variant().trim())
                        || !vehicle.getModelYear().equals(request.modelYear())
                        || !vehicle.getMarket().equals(request.market().trim());
        if (identityChanged) {
            rejectDuplicate(request);
        }
        vehicle.update(
                request.model().trim(),
                request.variant().trim(),
                request.modelYear(),
                request.market().trim());
        vehicle.replaceSpecifications(java.util.List.of());
        persistence.saveAndFlush(vehicle);
        specs.replace(vehicle, request.specifications());
        return queries.view(persistence.save(vehicle));
    }

    @Transactional
    public void delete(Long id) {
        var vehicle =
                persistence.findById(id).orElseThrow(() -> FordVehicleQueryService.notFound(id));
        persistence.delete(vehicle);
    }

    private void rejectDuplicate(FordVehicleRequest request) {
        if (persistence.duplicate(
                request.model().trim(),
                request.variant().trim(),
                request.modelYear(),
                request.market().trim())) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "FORD_VEHICLE_CONFLICT", "Este Ford ja esta cadastrado.");
        }
    }
}
