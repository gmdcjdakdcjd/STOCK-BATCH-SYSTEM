# =========================
# Spring Boot 3.x + Java 17
# =========================
FROM eclipse-temurin:17-jdk-jammy

# =========================
# Python 설치
# =========================
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
 && ln -s /usr/bin/python3 /usr/bin/python \
 && rm -rf /var/lib/apt/lists/*

# =========================
# Python 배치용 라이브러리 설치 ⭐ 핵심 추가
# =========================
COPY requirements-batch.txt /tmp/requirements.txt
RUN pip install --no-cache-dir -r /tmp/requirements.txt

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
