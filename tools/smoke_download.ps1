# smoke_download.ps1
# Bounded download feature smoke script for StreamVault.
# Runs exactly three bounded Gradle commands and reports X/3 results.

Set-StrictMode -Version Latest
$ErrorActionPreference = "Continue"

$Passed = 0
$Failed = 0

function Run-Command {
    param(
        [string]$Cmd,
        [string]$Description
    )

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "Running: $Description" -ForegroundColor Cyan
    Write-Host "Command: $Cmd" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Cyan

    # Run via cmd /c and capture full stdout + stderr into a temp file.
    $tmpFile = [System.IO.Path]::Combine(
        [System.IO.Path]::GetTempPath(),
        "smoke_cmd_$((Get-Date -Format 'yyyyMMdd_HHmmss_ffff').ToString().Replace(':','_')).txt"
    )

    # Redirect both stdout and stderr to the temp file via cmd,
    # then read it back so we can display the full output.
    $cmdLine = "/c $Cmd >`"$tmpFile`" 2>&1"
    $process = Start-Process `
        -FilePath "cmd.exe" `
        -ArgumentList $cmdLine `
        -WorkingDirectory (Get-Location).Path `
        -NoNewWindow `
        -PassThru `
        -Wait

    $exitCode = $process.ExitCode

    # Print full captured output (stdout + stderr merged)
    $output = Get-Content -Path $tmpFile -Raw -ErrorAction SilentlyContinue
    if ($output) {
        Write-Host $output
    }

    # Clean up temp file
    Remove-Item -Path $tmpFile -ErrorAction SilentlyContinue

    Write-Host "----------------------------------------" -ForegroundColor Cyan
    Write-Host "Exit code: $exitCode" -ForegroundColor Cyan

    if ($exitCode -eq 0) {
        Write-Host "PASS: $Cmd" -ForegroundColor Green
        $script:Passed++
    }
    else {
        Write-Host "FAIL: $Cmd" -ForegroundColor Red
        $script:Failed++
    }

    Write-Host "========================================" -ForegroundColor Cyan
}

# Command 1: Build domain module (DownloadModels compiles)
Run-Command `
    -Cmd ".\gradlew.bat :domain:build --no-daemon --console=plain" `
    -Description "Build domain module (DownloadModels)"

# Command 2: Compile data module (DownloadEntity, DownloadDao, DownloadManagerImpl)
Run-Command `
    -Cmd ".\gradlew.bat :data:compileDebugKotlin --no-daemon --console=plain" `
    -Description "Compile data module (DownloadEntity, DownloadDao, DownloadManagerImpl)"

# Command 3: Compile app module (DownloadForegroundService, download buttons, DownloadsScreen)
Run-Command `
    -Cmd ".\gradlew.bat :app:compileDebugKotlin --no-daemon --console=plain" `
    -Description "Compile app module (DownloadForegroundService, downloads UI)"

# Final summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "SUMMARY: $Passed/3 tests passed" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($Failed -gt 0) {
    Write-Host "ONE OR MORE COMMANDS FAILED" -ForegroundColor Red
    exit 1
}

Write-Host "ALL COMMANDS PASSED" -ForegroundColor Green
exit 0
