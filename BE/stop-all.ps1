$ErrorActionPreference = "SilentlyContinue"
Write-Host "Stopping all Java processes..." -ForegroundColor Yellow
Get-Process -Name "java" | Stop-Process -Force
Write-Host "All Java processes stopped!" -ForegroundColor Green
