package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "budget_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BudgetCategory {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "budget_id", nullable = false)
  private Budget budget;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  // Composite primary key
  @EmbeddedId
  private BudgetCategoryId id;

  @Embeddable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BudgetCategoryId {
    @Column(name = "budget_id", insertable = false, updatable = false)
    private UUID budgetId;
    
    @Column(name = "category_id", insertable = false, updatable = false)
    private UUID categoryId;
  }
}
