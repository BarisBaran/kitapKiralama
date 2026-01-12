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
 * Senaryo 1: Kullanıcı kaydı, kitap ekleme ve kiralama işlemi
 * Bu senaryo tam bir kullanıcı akışını test eder:
 * 1. Yeni bir üye kaydı
 * 2. Yeni bir kitap ekleme
 * 3. Kitabı üyeye kiralamak
 * 4. Kiralama bilgilerini kontrol etmek
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KitapKiralamaApplication.class)
@ActiveProfiles("test")
class E2ETestScenario1 {

    @LocalServerPort
    private int port;

    private Long uyeId;
    private Long kitapId;
    private Long kiralamaId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    void testSenaryo1_TamKiralamaAkisi() {
        // 1. Adım: Yeni bir üye kaydı oluştur
        Map<String, String> uye = new HashMap<>();
        uye.put("ad", "Ahmet");
        uye.put("soyad", "Yılmaz");
        uye.put("email", "ahmet.yilmaz@example.com");
        uye.put("telefon", "5551234567");

        uyeId = given()
                .contentType(ContentType.JSON)
                .body(uye)
                .when()
                .post("/api/uyeler")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("ad", equalTo("Ahmet"))
                .body("soyad", equalTo("Yılmaz"))
                .body("durum", equalTo("AKTIF"))
                .extract()
                .path("id");

        // 2. Adım: Yeni bir kitap ekle
        Map<String, Object> kitap = new HashMap<>();
        kitap.put("ad", "Spring Boot İle Web Geliştirme");
        kitap.put("yazar", "Mehmet Yıldız");
        kitap.put("isbn", "978-0-123456-78-1");
        kitap.put("yayinYili", 2024);
        kitap.put("toplamKopya", 3);
        kitap.put("mevcutKopya", 3);

        kitapId = given()
                .contentType(ContentType.JSON)
                .body(kitap)
                .when()
                .post("/api/kitaplar")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("ad", equalTo("Spring Boot İle Web Geliştirme"))
                .body("durum", equalTo("MUSAIT"))
                .body("mevcutKopya", equalTo(3))
                .extract()
                .path("id");

        // 3. Adım: Kitabı üyeye kirala
        Map<String, Long> kiralamaRequest = new HashMap<>();
        kiralamaRequest.put("uyeId", uyeId);
        kiralamaRequest.put("kitapId", kitapId);

        kiralamaId = given()
                .contentType(ContentType.JSON)
                .body(kiralamaRequest)
                .when()
                .post("/api/kiralamalar/kirala")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("durum", equalTo("AKTIF"))
                .body("cezaMiktari", equalTo(0.0f))
                .extract()
                .path("id");

        // 4. Adım: Kiralama bilgilerini kontrol et
        given()
                .when()
                .get("/api/kiralamalar/" + kiralamaId)
                .then()
                .statusCode(200)
                .body("id", equalTo(kiralamaId.intValue()))
                .body("durum", equalTo("AKTIF"));

        // 5. Adım: Kitabın mevcut kopya sayısının azaldığını kontrol et
        given()
                .when()
                .get("/api/kitaplar/" + kitapId)
                .then()
                .statusCode(200)
                .body("mevcutKopya", equalTo(2));

        // 6. Adım: Üyenin aktif kiralama sayısının arttığını kontrol et
        given()
                .when()
                .get("/api/uyeler/" + uyeId)
                .then()
                .statusCode(200)
                .body("aktifKiralamaSayisi", equalTo(1));

        // 7. Adım: Üyenin aktif kiralamalarını listele
        given()
                .when()
                .get("/api/kiralamalar/uye/" + uyeId + "/aktif")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].durum", equalTo("AKTIF"));
    }
}
