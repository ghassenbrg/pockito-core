package io.ghassen.pockito.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Utilities", description = "Utility endpoints for testing, health checks, and system information")
public class SampleController {

    @GetMapping(value = "/sample/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Health Check",
        description = "Returns the current health status of the Pockito API service. This endpoint is useful for monitoring and load balancer health checks.",
        operationId = "getHealthStatus"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Service is healthy and operational",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = HealthResponse.class),
                examples = @ExampleObject(
                    name = "Healthy Service",
                    value = """
                        {
                          "status": "healthy",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "service": "Pockito API",
                          "version": "1.0.0",
                          "uptime": "2h 15m 30s"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Service is unhealthy or experiencing issues",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse(
            "healthy",
            Instant.now().toString(),
            "Pockito API",
            "1.0.0",
            "2h 15m 30s"
        ));
    }

    @GetMapping(value = { "/public", "/public/" }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Public Endpoint",
        description = "A public endpoint that doesn't require authentication. Useful for checking if the service is accessible without credentials.",
        operationId = "getPublicInfo"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Public access granted successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PublicResponse.class),
                examples = @ExampleObject(
                    name = "Public Access",
                    value = """
                        {
                          "message": "This is a public endpoint",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "authenticated": false,
                          "service": "Pockito API"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<PublicResponse> publicEndpoint() {
        return ResponseEntity.ok(new PublicResponse(
            "This is a public endpoint",
            Instant.now().toString(),
            false,
            "Pockito API"
        ));
    }

    @GetMapping(value = "/sample/echo/{message}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Echo Message",
        description = "Echoes back the provided message along with additional metadata. Useful for testing API connectivity and parameter handling.",
        operationId = "echoMessage"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Message echoed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = EchoResponse.class),
                examples = @ExampleObject(
                    name = "Echo Response",
                    value = """
                        {
                          "message": "Hello World",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "length": 11,
                          "uppercase": "HELLO WORLD",
                          "lowercase": "hello world"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid message provided",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Invalid Message",
                    value = """
                        {
                          "error": "Bad Request",
                          "message": "Message cannot be empty or null",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "status": 400
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<EchoResponse> echo(
        @Parameter(
            description = "Message to echo back",
            required = true,
            in = ParameterIn.PATH,
            example = "Hello World",
            schema = @Schema(type = "string", minLength = 1, maxLength = 100)
        )
        @PathVariable String message
    ) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty or null");
        }
        
        return ResponseEntity.ok(new EchoResponse(
            message,
            Instant.now().toString(),
            message.length(),
            message.toUpperCase(),
            message.toLowerCase()
        ));
    }

    @PostMapping(value = "/sample/protected", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Protected Endpoint",
        description = "A protected endpoint that requires authentication with USER role. Demonstrates secure endpoint access control.",
        operationId = "accessProtectedEndpoint",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Access granted successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProtectedResponse.class),
                examples = @ExampleObject(
                    name = "Protected Access",
                    value = """
                        {
                          "message": "You have access to the protected endpoint",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "authenticated": true,
                          "userRole": "USER",
                          "accessLevel": "STANDARD"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid authentication token",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<ProtectedResponse> protectedEndpoint() {
        return ResponseEntity.ok(new ProtectedResponse(
            "You have access to the protected endpoint",
            Instant.now().toString(),
            true,
            "USER",
            "STANDARD"
        ));
    }

    // Response DTOs with proper schemas
    @Schema(description = "Health check response")
    public static class HealthResponse {
        @Schema(description = "Current health status", example = "healthy")
        public final String status;
        
        @Schema(description = "Timestamp of the health check", example = "2024-01-15T10:30:00Z")
        public final String timestamp;
        
        @Schema(description = "Service name", example = "Pockito API")
        public final String service;
        
        @Schema(description = "Service version", example = "1.0.0")
        public final String version;
        
        @Schema(description = "Service uptime", example = "2h 15m 30s")
        public final String uptime;

        public HealthResponse(String status, String timestamp, String service, String version, String uptime) {
            this.status = status;
            this.timestamp = timestamp;
            this.service = service;
            this.version = version;
            this.uptime = uptime;
        }
    }

    @Schema(description = "Public endpoint response")
    public static class PublicResponse {
        @Schema(description = "Response message", example = "This is a public endpoint")
        public final String message;
        
        @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00Z")
        public final String timestamp;
        
        @Schema(description = "Authentication status", example = "false")
        public final boolean authenticated;
        
        @Schema(description = "Service name", example = "Pockito API")
        public final String service;

        public PublicResponse(String message, String timestamp, boolean authenticated, String service) {
            this.message = message;
            this.timestamp = timestamp;
            this.authenticated = authenticated;
            this.service = service;
        }
    }

    @Schema(description = "Echo response")
    public static class EchoResponse {
        @Schema(description = "Original message", example = "Hello World")
        public final String message;
        
        @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00Z")
        public final String timestamp;
        
        @Schema(description = "Message length", example = "11")
        public final int length;
        
        @Schema(description = "Message in uppercase", example = "HELLO WORLD")
        public final String uppercase;
        
        @Schema(description = "Message in lowercase", example = "hello world")
        public final String lowercase;

        public EchoResponse(String message, String timestamp, int length, String uppercase, String lowercase) {
            this.message = message;
            this.timestamp = timestamp;
            this.length = length;
            this.uppercase = uppercase;
            this.lowercase = lowercase;
        }
    }

    @Schema(description = "Protected endpoint response")
    public static class ProtectedResponse {
        @Schema(description = "Response message", example = "You have access to the protected endpoint")
        public final String message;
        
        @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00Z")
        public final String timestamp;
        
        @Schema(description = "Authentication status", example = "true")
        public final boolean authenticated;
        
        @Schema(description = "User role", example = "USER")
        public final String userRole;
        
        @Schema(description = "Access level", example = "STANDARD")
        public final String accessLevel;

        public ProtectedResponse(String message, String timestamp, boolean authenticated, String userRole, String accessLevel) {
            this.message = message;
            this.timestamp = timestamp;
            this.authenticated = authenticated;
            this.userRole = userRole;
            this.accessLevel = accessLevel;
        }
    }

    @Schema(description = "Error response")
    public static class ErrorResponse {
        @Schema(description = "Error type", example = "Bad Request")
        public final String error;
        
        @Schema(description = "Error message", example = "Message cannot be empty or null")
        public final String message;
        
        @Schema(description = "Error timestamp", example = "2024-01-15T10:30:00Z")
        public final String timestamp;
        
        @Schema(description = "HTTP status code", example = "400")
        public final int status;

        public ErrorResponse(String error, String message, String timestamp, int status) {
            this.error = error;
            this.message = message;
            this.timestamp = timestamp;
            this.status = status;
        }
    }
}
