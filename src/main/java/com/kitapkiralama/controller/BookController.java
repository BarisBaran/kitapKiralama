package com.kitapkiralama.controller;

import com.kitapkiralama.dto.request.BookRequest;
import com.kitapkiralama.dto.response.ApiResponse;
import com.kitapkiralama.dto.response.BookResponse;
import com.kitapkiralama.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Kitap yönetimi işlemleri")
@SecurityRequirement(name = "bearer-jwt")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "Tüm kitapları listele", description = "Sistemdeki tüm kitapları listeler")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAllBooks() {
        List<BookResponse> books = bookService.getAllBooks();
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/available")
    @Operation(summary = "Müsait kitapları listele", description = "Stokta bulunan kitapları listeler")
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAvailableBooks() {
        List<BookResponse> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(ApiResponse.success(books));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Kitap detayı", description = "ID'ye göre kitap detayını getirir")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable Long id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Yeni kitap ekle", description = "Admin yetkisi gerektirir")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse book = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kitap başarıyla eklendi", book));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kitap güncelle", description = "Admin yetkisi gerektirir")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(@PathVariable Long id, 
                                                               @Valid @RequestBody BookRequest request) {
        BookResponse book = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Kitap başarıyla güncellendi", book));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kitap sil", description = "Admin yetkisi gerektirir")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Kitap başarıyla silindi", null));
    }
}
