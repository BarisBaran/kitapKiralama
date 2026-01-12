package com.kitapkiralama.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Kiralama boş olamaz")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kiralama_id", nullable = false)
    private Rental kiralama;

    @NotNull(message = "Kullanıcı boş olamaz")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private User kullanici;

    @NotNull(message = "Tutar boş olamaz")
    @Positive(message = "Tutar pozitif olmalıdır")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tutar;

    @Column(name = "odeme_tarihi")
    private LocalDate odemeTarihi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Durum durum = Durum.BEKLEMEDE;

    public enum Durum {
        BEKLEMEDE,
        ODEME_ALINDI,
        IADE_EDILDI
    }

    public Payment() {
    }

    public Payment(Rental kiralama, User kullanici, BigDecimal tutar) {
        this.kiralama = kiralama;
        this.kullanici = kullanici;
        this.tutar = tutar;
        this.durum = Durum.BEKLEMEDE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Rental getKiralama() {
        return kiralama;
    }

    public void setKiralama(Rental kiralama) {
        this.kiralama = kiralama;
    }

    public User getKullanici() {
        return kullanici;
    }

    public void setKullanici(User kullanici) {
        this.kullanici = kullanici;
    }

    public BigDecimal getTutar() {
        return tutar;
    }

    public void setTutar(BigDecimal tutar) {
        this.tutar = tutar;
    }

    public LocalDate getOdemeTarihi() {
        return odemeTarihi;
    }

    public void setOdemeTarihi(LocalDate odemeTarihi) {
        this.odemeTarihi = odemeTarihi;
    }

    public Durum getDurum() {
        return durum;
    }

    public void setDurum(Durum durum) {
        this.durum = durum;
    }
}
