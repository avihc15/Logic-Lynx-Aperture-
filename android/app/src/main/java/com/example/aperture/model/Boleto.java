package com.example.aperture.model;

public class Boleto {
    private int idBoleto;
    private int idFuncion;
    private int idAsiento;
    private int idTarifa;
    private Integer idPromocion;
    private Integer idVenta;
    private double precioFinal;
    private String estado;

    public Boleto() {
    }

    public Boleto(int idBoleto, int idFuncion, int idAsiento,
                  int idTarifa, Integer idPromocion, Integer idVenta,
                  double precioFinal, String estado) {
        this.idBoleto = idBoleto;
        this.idFuncion = idFuncion;
        this.idAsiento = idAsiento;
        this.idTarifa = idTarifa;
        this.idPromocion = idPromocion;
        this.idVenta = idVenta;
        this.precioFinal = precioFinal;
        this.estado = estado;
    }

    public int getIdBoleto() { return idBoleto; }
    public void setIdBoleto(int idBoleto) { this.idBoleto = idBoleto; }
    public int getIdFuncion() { return idFuncion; }
    public void setIdFuncion(int idFuncion) { this.idFuncion = idFuncion; }
    public int getIdAsiento() { return idAsiento; }
    public void setIdAsiento(int idAsiento) { this.idAsiento = idAsiento; }
    public int getIdTarifa() { return idTarifa; }
    public void setIdTarifa(int idTarifa) { this.idTarifa = idTarifa; }
    public Integer getIdPromocion() { return idPromocion; }
    public void setIdPromocion(Integer idPromocion) { this.idPromocion = idPromocion; }
    public Integer getIdVenta() { return idVenta; }
    public void setIdVenta(Integer idVenta) { this.idVenta = idVenta; }
    public double getPrecioFinal() { return precioFinal; }
    public void setPrecioFinal(double precioFinal) { this.precioFinal = precioFinal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
