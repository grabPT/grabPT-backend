# ---- build stage ----
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /app
COPY . .
# build/libs/grabpt-1.0.0.jar 생성 (tests skip 원하면 -x test)
RUN gradle clean bootJar -x test

# ---- runtime stage ----
# PlayWright : PDF 생성 필요 라이브러리 추가
FROM mcr.microsoft.com/playwright/java:v1.45.0-jammy
ENV TZ=Asia/Seoul
WORKDIR /app
# 위 build 산출물 이름은 build.gradle의 bootJar 설정과 일치
COPY --from=builder /app/build/libs/grabpt-1.0.0.jar /app/app.jar

# 컨테이너 내에서 외부 설정 파일을 읽고 싶으면 /config/.env.properties 로 마운트
# SPRING_CONFIG_IMPORT 환경변수로 지정 (compose에서 설정)
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
