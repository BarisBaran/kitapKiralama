package com.kitapkiralama.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class SeleniumTestBase {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl = "http://localhost:9090";

    @BeforeEach
    void setUp() {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--disable-extensions");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--window-size=1920,1080");
            // Chrome binary path'i opsiyonel (bulunamazsa varsayılan kullanılır)
            try {
                java.io.File chromeBinary = new java.io.File("/usr/bin/google-chrome-stable");
                if (chromeBinary.exists()) {
                    options.setBinary(chromeBinary.getAbsolutePath());
                }
            } catch (Exception e) {
                // Chrome binary bulunamazsa varsayılan kullanılır
            }
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        } catch (Exception e) {
            // Chrome bulunamazsa test'i atla
            System.out.println("Chrome başlatılamadı, test atlanıyor: " + e.getMessage());
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Chrome başlatılamadı: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
