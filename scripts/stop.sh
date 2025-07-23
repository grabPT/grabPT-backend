PROJECT_ROOT="/home/ubuntu/app"
JAR_FILE="$PROJECT_ROOT/app.jar"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date "+%Y-%m-%d %H:%M:%S")

CURRENT_PID=$(pgrep -f "$JAR_FILE")

if [ -z "$CURRENT_PID" ]; then
  echo "$TIME_NOW > 현재 실행 중인 애플리케이션이 없습니다." >> $DEPLOY_LOG
else
  echo "$TIME_NOW > 실행 중인 애플리케이션(PID: $CURRENT_PID) 종료" >> $DEPLOY_LOG
  kill -15 "$CURRENT_PID"
fi
