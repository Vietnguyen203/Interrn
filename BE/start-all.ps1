$ErrorActionPreference = "Stop"

$services = @("account-service", "catalog-service", "order-service", "kitchen-service", "statistics-service", "api-gateway")

Write-Host "Starting all Microservices..." -ForegroundColor Green

foreach ($service in $services) {
    Write-Host "Starting $service..." -ForegroundColor Cyan
    
    # Start each service in a separate new PowerShell window
    $args = "-NoExit -Command `"cd $service; if (Test-Path '.env') { Get-Content .env | Where-Object { `$_.trim() -ne '' -and `$_.StartsWith('#') -eq `$false } | ForEach-Object { `$name, `$value = `$_.split('=', 2); [System.Environment]::SetEnvironmentVariable(`$name, `$value, 'Process') } }; ..\gradlew bootRun`""
    Start-Process powershell.exe -ArgumentList $args -WindowStyle Normal
    
    Start-Sleep -Seconds 3 # Give it a brief moment before launching the next one
}

Write-Host "All services launched in separate windows!" -ForegroundColor Green
Write-Host "API Gateway is running on port 8080." -ForegroundColor Yellow
