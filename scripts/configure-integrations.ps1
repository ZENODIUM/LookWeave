param(
    [Parameter(Mandatory = $true)][string]$GeminiApiKey,
    [Parameter(Mandatory = $true)][string]$GroqApiKey,
    [Parameter(Mandatory = $true)][string]$ElevenLabsApiKey,
    [Parameter(Mandatory = $true)][string]$YouCamApiKey,
    [ValidateSet("Emulator", "Phone")][string]$Target = "Emulator"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$backendEnv = Join-Path $projectRoot ".env"
$localProperties = Join-Path $projectRoot "local.properties"

@(
    "GEMINI_API_KEY=$GeminiApiKey"
    "GROQ_API_KEY=$GroqApiKey"
    "ELEVENLABS_API_KEY=$ElevenLabsApiKey"
    "YOUCAM_API_KEY=$YouCamApiKey"
    "PORT=8787"
) | Set-Content -LiteralPath $backendEnv -Encoding utf8

$backendUrl = "http://127.0.0.1:8787"
$properties = if (Test-Path -LiteralPath $localProperties) { Get-Content -LiteralPath $localProperties } else { @() }
$properties = @($properties | Where-Object { $_ -notmatch '^LOOKWEAVE_BACKEND_URL=' })
$properties += "LOOKWEAVE_BACKEND_URL=$backendUrl"
$properties | Set-Content -LiteralPath $localProperties -Encoding utf8

& adb reverse tcp:8787 tcp:8787
if ($LASTEXITCODE -ne 0) { throw "adb reverse failed. Start the emulator or connect the phone with wireless debugging, then run this command again." }

Write-Host "LookWeave integrations configured for $Target. Rebuild the app, then run scripts\start-gateway.ps1."
