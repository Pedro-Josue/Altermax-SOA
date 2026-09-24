package com.altermax.comparison.application;

import com.altermax.comparison.api.NormalizedSpecification;
import com.altermax.comparison.domain.SpecificationAttribute;
import com.altermax.competitor.api.CompetitorSpecification;
import com.altermax.fordcatalog.api.FordSpecificationView;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SpecificationNormalizationService {
    private final UnitNormalizationService units;

    public SpecificationNormalizationService(UnitNormalizationService units) {
        this.units = units;
    }

    public Map<String, NormalizedSpecification> ford(
            List<String> requested, List<FordSpecificationView> specs) {
        Map<String, FordSpecificationView> byKey = new HashMap<>();
        specs.forEach(specification -> byKey.put(specification.key(), specification));
        return align(
                requested,
                key -> {
                    var specification = byKey.get(key);
                    return specification == null
                            ? null
                            : new Raw(specification.value(), specification.unit());
                });
    }

    public Map<String, NormalizedSpecification> competitor(
            List<String> requested, List<CompetitorSpecification> specs) {
        Map<String, CompetitorSpecification> byKey = new HashMap<>();
        specs.forEach(specification -> byKey.put(specification.key(), specification));
        return align(
                requested,
                key -> {
                    var specification = byKey.get(key);
                    return specification == null
                            ? null
                            : new Raw(specification.value(), specification.unit());
                });
    }

    private Map<String, NormalizedSpecification> align(
            List<String> requested, java.util.function.Function<String, Raw> provider) {
        Map<String, NormalizedSpecification> result = new LinkedHashMap<>();
        for (String key : requested) {
            var attribute = SpecificationAttribute.fromKey(key);
            var rawSpecification = provider.apply(key);
            result.put(
                    key,
                    rawSpecification == null || rawSpecification.value() == null
                            ? new NormalizedSpecification(null, attribute.unit(), false)
                            : new NormalizedSpecification(
                                    units.normalize(
                                            attribute,
                                            rawSpecification.value(),
                                            rawSpecification.unit()),
                                    attribute == SpecificationAttribute.PRICE
                                            ? rawSpecification.unit()
                                            : attribute.unit(),
                                    true));
        }
        return result;
    }

    private record Raw(Object value, String unit) {}
}
