package com.altermax.fordcatalog.infrastructure;

import com.altermax.fordcatalog.domain.FordVehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface FordVehicleRepository extends JpaRepository<FordVehicle, Long> {
    @Override
    @EntityGraph(attributePaths = "specifications")
    Optional<FordVehicle> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "specifications")
    List<FordVehicle> findAll();

    boolean existsByModelAndVariantAndModelYearAndMarket(
            String model, String variant, Integer modelYear, String market);
}
