package io.ghassen.pockito.web.mapper;

import io.ghassen.pockito.domain.Country;
import io.ghassen.pockito.domain.CurrencyCode;
import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.web.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for UserMapper.
 * 
 * Tests the MapStruct-generated mapping implementation to ensure
 * proper mapping between User entity and UserDto.
 */
class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapUserToDto() {
        // Given
        User user = User.builder()
                .username("testuser")
                .country(Country.US)
                .defaultCurrency(CurrencyCode.USD)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // When
        UserDto userDto = userMapper.toDto(user);

        // Then
        assertThat(userDto).isNotNull();
        assertThat(userDto.getUsername()).isEqualTo("testuser");
        assertThat(userDto.getCountry()).isEqualTo(Country.US);
        assertThat(userDto.getDefaultCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(userDto.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(userDto.getUpdatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void shouldMapUserToDtoWithNullValues() {
        // Given
        User user = User.builder()
                .username("testuser")
                .country(null)
                .defaultCurrency(null)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // When
        UserDto userDto = userMapper.toDto(user);

        // Then
        assertThat(userDto).isNotNull();
        assertThat(userDto.getUsername()).isEqualTo("testuser");
        assertThat(userDto.getCountry()).isNull();
        assertThat(userDto.getDefaultCurrency()).isNull();
        assertThat(userDto.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(userDto.getUpdatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void shouldMapDtoToEntity() {
        // Given
        UserDto userDto = UserDto.builder()
                .username("testuser")
                .country(Country.GB)
                .defaultCurrency(CurrencyCode.GBP)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // When
        User user = userMapper.toEntity(userDto);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getCountry()).isEqualTo(Country.GB);
        assertThat(user.getDefaultCurrency()).isEqualTo(CurrencyCode.GBP);
        // Audit fields should be ignored when mapping from DTO
        assertThat(user.getCreatedAt()).isNull();
        assertThat(user.getUpdatedAt()).isNull();
    }

    @Test
    void shouldUpdateEntityFromDto() {
        // Given
        User existingUser = User.builder()
                .username("testuser")
                .country(Country.US)
                .defaultCurrency(CurrencyCode.USD)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        UserDto updateDto = UserDto.builder()
                .username("newuser") // This should be ignored
                .country(Country.FR)
                .defaultCurrency(CurrencyCode.EUR)
                .createdAt(Instant.now()) // This should be ignored
                .updatedAt(Instant.now()) // This should be ignored
                .build();

        // When
        userMapper.updateEntityFromDto(updateDto, existingUser);

        // Then
        assertThat(existingUser.getUsername()).isEqualTo("testuser"); // Should remain unchanged
        assertThat(existingUser.getCountry()).isEqualTo(Country.FR); // Should be updated
        assertThat(existingUser.getDefaultCurrency()).isEqualTo(CurrencyCode.EUR); // Should be updated
        // Audit fields should remain unchanged
        assertThat(existingUser.getCreatedAt()).isNotNull();
        assertThat(existingUser.getUpdatedAt()).isNotNull();
    }
}
