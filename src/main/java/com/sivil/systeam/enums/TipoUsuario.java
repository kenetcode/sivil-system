package com.sivil.systeam.enums;

public enum TipoUsuario {
    COMPRADOR("comprador"),
    VENDEDOR("vendedor"),
    ADMIN("admin");

    private final String value;

    TipoUsuario(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
