package ${PACKAGE}.adapter.schedule;

import ${PACKAGE}.domain.models.entities.${ENTITY};
import ${PACKAGE}.domain.models.enums.${ENTITY}Status;
import ${PACKAGE}.domain.services.persistence.${ENTITY}Persistence;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ${ENTITY}Schedule {
  private final ${ENTITY}Persistence persistence;

  @Scheduled(cron = "${app.properties.${ENTITY_LOWER}.cronjob-change}")
  @Transactional
  void changeStatusScheduled() {
    LocalDateTime now = LocalDateTime.now();
    List<${ENTITY}> itemsToProcess =
        persistence.findByUpdatedAtBeforeAndStatusNot(now, ${ENTITY}Status.ACTIVE);

    log.info("Found {} ${ENTITY_LOWER} to process", itemsToProcess.size());

    for (${ENTITY} item : itemsToProcess) {
      item.setStatus(${ENTITY}Status.ACTIVE);
      persistence.save(item);

      log.info("${ENTITY} {} processed and set to ACTIVE", item.getCode());
    }
  }
}