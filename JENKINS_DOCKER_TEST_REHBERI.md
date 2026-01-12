# Jenkins ve Docker ile Test Rehberi

## 📋 İçindekiler
1. [Docker ile Manuel Test](#1-docker-ile-manuel-test)
2. [Jenkins Pipeline ile Otomatik Test](#2-jenkins-pipeline-ile-otomatik-test)
3. [Build Komutları](#3-build-komutları)
4. [Test Senaryoları](#4-test-senaryoları)

---

## 1. Docker ile Manuel Test

### 1.1. Docker Compose ile Projeyi Başlatma

```powershell
# Proje klasörüne git
cd C:\Users\baris\OneDrive\Desktop\kitapKiralama

# Docker Compose ile servisleri başlat (build ile)
docker-compose up -d --build
```

**Ne yapar?**
- PostgreSQL container'ını başlatır
- Spring Boot uygulamasını build eder ve container'da çalıştırır
- Her iki servisi de arka planda (`-d`) çalıştırır

### 1.2. Container Durumunu Kontrol Etme

```powershell
# Tüm container'ların durumunu göster
docker-compose ps

# Sadece çalışan container'ları göster
docker ps
```

**Beklenen Çıktı:**
```
NAME                      STATUS
kitap-kiralama-postgres   Up (healthy)
kitap-kiralama-app        Up (health: starting)
```

### 1.3. Logları İzleme

```powershell
# Uygulama loglarını izle
docker-compose logs -f app

# PostgreSQL loglarını izle
docker-compose logs -f postgres

# Tüm logları izle
docker-compose logs -f
```

### 1.4. Uygulamayı Test Etme

**Tarayıcıda test:**
- Ana sayfa: `http://localhost:8080/login`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API: `http://localhost:8080/api/books`

**PowerShell ile test:**
```powershell
# API'yi test et
Invoke-WebRequest -Uri http://localhost:8080/api/books -Method GET
```

### 1.5. Container'ları Durdurma

```powershell
# Container'ları durdur (veriler kalır)
docker-compose down

# Container'ları durdur ve volume'ları sil (veriler silinir)
docker-compose down -v

# Container'ları yeniden başlat
docker-compose restart
```

---

## 2. Jenkins Pipeline ile Otomatik Test

### 2.1. Jenkins Pipeline Oluşturma

#### Adım 1: Jenkins Web Arayüzüne Eriş
```
http://localhost:8080
```

#### Adım 2: Pipeline Oluştur
1. **Jenkins Dashboard** → **New Item**
2. **İsim:** `kitap-kiralama-pipeline`
3. **Pipeline** seç → **OK**

#### Adım 3: Pipeline Yapılandırması
- **Definition:** Pipeline script from SCM
- **SCM:** Git
- **Repository URL:** GitHub repo URL'niz
- **Branch:** `main`
- **Script Path:** `Jenkinsfile`
- **Save**

### 2.2. Jenkinsfile'ı Güncelleme

**Jenkinsfile'da güncellenecek yer (Satır 17):**
```groovy
REPO_URL = 'https://github.com/YOUR_USERNAME/kitapKiralama.git'
```
`YOUR_USERNAME` kısmını kendi GitHub kullanıcı adınızla değiştirin.

### 2.3. Pipeline'ı Çalıştırma

1. **Jenkins Dashboard** → **kitap-kiralama-pipeline**
2. **Build Now** butonuna tıklayın
3. Pipeline'ın her aşamasını izleyin

### 2.4. Pipeline Aşamaları

Jenkins pipeline şu aşamalardan oluşur:

1. **Checkout** - GitHub'dan kodları çeker
2. **Build** - Maven ile projeyi build eder
3. **Unit Tests** - Birim testlerini çalıştırır
4. **Integration Tests** - Entegrasyon testlerini çalıştırır
5. **Docker Build** - Docker image oluşturur
6. **Docker Compose Up** - Container'ları başlatır
7. **Health Check** - Sistemin hazır olup olmadığını kontrol eder
8. **Selenium Tests** - 3 ayrı Selenium test senaryosu çalıştırır

---

## 3. Build Komutları

### 3.1. Maven Build Komutları

```powershell
# Projeyi build et (testleri atla)
mvn clean package -DskipTests

# Projeyi build et ve testleri çalıştır
mvn clean package

# Sadece compile et
mvn clean compile

# Testleri çalıştır
mvn test

# Entegrasyon testlerini çalıştır
mvn verify
```

### 3.2. Docker Build Komutları

```powershell
# Docker image oluştur
docker build -t kitap-kiralama-app:latest .

# Docker Compose ile build et
docker-compose build

# Docker Compose ile build et ve başlat
docker-compose up -d --build
```

### 3.3. Test Komutları

```powershell
# Birim testlerini çalıştır
mvn test -Dtest=*Test

# Entegrasyon testlerini çalıştır
mvn verify -Dtest=*IntegrationTest

# Selenium testlerini çalıştır
mvn test -Dtest=UserRentBookTest -Pselenium
mvn test -Dtest=AdminAddBookTest -Pselenium
mvn test -Dtest=UserReturnBookTest -Pselenium
```

---

## 4. Test Senaryoları

### 4.1. Birim Testleri (Unit Tests)

**Test Dosyaları:**
- `src/test/java/com/kitapkiralama/service/BookServiceTest.java`
- `src/test/java/com/kitapkiralama/service/RentalServiceTest.java`
- `src/test/java/com/kitapkiralama/service/UserServiceTest.java`

**Çalıştırma:**
```powershell
mvn test -Dtest=*ServiceTest
```

**Test Senaryoları:**
- ✅ `kitapKiralamaBasarili` - Kitap kiralama başarılı
- ✅ `stokYoksaKiralamaBasarisiz` - Stok yoksa kiralama başarısız
- ✅ `kitapIadeBasarili` - Kitap iade başarılı
- ✅ `kullaniciBulunamadiDurumu` - Kullanıcı bulunamadı durumu

### 4.2. Entegrasyon Testleri (Integration Tests)

**Test Dosyaları:**
- `src/test/java/com/kitapkiralama/integration/BookIntegrationTest.java`
- `src/test/java/com/kitapkiralama/integration/RentalIntegrationTest.java`

**Çalıştırma:**
```powershell
mvn verify -Dtest=*IntegrationTest
```

**Test Senaryoları:**
- ✅ `POST /kirala` - Kitap kiralama endpoint'i
- ✅ `POST /iade` - Kitap iade endpoint'i
- ✅ `GET /kitaplar` - Kitap listesi endpoint'i

### 4.3. Selenium Testleri (E2E Tests)

**Test Dosyaları:**
- `src/test/java/com/kitapkiralama/selenium/UserRentBookTest.java`
- `src/test/java/com/kitapkiralama/selenium/AdminAddBookTest.java`
- `src/test/java/com/kitapkiralama/selenium/UserReturnBookTest.java`

**Çalıştırma:**
```powershell
# Selenium profili ile testleri çalıştır
mvn test -Dtest=UserRentBookTest -Pselenium
mvn test -Dtest=AdminAddBookTest -Pselenium
mvn test -Dtest=UserReturnBookTest -Pselenium
```

**Test Senaryoları:**

**Senaryo 1: Kullanıcı Giriş ve Kitap Kiralama**
- Kullanıcı giriş yapar
- Kitap kiralar
- Başarı mesajı görür

**Senaryo 2: Admin Kitap Ekleme**
- Admin giriş yapar
- Yeni kitap ekler
- Kitabı listede görür

**Senaryo 3: Kullanıcı Kitap İade**
- Kullanıcı kitabı iade eder
- Stok artar

---

## 5. Hızlı Başlangıç Komutları

### 5.1. Tek Seferde Her Şeyi Başlat

```powershell
# Proje klasörüne git
cd C:\Users\baris\OneDrive\Desktop\kitapKiralama

# Docker Compose ile başlat
docker-compose up -d --build

# Logları izle
docker-compose logs -f app
```

### 5.2. Testleri Çalıştır

```powershell
# Birim testleri
mvn test

# Entegrasyon testleri
mvn verify

# Selenium testleri (Docker Compose çalışıyorsa)
mvn test -Pselenium
```

### 5.3. Temiz Başlangıç

```powershell
# Her şeyi durdur ve temizle
docker-compose down -v

# Yeniden başlat
docker-compose up -d --build
```

---

## 6. Sorun Giderme

### 6.1. Port Çakışması

**Sorun:** Port 8080 veya 5432 zaten kullanılıyor

**Çözüm:**
```powershell
# Port kullanan process'i bul
netstat -ano | findstr :8080

# Process'i durdur (PID'yi değiştirin)
taskkill /PID <PID> /F
```

### 6.2. Docker Build Hatası

**Sorun:** Docker build sırasında hata

**Çözüm:**
```powershell
# Docker cache'i temizle
docker system prune -a

# Yeniden build et
docker-compose build --no-cache
```

### 6.3. Container Başlamıyor

**Sorun:** Container başlamıyor veya hemen kapanıyor

**Çözüm:**
```powershell
# Logları kontrol et
docker-compose logs app

# Container'ı manuel başlat
docker-compose up app
```

---

## 7. Jenkins Pipeline Test Senaryosu

### 7.1. Pipeline'ı Test Etme

1. **Jenkins Dashboard** → **kitap-kiralama-pipeline**
2. **Build Now** → Pipeline başlar
3. Her aşamayı izleyin:
   - ✅ Checkout
   - ✅ Build
   - ✅ Unit Tests
   - ✅ Integration Tests
   - ✅ Docker Build
   - ✅ Docker Compose Up
   - ✅ Health Check
   - ✅ Selenium Tests

### 7.2. Test Raporlarını Görüntüleme

1. **Pipeline** → **Test Results** sekmesi
2. Her test aşamasının sonuçlarını görün
3. **HTML Reports** sekmesinden detaylı raporları görüntüleyin

---

## 8. Özet

### Manuel Test İçin:
```powershell
docker-compose up -d --build
docker-compose logs -f app
```

### Otomatik Test İçin:
1. Jenkins'te pipeline oluştur
2. GitHub URL'ini güncelle
3. Build Now'a tıkla
4. Sonuçları izle

### Test Komutları:
```powershell
mvn test                    # Birim testleri
mvn verify                  # Entegrasyon testleri
mvn test -Pselenium         # Selenium testleri
```

---

## 📝 Notlar

- Docker Compose çalışırken port 8080 ve 5432 kullanılır
- Jenkins pipeline'ı çalıştırmadan önce GitHub repo URL'ini güncelleyin
- Selenium testleri için Chrome ve ChromeDriver gerekir
- Test sonuçları `target/surefire-reports` ve `target/failsafe-reports` klasörlerinde

---

## 🎯 Sonraki Adımlar

1. ✅ Docker Compose ile projeyi başlat
2. ✅ Jenkins pipeline'ı oluştur
3. ✅ GitHub URL'ini güncelle
4. ✅ Pipeline'ı çalıştır
5. ✅ Test sonuçlarını kontrol et
