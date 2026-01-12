package com.kitapkiralama.repository;

import com.kitapkiralama.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByKiralamaId(Long kiralamaId);
    List<Payment> findByKullaniciId(Long kullaniciId);
    List<Payment> findByDurum(Payment.Durum durum);
    Optional<Payment> findByKiralamaIdAndDurum(Long kiralamaId, Payment.Durum durum);
}
