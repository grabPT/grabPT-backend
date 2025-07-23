#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu"
DEPLOY_DIR="$PROJECT_ROOT/app"
JAR_SOURCE="$DEPLOY_DIR/build/libs"
JAR_FILE="$PROJECT_ROOT/app.jar"
ENV_FILE="$PROJECT_ROOT/.env.properties"  # 추가

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date "+%Y-%m-%d %H:%M:%S")

echo "$TIME_NOW > JAR 파일 복사: $JAR_SOURCE → $JAR_FILE" >> $DEPLOY_LOG
cp $JAR_SOURCE/*.jar $JAR_FILE

# jar 파일 실행 (환경설정 포함)
echo "$TIME_NOW > $JAR_FILE 파일 실행" >> $DEPLOY_LOG
nohup java -jar $JAR_FILE --spring.config.import=optional:file:$ENV_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo "$TIME_NOW > 실행된 프로세스 아이디 $CURRENT_PID 입니다." >> $DEPLOY_LOG
