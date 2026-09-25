#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/home/ubuntu/apps/grabpt"
cd "$APP_DIR"

# ====== 배포 환경 변수(.env) 읽기 ======
# GitHub Actions에서 같이 내려주는 .env 파일 (ECR_REGISTRY/ECR_REPO/IMAGE_TAG/DB_*)
if [ -f "$APP_DIR/.env" ]; then
  set -a
  source "$APP_DIR/.env"
  set +a
fi

# ====== ECR 로그인 ======
aws ecr get-login-password --region "${AWS_REGION:-ap-northeast-2}" \
| docker login --username AWS --password-stdin "${ECR_REGISTRY}"

# ====== 이미지 미리 풀(옵션) ======
docker pull "${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"

# ====== Compose 업데이트(무중단) ======
docker compose --env-file "$APP_DIR/.env" up -d

# ====== 앱 헬스 확인 (실패 시 배포 실패 처리) ======
# CodeDeploy ApplicationStart timeout(300초) 안에 끝나도록 최대 240초 대기
HEALTH_TIMEOUT=240
SECONDS=0
while [ "$SECONDS" -lt "$HEALTH_TIMEOUT" ]; do
  if curl -fsS --max-time 5 "http://127.0.0.1:8080/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
    echo "App is UP (${SECONDS}s)"
    exit 0
  fi
  echo "Waiting app health... (${SECONDS}s/${HEALTH_TIMEOUT}s)"; sleep 3
done

echo "App health check failed"
docker compose --env-file "$APP_DIR/.env" logs --tail=100 app || true
exit 1

