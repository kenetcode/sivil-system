package com.sivil.systeam.enums;

public enum MetodoPago {

    TARJETA("tarjeta"),
    EFECTIVO("efectivo");

    private final String value;

    MetodoPago(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
