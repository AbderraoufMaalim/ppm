#!/bin/bash
# ============================================
# PushPlayManager V2 — Backup MySQL automatique
# 
# Usage: ./backup.sh
# Planifier via cron (Linux) ou Planificateur de tâches (Windows)
# Exemple cron: 0 3 * * * /chemin/vers/backup.sh
# ============================================

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
BACKUP_DIR="$PROJECT_DIR/backups"
CONTAINER_NAME="ppm-mysql"
DATE=$(date +%Y%m%d_%H%M%S)
KEEP_DAYS=7

# Charger les variables d'environnement
if [ -f "$PROJECT_DIR/.env" ]; then
  export $(grep -v '^#' "$PROJECT_DIR/.env" | xargs)
fi

# Créer le dossier de backup si nécessaire
mkdir -p "$BACKUP_DIR"

echo "📦 Backup PushPlay DB — $DATE"

# Vérifier que le conteneur tourne
if ! docker ps --format '{{.Names}}' | grep -q "$CONTAINER_NAME"; then
  echo "❌ Le conteneur $CONTAINER_NAME n'est pas en cours d'exécution"
  exit 1
fi

# Exécuter le dump
BACKUP_FILE="$BACKUP_DIR/pushplay_$DATE.sql"
docker exec "$CONTAINER_NAME" mysqldump \
  -u root \
  -p"$DB_ROOT_PASSWORD" \
  --single-transaction \
  --routines \
  --triggers \
  "$DB_NAME" > "$BACKUP_FILE"

# Compresser
gzip "$BACKUP_FILE"
FINAL_FILE="${BACKUP_FILE}.gz"

echo "✅ Backup créé : $FINAL_FILE ($(du -sh "$FINAL_FILE" | cut -f1))"

# Rotation : supprimer les backups de plus de N jours
DELETED=$(find "$BACKUP_DIR" -name "pushplay_*.sql.gz" -mtime +$KEEP_DAYS -delete -print | wc -l)
if [ "$DELETED" -gt 0 ]; then
  echo "🗑️  $DELETED ancien(s) backup(s) supprimé(s)"
fi

echo "📁 Backups actuels :"
ls -lh "$BACKUP_DIR"/*.sql.gz 2>/dev/null || echo "  (aucun)"
