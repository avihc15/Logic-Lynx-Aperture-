package com.example.aperture.model;

public class Combo {
    private int idCombo;
    private String nombre;
    private String descripcion;
    private double precio;
    private boolean activo;

    public Combo() {
    }

    public Combo(int idCombo, String nombre, String descripcion,
                 double precio, boolean activo) {
        this.idCombo = idCombo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.activo = activo;
    }

    public int getIdCombo() { return idCombo; }
    public void setIdCombo(int idCombo) { this.idCombo = idCombo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
