package ${PACKAGE}.domain.models.entities;

import com.eps.shared.models.entities.AuditingEntity;
import ${PACKAGE}.domain.models.enums.${ENTITY}Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "${ENTITY_LOWER}")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ${ENTITY} extends AuditingEntity {

  @Column(name = "name")
  private String name;

  @Column(name = "code")
  private String code;

  @Column(name = "status")
  private ${ENTITY}Status status = ${ENTITY}Status.ACTIVE;

  @Column(name = "description")
  private String description;

  @Column(name = "is_active")
  private Boolean isActive = true;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}