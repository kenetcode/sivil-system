package com.sivil.systeam.enums;

public enum EstadoCompra {

    PENDIENTE("pendiente"),
    PROCESADA("procesada"),
    ENVIADA("enviada"),
    ENTREGADA("entregada");

    private final String value;

    EstadoCompra(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
