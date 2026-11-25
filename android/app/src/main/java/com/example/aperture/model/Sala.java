package com.example.aperture.model;

public class Sala {
    private int idSala;
    private int idSucursal;
    private String nombre;
    private int capacidad;
    private String tipo;
    private boolean activo;

    public Sala() {
    }

    public Sala(int idSala, int idSucursal, String nombre,
                int capacidad, String tipo, boolean activo) {
        this.idSala = idSala;
        this.idSucursal = idSucursal;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.tipo = tipo;
        this.activo = activo;
    }

    public int getIdSala() { return idSala; }
    public void setIdSala(int idSala) { this.idSala = idSala; }
    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
