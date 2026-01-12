package com.kitapkiralama.service;

import com.kitapkiralama.dto.request.BookRequest;
import com.kitapkiralama.dto.response.BookResponse;
import com.kitapkiralama.entity.Book;
import com.kitapkiralama.exception.ConflictException;
import com.kitapkiralama.exception.ResourceNotFoundException;
import com.kitapkiralama.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setBaslik("Test Kitap");
        testBook.setYazar("Test Yazar");
        testBook.setIsbn("1234567890");
        testBook.setStok(5);

        bookRequest = new BookRequest();
        bookRequest.setBaslik("Test Kitap");
        bookRequest.setYazar("Test Yazar");
        bookRequest.setIsbn("1234567890");
        bookRequest.setStok(5);
    }

    @Test
    void testCreateBook() {
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        BookResponse response = bookService.createBook(bookRequest);

        assertNotNull(response);
        assertEquals(testBook.getId(), response.getId());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testCreateBookWithExistingIsbn() {
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(true);

        assertThrows(ConflictException.class, () -> {
            bookService.createBook(bookRequest);
        });

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testGetBookById() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        BookResponse response = bookService.getBookById(1L);

        assertNotNull(response);
        assertEquals(testBook.getId(), response.getId());
    }

    @Test
    void testGetBookByIdNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.getBookById(999L);
        });
    }

    @Test
    void testDecreaseStock() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        bookService.decreaseStock(1L);

        assertEquals(4, testBook.getStok());
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    void testDecreaseStockWhenStockIsZero() {
        testBook.setStok(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        assertThrows(com.kitapkiralama.exception.BadRequestException.class, () -> {
            bookService.decreaseStock(1L);
        });
    }
}
