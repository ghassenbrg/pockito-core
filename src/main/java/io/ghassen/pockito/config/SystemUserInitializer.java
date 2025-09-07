package io.ghassen.pockito.config;

import io.ghassen.pockito.domain.User;
import io.ghassen.pockito.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Component responsible for initializing system user during application startup.
 * 
 * This CommandLineRunner ensures that a system user with username "system" exists
 * in the database. If the system user doesn't exist, it will be created automatically.
 * 
 * The system user is used for:
 * - System-generated operations that need audit trails
 * - Background processes that require a user context
 * - Operations performed outside of user authentication context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemUserInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Initializing system user...");
            User systemUser = userService.createSystemUserIfNotExists();
            log.info("System user initialization completed successfully. Username: {}", systemUser.getUsername());
        } catch (Exception e) {
            log.error("Failed to initialize system user", e);
            // Don't throw the exception to prevent application startup failure
            // The system can still function without the system user, though some operations might fail
        }
    }
}
