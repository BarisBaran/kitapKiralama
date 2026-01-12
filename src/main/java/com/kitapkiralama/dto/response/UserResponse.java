package com.kitapkiralama.dto.response;

import com.kitapkiralama.entity.User;

public class UserResponse {
    private Long id;
    private String adSoyad;
    private String email;
    private User.Rol rol;

    public UserResponse() {
    }

    public UserResponse(Long id, String adSoyad, String email, User.Rol rol) {
        this.id = id;
        this.adSoyad = adSoyad;
        this.email = email;
        this.rol = rol;
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getAdSoyad(),
                user.getEmail(),
                user.getRol()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User.Rol getRol() {
        return rol;
    }

    public void setRol(User.Rol rol) {
        this.rol = rol;
    }
}
