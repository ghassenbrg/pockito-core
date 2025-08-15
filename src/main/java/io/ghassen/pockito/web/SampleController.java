package io.ghassen.pockito.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Sample", description = "Sample endpoints for testing OpenAPI documentation")
public class SampleController {

    @GetMapping("/sample/health")
    @Operation(summary = "Health check", description = "Returns a simple health status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "timestamp", Instant.now().toString(),
                "service", "Pockito API"
        ));
    }

    @GetMapping({ "/public", "/public/" })
    @Operation(summary = "Public endpoint", description = "A public endpoint that doesn't require authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public access granted")
    })
    public ResponseEntity<Map<String, Object>> publicEndpoint() {
        return ResponseEntity.ok(Map.of(
                "message", "This is a public endpoint",
                "timestamp", Instant.now().toString(),
                "authenticated", false
        ));
    }

    @GetMapping("/sample/echo/{message}")
    @Operation(summary = "Echo message", description = "Echoes back the provided message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message echoed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid message")
    })
    public ResponseEntity<Map<String, Object>> echo(
            @Parameter(description = "Message to echo", required = true)
            @PathVariable String message) {
        return ResponseEntity.ok(Map.of(
                "message", message,
                "timestamp", Instant.now().toString(),
                "length", message.length()
        ));
    }

    @PostMapping("/sample/protected")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Protected endpoint", description = "A protected endpoint that requires authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access granted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        return ResponseEntity.ok(Map.of(
                "message", "You have access to the protected endpoint",
                "timestamp", Instant.now().toString(),
                "authenticated", true
        ));
    }
}
