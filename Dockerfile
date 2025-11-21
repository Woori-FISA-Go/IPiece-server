# ===========================
# Stage 1: Build Stage
# ===========================
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

# Gradle 캐시 레이어 (의존성만 먼저)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || return 0

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle clean build -x test --no-daemon --stacktrace

# JAR 파일 확인
RUN ls -la /app/build/libs/ && \
    test -f /app/build/libs/IPiece-0.0.1-SNAPSHOT.jar

# ===========================
# Stage 2: Runtime Stage (보안 강화)
# ===========================
FROM eclipse-temurin:17-jre-alpine

# 라벨 추가 (메타데이터)
LABEL maintainer="Woori-FISA-Go Team" \
      version="1.0" \
      description="IPiece Backend Server"

WORKDIR /app

# 보안: non-root 사용자 생성 및 디렉토리 권한 설정
RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

# 필요한 패키지만 설치 (보안 업데이트 포함)
RUN apk update && \
    apk upgrade && \
    apk add --no-cache wget && \
    rm -rf /var/cache/apk/*

# JAR 파일 복사 (non-root 소유)
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

# 사용자 전환 (non-root로 실행)
USER spring:spring

# Health Check 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 환경변수 (기본값)
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
               -XX:+UseContainerSupport \
               -Djava.security.egd=file:/dev/./urandom \
               -Dserver.port=8080" \
    TZ=Asia/Seoul

# 포트 노출
EXPOSE 8080

# 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]