package com.kitapkiralama.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rentals")
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Kullanıcı boş olamaz")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private User kullanici;

    @NotNull(message = "Kitap boş olamaz")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kitap_id", nullable = false)
    private Book kitap;

    @NotNull(message = "Kiralama tarihi boş olamaz")
    @Column(name = "kiralama_tarihi", nullable = false)
    private LocalDate kiralamaTarihi;

    @NotNull(message = "İade tarihi boş olamaz")
    @Column(name = "iade_tarihi", nullable = false)
    private LocalDate iadeTarihi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Durum durum = Durum.KIRADA;

    @OneToMany(mappedBy = "kiralama", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();

    public enum Durum {
        KIRADA,
        IADE_EDILDI
    }

    public Rental() {
    }

    public Rental(User kullanici, Book kitap, LocalDate kiralamaTarihi, LocalDate iadeTarihi) {
        this.kullanici = kullanici;
        this.kitap = kitap;
        this.kiralamaTarihi = kiralamaTarihi;
        this.iadeTarihi = iadeTarihi;
        this.durum = Durum.KIRADA;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getKullanici() {
        return kullanici;
    }

    public void setKullanici(User kullanici) {
        this.kullanici = kullanici;
    }

    public Book getKitap() {
        return kitap;
    }

    public void setKitap(Book kitap) {
        this.kitap = kitap;
    }

    public LocalDate getKiralamaTarihi() {
        return kiralamaTarihi;
    }

    public void setKiralamaTarihi(LocalDate kiralamaTarihi) {
        this.kiralamaTarihi = kiralamaTarihi;
    }

    public LocalDate getIadeTarihi() {
        return iadeTarihi;
    }

    public void setIadeTarihi(LocalDate iadeTarihi) {
        this.iadeTarihi = iadeTarihi;
    }

    public Durum getDurum() {
        return durum;
    }

    public void setDurum(Durum durum) {
        this.durum = durum;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
}
