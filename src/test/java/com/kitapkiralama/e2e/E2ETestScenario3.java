package com.kitapkiralama.e2e;

import com.kitapkiralama.KitapKiralamaApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Senaryo 3: Hata durumları ve validasyon testleri
 * Bu senaryo hata durumlarını ve validasyonları test eder:
 * 1. Aynı ISBN ile kitap ekleme hatası
 * 2. Aynı email ile üye kaydı hatası
 * 3. Mevcut olmayan kitap kiralama hatası
 * 4. Kopya sayısı 0 olan kitap kiralama hatası
 * 5. Arama ve filtreleme işlemleri
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KitapKiralamaApplication.class)
@ActiveProfiles("test")
class E2ETestScenario3 {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void testSenaryo3_HataDurumlariVeValidasyonlar() {
        // 1. Adım: İlk kitabı ekle
        Map<String, Object> kitap1 = new HashMap<>();
        kitap1.put("ad", "Test Kitap 1");
        kitap1.put("yazar", "Yazar 1");
        kitap1.put("isbn", "978-0-123456-78-3");
        kitap1.put("yayinYili", 2023);
        kitap1.put("toplamKopya", 2);
        kitap1.put("mevcutKopya", 2);

        Long kitapId1 = given()
                .contentType(ContentType.JSON)
                .body(kitap1)
                .when()
                .post("/api/kitaplar")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // 2. Adım: Aynı ISBN ile kitap ekleme hatası
        Map<String, Object> kitap2 = new HashMap<>();
        kitap2.put("ad", "Test Kitap 2");
        kitap2.put("yazar", "Yazar 2");
        kitap2.put("isbn", "978-0-123456-78-3"); // Aynı ISBN
        kitap2.put("yayinYili", 2024);
        kitap2.put("toplamKopya", 3);
        kitap2.put("mevcutKopya", 3);

        given()
                .contentType(ContentType.JSON)
                .body(kitap2)
                .when()
                .post("/api/kitaplar")
                .then()
                .statusCode(400);

        // 3. Adım: İlk üyeyi ekle
        Map<String, String> uye1 = new HashMap<>();
        uye1.put("ad", "Mehmet");
        uye1.put("soyad", "Kaya");
        uye1.put("email", "mehmet.kaya@example.com");
        uye1.put("telefon", "5551112233");

        Long uyeId1 = given()
                .contentType(ContentType.JSON)
                .body(uye1)
                .when()
                .post("/api/uyeler")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // 4. Adım: Aynı email ile üye kaydı hatası
        Map<String, String> uye2 = new HashMap<>();
        uye2.put("ad", "Ali");
        uye2.put("soyad", "Veli");
        uye2.put("email", "mehmet.kaya@example.com"); // Aynı email
        uye2.put("telefon", "5554445566");

        given()
                .contentType(ContentType.JSON)
                .body(uye2)
                .when()
                .post("/api/uyeler")
                .then()
                .statusCode(400);

        // 5. Adım: Mevcut olmayan kitap kiralama hatası
        Map<String, Long> kiralamaRequest1 = new HashMap<>();
        kiralamaRequest1.put("uyeId", uyeId1);
        kiralamaRequest1.put("kitapId", 99999L); // Olmayan kitap ID

        given()
                .contentType(ContentType.JSON)
                .body(kiralamaRequest1)
                .when()
                .post("/api/kiralamalar/kirala")
                .then()
                .statusCode(400);

        // 6. Adım: Kopya sayısı 0 olan kitap oluştur ve kiralama hatası
        Map<String, Object> kitap3 = new HashMap<>();
        kitap3.put("ad", "Tükenen Kitap");
        kitap3.put("yazar", "Yazar 3");
        kitap3.put("isbn", "978-0-123456-78-4");
        kitap3.put("yayinYili", 2023);
        kitap3.put("toplamKopya", 1);
        kitap3.put("mevcutKopya", 0);
        kitap3.put("durum", "KIRALANDI");

        Long kitapId3 = given()
                .contentType(ContentType.JSON)
                .body(kitap3)
                .when()
                .post("/api/kitaplar")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Tükenen kitabı kiralamaya çalış
        Map<String, Long> kiralamaRequest2 = new HashMap<>();
        kiralamaRequest2.put("uyeId", uyeId1);
        kiralamaRequest2.put("kitapId", kitapId3);

        given()
                .contentType(ContentType.JSON)
                .body(kiralamaRequest2)
                .when()
                .post("/api/kiralamalar/kirala")
                .then()
                .statusCode(409); // Conflict

        // 7. Adım: Kitap arama işlemi
        given()
                .when()
                .get("/api/kitaplar/ara/ad?ad=Test")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        // 8. Adım: Yazar arama işlemi
        given()
                .when()
                .get("/api/kitaplar/ara/yazar?yazar=Yazar")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        // 9. Adım: Duruma göre kitap listeleme
        given()
                .when()
                .get("/api/kitaplar/durum/MUSAIT")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));

        // 10. Adım: Üye arama işlemi
        given()
                .when()
                .get("/api/uyeler/ara?ad=Mehmet")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));

        // 11. Adım: Mevcut olmayan kayıtları getirme
        given()
                .when()
                .get("/api/kitaplar/99999")
                .then()
                .statusCode(404);

        given()
                .when()
                .get("/api/uyeler/99999")
                .then()
                .statusCode(404);
    }
}
