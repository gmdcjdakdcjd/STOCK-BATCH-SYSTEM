# =========================
# Spring Boot 3.x + Java 17
# =========================
FROM eclipse-temurin:17-jdk-jammy

# =========================
# App directory
# =========================
WORKDIR /app

# =========================
# BootJar 복사
# =========================
COPY build/libs/*.jar app.jar

# =========================
# Batch 파일 경로 (볼륨 마운트 대상)
# =========================
VOLUME ["/data"]

# =========================
# 실행
# =========================
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
