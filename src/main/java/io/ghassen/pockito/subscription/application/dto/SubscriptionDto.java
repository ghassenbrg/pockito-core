package io.ghassen.pockito.subscription.application.dto;

import io.ghassen.pockito.domain.enums.CurrencyCode;
import io.ghassen.pockito.domain.enums.DayOfWeek;
import io.ghassen.pockito.domain.enums.MonthOfYear;
import io.ghassen.pockito.domain.enums.SubscriptionFrequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {

    private String id;

    private String username;

    private String name;

    private String iconUrl;

    private SubscriptionFrequency frequency;

    private Integer interval;

    private BigDecimal amount;

    private CurrencyCode currency;

    private LocalDate startDate;

    private LocalDate nextDueDate;

    private LocalDate lastPaymentDate;

    private LocalDate endDate;

    private Boolean enabled;

    private Boolean isActive;

    private String categoryId;

    private String categoryName;

    private Integer dayOfMonth;

    private DayOfWeek dayOfWeek;

    private MonthOfYear monthOfYear;

    private String defaultWalletId;

    private String defaultWalletName;

    private String note;

    private Instant createdAt;

    private Instant updatedAt;

    private BigDecimal monthlyEquivalentAmount;
}


