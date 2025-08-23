package io.ghassen.pockito.web.dto;

import io.ghassen.pockito.domain.Wallet.WalletType;
import io.ghassen.pockito.domain.Wallet.IconType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public class WalletDtos {

  public record CreateReq(
    @NotBlank(message = "Wallet name is required")
    @Size(min = 1, max = 100, message = "Wallet name must be between 1 and 100 characters")
    String name,
    
    @NotNull(message = "Icon type is required")
    IconType iconType,
    
    @NotBlank(message = "Icon value is required")
    @Size(max = 255, message = "Icon value must not exceed 255 characters")
    String iconValue,
    
    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be exactly 3 uppercase letters")
    String currencyCode,
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid hex color code")
    String color,
    
    @NotNull(message = "Wallet type is required")
    WalletType type,
    
    @DecimalMin(value = "0.00", message = "Initial balance must be non-negative")
    @Digits(integer = 16, fraction = 2, message = "Initial balance must have at most 16 digits and 2 decimal places")
    BigDecimal initialBalance,
    
    @DecimalMin(value = "0.00", message = "Goal amount must be non-negative")
    @Digits(integer = 16, fraction = 2, message = "Goal amount must have at most 16 digits and 2 decimal places")
    BigDecimal goalAmount,
    
    boolean setDefault
  ) {}

  public record UpdateReq(
    @NotBlank(message = "Wallet name is required")
    @Size(min = 1, max = 100, message = "Wallet name must be between 1 and 100 characters")
    String name,
    
    @NotNull(message = "Icon type is required")
    IconType iconType,
    
    @NotBlank(message = "Icon value is required")
    @Size(max = 255, message = "Icon value must not exceed 255 characters")
    String iconValue,
    
    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency code must be exactly 3 uppercase letters")
    String currencyCode,
    
    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be a valid hex color code")
    String color,
    
    @NotNull(message = "Wallet type is required")
    WalletType type,
    
    @DecimalMin(value = "0.00", message = "Goal amount must be non-negative")
    @Digits(integer = 16, fraction = 2, message = "Goal amount must have at most 16 digits and 2 decimal places")
    BigDecimal goalAmount
  ) {}

  public record Resp(
    UUID id,
    String name,
    IconType iconType,
    String iconValue,
    String currencyCode,
    String color,
    WalletType type,
    BigDecimal initialBalance,
    boolean isDefault,
    BigDecimal goalAmount,
    UUID userId,
    java.time.Instant createdAt,
    java.time.Instant updatedAt
  ) {}
}
