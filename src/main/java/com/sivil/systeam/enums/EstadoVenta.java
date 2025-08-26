package com.sivil.systeam.enums;

public enum EstadoVenta {

    ACTIVA("activa"),
    INACTIVA("inactiva"),
    FINALIZADA("finalizada");

    private final String value;

    EstadoVenta(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
