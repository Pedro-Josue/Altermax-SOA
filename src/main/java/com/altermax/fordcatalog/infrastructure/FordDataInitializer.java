package com.altermax.fordcatalog.infrastructure;

import com.altermax.fordcatalog.domain.FordVehicle;
import com.altermax.fordcatalog.domain.FordVehicleSpecification;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(2)
public class FordDataInitializer implements ApplicationRunner {
    private final FordCatalogPersistence persistence;

    public FordDataInitializer(FordCatalogPersistence persistence) {
        this.persistence = persistence;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (persistence.count() > 0) {
            return;
        }
        persistence.save(
                vehicle(
                        "Ranger",
                        "Limited 3.0 V6 Diesel",
                        2025,
                        "BR",
                        new String[][] {
                            {"power_hp", "Potencia", "performance", "250", "hp"},
                            {"torque_nm", "Torque", "performance", "600", "Nm"},
                            {"engine_displacement_cc", "Cilindrada", "engine", "2993", "cc"},
                            {"fuel_type", "Combustivel", "engine", "diesel", null},
                            {"transmission", "Transmissao", "drivetrain", "automatic", null},
                            {"drivetrain", "Tracao", "drivetrain", "4WD", null},
                            {"length_mm", "Comprimento", "dimensions", "5370", "mm"},
                            {"curb_weight_kg", "Peso", "weight", "2346", "kg"},
                            {"seats", "Lugares", "capacity", "5", null}
                        }));
        persistence.save(
                vehicle(
                        "Mustang",
                        "GT Performance",
                        2024,
                        "BR",
                        new String[][] {
                            {"power_hp", "Potencia", "performance", "488", "hp"},
                            {"torque_nm", "Torque", "performance", "564", "Nm"},
                            {"engine_displacement_cc", "Cilindrada", "engine", "5038", "cc"},
                            {"fuel_type", "Combustivel", "engine", "gasoline", null},
                            {"transmission", "Transmissao", "drivetrain", "automatic", null},
                            {"drivetrain", "Tracao", "drivetrain", "RWD", null},
                            {"acceleration_0_100_s", "0-100 km/h", "performance", "4.3", "s"},
                            {"seats", "Lugares", "capacity", "4", null}
                        }));
        persistence.save(
                vehicle(
                        "Territory",
                        "Titanium 1.5 EcoBoost",
                        2025,
                        "BR",
                        new String[][] {
                            {"power_hp", "Potencia", "performance", "169", "hp"},
                            {"torque_nm", "Torque", "performance", "250", "Nm"},
                            {"engine_displacement_cc", "Cilindrada", "engine", "1490", "cc"},
                            {"fuel_type", "Combustivel", "engine", "gasoline", null},
                            {"transmission", "Transmissao", "drivetrain", "automatic", null},
                            {"drivetrain", "Tracao", "drivetrain", "FWD", null},
                            {"length_mm", "Comprimento", "dimensions", "4630", "mm"},
                            {"width_mm", "Largura", "dimensions", "1935", "mm"},
                            {"cargo_capacity_l", "Porta-malas", "capacity", "448", "L"},
                            {"seats", "Lugares", "capacity", "5", null}
                        }));
    }

    private FordVehicle vehicle(
            String model, String variant, int year, String market, String[][] data) {
        var vehicle = new FordVehicle(model, variant, year, market);
        for (var specificationData : data) {
            vehicle.addSpecification(
                    new FordVehicleSpecification(
                            specificationData[0],
                            specificationData[1],
                            specificationData[2],
                            specificationData[3],
                            specificationData[4]));
        }
        return vehicle;
    }
}
