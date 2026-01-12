# Kitap Kiralama Sistemi

Yazılım Doğrulama ve Geçerleme (YDG) dersi için geliştirilmiş kapsamlı bir kitap kiralama sistemi.

## Proje Özellikleri

- **Spring Boot 3.2.0** tabanlı RESTful API
- **PostgreSQL** veritabanı
- **JPA/Hibernate** ORM
- **Docker** containerization
- **Jenkins** CI/CD pipeline
- Kapsamlı test kapsamı (Birim, Entegrasyon, E2E)

## Sistem Gereksinimleri

- Java 17
- Maven 3.9+
- Docker ve Docker Compose
- Jenkins (CI/CD için)

## Proje Yapısı

```
kitapKiralama/
├── src/
│   ├── main/
│   │   ├── java/com/kitapkiralama/
│   │   │   ├── model/          # Domain modelleri (Kitap, Üye, Kiralama)
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── service/        # Business logic
│   │   │   └── controller/     # REST controllers
│   │   └── resources/
│   │       └── application*.properties
│   └── test/
│       ├── java/com/kitapkiralama/
│       │   ├── service/        # Unit tests
│       │   ├── integration/    # Integration tests
│       │   └── e2e/           # E2E test scenarios
│       └── resources/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
└── README.md
```

## Yerel Geliştirme

### Seçenek 1: H2 In-Memory Database (Önerilen - Varsayılan, PostgreSQL gerektirmez)

**Varsayılan olarak H2 kullanılır!** PostgreSQL kurulumu yapmadan doğrudan çalıştırabilirsiniz:

```bash
mvn spring-boot:run
```

veya

```bash
java -jar target/*.jar
```

**H2 Console'a erişim:**
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:kitapkiralama`
- Username: `sa`
- Password: (boş bırakın)

### Seçenek 2: PostgreSQL ile Çalıştırma

PostgreSQL kullanmak için `postgres` profilini aktif edin:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

veya

```bash
java -jar target/*.jar --spring.profiles.active=postgres
```

#### 2a. Docker ile PostgreSQL Başlatma

```bash
docker-compose up -d postgres
```

#### 2b. Kendi PostgreSQL'inizi Kullanma

Eğer kendi PostgreSQL kurulumunuz varsa ve şifreniz farklıysa:

**Yöntem 1: Environment Variable Kullanma (Önerilen)**

Windows (PowerShell):
```powershell
$env:SPRING_DATASOURCE_PASSWORD="sizin_sifreniz"
mvn spring-boot:run
```

Linux/Mac:
```bash
export SPRING_DATASOURCE_PASSWORD="sizin_sifreniz"
mvn spring-boot:run
```

**Yöntem 2: application.properties Dosyasını Düzenleme**

`src/main/resources/application.properties` dosyasındaki şifreyi kendi şifrenizle değiştirin:
```properties
spring.datasource.password=sizin_sifreniz
```

**Yöntem 3: application-local.properties Oluşturma**

`src/main/resources/` klasörüne `application-local.properties` dosyası oluşturun:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/kitapkiralama
spring.datasource.username=postgres
spring.datasource.password=sizin_sifreniz
```

Sonra uygulamayı şu şekilde çalıştırın:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Uygulamayı Çalıştır

```bash
mvn spring-boot:run
```

Uygulama `http://localhost:8080` adresinde çalışacaktır.

### Veritabanı Oluşturma

PostgreSQL kullanıyorsanız, önce veritabanını oluşturmanız gerekebilir:

```sql
CREATE DATABASE kitapkiralama;
```

### 3. Testleri Çalıştır

```bash
# Tüm testler
mvn test

# Sadece birim testleri
mvn test -Dtest=*Test

# Sadece entegrasyon testleri
mvn verify -Dtest=*IntegrationTest

# E2E test senaryoları
mvn test -Dtest=E2ETestScenario*
```

## Docker ile Çalıştırma

```bash
# Tüm servisleri başlat (PostgreSQL + Uygulama)
docker-compose up -d --build

# Logları görüntüle
docker-compose logs -f app

# Servisleri durdur
docker-compose down
```

## SQL Dosyaları

Proje için hazır SQL dosyaları `sql/` klasöründe bulunmaktadır:

- **`schema.sql`**: Veritabanı şema tanımları (tablolar, index'ler, constraint'ler)
- **`seed-data.sql`**: Örnek test verileri (development/test için)
- **`queries.sql`**: Yararlı SQL sorguları ve raporlar
- **`migrations/`**: Flyway/Liquibase migration dosyaları

Detaylı bilgi için `sql/README.md` dosyasına bakın.

## API Endpoints

### Kitaplar
- `GET /api/kitaplar` - Tüm kitapları listele
- `GET /api/kitaplar/{id}` - Kitap detayı
- `POST /api/kitaplar` - Yeni kitap ekle
- `PUT /api/kitaplar/{id}` - Kitap güncelle
- `DELETE /api/kitaplar/{id}` - Kitap sil
- `GET /api/kitaplar/ara/ad?ad={ad}` - Ada göre ara
- `GET /api/kitaplar/ara/yazar?yazar={yazar}` - Yazara göre ara

### Üyeler
- `GET /api/uyeler` - Tüm üyeleri listele
- `GET /api/uyeler/{id}` - Üye detayı
- `POST /api/uyeler` - Yeni üye ekle
- `PUT /api/uyeler/{id}` - Üye güncelle
- `DELETE /api/uyeler/{id}` - Üye sil
- `GET /api/uyeler/ara?ad={ad}` - Ada göre ara

### Kiralamalar
- `GET /api/kiralamalar` - Tüm kiralamaları listele
- `GET /api/kiralamalar/{id}` - Kiralama detayı
- `POST /api/kiralamalar/kirala` - Kitap kirala
- `POST /api/kiralamalar/{id}/iade` - Kitap iade et
- `GET /api/kiralamalar/uye/{uyeId}` - Üyenin kiralamaları
- `GET /api/kiralamalar/uye/{uyeId}/aktif` - Üyenin aktif kiralamaları
- `GET /api/kiralamalar/gecikmis` - Gecikmiş kiralamalar

## Test Senaryoları

### E2E Test Senaryoları

1. **Senaryo 1: Tam Kiralama Akışı**
   - Üye kaydı
   - Kitap ekleme
   - Kitap kiralama
   - Kiralama bilgilerini kontrol etme

2. **Senaryo 2: İade ve Gecikme Cezası**
   - Kitap iade işlemi
   - Gecikme durumu kontrolü
   - Ceza hesaplama

3. **Senaryo 3: Hata Durumları ve Validasyonlar**
   - Duplicate kontrolü (ISBN, Email)
   - Hatalı kiralama durumları
   - Arama ve filtreleme işlemleri

## Jenkins CI/CD Pipeline

Pipeline aşağıdaki aşamaları içerir:

1. **Checkout**: GitHub'dan kodlar çekilir
2. **Build**: Maven ile kodlar derlenir
3. **Unit Tests**: Birim testleri çalıştırılır ve raporlanır
4. **Integration Tests**: Entegrasyon testleri çalıştırılır ve raporlanır
5. **Docker Build**: Docker image oluşturulur
6. **Docker Compose Up**: Sistem container'lar üzerinde çalıştırılır
7. **E2E Tests**: En az 3 test senaryosu çalıştırılır ve raporlanır

### Jenkins Pipeline'ı Yapılandırma

1. Jenkins'te yeni bir Pipeline job oluşturun
2. "Pipeline script from SCM" seçeneğini seçin
3. Repository URL'ini ve branch'i belirtin
4. Jenkinsfile yolunu doğrulayın
5. Gerekli plugin'lerin yüklü olduğundan emin olun:
   - Pipeline
   - Maven Integration
   - Docker Pipeline
   - JUnit Plugin
   - HTML Publisher Plugin

## Test Raporları

Test sonuçları aşağıdaki yerlerde raporlanır:
- `target/surefire-reports/` - Birim test raporları
- `target/failsafe-reports/` - Entegrasyon test raporları
- Jenkins HTML Publisher ile görüntülenebilir

## Lisans

Bu proje eğitim amaçlı geliştirilmiştir.
