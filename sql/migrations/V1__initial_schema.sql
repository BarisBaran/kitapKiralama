-- ============================================
-- Flyway/Liquibase Migration: V1 - Initial Schema
-- ============================================
-- Bu dosya Flyway veya Liquibase ile kullanılabilir
-- Hibernate otomatik tablo oluşturuyor, ancak migration tool'ları için hazırlandı

-- Üye Tablosu
CREATE TABLE IF NOT EXISTS uye (
    id BIGSERIAL PRIMARY KEY,
    ad VARCHAR(255) NOT NULL,
    soyad VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefon VARCHAR(15) NOT NULL,
    aktif_kiralama_sayisi INTEGER NOT NULL DEFAULT 0,
    durum VARCHAR(20) NOT NULL DEFAULT 'AKTIF',
    CONSTRAINT chk_uye_durum CHECK (durum IN ('AKTIF', 'PASIF', 'CEZALI'))
);

-- Kitap Tablosu
CREATE TABLE IF NOT EXISTS kitap (
    id BIGSERIAL PRIMARY KEY,
    ad VARCHAR(255) NOT NULL,
    yazar VARCHAR(255) NOT NULL,
    isbn VARCHAR(50) NOT NULL UNIQUE,
    yayin_yili INTEGER NOT NULL,
    toplam_kopya INTEGER NOT NULL,
    mevcut_kopya INTEGER NOT NULL,
    durum VARCHAR(20) NOT NULL DEFAULT 'MUSAIT',
    CONSTRAINT chk_kitap_durum CHECK (durum IN ('MUSAIT', 'KIRALANDI', 'BAKIMDA')),
    CONSTRAINT chk_kitap_kopya CHECK (toplam_kopya > 0),
    CONSTRAINT chk_kitap_mevcut_kopya CHECK (mevcut_kopya >= 0 AND mevcut_kopya <= toplam_kopya)
);

-- Kiralama Tablosu
CREATE TABLE IF NOT EXISTS kiralama (
    id BIGSERIAL PRIMARY KEY,
    uye_id BIGINT NOT NULL,
    kitap_id BIGINT NOT NULL,
    kiralama_tarihi DATE NOT NULL,
    iade_tarihi DATE NOT NULL,
    gercek_iade_tarihi DATE,
    durum VARCHAR(20) NOT NULL DEFAULT 'AKTIF',
    ceza_miktari DECIMAL(10, 2) DEFAULT 0.0,
    CONSTRAINT fk_kiralama_uye FOREIGN KEY (uye_id) REFERENCES uye(id) ON DELETE CASCADE,
    CONSTRAINT fk_kiralama_kitap FOREIGN KEY (kitap_id) REFERENCES kitap(id) ON DELETE CASCADE,
    CONSTRAINT chk_kiralama_durum CHECK (durum IN ('AKTIF', 'IADE_EDILDI', 'GECIKMIS')),
    CONSTRAINT chk_kiralama_tarih CHECK (iade_tarihi >= kiralama_tarihi)
);

-- İndex'ler
CREATE INDEX IF NOT EXISTS idx_uye_email ON uye(email);
CREATE INDEX IF NOT EXISTS idx_uye_durum ON uye(durum);
CREATE INDEX IF NOT EXISTS idx_kitap_isbn ON kitap(isbn);
CREATE INDEX IF NOT EXISTS idx_kitap_durum ON kitap(durum);
CREATE INDEX IF NOT EXISTS idx_kiralama_uye_id ON kiralama(uye_id);
CREATE INDEX IF NOT EXISTS idx_kiralama_kitap_id ON kiralama(kitap_id);
CREATE INDEX IF NOT EXISTS idx_kiralama_durum ON kiralama(durum);
