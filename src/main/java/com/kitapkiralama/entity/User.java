package com.kitapkiralama.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ad soyad boş olamaz")
    @Column(name = "ad_soyad", nullable = false)
    private String adSoyad;

    @NotBlank(message = "E-posta boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Column(nullable = false)
    private String sifre;

    @NotNull(message = "Rol boş olamaz")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.KULLANICI;

    @OneToMany(mappedBy = "kullanici", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rental> rentals = new ArrayList<>();

    @OneToMany(mappedBy = "kullanici", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    public enum Rol {
        ADMIN,
        KULLANICI
    }

    public User() {
    }

    public User(String adSoyad, String email, String sifre, Rol rol) {
        this.adSoyad = adSoyad;
        this.email = email;
        this.sifre = sifre;
        this.rol = rol;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSifre() {
        return sifre;
    }

    public void setSifre(String sifre) {
        this.sifre = sifre;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public List<Rental> getRentals() {
        return rentals;
    }

    public void setRentals(List<Rental> rentals) {
        this.rentals = rentals;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
}
