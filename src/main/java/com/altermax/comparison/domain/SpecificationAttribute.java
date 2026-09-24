package com.altermax.comparison.domain;

public enum SpecificationAttribute {
    POWER_HP("power_hp", "hp", "performance"),
    TORQUE_NM("torque_nm", "Nm", "performance"),
    ENGINE_DISPLACEMENT_CC("engine_displacement_cc", "cc", "engine"),
    FUEL_TYPE("fuel_type", null, "engine"),
    TRANSMISSION("transmission", null, "drivetrain"),
    DRIVETRAIN("drivetrain", null, "drivetrain"),
    TOP_SPEED_KMH("top_speed_kmh", "km/h", "performance"),
    ACCELERATION_0_100_S("acceleration_0_100_s", "s", "performance"),
    FUEL_CONSUMPTION_L_100KM("fuel_consumption_l_100km", "L/100km", "efficiency"),
    CO2_G_KM("co2_g_km", "g/km", "emissions"),
    LENGTH_MM("length_mm", "mm", "dimensions"),
    WIDTH_MM("width_mm", "mm", "dimensions"),
    HEIGHT_MM("height_mm", "mm", "dimensions"),
    WHEELBASE_MM("wheelbase_mm", "mm", "dimensions"),
    CURB_WEIGHT_KG("curb_weight_kg", "kg", "weight"),
    CARGO_CAPACITY_L("cargo_capacity_l", "L", "capacity"),
    SEATS("seats", null, "capacity"),
    PRICE("price", null, "price");

    private final String key;
    private final String unit;
    private final String category;

    SpecificationAttribute(String key, String unit, String category) {
        this.key = key;
        this.unit = unit;
        this.category = category;
    }

    public String key() {
        return key;
    }

    public String unit() {
        return unit;
    }

    public String category() {
        return category;
    }

    public static SpecificationAttribute fromKey(String key) {
        for (var attribute : values()) {
            if (attribute.key.equals(key)) {
                return attribute;
            }
        }
        return null;
    }
}
