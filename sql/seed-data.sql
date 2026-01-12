-- ============================================
-- Kitap Kiralama Sistemi - Örnek Veri (Seed Data)
-- ============================================
-- Bu dosya test ve geliştirme amaçlı örnek veriler içerir
-- Production ortamında kullanmayın!

-- ============================================
-- ÜYE VERİLERİ
-- ============================================

INSERT INTO uye (ad, soyad, email, telefon, aktif_kiralama_sayisi, durum) VALUES
('Ahmet', 'Yılmaz', 'ahmet.yilmaz@example.com', '5551234567', 0, 'AKTIF'),
('Ayşe', 'Demir', 'ayse.demir@example.com', '5559876543', 0, 'AKTIF'),
('Mehmet', 'Kaya', 'mehmet.kaya@example.com', '5551112233', 0, 'AKTIF'),
('Fatma', 'Çelik', 'fatma.celik@example.com', '5554445566', 0, 'AKTIF'),
('Ali', 'Şahin', 'ali.sahin@example.com', '5557778899', 0, 'AKTIF'),
('Zeynep', 'Arslan', 'zeynep.arslan@example.com', '5552223344', 0, 'PASIF'),
('Mustafa', 'Yıldız', 'mustafa.yildiz@example.com', '5558889900', 0, 'CEZALI');

-- ============================================
-- KİTAP VERİLERİ
-- ============================================

INSERT INTO kitap (ad, yazar, isbn, yayin_yili, toplam_kopya, mevcut_kopya, durum) VALUES
('Spring Boot İle Web Geliştirme', 'Mehmet Yıldız', '978-0-123456-78-1', 2024, 5, 5, 'MUSAIT'),
('Java Programlama Temelleri', 'Ali Veli', '978-0-123456-78-2', 2023, 3, 3, 'MUSAIT'),
('Veritabanı Yönetim Sistemleri', 'Ayşe Kaya', '978-0-123456-78-3', 2023, 4, 4, 'MUSAIT'),
('Yazılım Mimarisi ve Tasarım Desenleri', 'Can Demir', '978-0-123456-78-4', 2024, 2, 2, 'MUSAIT'),
('RESTful API Tasarımı', 'Deniz Arslan', '978-0-123456-78-5', 2024, 3, 3, 'MUSAIT'),
('Docker ve Container Teknolojileri', 'Emre Şahin', '978-0-123456-78-6', 2024, 2, 2, 'MUSAIT'),
('CI/CD Pipeline Kurulumu', 'Fulya Çelik', '978-0-123456-78-7', 2024, 3, 3, 'MUSAIT'),
('Microservices Mimarisi', 'Gökhan Yılmaz', '978-0-123456-78-8', 2023, 4, 3, 'MUSAIT'),
('Test Driven Development', 'Hakan Özdemir', '978-0-123456-78-9', 2023, 5, 4, 'MUSAIT'),
('Clean Code', 'Robert C. Martin', '978-0-13-235088-4', 2022, 3, 2, 'MUSAIT'),
('Design Patterns', 'Gang of Four', '978-0-201-63361-0', 2021, 2, 1, 'MUSAIT'),
('Kafka: The Definitive Guide', 'Neha Narkhede', '978-1-491-90315-2', 2022, 2, 0, 'KIRALANDI'),
('Effective Java', 'Joshua Bloch', '978-0-13-468599-1', 2023, 4, 3, 'MUSAIT');

-- ============================================
-- KİRALAMA VERİLERİ (Örnek)
-- ============================================

-- Aktif kiralamalar
INSERT INTO kiralama (uye_id, kitap_id, kiralama_tarihi, iade_tarihi, durum, ceza_miktari) VALUES
(1, 12, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '9 days', 'AKTIF', 0.0),
(2, 11, CURRENT_DATE - INTERVAL '3 days', CURRENT_DATE + INTERVAL '11 days', 'AKTIF', 0.0);

-- Tamamlanmış kiralamalar (iade edilmiş)
INSERT INTO kiralama (uye_id, kitap_id, kiralama_tarihi, iade_tarihi, gercek_iade_tarihi, durum, ceza_miktari) VALUES
(1, 1, CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE - INTERVAL '6 days', CURRENT_DATE - INTERVAL '5 days', 'IADE_EDILDI', 0.0),
(3, 2, CURRENT_DATE - INTERVAL '18 days', CURRENT_DATE - INTERVAL '4 days', CURRENT_DATE - INTERVAL '4 days', 'IADE_EDILDI', 0.0);

-- Gecikmiş kiralama örneği
INSERT INTO kiralama (uye_id, kitap_id, kiralama_tarihi, iade_tarihi, durum, ceza_miktari) VALUES
(5, 9, CURRENT_DATE - INTERVAL '18 days', CURRENT_DATE - INTERVAL '4 days', 'GECIKMIS', 20.0);

-- ============================================
-- ÜYE VE KİTAP SAYISLARINI GÜNCELLE
-- ============================================

-- Aktif kiralama sayılarını güncelle
UPDATE uye SET aktif_kiralama_sayisi = (
    SELECT COUNT(*) 
    FROM kiralama 
    WHERE kiralama.uye_id = uye.id 
    AND kiralama.durum = 'AKTIF'
);

-- Mevcut kopya sayılarını güncelle
UPDATE kitap SET mevcut_kopya = (
    toplam_kopya - (
        SELECT COUNT(*) 
        FROM kiralama 
        WHERE kiralama.kitap_id = kitap.id 
        AND kiralama.durum = 'AKTIF'
    )
);
