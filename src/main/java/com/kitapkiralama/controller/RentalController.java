package com.kitapkiralama.controller;

import com.kitapkiralama.dto.request.RentalRequest;
import com.kitapkiralama.dto.response.ApiResponse;
import com.kitapkiralama.dto.response.RentalResponse;
import com.kitapkiralama.security.JwtUtil;
import com.kitapkiralama.service.RentalService;
import com.kitapkiralama.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@Tag(name = "Rentals", description = "Kitap kiralama işlemleri")
@SecurityRequirement(name = "bearer-jwt")
@CrossOrigin(origins = "*")
public class RentalController {

    private final RentalService rentalService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public RentalController(RentalService rentalService, UserService userService, JwtUtil jwtUtil) {
        this.rentalService = rentalService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtUtil.extractEmail(token);
        return userService.getUserByEmail(email).getId();
    }

    @PostMapping("/kirala")
    @Operation(summary = "Kitap kirala", description = "Kullanıcı kitap kiralar")
    public ResponseEntity<ApiResponse<RentalResponse>> rentBook(@Valid @RequestBody RentalRequest request,
                                                              HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        RentalResponse rental = rentalService.rentBook(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kitap başarıyla kiralandı", rental));
    }

    @PostMapping("/{id}/iade")
    @Operation(summary = "Kitap iade et", description = "Kiralanan kitabı iade eder")
    public ResponseEntity<ApiResponse<RentalResponse>> returnBook(@PathVariable Long id) {
        RentalResponse rental = rentalService.returnBook(id);
        return ResponseEntity.ok(ApiResponse.success("Kitap başarıyla iade edildi", rental));
    }

    @GetMapping("/my-rentals")
    @Operation(summary = "Kullanıcının kiralamaları", description = "Giriş yapan kullanıcının kiralamalarını listeler")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getMyRentals(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<RentalResponse> rentals = rentalService.getUserRentals(userId);
        return ResponseEntity.ok(ApiResponse.success(rentals));
    }

    @GetMapping("/my-rentals/active")
    @Operation(summary = "Aktif kiralamalar", description = "Giriş yapan kullanıcının aktif kiralamalarını listeler")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getMyActiveRentals(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<RentalResponse> rentals = rentalService.getActiveRentals(userId);
        return ResponseEntity.ok(ApiResponse.success(rentals));
    }
}
