# ===================================================================
# MapSy-BE Spring Boot Dockerfile
# ===================================================================
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="MapSy Team"
LABEL description="MapSy Backend API Server"

WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# JAR 파일 복사 (MS-Web 모듈)
COPY MS-Web/build/libs/app.jar app.jar

# Health check (12시간마다 실행)
HEALTHCHECK --interval=43200s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/docs/swagger || exit 1

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
