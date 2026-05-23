package com.mentoredu.library.dto;

import jakarta.validation.constraints.NotNull;

public class UpdateResourceSettingsRequest {

    @NotNull(message = "aceptaResoluciones es obligatorio")
    private Boolean aceptaResoluciones;

    public Boolean getAceptaResoluciones() { return aceptaResoluciones; }
    public void setAceptaResoluciones(Boolean aceptaResoluciones) { this.aceptaResoluciones = aceptaResoluciones; }
}
