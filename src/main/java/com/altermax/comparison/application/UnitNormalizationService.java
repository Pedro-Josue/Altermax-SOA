package com.altermax.comparison.application;

import com.altermax.comparison.domain.SpecificationAttribute;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class UnitNormalizationService {
    public Object normalize(SpecificationAttribute attribute, Object value, String sourceUnit) {
        if (value == null) {
            return null;
        }
        String unit = sourceUnit == null ? "" : sourceUnit.trim().toLowerCase(Locale.ROOT);
        try {
            BigDecimal number = new BigDecimal(value.toString().replace(",", "."));
            return switch (attribute) {
                case POWER_HP ->
                        unit.equals("kw")
                                ? rounded(number.multiply(new BigDecimal("1.34102209")))
                                : number(number);
                case TORQUE_NM ->
                        unit.equals("lb-ft") || unit.equals("lb ft")
                                ? rounded(number.multiply(new BigDecimal("1.35581795")))
                                : number(number);
                case ENGINE_DISPLACEMENT_CC ->
                        unit.equals("l") || unit.equals("liter") || unit.equals("litre")
                                ? number(number.multiply(new BigDecimal("1000")))
                                : number(number);
                case LENGTH_MM, WIDTH_MM, HEIGHT_MM, WHEELBASE_MM ->
                        unit.equals("cm")
                                ? number(number.multiply(BigDecimal.TEN))
                                : unit.equals("m")
                                        ? number(number.multiply(new BigDecimal("1000")))
                                        : number(number);
                case CURB_WEIGHT_KG ->
                        unit.equals("lb") || unit.equals("lbs")
                                ? rounded(number.multiply(new BigDecimal("0.45359237")))
                                : number(number);
                case TOP_SPEED_KMH ->
                        unit.equals("mph")
                                ? rounded(number.multiply(new BigDecimal("1.609344")))
                                : number(number);
                case FUEL_CONSUMPTION_L_100KM ->
                        unit.contains("mpg")
                                ? rounded(
                                        new BigDecimal("235.214583")
                                                .divide(number, 4, RoundingMode.HALF_UP))
                                : number(number);
                default -> number(number);
            };
        } catch (NumberFormatException ex) {
            return value.toString();
        }
    }

    private Object rounded(BigDecimal value) {
        return number(value.setScale(2, RoundingMode.HALF_UP));
    }

    private Object number(BigDecimal value) {
        BigDecimal normalizedValue = value.stripTrailingZeros();
        return normalizedValue.scale() <= 0 ? normalizedValue.longValueExact() : normalizedValue;
    }
}
