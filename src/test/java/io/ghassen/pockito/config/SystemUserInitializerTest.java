package io.ghassen.pockito.config;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SystemUserInitializer.
 */
@ExtendWith(MockitoExtension.class)
class SystemUserInitializerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private SystemUserInitializer systemUserInitializer;

    private User mockSystemUser;

    @BeforeEach
    void setUp() {
        mockSystemUser = User.builder()
                .username("system")
                .build();
    }

    @Test
    void run_ShouldCreateSystemUser_WhenSystemUserDoesNotExist() throws Exception {
        // Given
        when(userService.createSystemUserIfNotExists()).thenReturn(mockSystemUser);

        // When
        systemUserInitializer.run();

        // Then
        verify(userService, times(1)).createSystemUserIfNotExists();
    }

    @Test
    void run_ShouldNotFail_WhenUserServiceThrowsException() throws Exception {
        // Given
        when(userService.createSystemUserIfNotExists()).thenThrow(new RuntimeException("Database error"));

        // When & Then - should not throw exception
        systemUserInitializer.run();

        // Verify the service was called
        verify(userService, times(1)).createSystemUserIfNotExists();
    }

    @Test
    void run_ShouldHandleEmptyArgs() throws Exception {
        // Given
        when(userService.createSystemUserIfNotExists()).thenReturn(mockSystemUser);

        // When
        systemUserInitializer.run("arg1", "arg2");

        // Then
        verify(userService, times(1)).createSystemUserIfNotExists();
    }
}
