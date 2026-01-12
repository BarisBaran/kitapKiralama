package com.kitapkiralama.service;

import com.kitapkiralama.dto.request.UserLoginRequest;
import com.kitapkiralama.dto.request.UserRegisterRequest;
import com.kitapkiralama.dto.response.LoginResponse;
import com.kitapkiralama.dto.response.UserResponse;
import com.kitapkiralama.entity.User;
import com.kitapkiralama.exception.BadRequestException;
import com.kitapkiralama.exception.ResourceNotFoundException;
import com.kitapkiralama.exception.UnauthorizedException;
import com.kitapkiralama.repository.UserRepository;
import com.kitapkiralama.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserResponse register(UserRegisterRequest request) {
        logger.info("Kullanıcı kaydı başlatılıyor: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Bu e-posta adresi zaten kayıtlı");
        }

        User user = new User();
        user.setAdSoyad(request.getAdSoyad());
        user.setEmail(request.getEmail());
        user.setSifre(passwordEncoder.encode(request.getSifre()));
        user.setRol(User.Rol.KULLANICI);

        User savedUser = userRepository.save(user);
        logger.info("Kullanıcı başarıyla kaydedildi: {}", savedUser.getId());

        return UserResponse.fromEntity(savedUser);
    }

    public LoginResponse login(UserLoginRequest request) {
        logger.info("Kullanıcı girişi başlatılıyor: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("E-posta veya şifre hatalı"));

        if (!passwordEncoder.matches(request.getSifre(), user.getSifre())) {
            throw new UnauthorizedException("E-posta veya şifre hatalı");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRol().name());
        logger.info("Kullanıcı başarıyla giriş yaptı: {}", user.getId());

        return new LoginResponse(token, UserResponse.fromEntity(user));
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + id));
        return UserResponse.fromEntity(user);
    }

    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + email));
    }
}
