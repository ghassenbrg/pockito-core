package io.ghassen.pockito.service;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.repo.UserRepository;
import io.ghassen.pockito.web.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService system user creation functionality.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceSystemUserTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User mockSystemUser;

    @BeforeEach
    void setUp() {
        mockSystemUser = User.builder()
                .username("system")
                .build();
    }

    @Test
    void createSystemUserIfNotExists_ShouldReturnExistingUser_WhenSystemUserExists() {
        // Given
        when(userRepository.findByUsername("system")).thenReturn(Optional.of(mockSystemUser));

        // When
        User result = userService.createSystemUserIfNotExists();

        // Then
        assertThat(result).isEqualTo(mockSystemUser);
        verify(userRepository, times(1)).findByUsername("system");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createSystemUserIfNotExists_ShouldCreateNewUser_WhenSystemUserDoesNotExist() {
        // Given
        when(userRepository.findByUsername("system")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(mockSystemUser);

        // When
        User result = userService.createSystemUserIfNotExists();

        // Then
        assertThat(result).isEqualTo(mockSystemUser);
        verify(userRepository, times(1)).findByUsername("system");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createSystemUserIfNotExists_ShouldCreateUserWithCorrectUsername() {
        // Given
        when(userRepository.findByUsername("system")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getUsername()).isEqualTo("system");
            return user;
        });

        // When
        userService.createSystemUserIfNotExists();

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }
}
