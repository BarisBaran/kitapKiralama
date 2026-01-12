package com.kitapkiralama.integration;

import com.kitapkiralama.dto.response.BookResponse;
import com.kitapkiralama.entity.Book;
import com.kitapkiralama.repository.BookRepository;
import com.kitapkiralama.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@Transactional
class BookIntegrationTest {

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
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    @Test
    void testGetAllBooks() {
        // Given
        Book book1 = new Book();
        book1.setBaslik("Kitap 1");
        book1.setYazar("Yazar 1");
        book1.setIsbn("1111111111");
        book1.setStok(5);
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setBaslik("Kitap 2");
        book2.setYazar("Yazar 2");
        book2.setIsbn("2222222222");
        book2.setStok(3);
        bookRepository.save(book2);

        // When
        List<BookResponse> books = bookService.getAllBooks();

        // Then
        assertEquals(2, books.size());
        assertTrue(books.stream().anyMatch(b -> b.getIsbn().equals("1111111111")));
        assertTrue(books.stream().anyMatch(b -> b.getIsbn().equals("2222222222")));
    }

    @Test
    void testGetAvailableBooks() {
        // Given
        Book book1 = new Book();
        book1.setBaslik("Kitap 1");
        book1.setYazar("Yazar 1");
        book1.setIsbn("1111111111");
        book1.setStok(5);
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setBaslik("Kitap 2");
        book2.setYazar("Yazar 2");
        book2.setIsbn("2222222222");
        book2.setStok(0);
        bookRepository.save(book2);

        // When
        List<BookResponse> books = bookService.getAvailableBooks();

        // Then
        assertEquals(1, books.size());
        assertEquals("1111111111", books.get(0).getIsbn());
    }
}
