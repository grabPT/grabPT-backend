#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/app"
JAR_FILE="$PROJECT_ROOT/app.jar"

DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# 현재 구동 중인 애플리케이션 pid 확인
CURRENT_PID=$(pgrep -f $JAR_FILE)

# 프로세스가 켜져 있으면 종료
if [ -z $CURRENT_PID ]; then
  echo "$TIME_NOW > 현재 실행중인 애플리케이션이 없습니다" >> $DEPLOY_LOG
else
  echo "$TIME_NOW > 실행중인 $CURRENT_PID 애플리케이션 종료 " >> $DEPLOY_LOG
  kill -15 $CURRENT_PID
  sleep 5

  # 아직 살아 있으면 강제 종료
  CURRENT_PID=$(pgrep -f $JAR_FILE)
  if [ -n "$CURRENT_PID" ]; then
    echo "$TIME_NOW > 애플리케이션이 종료되지 않아 강제 종료합니다." >> $DEPLOY_LOG
    kill -9 $CURRENT_PID
  fi
fi
