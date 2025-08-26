package com.sivil.systeam.enums;

public enum EstadoPago {

    PENDIENTE("pendiente"),
    COMPLETADO("completado"),
    FALLIDO("fallido");

    private final String value;

    EstadoPago(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
