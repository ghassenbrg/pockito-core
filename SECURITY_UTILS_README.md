# SecurityUtils - Centralized User Authentication

This document explains how to use the centralized `SecurityUtils` class for consistent user authentication across all services.

## Overview

Instead of duplicating JWT token extraction logic in every service, use the centralized `SecurityUtils` class. This ensures consistency and makes maintenance easier.

**Key Point**: The system now uses `preferred_username` as a string directly from JWT tokens. All user IDs are strings, making the system simpler and more consistent.

## Key Benefits

- ✅ **Centralized Logic**: One place to manage JWT token extraction
- ✅ **Consistent Behavior**: Same user ID extraction across all services
- ✅ **Easy Maintenance**: Update JWT logic in one place
- ✅ **Better Error Handling**: Consistent error messages and logging
- ✅ **String-Only Approach**: All user IDs are strings, no UUID conversion needed
- ✅ **Simplified API**: Single method for getting user ID

## Usage

### 1. Get Current User ID (String)

```java
import io.ghassen.pockito.security.SecurityUtils;

@Service
public class SomeService {
  public void someMethod() {
    String userId = SecurityUtils.getCurrentUserId();           // Returns string directly
    
    // Use userId for logging, display, database operations, etc.
    log.info("Processing request for user: {}", userId);
    List<Entity> entities = repo.findByUserId(userId);
  }
}
```

### 2. Get Current Username (String)

```java
@Service
public class SomeService {
  public void someMethod() {
    String username = SecurityUtils.getCurrentUsername();
    // Use username for logging, display, etc.
  }
}
```

### 3. Get Current User Email

```java
@Service
public class SomeService {
  public void someMethod() {
    String email = SecurityUtils.getCurrentUserEmail();
    if (email != null) {
      // Use email for notifications, etc.
    }
  }
}
```

### 4. Check Authentication Status

```java
@Service
public class SomeService {
  public void someMethod() {
    if (SecurityUtils.isAuthenticated()) {
      // User is authenticated
      String userId = SecurityUtils.getCurrentUserId();
    }
  }
}
```

### 5. Debug JWT Claims

```java
@Service
public class SomeService {
  public void someMethod() {
    Map<String, Object> claims = SecurityUtils.getCurrentUserClaims();
    if (claims != null) {
      log.debug("Available JWT claims: {}", claims);
    }
  }
}
```

## JWT Claim Priority

The system tries to extract user information in this order:

1. **`preferred_username`** - Primary identifier (Keycloak standard) - **Returns as string directly**
2. **`sub`** - Fallback identifier (OIDC standard) - **Returns as string directly**

## When to Use Which Method

### Use String Methods When:
- **Logging** - `SecurityUtils.getCurrentUserId()`
- **Display** - Showing user info in UI
- **Auditing** - Storing who performed an action
- **API responses** - Returning user identifiers
- **Database operations** - Finding data by user ID
- **Entity relationships** - Linking to other entities

## Example Service Implementation

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleService {
  
  private final ExampleRepository repo;
  
  public List<Example> getUserExamples() {
    String userId = SecurityUtils.getCurrentUserId();
    
    log.debug("Fetching examples for user: {}", userId);
    
    List<Example> examples = repo.findByUserId(userId);
    log.info("Found {} examples for user: {}", examples.size(), userId);
    
    return examples;
  }
  
  public Example createExample(ExampleDto dto) {
    String userId = SecurityUtils.getCurrentUserId();
    
    log.debug("Creating example '{}' for user: {}", dto.name(), userId);
    
    Example example = Example.builder()
      .userId(userId)           // Use string for entity
      .name(dto.name())
      .createdBy(userId)        // Use string for audit field
      .build();
    
    Example saved = repo.save(example);
    log.info("Created example '{}' with ID {} for user: {}", 
             dto.name(), saved.getId(), userId);
    
    return saved;
  }
}
```

## Error Handling

All methods throw `IllegalStateException` with descriptive messages if:
- No JWT token is found
- Required claims are missing

These exceptions are automatically handled by `GlobalExceptionHandler` and return appropriate HTTP status codes.

## Migration from Old Approach

### Before (Don't do this anymore):

```java
// ❌ Don't duplicate this logic in every service
private UUID currentUserId() {
  var auth = SecurityContextHolder.getContext().getAuthentication();
  if (auth instanceof JwtAuthenticationToken jwt) {
    String subject = jwt.getToken().getSubject();
    // ... validation logic
  }
  // ... error handling
}
```

### After (Use this instead):

```java
// ✅ Use the centralized utility
public List<Entity> getEntities() {
  String userId = SecurityUtils.getCurrentUserId();  // For everything
  
  log.debug("Fetching entities for user: {}", userId);
  return repo.findByUserId(userId);
}
```

## Testing

When writing tests, you can still mock the `SecurityContextHolder` as before, or mock the `SecurityUtils` methods directly:

```java
@Test
void testWithMockedUser() {
  // Mock the SecurityUtils method
  try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
    mockedSecurity.when(SecurityUtils::getCurrentUserId)
                  .thenReturn("testuser");
    
    // Your test logic here
  }
}
```

## Best Practices

1. **Always use SecurityUtils** instead of direct JWT token access
2. **Use string methods for everything** - `getCurrentUserId()` returns the string you need
3. **Handle exceptions appropriately** - they provide clear error messages
4. **Log user context** in important operations for debugging
5. **Use string values for all user ID fields** - database, audit fields, etc.

## Why This Approach?

- **Keycloak Integration**: `preferred_username` is the standard Keycloak field for usernames
- **Simplicity**: No UUID conversion needed, everything is a string
- **Consistency**: Same data type across all layers (JWT → Service → Database)
- **Better Logging**: String usernames are more readable than UUIDs
- **Audit Trail**: String usernames are more meaningful in audit fields
- **Maintainability**: Simpler code with fewer type conversions