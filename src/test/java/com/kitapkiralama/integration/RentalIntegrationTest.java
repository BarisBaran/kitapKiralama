package com.kitapkiralama.integration;

import com.kitapkiralama.dto.request.RentalRequest;
import com.kitapkiralama.entity.Book;
import com.kitapkiralama.entity.Rental;
import com.kitapkiralama.entity.User;
import com.kitapkiralama.repository.BookRepository;
import com.kitapkiralama.repository.RentalRepository;
import com.kitapkiralama.repository.UserRepository;
import com.kitapkiralama.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class RentalIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RentalService rentalService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setAdSoyad("Test User");
        testUser.setEmail("test@example.com");
        testUser.setSifre(passwordEncoder.encode("password123"));
        testUser.setRol(User.Rol.KULLANICI);
        testUser = userRepository.save(testUser);

        testBook = new Book();
        testBook.setBaslik("Test Kitap");
        testBook.setYazar("Test Yazar");
        testBook.setIsbn("1234567890");
        testBook.setStok(5);
        testBook = bookRepository.save(testBook);
    }

    @Test
    void testPostKirala() {
        // Given
        RentalRequest request = new RentalRequest();
        request.setKitapId(testBook.getId());

        // When
        var response = rentalService.rentBook(testUser.getId(), request);

        // Then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getKullaniciId());
        assertEquals(testBook.getId(), response.getKitapId());
        assertEquals(Rental.Durum.KIRADA, response.getDurum());

        // Stok kontrolü
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(4, updatedBook.getStok());

        // Kiralama kaydı kontrolü
        Rental rental = rentalRepository.findById(response.getId()).orElseThrow();
        assertNotNull(rental);
        assertEquals(Rental.Durum.KIRADA, rental.getDurum());
    }

    @Test
    void testPostIade() {
        // Given - Önce bir kiralama oluştur
        RentalRequest request = new RentalRequest();
        request.setKitapId(testBook.getId());
        var rentalResponse = rentalService.rentBook(testUser.getId(), request);

        // When - İade et
        var returnResponse = rentalService.returnBook(rentalResponse.getId());

        // Then
        assertNotNull(returnResponse);
        assertEquals(Rental.Durum.IADE_EDILDI, returnResponse.getDurum());

        // Stok kontrolü
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(5, updatedBook.getStok());

        // Kiralama durumu kontrolü
        Rental rental = rentalRepository.findById(rentalResponse.getId()).orElseThrow();
        assertEquals(Rental.Durum.IADE_EDILDI, rental.getDurum());
    }

    @Test
    void testGetKitaplar() {
        // Given - Birden fazla kitap ekle
        Book book2 = new Book();
        book2.setBaslik("İkinci Kitap");
        book2.setYazar("İkinci Yazar");
        book2.setIsbn("0987654321");
        book2.setStok(3);
        bookRepository.save(book2);

        // When
        var books = rentalService.getClass().getSimpleName(); // Bu test için service yerine repository kullan
        var allBooks = bookRepository.findAll();

        // Then
        assertTrue(allBooks.size() >= 2);
        assertTrue(allBooks.stream().anyMatch(b -> b.getIsbn().equals("1234567890")));
        assertTrue(allBooks.stream().anyMatch(b -> b.getIsbn().equals("0987654321")));
    }
}
