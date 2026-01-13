# Jenkins Durum Kontrol ve Duzeltme Scripti

Write-Host "=== Jenkins Durum Kontrolu ===" -ForegroundColor Green

# Container durumunu kontrol et
Write-Host "`n1. Container durumu kontrol ediliyor..." -ForegroundColor Yellow
$containerStatus = docker ps -a --filter "name=jenkins" --format "{{.Names}}|{{.Status}}|{{.ID}}" 2>$null

if (-not $containerStatus) {
    Write-Host "   [BILGI] Jenkins container'i bulunamadi" -ForegroundColor Yellow
    Write-Host "   Jenkins'i baslatmak icin: .\jenkins-baslat.ps1" -ForegroundColor Cyan
    exit 0
}

$containerInfo = $containerStatus -split '\|'
$containerName = $containerInfo[0]
$status = $containerInfo[1]
$containerId = $containerInfo[2]

Write-Host "   Container: $containerName" -ForegroundColor White
Write-Host "   Durum: $status" -ForegroundColor White

# Container calisiyor mu?
$isRunning = docker ps --filter "name=jenkins" --format "{{.Names}}" 2>$null

if ($isRunning) {
    Write-Host "`n   [OK] Container calisiyor!" -ForegroundColor Green
    
    # Jenkins erisilebilir mi?
    Write-Host "`n2. Jenkins erisilebilirlik kontrolu..." -ForegroundColor Yellow
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
        Write-Host "   [OK] Jenkins erisilebilir (HTTP $($response.StatusCode))" -ForegroundColor Green
        
        # Admin sifresini al
        Write-Host "`n3. Admin sifresi aliniyor..." -ForegroundColor Yellow
        $password = docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
        if ($password) {
            Write-Host "`n   ===============================================================" -ForegroundColor Cyan
            Write-Host "   JENKINS BILGILERI" -ForegroundColor Green
            Write-Host "   ===============================================================" -ForegroundColor Cyan
            Write-Host "   URL: http://localhost:8080" -ForegroundColor Yellow
            Write-Host "   Admin Password: $($password.Trim())" -ForegroundColor Yellow
            Write-Host "   ===============================================================" -ForegroundColor Cyan
        } else {
            Write-Host "   [UYARI] Admin sifresi henuz hazir degil" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "   [UYARI] Jenkins henuz hazir degil" -ForegroundColor Yellow
        Write-Host "   Loglar kontrol ediliyor..." -ForegroundColor Cyan
        docker logs jenkins --tail 20 2>$null
    }
} else {
    Write-Host "`n   [HATA] Container calismiyor!" -ForegroundColor Red
    
    # Loglari goster
    Write-Host "`n2. Container loglari:" -ForegroundColor Yellow
    docker logs jenkins --tail 30 2>$null
    
    # Yeniden baslatmayi dene
    Write-Host "`n3. Container yeniden baslatiliyor..." -ForegroundColor Yellow
    docker start jenkins 2>&1 | Out-Null
    
    Start-Sleep -Seconds 5
    
    $isRunningNow = docker ps --filter "name=jenkins" --format "{{.Names}}" 2>$null
    if ($isRunningNow) {
        Write-Host "   [OK] Container baslatildi!" -ForegroundColor Green
        Write-Host "   Jenkins'in hazir olmasi icin 30 saniye bekleyin..." -ForegroundColor Cyan
    } else {
        Write-Host "   [HATA] Container baslatilamadi!" -ForegroundColor Red
        Write-Host "   Container'i silip yeniden olusturmayi deneyin:" -ForegroundColor Yellow
        Write-Host "   docker rm -f jenkins" -ForegroundColor Cyan
        Write-Host "   .\jenkins-baslat.ps1" -ForegroundColor Cyan
    }
}

# Container listesi
Write-Host "`n4. Tum Jenkins container'lari:" -ForegroundColor Yellow
docker ps -a --filter "name=jenkins" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>$null
