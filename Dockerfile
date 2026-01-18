# ===================================================================
# MapSee-BE Spring Boot Dockerfile
# ===================================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="MapSee Team"
LABEL description="MapSee Backend API Server"

WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# JAR 파일 복사 (MS-Web 모듈)
COPY MS-Web/build/libs/app.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
