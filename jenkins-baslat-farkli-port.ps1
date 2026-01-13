# Jenkins Baslatma - Farkli Port (8081) ile
# Port 8080 kullaniliyorsa bu script'i kullanin

Write-Host "=== Jenkins Baslatma (Port 8081) ===" -ForegroundColor Green

# Docker kontrolu
Write-Host "`n1. Docker kontrol ediliyor..." -ForegroundColor Yellow
try {
    docker version | Out-Null
    Write-Host "   [OK] Docker calisiyor" -ForegroundColor Green
} catch {
    Write-Host "   [HATA] Docker Desktop calismiyor!" -ForegroundColor Red
    exit 1
}

# Eski container'lari temizle
Write-Host "`n2. Eski container'lar temizleniyor..." -ForegroundColor Yellow
docker stop jenkins 2>$null | Out-Null
docker rm jenkins 2>$null | Out-Null
Write-Host "   [OK] Temizlik tamamlandi" -ForegroundColor Green

# Jenkins'i farkli port ile baslat (8081)
Write-Host "`n3. Jenkins container'i baslatiliyor (Port 8081)..." -ForegroundColor Yellow
$containerId = docker run -d `
    --name jenkins `
    --restart=unless-stopped `
    -p 8081:8080 `
    -p 50000:50000 `
    -v jenkins_home:/var/jenkins_home `
    -v /var/run/docker.sock:/var/run/docker.sock `
    jenkins/jenkins:lts 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "   [OK] Container baslatildi" -ForegroundColor Green
} else {
    Write-Host "   [HATA] Container baslatilamadi!" -ForegroundColor Red
    Write-Host "   Hata: $containerId" -ForegroundColor Red
    exit 1
}

# Container'in calisip calismadigini kontrol et
Write-Host "`n4. Container durumu kontrol ediliyor..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

$containerStatus = docker ps --filter "name=jenkins" --format "{{.Status}}" 2>$null
if ($containerStatus) {
    Write-Host "   [OK] Container calisiyor: $containerStatus" -ForegroundColor Green
} else {
    Write-Host "   [HATA] Container calismiyor! Loglar:" -ForegroundColor Red
    docker logs jenkins --tail 20 2>$null
    exit 1
}

# Jenkins'in hazir olmasini bekle
Write-Host "`n5. Jenkins baslatiliyor, bekleniyor..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$isReady = $false

while ($attempt -lt $maxAttempts -and -not $isReady) {
    Start-Sleep -Seconds 2
    $attempt++
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8081" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response -and ($response.StatusCode -eq 200 -or $response.StatusCode -eq 403)) {
            $isReady = $true
            Write-Host "   [OK] Jenkins hazir! ($attempt. deneme)" -ForegroundColor Green
        }
    } catch {
        if ($attempt -lt $maxAttempts) {
            Write-Host "   [BEKLENIYOR] ($attempt/$maxAttempts)..." -ForegroundColor Cyan
        }
    }
}

# Admin sifresini al
Write-Host "`n6. Admin sifresi aliniyor..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

$password = $null
$passwordAttempts = 0
$maxPasswordAttempts = 5

while ($passwordAttempts -lt $maxPasswordAttempts -and -not $password) {
    $password = docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null
    if ($password) {
        $password = $password.Trim()
    } else {
        $passwordAttempts++
        if ($passwordAttempts -lt $maxPasswordAttempts) {
            Start-Sleep -Seconds 3
        }
    }
}

# Sonuclari goster
Write-Host "`n===============================================================" -ForegroundColor Cyan
Write-Host "   JENKINS BILGILERI (Port 8081)" -ForegroundColor Green
Write-Host "===============================================================" -ForegroundColor Cyan
Write-Host "`n   URL: " -NoNewline -ForegroundColor White
Write-Host "http://localhost:8081" -ForegroundColor Yellow

if ($password) {
    Write-Host "`n   Admin Password: " -NoNewline -ForegroundColor White
    Write-Host $password -ForegroundColor Yellow
    Write-Host "`n   Bu sifreyi kopyalayin ve Jenkins kurulumunda kullanin!" -ForegroundColor Cyan
} else {
    Write-Host "`n   [UYARI] Admin sifresi henuz hazir degil" -ForegroundColor Yellow
    Write-Host "   Komut: docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword" -ForegroundColor Cyan
}

Write-Host "`n===============================================================" -ForegroundColor Cyan
Write-Host "`n[OK] Jenkins baslatma islemi tamamlandi!" -ForegroundColor Green
Write-Host "`nNOT: Jenkins Port 8081'de calisiyor (8080 kullaniliyordu)" -ForegroundColor Yellow
