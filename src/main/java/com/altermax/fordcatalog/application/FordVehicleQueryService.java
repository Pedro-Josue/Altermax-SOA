package com.altermax.fordcatalog.application;

import com.altermax.fordcatalog.api.FordSpecificationView;
import com.altermax.fordcatalog.api.FordVehicleCatalog;
import com.altermax.fordcatalog.api.FordVehicleView;
import com.altermax.fordcatalog.domain.FordVehicle;
import com.altermax.fordcatalog.infrastructure.FordCatalogPersistence;
import com.altermax.shared.error.ApiException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FordVehicleQueryService implements FordVehicleCatalog {
    private final FordCatalogPersistence persistence;

    public FordVehicleQueryService(FordCatalogPersistence persistence) {
        this.persistence = persistence;
    }

    @Transactional(readOnly = true)
    public List<FordVehicleView> list() {
        return persistence.findAll().stream().map(this::view).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FordVehicleView getById(Long id) {
        return view(persistence.findById(id).orElseThrow(() -> notFound(id)));
    }

    FordVehicleView view(FordVehicle vehicle) {
        return new FordVehicleView(
                vehicle.getId(),
                "Ford",
                vehicle.getModel(),
                vehicle.getVariant(),
                vehicle.getModelYear(),
                vehicle.getMarket(),
                vehicle.getSpecifications().stream()
                        .map(
                                specification ->
                                        new FordSpecificationView(
                                                specification.getAttributeKey(),
                                                specification.getLabel(),
                                                specification.getCategory(),
                                                specification.getValue(),
                                                specification.getUnit()))
                        .toList());
    }

    static ApiException notFound(Long id) {
        return new ApiException(
                HttpStatus.NOT_FOUND,
                "FORD_VEHICLE_NOT_FOUND",
                "Veiculo Ford " + id + " nao encontrado.");
    }
}
