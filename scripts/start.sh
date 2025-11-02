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

# ====== 앱 헬스 확인(필요 시 포트/엔드포인트 조정) ======
for i in {1..30}; do
  if curl -fsS "http://127.0.0.1:8080/actuator/health" | grep -q '"status":"UP"'; then
    echo "App is UP"
    break
  fi
  echo "Waiting app health... ($i/30)"; sleep 2
done

