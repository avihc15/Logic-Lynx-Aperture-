package com.example.aperture.model;

public class Funcion {
    private int idFuncion;
    private int idCartelera;
    private int idPelicula;
    private int idSala;
    private String fechaHora;
    private String idioma;
    private String formato;
    private String estado;

    public Funcion() {
    }

    public Funcion(int idFuncion, int idCartelera, int idPelicula,
                   int idSala, String fechaHora, String idioma,
                   String formato, String estado) {
        this.idFuncion = idFuncion;
        this.idCartelera = idCartelera;
        this.idPelicula = idPelicula;
        this.idSala = idSala;
        this.fechaHora = fechaHora;
        this.idioma = idioma;
        this.formato = formato;
        this.estado = estado;
    }

    public int getIdFuncion() { return idFuncion; }
    public void setIdFuncion(int idFuncion) { this.idFuncion = idFuncion; }
    public int getIdCartelera() { return idCartelera; }
    public void setIdCartelera(int idCartelera) { this.idCartelera = idCartelera; }
    public int getIdPelicula() { return idPelicula; }
    public void setIdPelicula(int idPelicula) { this.idPelicula = idPelicula; }
    public int getIdSala() { return idSala; }
    public void setIdSala(int idSala) { this.idSala = idSala; }
    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
