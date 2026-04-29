# ============================================
# PushPlayManager V2 — Backup MySQL (Windows PowerShell)
#
# Usage: .\backup.ps1
# Planifier via Planificateur de tâches Windows
# ============================================

$ProjectDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $ProjectDir) { $ProjectDir = Split-Path -Parent $PSScriptRoot }
$BackupDir = Join-Path $ProjectDir "backups"
$ContainerName = "ppm-mysql"
$Date = Get-Date -Format "yyyyMMdd_HHmmss"
$KeepCount = 7

# Charger .env
$envFile = Join-Path $ProjectDir ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
        }
    }
}

$DbRootPassword = $env:DB_ROOT_PASSWORD
$DbName = $env:DB_NAME
if (-not $DbName) { $DbName = "pushplay" }

# Créer le dossier
if (-not (Test-Path $BackupDir)) { New-Item -ItemType Directory -Path $BackupDir | Out-Null }

Write-Host "📦 Backup PushPlay DB — $Date"

# Vérifier le conteneur
$running = docker ps --format '{{.Names}}' | Where-Object { $_ -eq $ContainerName }
if (-not $running) {
    Write-Host "❌ Le conteneur $ContainerName n'est pas en cours d'exécution" -ForegroundColor Red
    exit 1
}

# Dump
$BackupFile = Join-Path $BackupDir "pushplay_$Date.sql"
docker exec $ContainerName mysqldump -u root "-p$DbRootPassword" --single-transaction --routines --triggers $DbName > $BackupFile

if ($LASTEXITCODE -eq 0) {
    $size = (Get-Item $BackupFile).Length / 1KB
    Write-Host "✅ Backup créé : $BackupFile ($([math]::Round($size, 1)) KB)" -ForegroundColor Green
} else {
    Write-Host "❌ Erreur lors du backup" -ForegroundColor Red
    exit 1
}

# Rotation : garder les N derniers
$backups = Get-ChildItem $BackupDir -Filter "pushplay_*.sql" | Sort-Object LastWriteTime -Descending
if ($backups.Count -gt $KeepCount) {
    $toDelete = $backups | Select-Object -Skip $KeepCount
    $toDelete | ForEach-Object {
        Remove-Item $_.FullName
        Write-Host "🗑️  Ancien backup supprimé : $($_.Name)"
    }
}

Write-Host "`n📁 Backups actuels :"
Get-ChildItem $BackupDir -Filter "pushplay_*.sql" | Sort-Object LastWriteTime -Descending | Format-Table Name, @{N='Taille';E={'{0:N1} KB' -f ($_.Length/1KB)}}, LastWriteTime -AutoSize
