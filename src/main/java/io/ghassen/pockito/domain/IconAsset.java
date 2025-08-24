package io.ghassen.pockito.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "icon_asset")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IconAsset extends AuditableEntity {

  public enum IconType {
    EMOJI,
    URL;
  }

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false, columnDefinition = "icon_type_enum")
  private IconType type;

  @Column(nullable = false)
  private String value;

  private String label;

  @Column(columnDefinition = "TEXT[]")
  private List<String> tags;

  @Column(nullable = false, name = "is_active")
  private Boolean isActive = true;
}
