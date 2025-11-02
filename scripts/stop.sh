#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/home/ubuntu/apps/grabpt"
cd "$APP_DIR"

# 앱만 재시작 하고 싶다면 아래를 주석 처리
docker compose down || true
