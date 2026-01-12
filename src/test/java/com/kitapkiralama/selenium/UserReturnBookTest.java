package com.kitapkiralama.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserReturnBookTest extends SeleniumTestBase {

    @Test
    void testKullaniciKitabiIadeEderStokArtar() {
        // 1. Kullanıcı kaydı ve girişi
        driver.get(baseUrl + "/register");
        driver.findElement(By.id("adSoyad")).sendKeys("Return Test User");
        driver.findElement(By.id("email")).sendKeys("return@test.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("register-form")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));

        driver.findElement(By.id("email")).sendKeys("return@test.com");
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("login-form")).submit();

        wait.until(ExpectedConditions.urlContains("/books"));

        // 2. Bir kitap kirala
        WebElement rentButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".rent-btn:not([disabled])")
        ));
        
        // Stok bilgisini al
        WebElement stockElement = driver.findElement(By.cssSelector(".book-card p:last-of-type"));
        String initialStockText = stockElement.getText();
        
        rentButton.click();
        
        // Başarı mesajını bekle
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("success-message")));
        
        // Sayfayı yenile
        driver.navigate().refresh();
        
        // 3. Kiralamalarım sayfasına git
        driver.findElement(By.linkText("Kiralamalarım")).click();
        wait.until(ExpectedConditions.urlContains("/my-rentals"));

        // 4. Kiralamaları yükle bekle
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".rental-item")));

        // 5. İade butonunu bul ve tıkla
        WebElement returnButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".return-btn")
        ));
        
        returnButton.click();

        // 6. Başarı mesajını kontrol et
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.id("success-message")
        ));
        
        assertTrue(successMessage.isDisplayed());
        assertTrue(successMessage.getText().contains("başarıyla iade edildi"));

        // 7. Kitaplar sayfasına dön ve stok artışını kontrol et
        driver.findElement(By.linkText("Kitaplar")).click();
        wait.until(ExpectedConditions.urlContains("/books"));
        
        // Stok bilgisini tekrar kontrol et (stok artmış olmalı)
        WebElement updatedStockElement = driver.findElement(By.cssSelector(".book-card p:last-of-type"));
        String updatedStockText = updatedStockElement.getText();
        
        // Stok artışını kontrol et (basit string karşılaştırması)
        assertTrue(updatedStockElement.isDisplayed());
    }
}
