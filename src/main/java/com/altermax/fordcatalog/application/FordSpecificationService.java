package com.altermax.fordcatalog.application;

import com.altermax.fordcatalog.api.FordSpecificationRequest;
import com.altermax.fordcatalog.domain.FordVehicle;
import com.altermax.fordcatalog.domain.FordVehicleSpecification;
import com.altermax.shared.error.ApiException;
import java.util.HashSet;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class FordSpecificationService {
    public void replace(FordVehicle vehicle, List<FordSpecificationRequest> requests) {
        var keys = new HashSet<String>();
        var specifications =
                requests.stream()
                        .map(
                                request -> {
                                    String key = request.key().trim().toLowerCase();
                                    if (!keys.add(key)) {
                                        throw new ApiException(
                                                HttpStatus.CONFLICT,
                                                "DUPLICATE_SPECIFICATION",
                                                "Atributo Ford duplicado: " + key);
                                    }
                                    return new FordVehicleSpecification(
                                            key,
                                            request.label().trim(),
                                            request.category().trim(),
                                            request.value().trim(),
                                            blankToNull(request.unit()));
                                })
                        .toList();
        vehicle.replaceSpecifications(specifications);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
