$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$rootEnvFile = Join-Path $projectRoot ".env"
$backendEnvFile = Join-Path $projectRoot "backend\.env"
if (-not (Test-Path -LiteralPath $rootEnvFile) -and -not (Test-Path -LiteralPath $backendEnvFile)) {
    throw "Missing .env. Run scripts\configure-integrations.ps1 first."
}

$process = Start-Process -FilePath "node" -ArgumentList "server.mjs" -WorkingDirectory (Join-Path $projectRoot "backend") -WindowStyle Hidden -PassThru
Start-Sleep -Milliseconds 700
try {
    $health = Invoke-RestMethod -Uri "http://127.0.0.1:8787/health" -TimeoutSec 5
    Write-Host "LookWeave gateway started (PID $($process.Id))."
    $health | ConvertTo-Json -Depth 4
} catch {
    Stop-Process -Id $process.Id -ErrorAction SilentlyContinue
    throw "Gateway did not become healthy: $($_.Exception.Message)"
}
