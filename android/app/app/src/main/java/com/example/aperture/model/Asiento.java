package com.example.aperture.model;

public class Asiento {
    private int idAsiento;
    private int idSala;
    private String fila;
    private int numero;
    private String tipo;

    public Asiento() {
    }

    public Asiento(int idAsiento, int idSala, String fila,
                   int numero, String tipo) {
        this.idAsiento = idAsiento;
        this.idSala = idSala;
        this.fila = fila;
        this.numero = numero;
        this.tipo = tipo;
    }

    public int getIdAsiento() { return idAsiento; }
    public void setIdAsiento(int idAsiento) { this.idAsiento = idAsiento; }
    public int getIdSala() { return idSala; }
    public void setIdSala(int idSala) { this.idSala = idSala; }
    public String getFila() { return fila; }
    public void setFila(String fila) { this.fila = fila; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
