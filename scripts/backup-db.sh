#!/usr/bin/env bash
set -euo pipefail

# ====== MySQL 백업 (mysqldump -> gzip -> S3) ======
# EC2 crontab 등록 예시 (매일 새벽 4시):
# 0 4 * * * /home/ubuntu/apps/grabpt/scripts/backup-db.sh >> /home/ubuntu/backup/backup.log 2>&1

APP_DIR="/home/ubuntu/apps/grabpt"
BACKUP_DIR="/home/ubuntu/backup"
RETENTION_DAYS=7
cd "$APP_DIR"

# ====== 배포 환경 변수(.env) 읽기 (DB_ROOT_PASSWORD, DB_NAME) ======
if [ -f "$APP_DIR/.env" ]; then
  set -a
  source "$APP_DIR/.env"
  set +a
fi

# 백업 버킷은 EC2의 /home/ubuntu/.backup.env 에 BACKUP_S3_BUCKET=버킷명 으로 지정
if [ -f "/home/ubuntu/.backup.env" ]; then
  set -a
  source "/home/ubuntu/.backup.env"
  set +a
fi
: "${BACKUP_S3_BUCKET:?BACKUP_S3_BUCKET is not set}"

mkdir -p "$BACKUP_DIR"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
FILE="$BACKUP_DIR/${DB_NAME}-${TIMESTAMP}.sql.gz"
trap 'rm -f "$FILE"' ERR # 실패 시 불완전한 백업 파일 삭제

# ====== 덤프 (--single-transaction: 서비스 중단 없이 일관된 스냅샷) ======
docker compose --env-file "$APP_DIR/.env" exec -T -e MYSQL_PWD="$DB_ROOT_PASSWORD" mysql \
  mysqldump -uroot --single-transaction --routines --triggers "$DB_NAME" \
  | gzip > "$FILE"
echo "[$(date '+%F %T')] Dump created: $FILE ($(du -h "$FILE" | cut -f1))"

# ====== S3 업로드 ======
aws s3 cp "$FILE" "s3://${BACKUP_S3_BUCKET}/db-backup/$(basename "$FILE")" --only-show-errors
echo "[$(date '+%F %T')] Uploaded to s3://${BACKUP_S3_BUCKET}/db-backup/"

# ====== 로컬 보관 기간 지난 백업 삭제 ======
find "$BACKUP_DIR" -name "*.sql.gz" -mtime +"$RETENTION_DAYS" -delete
