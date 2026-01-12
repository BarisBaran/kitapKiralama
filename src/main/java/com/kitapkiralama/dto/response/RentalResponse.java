package com.kitapkiralama.dto.response;

import com.kitapkiralama.entity.Rental;

import java.time.LocalDate;

public class RentalResponse {
    private Long id;
    private Long kullaniciId;
    private String kullaniciAdSoyad;
    private Long kitapId;
    private String kitapBaslik;
    private LocalDate kiralamaTarihi;
    private LocalDate iadeTarihi;
    private Rental.Durum durum;

    public RentalResponse() {
    }

    public RentalResponse(Long id, Long kullaniciId, String kullaniciAdSoyad, Long kitapId, 
                        String kitapBaslik, LocalDate kiralamaTarihi, LocalDate iadeTarihi, 
                        Rental.Durum durum) {
        this.id = id;
        this.kullaniciId = kullaniciId;
        this.kullaniciAdSoyad = kullaniciAdSoyad;
        this.kitapId = kitapId;
        this.kitapBaslik = kitapBaslik;
        this.kiralamaTarihi = kiralamaTarihi;
        this.iadeTarihi = iadeTarihi;
        this.durum = durum;
    }

    public static RentalResponse fromEntity(Rental rental) {
        return new RentalResponse(
                rental.getId(),
                rental.getKullanici().getId(),
                rental.getKullanici().getAdSoyad(),
                rental.getKitap().getId(),
                rental.getKitap().getBaslik(),
                rental.getKiralamaTarihi(),
                rental.getIadeTarihi(),
                rental.getDurum()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getKullaniciId() {
        return kullaniciId;
    }

    public void setKullaniciId(Long kullaniciId) {
        this.kullaniciId = kullaniciId;
    }

    public String getKullaniciAdSoyad() {
        return kullaniciAdSoyad;
    }

    public void setKullaniciAdSoyad(String kullaniciAdSoyad) {
        this.kullaniciAdSoyad = kullaniciAdSoyad;
    }

    public Long getKitapId() {
        return kitapId;
    }

    public void setKitapId(Long kitapId) {
        this.kitapId = kitapId;
    }

    public String getKitapBaslik() {
        return kitapBaslik;
    }

    public void setKitapBaslik(String kitapBaslik) {
        this.kitapBaslik = kitapBaslik;
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

    public Rental.Durum getDurum() {
        return durum;
    }

    public void setDurum(Rental.Durum durum) {
        this.durum = durum;
    }
}
