# Jenkins Basit Baslatma Komutlari
# Docker Desktop calistiktan sonra bu komutlari calistirin

Write-Host "=== Jenkins Baslatma ===" -ForegroundColor Green

# Docker kontrolu
Write-Host "`n1. Docker kontrol ediliyor..." -ForegroundColor Yellow
try {
    $dockerCheck = docker version 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "   [HATA] Docker Desktop calismiyor! Lutfen Docker Desktop'i baslatin." -ForegroundColor Red
        exit 1
    }
    Write-Host "   [OK] Docker calisiyor" -ForegroundColor Green
} catch {
    Write-Host "   [HATA] Docker bulunamadi!" -ForegroundColor Red
    exit 1
}

# Eski container'lari temizle
Write-Host "`n2. Eski container'lar temizleniyor..." -ForegroundColor Yellow
$oldContainers = docker ps -a --filter "name=jenkins" --format "{{.ID}}" 2>$null
if ($oldContainers) {
    foreach ($container in $oldContainers) {
        docker stop $container 2>$null | Out-Null
        docker rm $container 2>$null | Out-Null
    }
    Write-Host "   [OK] Eski container'lar temizlendi" -ForegroundColor Green
} else {
    Write-Host "   [INFO] Temizlenecek container yok" -ForegroundColor Cyan
}

# Jenkins'i baslat
Write-Host "`n3. Jenkins container'i baslatiliyor..." -ForegroundColor Yellow
$containerId = docker run -d `
    --name jenkins `
    --restart=unless-stopped `
    -p 8080:8080 `
    -p 50000:50000 `
    -v jenkins_home:/var/jenkins_home `
    -v /var/run/docker.sock:/var/run/docker.sock `
    jenkins/jenkins:lts 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "   [OK] Container baslatildi: $containerId" -ForegroundColor Green
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
    Write-Host "   [HATA] Container calismiyor! Loglar kontrol ediliyor..." -ForegroundColor Red
    Write-Host "`n   Container Loglari:" -ForegroundColor Yellow
    docker logs jenkins 2>&1 | Select-Object -Last 20
    Write-Host "`n   [UYARI] Container baslatildi ama calismiyor. Loglari kontrol edin." -ForegroundColor Yellow
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
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
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

if (-not $isReady) {
    Write-Host "   [UYARI] Jenkins henuz hazir degil, ancak container calisiyor" -ForegroundColor Yellow
    Write-Host "   Biraz daha bekleyip tekrar deneyin" -ForegroundColor Yellow
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
            Write-Host "   [BEKLENIYOR] Sifre hazirlaniyor... ($passwordAttempts/$maxPasswordAttempts)" -ForegroundColor Cyan
            Start-Sleep -Seconds 3
        }
    }
}

# Sonuclari goster
Write-Host "`n===============================================================" -ForegroundColor Cyan
Write-Host "   JENKINS BILGILERI" -ForegroundColor Green
Write-Host "===============================================================" -ForegroundColor Cyan
Write-Host "`n   URL: " -NoNewline -ForegroundColor White
Write-Host "http://localhost:8080" -ForegroundColor Yellow

if ($password) {
    Write-Host "`n   Admin Password: " -NoNewline -ForegroundColor White
    Write-Host $password -ForegroundColor Yellow
    Write-Host "`n   Bu sifreyi kopyalayin ve Jenkins kurulumunda kullanin!" -ForegroundColor Cyan
} else {
    Write-Host "`n   [UYARI] Admin sifresi henuz hazir degil" -ForegroundColor Yellow
    Write-Host "   Asagidaki komutu calistirarak sifreyi alabilirsiniz:" -ForegroundColor White
    Write-Host "   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword" -ForegroundColor Cyan
}

Write-Host "`n   Container Durumu:" -ForegroundColor White
docker ps --filter "name=jenkins" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" 2>$null

Write-Host "`n===============================================================" -ForegroundColor Cyan
Write-Host "`n[OK] Jenkins baslatma islemi tamamlandi!" -ForegroundColor Green
Write-Host "`nSonraki adimlar:" -ForegroundColor Cyan
Write-Host "   1. Tarayicida http://localhost:8080 adresine gidin" -ForegroundColor White
Write-Host "   2. Admin sifresini girin" -ForegroundColor White
Write-Host "   3. 'Install suggested plugins' secenegini secin" -ForegroundColor White
Write-Host "   4. Admin kullanici olusturun" -ForegroundColor White
