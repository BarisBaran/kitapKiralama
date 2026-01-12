package com.kitapkiralama.controller;

import com.kitapkiralama.dto.response.ApiResponse;
import com.kitapkiralama.dto.response.RentalResponse;
import com.kitapkiralama.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin işlemleri")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminController {

    private final RentalService rentalService;

    @Autowired
    public AdminController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("/rentals")
    @Operation(summary = "Tüm kiralamalar", description = "Sistemdeki tüm kiralamaları listeler (Admin)")
    public ResponseEntity<ApiResponse<List<RentalResponse>>> getAllRentals() {
        List<RentalResponse> rentals = rentalService.getAllRentals();
        return ResponseEntity.ok(ApiResponse.success(rentals));
    }
}
