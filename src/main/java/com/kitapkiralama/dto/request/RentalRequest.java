package com.kitapkiralama.dto.request;

import jakarta.validation.constraints.NotNull;

public class RentalRequest {
    @NotNull(message = "Kitap ID boş olamaz")
    private Long kitapId;

    public RentalRequest() {
    }

    public RentalRequest(Long kitapId) {
        this.kitapId = kitapId;
    }

    public Long getKitapId() {
        return kitapId;
    }

    public void setKitapId(Long kitapId) {
        this.kitapId = kitapId;
    }
}
