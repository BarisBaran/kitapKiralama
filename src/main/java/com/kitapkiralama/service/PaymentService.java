package com.kitapkiralama.service;

import com.kitapkiralama.entity.Payment;
import com.kitapkiralama.entity.Rental;
import com.kitapkiralama.entity.User;
import com.kitapkiralama.exception.ResourceNotFoundException;
import com.kitapkiralama.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(Rental rental, User user, java.math.BigDecimal tutar) {
        logger.info("Ödeme kaydı oluşturuluyor - Kiralama: {}, Tutar: {}", rental.getId(), tutar);

        Payment payment = new Payment();
        payment.setKiralama(rental);
        payment.setKullanici(user);
        payment.setTutar(tutar);
        payment.setDurum(Payment.Durum.BEKLEMEDE);

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Ödeme kaydı oluşturuldu: {}", savedPayment.getId());

        return savedPayment;
    }

    public void completePayment(Long rentalId) {
        logger.info("Ödeme tamamlanıyor - Kiralama: {}", rentalId);

        Payment payment = paymentRepository.findByKiralamaIdAndDurum(rentalId, Payment.Durum.BEKLEMEDE)
                .orElseThrow(() -> new ResourceNotFoundException("Ödeme kaydı bulunamadı: " + rentalId));

        payment.setDurum(Payment.Durum.ODEME_ALINDI);
        payment.setOdemeTarihi(LocalDate.now());

        paymentRepository.save(payment);
        logger.info("Ödeme tamamlandı: {}", payment.getId());
    }
}
