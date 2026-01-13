# Git Push Scripti
# Tüm değişiklikleri commit edip push eder

Write-Host "=== Git Push İşlemi ===" -ForegroundColor Green

# 1. Durum kontrolü
Write-Host "`n1. Git durumu kontrol ediliyor..." -ForegroundColor Yellow
$status = git status --short
if ($status) {
    Write-Host "   Değişiklikler bulundu:" -ForegroundColor Cyan
    git status --short
} else {
    Write-Host "   [BİLGİ] Commit edilecek değişiklik yok" -ForegroundColor Yellow
}

# 2. Tüm değişiklikleri ekle
Write-Host "`n2. Değişiklikler ekleniyor..." -ForegroundColor Yellow
git add .
if ($LASTEXITCODE -eq 0) {
    Write-Host "   [OK] Tüm değişiklikler eklendi" -ForegroundColor Green
} else {
    Write-Host "   [HATA] Git add başarısız!" -ForegroundColor Red
    exit 1
}

# 3. Commit mesajı al
Write-Host "`n3. Commit mesajı:" -ForegroundColor Yellow
$defaultMessage = "Jenkins ve Docker Compose scriptleri eklendi, Jenkinsfile güncellendi"
Write-Host "   Varsayılan mesaj: $defaultMessage" -ForegroundColor Cyan
$commitMessage = Read-Host "   Commit mesajını girin (Enter = varsayılan)"

if ([string]::IsNullOrWhiteSpace($commitMessage)) {
    $commitMessage = $defaultMessage
}

# 4. Commit et
Write-Host "`n4. Commit ediliyor..." -ForegroundColor Yellow
git commit -m $commitMessage
if ($LASTEXITCODE -eq 0) {
    Write-Host "   [OK] Commit başarılı" -ForegroundColor Green
} else {
    Write-Host "   [UYARI] Commit başarısız veya commit edilecek değişiklik yok" -ForegroundColor Yellow
}

# 5. Push et
Write-Host "`n5. GitHub'a push ediliyor..." -ForegroundColor Yellow
git push origin main
if ($LASTEXITCODE -eq 0) {
    Write-Host "   [OK] Push başarılı!" -ForegroundColor Green
    Write-Host "`n   GitHub Repository: https://github.com/BarisBaran/kitapKiralama" -ForegroundColor Cyan
} else {
    Write-Host "   [HATA] Push başarısız!" -ForegroundColor Red
    Write-Host "   Hata detayları yukarıda gösterilmiştir." -ForegroundColor Yellow
    exit 1
}

Write-Host "`n[OK] Git push işlemi tamamlandı!" -ForegroundColor Green
