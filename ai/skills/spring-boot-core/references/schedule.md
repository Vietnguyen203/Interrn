# Schedule (Cron Job) Convention

## Overview
Schedule jobs được đặt trong `adapter/schedule`, dùng Spring `@Scheduled` với cron expression từ config.

## Structure
- Package: `adapter/schedule`
- Annotations:
  - `@Component`
  - `@RequiredArgsConstructor`
  - `@Slf4j`
  - `@Scheduled(cron = "${app.properties.{entity}.cronjob-change}")`
  - `@Transactional` (nếu có write DB)

## Naming Convention
Class: `{Entity}Schedule` hoặc `Cronjob{Action}{Entity}`
Method: `changeStatusScheduled`, `extractDocumentScheduled`, v.v.

## Cron Expression
- Đọc từ `application.yml`:
  ```yaml
  app.properties:
    user:
      cronjob-change: ${CRON_CHANGE_STATUS:0 * * * * *}
  ```

## Transaction & Logging
- Luôn dùng `@Transactional` cho job có write DB
- Log số lượng record và từng record quan trọng:
  ```java
  log.info("Found {} users to unlock", usersToUnlock.size());
  for (UserAccount user : usersToUnlock) {
      // ...
      log.info("User {} unlocked and set to ACTIVE", user.getUserCode());
  }
  ```

## Persistence Access
- Inject `EntityPersistence` interface
- Dùng các query method từ persistence:
  - `findByUpdatedAtBeforeAndStatusNot`
  - `save(entity)`

## Error Handling
- Exception sẽ log tự động qua `@Slf4j`
- Nên tránh throw runtime exception trong job (để job tiếp tục chạy)

## Template
Xem `assets/templates/schedule-template.java` để tạo job mới theo chuẩn.

## Enable Scheduling
Main application cần có `@EnableScheduling` (đã có trong `UserApplication.java`).