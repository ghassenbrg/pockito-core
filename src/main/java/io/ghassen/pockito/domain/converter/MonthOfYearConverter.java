package io.ghassen.pockito.domain.converter;

import io.ghassen.pockito.domain.enums.MonthOfYear;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter for converting between MonthOfYear enum and Integer.
 * 
 * Stores the enum as an integer value (1-12) in the database where 1=January, 12=December.
 */
@Converter(autoApply = false)
public class MonthOfYearConverter implements AttributeConverter<MonthOfYear, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MonthOfYear attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue();
    }

    @Override
    public MonthOfYear convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return MonthOfYear.fromValue(dbData);
    }
}

