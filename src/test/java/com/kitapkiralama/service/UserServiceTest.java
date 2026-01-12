package com.kitapkiralama.service;

import com.kitapkiralama.dto.request.UserLoginRequest;
import com.kitapkiralama.dto.request.UserRegisterRequest;
import com.kitapkiralama.dto.response.LoginResponse;
import com.kitapkiralama.dto.response.UserResponse;
import com.kitapkiralama.entity.User;
import com.kitapkiralama.exception.BadRequestException;
import com.kitapkiralama.exception.UnauthorizedException;
import com.kitapkiralama.repository.UserRepository;
import com.kitapkiralama.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegisterRequest registerRequest;
    private UserLoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setAdSoyad("Test User");
        testUser.setEmail("test@example.com");
        testUser.setSifre("$2a$10$encodedPassword");
        testUser.setRol(User.Rol.KULLANICI);

        registerRequest = new UserRegisterRequest();
        registerRequest.setAdSoyad("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setSifre("password123");

        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setSifre("password123");
    }

    @Test
    void testRegister() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterWithExistingEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> {
            userService.register(registerRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("test@example.com", "KULLANICI")).thenReturn("jwt-token");

        LoginResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void testLoginWithWrongPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).thenReturn(false);

        loginRequest.setSifre("wrongpassword");

        assertThrows(UnauthorizedException.class, () -> {
            userService.login(loginRequest);
        });
    }

    @Test
    void testLoginWithNonExistentUser() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        loginRequest.setEmail("nonexistent@example.com");

        assertThrows(UnauthorizedException.class, () -> {
            userService.login(loginRequest);
        });
    }
}
