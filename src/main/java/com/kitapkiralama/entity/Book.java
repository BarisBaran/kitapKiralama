package com.kitapkiralama.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Başlık boş olamaz")
    @Column(nullable = false)
    private String baslik;

    @NotBlank(message = "Yazar boş olamaz")
    @Column(nullable = false)
    private String yazar;

    @NotBlank(message = "ISBN boş olamaz")
    @Column(unique = true, nullable = false)
    private String isbn;

    @NotNull(message = "Stok boş olamaz")
    @PositiveOrZero(message = "Stok negatif olamaz")
    @Column(nullable = false)
    private Integer stok;

    @OneToMany(mappedBy = "kitap", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rental> rentals = new ArrayList<>();

    public Book() {
    }

    public Book(String baslik, String yazar, String isbn, Integer stok) {
        this.baslik = baslik;
        this.yazar = yazar;
        this.isbn = isbn;
        this.stok = stok;
    }

    // Getters and Setters
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

    public List<Rental> getRentals() {
        return rentals;
    }

    public void setRentals(List<Rental> rentals) {
        this.rentals = rentals;
    }
}
