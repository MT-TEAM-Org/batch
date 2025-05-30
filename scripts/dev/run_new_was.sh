#!/bin/bash

REPOSITORY=/home/ubuntu/app
TARGET_PORT=8080
LOG_FILE="$REPOSITORY/nohup.out"

echo "> 🔍 기존 batch jar 프로세스 종료 시도..."
PIDS=$(ps -ef | grep 'batch-0.0.1-SNAPSHOT.jar' | grep -v grep | awk '{print $2}')

if [ -n "$PIDS" ]; then
  echo "> 🔴 종료할 PID 목록: $PIDS"
  kill -9 $PIDS
else
  echo "> ✅ 실행 중인 배치 프로세스 없음"
fi

JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)
echo "> 🔍 JAR 파일: $JAR_NAME"

echo "> 🔧 실행 권한 추가"
chmod +x $JAR_NAME

echo "> 📄 로그 파일 확인 및 생성"
touch $LOG_FILE

echo "> 🚀 배치 실행 시작"
nohup java -Duser.timezone=Asia/Seoul -jar \
          $JAR_NAME > $LOG_FILE 2>&1 &

echo "> ✅ 배치가 정상 실행되었습니다."
exit 0