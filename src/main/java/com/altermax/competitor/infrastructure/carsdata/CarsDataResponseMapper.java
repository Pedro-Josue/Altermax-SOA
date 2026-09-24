package com.altermax.competitor.infrastructure.carsdata;

import com.altermax.competitor.api.CompetitorDetails;
import com.altermax.competitor.api.CompetitorSpecification;
import com.altermax.competitor.api.CompetitorSummary;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import org.springframework.stereotype.Component;

@Component
public class CarsDataResponseMapper {
    private static final Map<String, String> KEYS =
            Map.ofEntries(
                    Map.entry("powerhp", "power_hp"),
                    Map.entry("horsepower", "power_hp"),
                    Map.entry("power", "power_hp"),
                    Map.entry("torquenm", "torque_nm"),
                    Map.entry("torque", "torque_nm"),
                    Map.entry("enginedisplacementcc", "engine_displacement_cc"),
                    Map.entry("enginesize", "engine_displacement_cc"),
                    Map.entry("fueltype", "fuel_type"),
                    Map.entry("transmission", "transmission"),
                    Map.entry("drivetrain", "drivetrain"),
                    Map.entry("topspeedkmh", "top_speed_kmh"),
                    Map.entry("acceleration0100s", "acceleration_0_100_s"),
                    Map.entry("fuelconsumptionl100km", "fuel_consumption_l_100km"),
                    Map.entry("co2gkm", "co2_g_km"),
                    Map.entry("lengthmm", "length_mm"),
                    Map.entry("widthmm", "width_mm"),
                    Map.entry("heightmm", "height_mm"),
                    Map.entry("wheelbasemm", "wheelbase_mm"),
                    Map.entry("curbweightkg", "curb_weight_kg"),
                    Map.entry("cargocapacityl", "cargo_capacity_l"),
                    Map.entry("seats", "seats"),
                    Map.entry("price", "price"));

    public List<CompetitorSummary> search(JsonNode root) {
        JsonNode items = array(root, "items", "results", "data");
        if (!items.isArray()) {
            throw invalid();
        }
        List<CompetitorSummary> summaries = new ArrayList<>();
        for (JsonNode item : items) {
            String externalId = metadataText(item, "variant_id", "externalId", "id");
            if (externalId != null) {
                summaries.add(
                        new CompetitorSummary(
                                externalId,
                                brand(item),
                                model(item),
                                metadataText(
                                        item, "generation", "generation_name", "generation_id"),
                                metadataText(item, "display_name", "variant", "name", "trim"),
                                metadataInteger(
                                        item, "year", "model_year", "year_from", "year_to")));
            }
        }
        return summaries;
    }

    public CompetitorDetails details(String externalId, JsonNode variant, JsonNode specs) {
        return details(externalId, variant, specs, null);
    }

    public CompetitorDetails details(
            String externalId,
            JsonNode variant,
            JsonNode specs,
            CompetitorSummary fallbackMetadata) {
        JsonNode variantData = object(variant);
        if (variantData == null || variantData.isMissingNode()) {
            throw invalid();
        }
        List<CompetitorSpecification> specifications = new ArrayList<>();
        JsonNode specificationItems = array(specs, "items", "specifications", "specs", "data");
        if (specificationItems.isObject()) {
            specificationItems
                    .fields()
                    .forEachRemaining(
                            entry -> add(specifications, entry.getKey(), entry.getValue()));
        } else if (specificationItems.isArray()) {
            for (JsonNode item : specificationItems) {
                String rawKey = text(item, "key", "code", "name", "slug");
                if (rawKey != null) {
                    add(specifications, rawKey, item);
                }
            }
        } else {
            throw invalid();
        }
        return new CompetitorDetails(
                firstAvailable(
                        metadataText(variantData, "variant_id", "externalId", "id"),
                        fallbackMetadata == null ? null : fallbackMetadata.externalId(),
                        externalId),
                firstAvailable(
                        brand(variantData),
                        fallbackMetadata == null ? null : fallbackMetadata.brand()),
                firstAvailable(
                        model(variantData),
                        fallbackMetadata == null ? null : fallbackMetadata.model()),
                firstAvailable(
                        metadataText(variantData, "generation", "generation_name", "generation_id"),
                        fallbackMetadata == null ? null : fallbackMetadata.generation()),
                firstAvailable(
                        metadataText(variantData, "display_name", "variant", "name", "trim"),
                        fallbackMetadata == null ? null : fallbackMetadata.variant()),
                firstAvailable(
                        metadataInteger(variantData, "year", "model_year", "year_from", "year_to"),
                        fallbackMetadata == null ? null : fallbackMetadata.year()),
                List.copyOf(specifications));
    }

    private void add(List<CompetitorSpecification> output, String rawKey, JsonNode specification) {
        String key = normalizeKey(rawKey);
        if (key == null) {
            return;
        }
        JsonNode value =
                specification.isValueNode()
                        ? specification
                        : first(specification, "value", "display_value", "numeric_value");
        if (value == null || value.isNull()) {
            return;
        }
        Object parsedValue = value.isNumber() ? value.numberValue() : value.asText();
        String unit = specification.isObject() ? text(specification, "unit", "symbol") : null;
        output.add(new CompetitorSpecification(key, parsedValue, unit));
    }

    private String normalizeKey(String raw) {
        String compact = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return KEYS.getOrDefault(compact, KEYS.containsValue(raw) ? raw : null);
    }

    private String brand(JsonNode node) {
        return firstAvailable(
                metadataText(node, "brand", "make"),
                readableSlug(metadataText(node, "brand_slug")));
    }

    private String model(JsonNode node) {
        return firstAvailable(
                metadataText(node, "model"), readableSlug(metadataText(node, "model_slug")));
    }

    private String readableSlug(String slug) {
        if (slug == null) {
            return null;
        }
        StringJoiner result = new StringJoiner(" ");
        for (String word : slug.trim().split("[-_\\s]+")) {
            if (word.isEmpty()) {
                continue;
            }
            String normalized = word.toLowerCase(Locale.ROOT);
            result.add(
                    normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1));
        }
        String display = result.toString();
        return display.isEmpty() ? null : display;
    }

    private String metadataText(JsonNode node, String... names) {
        if (node == null) {
            return null;
        }
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull()) {
                return value.asText();
            }
        }
        return null;
    }

    private Integer metadataInteger(JsonNode node, String... names) {
        if (node == null) {
            return null;
        }
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null && !value.isNull() && value.canConvertToInt()) {
                return value.asInt();
            }
        }
        return null;
    }

    private String firstAvailable(String... values) {
        for (String value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private Integer firstAvailable(Integer... values) {
        for (Integer value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private JsonNode object(JsonNode root) {
        if (root == null) {
            return null;
        }
        JsonNode data = root.get("data");
        return data != null && data.isObject() ? data : root;
    }

    private JsonNode array(JsonNode root, String... names) {
        if (root == null) {
            return MissingNode.getInstance();
        }
        if (root.isArray()) {
            return root;
        }
        for (String name : names) {
            JsonNode value = root.get(name);
            if (value != null) {
                return value;
            }
        }
        return MissingNode.getInstance();
    }

    private JsonNode first(JsonNode node, String... names) {
        for (String name : names) {
            JsonNode value = node.get(name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String... names) {
        JsonNode value = first(node, names);
        return value == null || value.isNull() ? null : value.asText();
    }

    private CarsDataException invalid() {
        return new CarsDataException(
                CarsDataException.Kind.INVALID_RESPONSE, "Resposta externa invalida.");
    }
}
