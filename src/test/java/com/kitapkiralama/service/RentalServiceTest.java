package com.kitapkiralama.service;

import com.kitapkiralama.dto.request.RentalRequest;
import com.kitapkiralama.dto.response.RentalResponse;
import com.kitapkiralama.entity.Book;
import com.kitapkiralama.entity.Rental;
import com.kitapkiralama.entity.User;
import com.kitapkiralama.exception.BadRequestException;
import com.kitapkiralama.exception.ConflictException;
import com.kitapkiralama.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private RentalService rentalService;

    private User testUser;
    private Book testBook;
    private Rental testRental;
    private RentalRequest rentalRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setAdSoyad("Test User");
        testUser.setEmail("test@example.com");

        testBook = new Book();
        testBook.setId(1L);
        testBook.setBaslik("Test Kitap");
        testBook.setYazar("Test Yazar");
        testBook.setIsbn("1234567890");
        testBook.setStok(5);

        testRental = new Rental();
        testRental.setId(1L);
        testRental.setKullanici(testUser);
        testRental.setKitap(testBook);
        testRental.setKiralamaTarihi(LocalDate.now());
        testRental.setIadeTarihi(LocalDate.now().plusDays(14));
        testRental.setDurum(Rental.Durum.KIRADA);

        rentalRequest = new RentalRequest();
        rentalRequest.setKitapId(1L);
    }

    @Test
    void testKitapKiralamaBasarili() {
        // Given
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(bookService.getBookEntity(1L)).thenReturn(testBook);
        when(rentalRepository.findByKullaniciIdAndKitapIdAndDurum(1L, 1L, Rental.Durum.KIRADA))
                .thenReturn(Optional.empty());
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);

        // When
        RentalResponse response = rentalService.rentBook(1L, rentalRequest);

        // Then
        assertNotNull(response);
        assertEquals(testRental.getId(), response.getId());
        verify(bookService, times(1)).decreaseStock(1L);
        verify(paymentService, times(1)).createPayment(any(Rental.class), eq(testUser), any(BigDecimal.class));
        verify(rentalRepository, times(1)).save(any(Rental.class));
    }

    @Test
    void testStokYoksaKiralamaBasarisiz() {
        // Given
        testBook.setStok(0);
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(bookService.getBookEntity(1L)).thenReturn(testBook);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            rentalService.rentBook(1L, rentalRequest);
        });

        assertEquals("Kitap stokta yok", exception.getMessage());
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void testKitapIadeBasarili() {
        // Given
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(testRental);

        // When
        RentalResponse response = rentalService.returnBook(1L);

        // Then
        assertNotNull(response);
        assertEquals(Rental.Durum.IADE_EDILDI, testRental.getDurum());
        verify(bookService, times(1)).increaseStock(1L);
        verify(paymentService, times(1)).completePayment(1L);
        verify(rentalRepository, times(1)).save(testRental);
    }

    @Test
    void testKullaniciBulunamadiDurumu() {
        // Given
        when(userService.getUserEntity(999L))
                .thenThrow(new com.kitapkiralama.exception.ResourceNotFoundException("Kullanıcı bulunamadı: 999"));

        // When & Then
        assertThrows(com.kitapkiralama.exception.ResourceNotFoundException.class, () -> {
            rentalService.rentBook(999L, rentalRequest);
        });

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void testZatenKiralanmisKitap() {
        // Given
        when(userService.getUserEntity(1L)).thenReturn(testUser);
        when(bookService.getBookEntity(1L)).thenReturn(testBook);
        when(rentalRepository.findByKullaniciIdAndKitapIdAndDurum(1L, 1L, Rental.Durum.KIRADA))
                .thenReturn(Optional.of(testRental));

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            rentalService.rentBook(1L, rentalRequest);
        });

        assertEquals("Bu kitap zaten kiralanmış durumda", exception.getMessage());
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void testZatenIadeEdilmisKitap() {
        // Given
        testRental.setDurum(Rental.Durum.IADE_EDILDI);
        when(rentalRepository.findById(1L)).thenReturn(Optional.of(testRental));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            rentalService.returnBook(1L);
        });

        assertEquals("Bu kitap zaten iade edilmiş", exception.getMessage());
        verify(bookService, never()).increaseStock(anyLong());
    }
}
