-- ============================================
-- Kitap Kiralama Sistemi - Yararlı SQL Sorguları
-- ============================================

-- ============================================
-- TEMEL SORGULAR
-- ============================================

-- Tüm kitapları listele
SELECT * FROM kitap ORDER BY ad;

-- Tüm üyeleri listele
SELECT * FROM uye ORDER BY soyad, ad;

-- Tüm kiralamaları listele (detaylı)
SELECT 
    k.id,
    u.ad || ' ' || u.soyad AS uye_adi,
    kit.ad AS kitap_adi,
    k.kiralama_tarihi,
    k.iade_tarihi,
    k.gercek_iade_tarihi,
    k.durum,
    k.ceza_miktari
FROM kiralama k
JOIN uye u ON k.uye_id = u.id
JOIN kitap kit ON k.kitap_id = kit.id
ORDER BY k.kiralama_tarihi DESC;

-- ============================================
-- ARAMA SORGULARI
-- ============================================

-- Ada göre kitap ara
SELECT * FROM kitap 
WHERE LOWER(ad) LIKE LOWER('%arama_terimi%')
ORDER BY ad;

-- Yazara göre kitap ara
SELECT * FROM kitap 
WHERE LOWER(yazar) LIKE LOWER('%arama_terimi%')
ORDER BY yazar, ad;

-- Ada göre üye ara
SELECT * FROM uye 
WHERE LOWER(ad) LIKE LOWER('%arama_terimi%')
   OR LOWER(soyad) LIKE LOWER('%arama_terimi%')
ORDER BY soyad, ad;

-- ============================================
-- DURUM SORGULARI
-- ============================================

-- Müsait kitapları listele
SELECT * FROM kitap 
WHERE durum = 'MUSAIT' 
  AND mevcut_kopya > 0
ORDER BY ad;

-- Aktif üyeleri listele
SELECT * FROM uye 
WHERE durum = 'AKTIF'
ORDER BY soyad, ad;

-- Aktif kiralamaları listele
SELECT 
    k.id,
    u.ad || ' ' || u.soyad AS uye_adi,
    kit.ad AS kitap_adi,
    k.kiralama_tarihi,
    k.iade_tarihi
FROM kiralama k
JOIN uye u ON k.uye_id = u.id
JOIN kitap kit ON k.kitap_id = kit.id
WHERE k.durum = 'AKTIF'
ORDER BY k.iade_tarihi;

-- ============================================
-- ÜYE BAZLI SORGULAR
-- ============================================

-- Bir üyenin tüm kiralamaları
SELECT 
    k.id,
    kit.ad AS kitap_adi,
    kit.yazar,
    k.kiralama_tarihi,
    k.iade_tarihi,
    k.gercek_iade_tarihi,
    k.durum,
    k.ceza_miktari
FROM kiralama k
JOIN kitap kit ON k.kitap_id = kit.id
WHERE k.uye_id = 1  -- Üye ID'sini değiştirin
ORDER BY k.kiralama_tarihi DESC;

-- Bir üyenin aktif kiralamaları
SELECT 
    k.id,
    kit.ad AS kitap_adi,
    k.kiralama_tarihi,
    k.iade_tarihi,
    k.iade_tarihi - CURRENT_DATE AS kalan_gun
FROM kiralama k
JOIN kitap kit ON k.kitap_id = kit.id
WHERE k.uye_id = 1  -- Üye ID'sini değiştirin
  AND k.durum = 'AKTIF'
ORDER BY k.iade_tarihi;

-- ============================================
-- KİTAP BAZLI SORGULAR
-- ============================================

-- Bir kitabın kiralama geçmişi
SELECT 
    k.id,
    u.ad || ' ' || u.soyad AS uye_adi,
    k.kiralama_tarihi,
    k.iade_tarihi,
    k.gercek_iade_tarihi,
    k.durum
FROM kiralama k
JOIN uye u ON k.uye_id = u.id
WHERE k.kitap_id = 1  -- Kitap ID'sini değiştirin
ORDER BY k.kiralama_tarihi DESC;

-- En çok kiralanan kitaplar
SELECT 
    kit.id,
    kit.ad,
    kit.yazar,
    COUNT(k.id) AS kiralama_sayisi
FROM kitap kit
LEFT JOIN kiralama k ON kit.id = k.kitap_id
GROUP BY kit.id, kit.ad, kit.yazar
ORDER BY kiralama_sayisi DESC
LIMIT 10;

-- ============================================
-- GECİKME VE CEZA SORGULARI
-- ============================================

-- Gecikmiş kiralamaları listele
SELECT 
    k.id,
    u.ad || ' ' || u.soyad AS uye_adi,
    u.email,
    kit.ad AS kitap_adi,
    k.kiralama_tarihi,
    k.iade_tarihi,
    CURRENT_DATE - k.iade_tarihi AS gecikme_gunu,
    k.ceza_miktari
FROM kiralama k
JOIN uye u ON k.uye_id = u.id
JOIN kitap kit ON k.kitap_id = kit.id
WHERE k.durum = 'AKTIF'
  AND k.iade_tarihi < CURRENT_DATE
ORDER BY gecikme_gunu DESC;

-- Ceza toplamları
SELECT 
    u.id,
    u.ad || ' ' || u.soyad AS uye_adi,
    SUM(k.ceza_miktari) AS toplam_ceza
FROM uye u
JOIN kiralama k ON u.id = k.uye_id
WHERE k.ceza_miktari > 0
GROUP BY u.id, u.ad, u.soyad
ORDER BY toplam_ceza DESC;

-- Ceza durumundaki üyeler
SELECT * FROM uye WHERE durum = 'CEZALI';

-- ============================================
-- İSTATİSTİK SORGULARI
-- ============================================

-- Toplam kitap sayısı
SELECT 
    COUNT(*) AS toplam_kitap,
    SUM(toplam_kopya) AS toplam_kopya_sayisi,
    SUM(mevcut_kopya) AS mevcut_kopya_sayisi
FROM kitap;

-- Toplam üye sayısı (durum bazlı)
SELECT 
    durum,
    COUNT(*) AS sayi
FROM uye
GROUP BY durum;

-- Toplam kiralama istatistikleri
SELECT 
    durum,
    COUNT(*) AS sayi,
    SUM(ceza_miktari) AS toplam_ceza
FROM kiralama
GROUP BY durum;

-- Bu ay yapılan kiralamalar
SELECT COUNT(*) AS bu_ay_kiralama
FROM kiralama
WHERE DATE_TRUNC('month', kiralama_tarihi) = DATE_TRUNC('month', CURRENT_DATE);

-- Bu ay iade edilen kitaplar
SELECT COUNT(*) AS bu_ay_iade
FROM kiralama
WHERE durum = 'IADE_EDILDI'
  AND DATE_TRUNC('month', gercek_iade_tarihi) = DATE_TRUNC('month', CURRENT_DATE);

-- ============================================
-- RAPORLAR
-- ============================================

-- Üye aktivite raporu
SELECT 
    u.id,
    u.ad || ' ' || u.soyad AS uye_adi,
    COUNT(k.id) AS toplam_kiralama,
    COUNT(CASE WHEN k.durum = 'AKTIF' THEN 1 END) AS aktif_kiralama,
    SUM(k.ceza_miktari) AS toplam_ceza
FROM uye u
LEFT JOIN kiralama k ON u.id = k.uye_id
GROUP BY u.id, u.ad, u.soyad
ORDER BY toplam_kiralama DESC;

-- Kitap popülerlik raporu
SELECT 
    kit.id,
    kit.ad,
    kit.yazar,
    kit.toplam_kopya,
    kit.mevcut_kopya,
    COUNT(k.id) AS toplam_kiralama,
    ROUND(COUNT(k.id)::NUMERIC / NULLIF(kit.toplam_kopya, 0), 2) AS kiralama_ortalama
FROM kitap kit
LEFT JOIN kiralama k ON kit.id = k.kitap_id
GROUP BY kit.id, kit.ad, kit.yazar, kit.toplam_kopya, kit.mevcut_kopya
ORDER BY toplam_kiralama DESC;

-- Aylık kiralama raporu
SELECT 
    DATE_TRUNC('month', kiralama_tarihi) AS ay,
    COUNT(*) AS kiralama_sayisi
FROM kiralama
GROUP BY DATE_TRUNC('month', kiralama_tarihi)
ORDER BY ay DESC;

-- ============================================
-- YARDIMCI SORGULAR
-- ============================================

-- En aktif üyeler (en çok kitap kiralayan)
SELECT 
    u.id,
    u.ad || ' ' || u.soyad AS uye_adi,
    COUNT(k.id) AS toplam_kiralama
FROM uye u
JOIN kiralama k ON u.id = k.uye_id
GROUP BY u.id, u.ad, u.soyad
ORDER BY toplam_kiralama DESC
LIMIT 10;

-- Yakında iade edilmesi gereken kitaplar (3 gün içinde)
SELECT 
    k.id,
    u.ad || ' ' || u.soyad AS uye_adi,
    u.email,
    kit.ad AS kitap_adi,
    k.iade_tarihi,
    k.iade_tarihi - CURRENT_DATE AS kalan_gun
FROM kiralama k
JOIN uye u ON k.uye_id = u.id
JOIN kitap kit ON k.kitap_id = kit.id
WHERE k.durum = 'AKTIF'
  AND k.iade_tarihi BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '3 days'
ORDER BY k.iade_tarihi;
