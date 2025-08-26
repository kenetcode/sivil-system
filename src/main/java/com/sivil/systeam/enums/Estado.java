package com.sivil.systeam.enums;

public enum Estado {

    ACTIVO("activo"),
    INACTIVO("inactivo");

    private final String value;

    Estado(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
