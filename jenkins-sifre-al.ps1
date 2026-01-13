# Jenkins Admin Sifresi Alma Scripti

Write-Host "=== Jenkins Admin Sifresi ===" -ForegroundColor Green

# Container calisiyor mu kontrol et
$isRunning = docker ps --filter "name=jenkins" --format "{{.Names}}" 2>$null

if (-not $isRunning) {
    Write-Host "[HATA] Jenkins container'i calismiyor!" -ForegroundColor Red
    Write-Host "Once Jenkins'i baslatin: .\jenkins-baslat-farkli-port.ps1" -ForegroundColor Yellow
    exit 1
}

# Port bilgisini al
$ports = docker ps --filter "name=jenkins" --format "{{.Ports}}" 2>$null
if ($ports -match "8081") {
    $jenkinsUrl = "http://localhost:8081"
} else {
    $jenkinsUrl = "http://localhost:8080"
}

Write-Host "`nJenkins URL: $jenkinsUrl" -ForegroundColor Yellow
Write-Host "`nAdmin Sifresi aliniyor..." -ForegroundColor Cyan

# Sifreyi al
$password = docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>&1

if ($password -and $password -notmatch "Error" -and $password.Trim().Length -gt 0) {
    Write-Host "`n===============================================================" -ForegroundColor Cyan
    Write-Host "   JENKINS ADMIN SIFRESI" -ForegroundColor Green
    Write-Host "===============================================================" -ForegroundColor Cyan
    Write-Host "`n   URL: $jenkinsUrl" -ForegroundColor White
    Write-Host "`n   Admin Password: " -NoNewline -ForegroundColor White
    Write-Host $password.Trim() -ForegroundColor Yellow
    Write-Host "`n   Bu sifreyi kopyalayin!" -ForegroundColor Cyan
    Write-Host "===============================================================" -ForegroundColor Cyan
} else {
    Write-Host "`n[UYARI] Admin sifresi henuz hazir degil" -ForegroundColor Yellow
    Write-Host "Jenkins'in tamamen baslamasi icin biraz daha bekleyin." -ForegroundColor Cyan
    Write-Host "`nContainer loglarini kontrol edin:" -ForegroundColor White
    Write-Host "docker logs jenkins --tail 20" -ForegroundColor Cyan
}
