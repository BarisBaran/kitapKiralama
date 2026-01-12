package com.kitapkiralama.repository;

import com.kitapkiralama.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findByKullaniciId(Long kullaniciId);
    List<Rental> findByKullaniciIdAndDurum(Long kullaniciId, Rental.Durum durum);
    List<Rental> findByKitapId(Long kitapId);
    List<Rental> findByDurum(Rental.Durum durum);
    Optional<Rental> findByKullaniciIdAndKitapIdAndDurum(Long kullaniciId, Long kitapId, Rental.Durum durum);
}
