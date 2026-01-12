package com.kitapkiralama.service;

import com.kitapkiralama.dto.request.BookRequest;
import com.kitapkiralama.dto.response.BookResponse;
import com.kitapkiralama.entity.Book;
import com.kitapkiralama.exception.BadRequestException;
import com.kitapkiralama.exception.ConflictException;
import com.kitapkiralama.exception.ResourceNotFoundException;
import com.kitapkiralama.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookResponse> getAllBooks() {
        logger.info("Tüm kitaplar listeleniyor");
        return bookRepository.findAll().stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getAvailableBooks() {
        logger.info("Müsait kitaplar listeleniyor");
        return bookRepository.findByStokGreaterThan(0).stream()
                .map(BookResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitap bulunamadı: " + id));
        return BookResponse.fromEntity(book);
    }

    public Book getBookEntity(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitap bulunamadı: " + id));
    }

    public BookResponse createBook(BookRequest request) {
        logger.info("Yeni kitap oluşturuluyor: {}", request.getBaslik());

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new ConflictException("Bu ISBN numaralı kitap zaten mevcut: " + request.getIsbn());
        }

        Book book = new Book();
        book.setBaslik(request.getBaslik());
        book.setYazar(request.getYazar());
        book.setIsbn(request.getIsbn());
        book.setStok(request.getStok());

        Book savedBook = bookRepository.save(book);
        logger.info("Kitap başarıyla oluşturuldu: {}", savedBook.getId());

        return BookResponse.fromEntity(savedBook);
    }

    public BookResponse updateBook(Long id, BookRequest request) {
        logger.info("Kitap güncelleniyor: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kitap bulunamadı: " + id));

        if (!book.getIsbn().equals(request.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new ConflictException("Bu ISBN numaralı kitap zaten mevcut: " + request.getIsbn());
        }

        book.setBaslik(request.getBaslik());
        book.setYazar(request.getYazar());
        book.setIsbn(request.getIsbn());
        book.setStok(request.getStok());

        Book updatedBook = bookRepository.save(book);
        logger.info("Kitap başarıyla güncellendi: {}", updatedBook.getId());

        return BookResponse.fromEntity(updatedBook);
    }

    public void deleteBook(Long id) {
        logger.info("Kitap siliniyor: {}", id);

        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Kitap bulunamadı: " + id);
        }

        bookRepository.deleteById(id);
        logger.info("Kitap başarıyla silindi: {}", id);
    }

    public void decreaseStock(Long bookId) {
        Book book = getBookEntity(bookId);
        if (book.getStok() <= 0) {
            throw new BadRequestException("Kitap stokta yok");
        }
        book.setStok(book.getStok() - 1);
        bookRepository.save(book);
        logger.info("Kitap stoku azaltıldı: {} (Yeni stok: {})", bookId, book.getStok());
    }

    public void increaseStock(Long bookId) {
        Book book = getBookEntity(bookId);
        book.setStok(book.getStok() + 1);
        bookRepository.save(book);
        logger.info("Kitap stoku artırıldı: {} (Yeni stok: {})", bookId, book.getStok());
    }
}
