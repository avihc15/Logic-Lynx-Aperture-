package com.example.aperture.model;

public class Sucursal {
    private int idSucursal;
    private String nombre;
    private String direccion;
    private String telefono;
    private boolean activo;

    public Sucursal() {
    }

    public Sucursal(int idSucursal, String nombre, String direccion,
                    String telefono, boolean activo) {
        this.idSucursal = idSucursal;
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.activo = activo;
    }

    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
