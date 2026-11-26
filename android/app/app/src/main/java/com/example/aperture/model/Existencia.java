package com.example.aperture.model;

public class Existencia {
    private int idExistencia;
    private int idSucursal;
    private int idProducto;
    private int cantidad;

    public Existencia() {
    }

    public Existencia(int idExistencia, int idSucursal,
                      int idProducto, int cantidad) {
        this.idExistencia = idExistencia;
        this.idSucursal = idSucursal;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    public int getIdExistencia() { return idExistencia; }
    public void setIdExistencia(int idExistencia) { this.idExistencia = idExistencia; }
    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
