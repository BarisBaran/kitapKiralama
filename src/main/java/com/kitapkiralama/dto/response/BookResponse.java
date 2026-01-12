package com.kitapkiralama.dto.response;

import com.kitapkiralama.entity.Book;

public class BookResponse {
    private Long id;
    private String baslik;
    private String yazar;
    private String isbn;
    private Integer stok;

    public BookResponse() {
    }

    public BookResponse(Long id, String baslik, String yazar, String isbn, Integer stok) {
        this.id = id;
        this.baslik = baslik;
        this.yazar = yazar;
        this.isbn = isbn;
        this.stok = stok;
    }

    public static BookResponse fromEntity(Book book) {
        return new BookResponse(
                book.getId(),
                book.getBaslik(),
                book.getYazar(),
                book.getIsbn(),
                book.getStok()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaslik() {
        return baslik;
    }

    public void setBaslik(String baslik) {
        this.baslik = baslik;
    }

    public String getYazar() {
        return yazar;
    }

    public void setYazar(String yazar) {
        this.yazar = yazar;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getStok() {
        return stok;
    }

    public void setStok(Integer stok) {
        this.stok = stok;
    }
}
