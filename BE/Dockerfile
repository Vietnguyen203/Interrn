# ========================
# Stage 1: Build
# ========================
FROM gradle:7.6.4-jdk17 AS build

WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle clean build -x test --no-daemon

# ========================
# Stage 2: Runtime
# ========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Tạo user không phải root
RUN addgroup -S svcuser && adduser -S svcuser -G svcuser
USER svcuser

# Copy JAR từ stage build
COPY --from=build /app/build/libs/*.jar app.jar

ENV TZ="Asia/Ho_Chi_Minh"
EXPOSE 9720

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-Dspring.profiles.active=uat", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
