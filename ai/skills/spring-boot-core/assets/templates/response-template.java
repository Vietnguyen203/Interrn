package ${PACKAGE}.application.responses;

import ${PACKAGE}.domain.models.enums.${ENTITY}Status;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class ${ENTITY}PageResponse {

  private UUID id;
  private String name;
  private String code;
  private ${ENTITY}Status status;
  private String description;
  private Boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
}