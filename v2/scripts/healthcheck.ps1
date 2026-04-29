# ============================================
# PushPlayManager V2 — Healthcheck (Windows PowerShell)
#
# Vérifie que les conteneurs tournent et les redémarre si nécessaire
# Planifier toutes les 5 minutes via Planificateur de tâches
# ============================================

$ProjectDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
if (-not $ProjectDir) { $ProjectDir = Split-Path -Parent $PSScriptRoot }
$ComposeFile = Join-Path $ProjectDir "docker-compose.prod.yml"
$LogFile = Join-Path $ProjectDir "logs" "healthcheck.log"

# Créer le dossier de logs
$logDir = Split-Path $LogFile -Parent
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir | Out-Null }

$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

function Log($msg) {
    $line = "[$timestamp] $msg"
    Add-Content -Path $LogFile -Value $line
    Write-Host $line
}

# Vérifier les conteneurs
$containers = @("ppm-web", "ppm-mysql")
$needsRestart = $false

foreach ($container in $containers) {
    $status = docker inspect --format '{{.State.Status}}' $container 2>$null
    if ($status -ne "running") {
        Log "⚠️  $container n'est pas en cours d'exécution (status: $status)"
        $needsRestart = $true
    }
}

if ($needsRestart) {
    Log "🔄 Redémarrage des conteneurs..."
    Set-Location $ProjectDir
    docker compose -f $ComposeFile up -d
    Log "✅ Redémarrage effectué"
} else {
    # Vérifier que le web répond
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:3000" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Log "✅ Tous les services sont opérationnels"
        } else {
            Log "⚠️  Web répond avec le code $($response.StatusCode)"
        }
    } catch {
        Log "⚠️  Web ne répond pas — tentative de redémarrage"
        Set-Location $ProjectDir
        docker compose -f $ComposeFile restart web
        Log "🔄 Conteneur web redémarré"
    }
}

# Rotation du log (garder les 1000 dernières lignes)
if (Test-Path $LogFile) {
    $lines = Get-Content $LogFile
    if ($lines.Count -gt 1000) {
        $lines | Select-Object -Last 1000 | Set-Content $LogFile
    }
}
