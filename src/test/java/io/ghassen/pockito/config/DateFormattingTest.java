package io.ghassen.pockito.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ghassen.pockito.web.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DateFormattingTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testInstantSerialization() throws Exception {
        // Create a test user with Instant dates
        UserDto user = UserDto.builder()
                .username("testuser")
                .createdAt(Instant.parse("2024-01-15T10:30:45.123Z"))
                .updatedAt(Instant.parse("2024-01-15T14:22:33.456Z"))
                .build();

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(user);
        
        // Verify the dates are in ISO format
        assertTrue(json.contains("\"createdAt\":\"2024-01-15T10:30:45.123Z\""));
        assertTrue(json.contains("\"updatedAt\":\"2024-01-15T14:22:33.456Z\""));
        
        // Verify it's not in timestamp format
        assertFalse(json.contains("\"createdAt\":1705312245123"));
        assertFalse(json.contains("\"updatedAt\":1705326153456"));
    }

    @Test
    public void testLocalDateSerialization() throws Exception {
        // Create a test transaction DTO with LocalDate
        TestTransactionDto transaction = new TestTransactionDto();
        transaction.setEffectiveDate(LocalDate.of(2024, 1, 15));

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(transaction);
        
        // Verify the date is in ISO format (LocalDate should be yyyy-MM-dd)
        assertTrue(json.contains("\"effectiveDate\":\"2024-01-15\""));
        
        // Verify it's not in timestamp format
        assertFalse(json.contains("\"effectiveDate\":1705276800000"));
        
        // Verify it doesn't include time components
        assertFalse(json.contains("\"effectiveDate\":\"2024-01-15T"));
    }

    // Simple test DTO for LocalDate testing
    public static class TestTransactionDto {
        private LocalDate effectiveDate;

        public LocalDate getEffectiveDate() {
            return effectiveDate;
        }

        public void setEffectiveDate(LocalDate effectiveDate) {
            this.effectiveDate = effectiveDate;
        }
    }
}
