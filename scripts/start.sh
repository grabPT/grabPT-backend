PROJECT_ROOT="/home/ubuntu/app"
JAR_NAME=$(ls $PROJECT_ROOT/*.jar | grep -v 'app.jar' | head -n 1)
JAR_FILE="$PROJECT_ROOT/app.jar"
ENV_FILE="$PROJECT_ROOT/.env.properties"

APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date "+%Y-%m-%d %H:%M:%S")

# 기존 jar를 새 파일로 덮어쓰기
echo "$TIME_NOW > JAR 파일 복사: $JAR_NAME → $JAR_FILE" >> $DEPLOY_LOG
cp "$JAR_NAME" "$JAR_FILE"

# jar 실행
echo "$TIME_NOW > JAR 파일 실행: $JAR_FILE" >> $DEPLOY_LOG
nohup java -jar "$JAR_FILE" --spring.config.import=optional:file:"$ENV_FILE" > "$APP_LOG" 2> "$ERROR_LOG" &

CURRENT_PID=$(pgrep -f "$JAR_FILE")
echo "$TIME_NOW > 실행된 프로세스 ID: $CURRENT_PID" >> $DEPLOY_LOG
