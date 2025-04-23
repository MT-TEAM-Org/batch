# Step 1: 애플리케이션 빌드
FROM openjdk:17-jdk-slim AS builder

# 필수 패키지 설치
RUN apt-get update && apt-get install -y wget curl unzip

# 작업 디렉토리 설정
WORKDIR /app

# Gradle Wrapper와 관련 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x ./gradlew

# Gradle 종속성을 캐싱하여 빌드 속도 향상
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드 (bootJar 실행)
RUN ./gradlew clean
RUN ./gradlew bootJar --no-daemon

# Step 2: 런타임 이미지 생성
FROM openjdk:17-jdk-slim

# 실행 디렉토리 설정
WORKDIR /play-hive-batch

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 필수 패키지 설치 (wget 및 Chrome 설치)
RUN apt-get update && \
    apt-get install -y wget && \
    wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm ./google-chrome-stable_current_amd64.deb

# 크롬 버전 확인
RUN google-chrome --version

# 애플리케이션이 사용할 포트 노출
EXPOSE 8080

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-Duser.timezone=GMT+09:00", "-jar", "app.jar"]
