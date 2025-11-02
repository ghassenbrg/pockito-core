package io.ghassen.pockito.domain.converter;

import io.ghassen.pockito.domain.enums.DayOfWeek;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter for converting between DayOfWeek enum and Integer.
 * 
 * Stores the enum as an integer value (1-7) in the database where 1=Monday, 7=Sunday.
 */
@Converter(autoApply = false)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer> {

    @Override
    public Integer convertToDatabaseColumn(DayOfWeek attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public DayOfWeek convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return DayOfWeek.fromValue(dbData);
    }
}

