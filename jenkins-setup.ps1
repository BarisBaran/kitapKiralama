# Jenkins Sifirdan Kurulum ve Baslatma Scripti
# PowerShell Script

Write-Host "=== Jenkins Sifirdan Kurulum ===" -ForegroundColor Green

# 1. Docker Desktop'in calistigini kontrol et
Write-Host "`n1. Docker Desktop kontrol ediliyor..." -ForegroundColor Yellow
try {
    docker version | Out-Null
    Write-Host "   [OK] Docker calisiyor" -ForegroundColor Green
} catch {
    Write-Host "   [HATA] Docker Desktop calismiyor! Lutfen Docker Desktop'i baslatin." -ForegroundColor Red
    exit 1
}

# 2. Mevcut Jenkins container'ini durdur ve kaldir
Write-Host "`n2. Eski Jenkins container'lari temizleniyor..." -ForegroundColor Yellow
docker stop jenkins 2>$null
docker rm jenkins 2>$null
docker stop kitap-kiralama-jenkins 2>$null
docker rm kitap-kiralama-jenkins 2>$null
Write-Host "   [OK] Temizlik tamamlandi" -ForegroundColor Green

# 3. Jenkins volume'unu kontrol et (opsiyonel - verileri korumak icin)
Write-Host "`n3. Jenkins volume kontrol ediliyor..." -ForegroundColor Yellow
$jenkinsVolume = docker volume ls -q | Select-String -Pattern "jenkins"
if ($jenkinsVolume) {
    Write-Host "   [UYARI] Mevcut Jenkins volume bulundu: $jenkinsVolume" -ForegroundColor Yellow
    $response = Read-Host "   Jenkins verilerini silmek istiyor musunuz? (y/n)"
    if ($response -eq "y" -or $response -eq "Y") {
        docker volume rm $jenkinsVolume 2>$null
        Write-Host "   [OK] Volume silindi" -ForegroundColor Green
    } else {
        Write-Host "   [INFO] Volume korunuyor (veriler saklanacak)" -ForegroundColor Cyan
    }
}

# 4. Jenkins container'ini baslat
Write-Host "`n4. Jenkins container'i baslatiliyor..." -ForegroundColor Yellow
docker run -d `
    --name jenkins `
    --restart=unless-stopped `
    -p 8080:8080 `
    -p 50000:50000 `
    -v jenkins_home:/var/jenkins_home `
    -v /var/run/docker.sock:/var/run/docker.sock `
    jenkins/jenkins:lts

if ($LASTEXITCODE -eq 0) {
    Write-Host "   [OK] Jenkins container baslatildi" -ForegroundColor Green
} else {
    Write-Host "   [HATA] Jenkins container baslatilamadi!" -ForegroundColor Red
    exit 1
}

# 5. Jenkins'in hazir olmasini bekle
Write-Host "`n5. Jenkins baslatiliyor, lutfen bekleyin..." -ForegroundColor Yellow
$maxAttempts = 30
$attempt = 0
$isReady = $false

while ($attempt -lt $maxAttempts -and -not $isReady) {
    Start-Sleep -Seconds 2
    $attempt++
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080" -UseBasicParsing -TimeoutSec 2 -ErrorAction Stop
        if ($response.StatusCode -eq 200 -or $response.StatusCode -eq 403) {
            $isReady = $true
            Write-Host "   [OK] Jenkins hazir! ($attempt. deneme)" -ForegroundColor Green
        }
    } catch {
        Write-Host "   [BEKLENIYOR] ($attempt/$maxAttempts)" -ForegroundColor Cyan
    }
}

if (-not $isReady) {
    Write-Host "   [UYARI] Jenkins baslatma zaman asimi, ancak container calisiyor olabilir" -ForegroundColor Yellow
}

# 6. Initial Admin Password'i goster
Write-Host "`n6. Jenkins Admin Sifresi aliniyor..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
$password = docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>$null

if ($password) {
    Write-Host "`n" -NoNewline
    Write-Host "===============================================================" -ForegroundColor Cyan
    Write-Host "   JENKINS BASLATILDI!" -ForegroundColor Green
    Write-Host "===============================================================" -ForegroundColor Cyan
    Write-Host "`n   Jenkins URL: " -NoNewline -ForegroundColor White
    Write-Host "http://localhost:8080" -ForegroundColor Yellow
    Write-Host "`n   Initial Admin Password: " -NoNewline -ForegroundColor White
    Write-Host $password.Trim() -ForegroundColor Yellow
    Write-Host "`n   Bu sifreyi kopyalayin ve Jenkins kurulumunda kullanin!" -ForegroundColor Cyan
    Write-Host "===============================================================`n" -ForegroundColor Cyan
} else {
    Write-Host "   [UYARI] Sifre henuz hazir degil, birkac saniye sonra tekrar deneyin:" -ForegroundColor Yellow
    Write-Host "   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword" -ForegroundColor Cyan
}

# 7. Container durumunu goster
Write-Host "`n7. Container durumu:" -ForegroundColor Yellow
docker ps --filter "name=jenkins" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

Write-Host "`n[OK] Jenkins kurulumu tamamlandi!" -ForegroundColor Green
Write-Host "`nSonraki adimlar:" -ForegroundColor Cyan
Write-Host "   1. Tarayicida http://localhost:8080 adresine gidin" -ForegroundColor White
Write-Host "   2. Yukaridaki sifreyi girin" -ForegroundColor White
Write-Host "   3. 'Install suggested plugins' secenegini secin" -ForegroundColor White
Write-Host "   4. Admin kullanici olusturun" -ForegroundColor White
Write-Host "   5. Jenkins'i kullanmaya baslayin!" -ForegroundColor White
