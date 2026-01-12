package com.kitapkiralama.controller;

import com.kitapkiralama.dto.request.UserLoginRequest;
import com.kitapkiralama.dto.request.UserRegisterRequest;
import com.kitapkiralama.dto.response.ApiResponse;
import com.kitapkiralama.dto.response.LoginResponse;
import com.kitapkiralama.dto.response.UserResponse;
import com.kitapkiralama.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Kullanıcı kayıt ve giriş işlemleri")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Kullanıcı kaydı", description = "Yeni kullanıcı kaydı oluşturur")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody UserRegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kullanıcı başarıyla kaydedildi", user));
    }

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı girişi", description = "Kullanıcı girişi yapar ve JWT token döner")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody UserLoginRequest request) {
        LoginResponse loginResponse = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Giriş başarılı", loginResponse));
    }
}
