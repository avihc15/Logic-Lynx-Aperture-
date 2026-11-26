package com.example.aperture.model;

public class Pelicula {
    private int idPelicula;
    private String titulo;
    private int duracion;
    private String clasificacion;
    private String genero;
    private String sinopsis;
    private String poster;
    private boolean activo;

    public Pelicula() {
    }

    public Pelicula(int idPelicula, String titulo, int duracion,
                    String clasificacion, String genero, String sinopsis,
                    String poster, boolean activo) {
        this.idPelicula = idPelicula;
        this.titulo = titulo;
        this.duracion = duracion;
        this.clasificacion = clasificacion;
        this.genero = genero;
        this.sinopsis = sinopsis;
        this.poster = poster;
        this.activo = activo;
    }

    public int getIdPelicula() { return idPelicula; }
    public void setIdPelicula(int idPelicula) { this.idPelicula = idPelicula; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }
    public String getClasificacion() { return clasificacion; }
    public void setClasificacion(String clasificacion) { this.clasificacion = clasificacion; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getSinopsis() { return sinopsis; }
    public void setSinopsis(String sinopsis) { this.sinopsis = sinopsis; }
    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
