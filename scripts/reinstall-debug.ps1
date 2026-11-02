# Install the arm64-only debug APK (never a multi-ABI fat copy).
# abiFilters keep app-debug.apk ~96MB; artifacts/app-debug-install.apk is synced from that.
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$adbCommand = Get-Command adb -ErrorAction SilentlyContinue
$adb = if ($adbCommand) { $adbCommand.Source } else { $null }
if (-not $adb) {
    throw "adb was not found on PATH. Install Android SDK Platform-Tools and add it to PATH."
}

$apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
$artifactApk = Join-Path $root "artifacts\app-debug-install.apk"

if (-not (Test-Path $apk)) {
    Write-Error "Missing arm64 debug APK: $apk — run .\gradlew.bat assembleDebug first."
}

# Keep legacy artifact path safe: always overwrite from the abi-filtered APK only.
$artifactsDir = Split-Path -Parent $artifactApk
if (-not (Test-Path $artifactsDir)) {
    New-Item -ItemType Directory -Path $artifactsDir | Out-Null
}
Copy-Item -LiteralPath $apk -Destination $artifactApk -Force

$apkItem = Get-Item -LiteralPath $apk
$artItem = Get-Item -LiteralPath $artifactApk
Write-Host ("SOURCE={0:N1} MB  ARTIFACT={1:N1} MB  (must match; install uses SOURCE)" -f ($apkItem.Length / 1MB), ($artItem.Length / 1MB))
if ($artItem.Length -ne $apkItem.Length) {
    Write-Error "Artifact sync failed: size mismatch."
}
if ($apkItem.Length -gt 150MB) {
    Write-Error "APK is unexpectedly large ($([math]::Round($apkItem.Length/1MB,1)) MB). Refusing install — check abiFilters (arm64-v8a only)."
}

& $adb kill-server 2>$null
Start-Sleep -Seconds 2
& $adb start-server
Start-Sleep -Seconds 3

for ($i = 1; $i -le 30; $i++) {
    $raw = & $adb devices 2>$null
    $lines = @($raw | Where-Object { $_ -match "\tdevice$" })
    Write-Host "poll $i : $($lines -join ' | ')"
    if ($lines.Count -lt 1) {
        Start-Sleep -Seconds 4
        continue
    }

    $serial = ($lines[0] -split "`t")[0].Trim()
    $mdns = @($lines | Where-Object { $_ -like "adb-*" })
    if ($mdns.Count -gt 0) {
        $serial = ($mdns[0] -split "`t")[0].Trim()
    }
    Write-Host "SERIAL=$serial APK=$($apkItem.Length) path=$apk"

    & $adb -s $serial wait-for-device
    & $adb -s $serial install -r -t $apk
    $code = $LASTEXITCODE
    Write-Host "EXIT=$code"
    if ($code -eq 0) {
        & $adb -s $serial reverse tcp:8787 tcp:8787
        & $adb -s $serial reverse --list
        & $adb -s $serial shell pm path com.vizeno.loomie
        & $adb -s $serial shell dumpsys package com.vizeno.loomie |
            Select-String -Pattern "lastUpdateTime|versionName" |
            Select-Object -First 5
        exit 0
    }
    Write-Host "retry after fail"
    Start-Sleep -Seconds 4
}

Write-Host "GAVE_UP"
exit 3
