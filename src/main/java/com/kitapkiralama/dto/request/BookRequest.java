package com.kitapkiralama.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class BookRequest {
    @NotBlank(message = "Başlık boş olamaz")
    private String baslik;

    @NotBlank(message = "Yazar boş olamaz")
    private String yazar;

    @NotBlank(message = "ISBN boş olamaz")
    private String isbn;

    @NotNull(message = "Stok boş olamaz")
    @PositiveOrZero(message = "Stok negatif olamaz")
    private Integer stok;

    public BookRequest() {
    }

    public BookRequest(String baslik, String yazar, String isbn, Integer stok) {
        this.baslik = baslik;
        this.yazar = yazar;
        this.isbn = isbn;
        this.stok = stok;
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
