package com.example.aperture.model;

public class TarifaBoleto {
    private int idTarifa;
    private String nombre;
    private double precio;
    private String descripcion;
    private boolean activo;

    public TarifaBoleto() {
    }

    public TarifaBoleto(int idTarifa, String nombre, double precio,
                        String descripcion, boolean activo) {
        this.idTarifa = idTarifa;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    public int getIdTarifa() { return idTarifa; }
    public void setIdTarifa(int idTarifa) { this.idTarifa = idTarifa; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
