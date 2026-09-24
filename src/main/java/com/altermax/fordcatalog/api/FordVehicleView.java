package com.altermax.fordcatalog.api;

import java.util.List;

public record FordVehicleView(
        Long id,
        String brand,
        String model,
        String variant,
        Integer modelYear,
        String market,
        List<FordSpecificationView> specifications) {}
