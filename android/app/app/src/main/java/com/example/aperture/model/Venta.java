package com.example.aperture.model;

public class Venta {
    private int idVenta;
    private int idSucursal;
    private String fechaHora;
    private double total;
    private String estado;

    public Venta() {
    }

    public Venta(int idVenta, int idSucursal, String fechaHora,
                 double total, String estado) {
        this.idVenta = idVenta;
        this.idSucursal = idSucursal;
        this.fechaHora = fechaHora;
        this.total = total;
        this.estado = estado;
    }

    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }
    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
