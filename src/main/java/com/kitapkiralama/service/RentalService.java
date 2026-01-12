package com.kitapkiralama.service;

import com.kitapkiralama.dto.request.RentalRequest;
import com.kitapkiralama.dto.response.RentalResponse;
import com.kitapkiralama.entity.Book;
import com.kitapkiralama.entity.Rental;
import com.kitapkiralama.entity.User;
import com.kitapkiralama.exception.BadRequestException;
import com.kitapkiralama.exception.ConflictException;
import com.kitapkiralama.exception.ResourceNotFoundException;
import com.kitapkiralama.repository.RentalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RentalService {

    private static final Logger logger = LoggerFactory.getLogger(RentalService.class);

    private static final int KIRALAMA_SURE_GUN = 14;
    private static final BigDecimal GUNLUK_UCRET = new BigDecimal("5.00");

    private final RentalRepository rentalRepository;
    private final BookService bookService;
    private final UserService userService;
    private final PaymentService paymentService;

    @Autowired
    public RentalService(RentalRepository rentalRepository, BookService bookService, 
                        UserService userService, PaymentService paymentService) {
        this.rentalRepository = rentalRepository;
        this.bookService = bookService;
        this.userService = userService;
        this.paymentService = paymentService;
    }

    public RentalResponse rentBook(Long userId, RentalRequest request) {
        logger.info("Kitap kiralama işlemi başlatılıyor - Kullanıcı: {}, Kitap: {}", userId, request.getKitapId());

        User user = userService.getUserEntity(userId);
        Book book = bookService.getBookEntity(request.getKitapId());

        if (book.getStok() <= 0) {
            throw new BadRequestException("Kitap stokta yok");
        }

        if (rentalRepository.findByKullaniciIdAndKitapIdAndDurum(userId, request.getKitapId(), Rental.Durum.KIRADA).isPresent()) {
            throw new ConflictException("Bu kitap zaten kiralanmış durumda");
        }

        LocalDate kiralamaTarihi = LocalDate.now();
        LocalDate iadeTarihi = kiralamaTarihi.plusDays(KIRALAMA_SURE_GUN);

        Rental rental = new Rental();
        rental.setKullanici(user);
        rental.setKitap(book);
        rental.setKiralamaTarihi(kiralamaTarihi);
        rental.setIadeTarihi(iadeTarihi);
        rental.setDurum(Rental.Durum.KIRADA);

        Rental savedRental = rentalRepository.save(rental);

        // Stok azalt
        bookService.decreaseStock(book.getId());

        // Ödeme kaydı oluştur
        BigDecimal tutar = GUNLUK_UCRET.multiply(new BigDecimal(KIRALAMA_SURE_GUN));
        paymentService.createPayment(savedRental, user, tutar);

        logger.info("Kitap başarıyla kiralandı: {}", savedRental.getId());

        return RentalResponse.fromEntity(savedRental);
    }

    public RentalResponse returnBook(Long rentalId) {
        logger.info("Kitap iade işlemi başlatılıyor: {}", rentalId);

        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Kiralama bulunamadı: " + rentalId));

        if (rental.getDurum() == Rental.Durum.IADE_EDILDI) {
            throw new BadRequestException("Bu kitap zaten iade edilmiş");
        }

        rental.setDurum(Rental.Durum.IADE_EDILDI);

        // Stok artır
        bookService.increaseStock(rental.getKitap().getId());

        Rental updatedRental = rentalRepository.save(rental);

        // Ödeme durumunu güncelle
        paymentService.completePayment(rental.getId());

        logger.info("Kitap başarıyla iade edildi: {}", rentalId);

        return RentalResponse.fromEntity(updatedRental);
    }

    public List<RentalResponse> getUserRentals(Long userId) {
        return rentalRepository.findByKullaniciId(userId).stream()
                .map(RentalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RentalResponse> getActiveRentals(Long userId) {
        return rentalRepository.findByKullaniciIdAndDurum(userId, Rental.Durum.KIRADA).stream()
                .map(RentalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RentalResponse> getAllRentals() {
        return rentalRepository.findAll().stream()
                .map(RentalResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
