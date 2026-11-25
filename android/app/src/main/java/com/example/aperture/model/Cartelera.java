package com.example.aperture.model;

public class Cartelera {
    private int idCartelera;
    private int idSucursal;
    private String nombre;
    private String fechaInicio;
    private String fechaFin;
    private boolean activo;

    public Cartelera() {
    }

    public Cartelera(int idCartelera, int idSucursal, String nombre,
                     String fechaInicio, String fechaFin, boolean activo) {
        this.idCartelera = idCartelera;
        this.idSucursal = idSucursal;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = activo;
    }

    public int getIdCartelera() { return idCartelera; }
    public void setIdCartelera(int idCartelera) { this.idCartelera = idCartelera; }
    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
