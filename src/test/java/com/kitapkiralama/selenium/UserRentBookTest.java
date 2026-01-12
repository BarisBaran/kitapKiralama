package com.kitapkiralama.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRentBookTest extends SeleniumTestBase {

    @Test
    void testKullaniciGirisYaparKitapKiralarBasariMesajiGorur() {
        // 1. Kayıt sayfasına git
        driver.get(baseUrl + "/register");

        // 2. Kullanıcı kaydı oluştur
        driver.findElement(By.id("adSoyad")).sendKeys("Selenium Test User");
        driver.findElement(By.id("email")).sendKeys("selenium@test.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("register-form")).submit();

        // 3. Başarı mesajını bekle ve giriş sayfasına yönlendir
        wait.until(ExpectedConditions.urlContains("/login"));

        // 4. Giriş yap
        driver.findElement(By.id("email")).sendKeys("selenium@test.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("login-form")).submit();

        // 5. Kitaplar sayfasına yönlendiril
        wait.until(ExpectedConditions.urlContains("/books"));

        // 6. İlk müsait kitabı bul ve kirala
        WebElement rentButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".rent-btn:not([disabled])")
        ));
        
        String bookTitle = driver.findElement(By.cssSelector(".book-card h3")).getText();
        rentButton.click();

        // 7. Başarı mesajını kontrol et
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("success-message")
        ));
        
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("başarıyla kiralandı"));
    }
}
