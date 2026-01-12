# SQL Dosyaları

Bu klasör projenin tüm SQL dosyalarını içerir.

## Dosya Yapısı

### 1. `schema.sql`
Veritabanı şema tanımları (CREATE TABLE, INDEX, CONSTRAINT)
- Tablolar: `uye`, `kitap`, `kiralama`
- Index'ler ve performans optimizasyonları
- Foreign key constraint'leri
- Check constraint'leri

**Kullanım:**
```bash
psql -U postgres -d kitapkiralama -f schema.sql
```

### 2. `seed-data.sql`
Örnek test verileri (development/test ortamları için)
- Örnek üye kayıtları
- Örnek kitap kayıtları
- Örnek kiralama kayıtları

**Kullanım:**
```bash
psql -U postgres -d kitapkiralama -f seed-data.sql
```

**Not:** Production ortamında kullanmayın!

### 3. `queries.sql`
Yararlı SQL sorguları ve raporlar
- Temel sorgular
- Arama sorguları
- Durum bazlı sorgular
- İstatistik ve raporlar
- Gecikme ve ceza sorguları

**Kullanım:**
```bash
psql -U postgres -d kitapkiralama -f queries.sql
```
veya bireysel sorguları kopyalayıp kullanabilirsiniz.

### 4. `migrations/V1__initial_schema.sql`
Flyway/Liquibase formatında migration dosyası
- İlk versiyon schema tanımları
- Migration tool'ları ile kullanım için

## Veritabanı Oluşturma

### PostgreSQL

```sql
-- Veritabanı oluştur
CREATE DATABASE kitapkiralama;

-- Veritabanına bağlan
\c kitapkiralama;

-- Şema oluştur
\i sql/schema.sql

-- Örnek verileri ekle (isteğe bağlı)
\i sql/seed-data.sql
```

### Docker ile

```bash
# PostgreSQL container'ını başlat
docker-compose up -d postgres

# SQL dosyalarını çalıştır
docker exec -i kitap-kiralama-postgres psql -U postgres -d kitapkiralama < sql/schema.sql
docker exec -i kitap-kiralama-postgres psql -U postgres -d kitapkiralama < sql/seed-data.sql
```

## Tablo Yapısı

### Üye Tablosu (uye)
- `id`: Primary Key (BIGSERIAL)
- `ad`: VARCHAR(255) NOT NULL
- `soyad`: VARCHAR(255) NOT NULL
- `email`: VARCHAR(255) NOT NULL UNIQUE
- `telefon`: VARCHAR(15) NOT NULL
- `aktif_kiralama_sayisi`: INTEGER DEFAULT 0
- `durum`: VARCHAR(20) DEFAULT 'AKTIF' (AKTIF, PASIF, CEZALI)

### Kitap Tablosu (kitap)
- `id`: Primary Key (BIGSERIAL)
- `ad`: VARCHAR(255) NOT NULL
- `yazar`: VARCHAR(255) NOT NULL
- `isbn`: VARCHAR(50) NOT NULL UNIQUE
- `yayin_yili`: INTEGER NOT NULL
- `toplam_kopya`: INTEGER NOT NULL
- `mevcut_kopya`: INTEGER NOT NULL
- `durum`: VARCHAR(20) DEFAULT 'MUSAIT' (MUSAIT, KIRALANDI, BAKIMDA)

### Kiralama Tablosu (kiralama)
- `id`: Primary Key (BIGSERIAL)
- `uye_id`: BIGINT NOT NULL (FK -> uye.id)
- `kitap_id`: BIGINT NOT NULL (FK -> kitap.id)
- `kiralama_tarihi`: DATE NOT NULL
- `iade_tarihi`: DATE NOT NULL
- `gercek_iade_tarihi`: DATE (nullable)
- `durum`: VARCHAR(20) DEFAULT 'AKTIF' (AKTIF, IADE_EDILDI, GECIKMIS)
- `ceza_miktari`: DECIMAL(10,2) DEFAULT 0.0

## İlişkiler

- `kiralama.uye_id` -> `uye.id` (Many-to-One)
- `kiralama.kitap_id` -> `kitap.id` (Many-to-One)

## Notlar

1. **Otomatik Tablo Oluşturma:** 
   - Spring Boot + Hibernate otomatik olarak tabloları oluşturur (`spring.jpa.hibernate.ddl-auto=update`)
   - Bu SQL dosyaları manuel kullanım veya migration tool'ları için hazırlanmıştır

2. **Veri Tipleri:**
   - PostgreSQL için optimize edilmiştir
   - H2 için ufak değişiklikler gerekebilir (DATE -> DATE, BIGSERIAL -> BIGINT AUTO_INCREMENT)

3. **Index'ler:**
   - Performans için önemli alanlarda index'ler tanımlanmıştır
   - Sık kullanılan arama alanlarında index'ler bulunur

4. **Constraint'ler:**
   - Foreign key constraint'leri veri bütünlüğünü sağlar
   - Check constraint'leri geçerli değerlerin girilmesini garanti eder
