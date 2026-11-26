package com.logiclynx.aperture.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
public class VentasRestController {

    private final JdbcTemplate jdbc;

    public VentasRestController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listarVentas() {
        return jdbc.queryForList("SELECT * FROM ventas ORDER BY id_venta DESC LIMIT 50");
    }

    @PostMapping("/taquilla")
    @Transactional
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> registrarVentaTaquilla(@RequestBody Map<String, Object> body) {
        try {
            int idSucursal = Integer.parseInt(body.getOrDefault("idSucursal", 1).toString());
            int idFuncion = Integer.parseInt(body.get("idFuncion").toString());
            int idTarifa = Integer.parseInt(body.getOrDefault("idTarifa", 1).toString());
            Integer idPromo = body.get("idPromocion") != null ? Integer.parseInt(body.get("idPromocion").toString()) : null;
            double precioUnitario = Double.parseDouble(body.get("precioUnitario").toString());
            List<Integer> asientos = (List<Integer>) body.get("asientos");

            double total = precioUnitario * asientos.size();
            jdbc.update(
                "INSERT INTO ventas (id_sucursal, tipo_venta, subtotal, descuento, total, metodo_pago, estado) VALUES (?, 'TAQUILLA', ?, 0, ?, 'EFECTIVO', 'COMPLETADA')",
                idSucursal, total, total
            );
            Long idVenta = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

            for (Integer idAsiento : asientos) {
                jdbc.update(
                    "INSERT INTO boletos (id_funcion, id_asiento, id_precio_boleto, id_promocion, id_venta, precio_pagado, estado) VALUES (?, ?, ?, ?, ?, ?, 'VENDIDO')",
                    idFuncion, idAsiento, idTarifa, idPromo, idVenta, precioUnitario
                );
                Long idBoleto = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
                jdbc.update(
                    "INSERT INTO detalles_venta (id_venta, id_boleto, descripcion, cantidad, precio_unitario, descuento, total_linea) VALUES (?, ?, ?, 1, ?, 0, ?)",
                    idVenta, idBoleto, "Boleto función " + idFuncion, precioUnitario, precioUnitario
                );
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("idVenta", idVenta, "mensaje", "Venta de taquilla sincronizada en MySQL"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/dulceria")
    @Transactional
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> registrarVentaDulceria(@RequestBody Map<String, Object> body) {
        try {
            int idSucursal = Integer.parseInt(body.getOrDefault("idSucursal", 1).toString());
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            double total = 0.0;
            for (Map<String, Object> item : items) {
                String tipo = item.get("tipo").toString();
                int idArt = Integer.parseInt(item.get("idArticulo").toString());
                int cant = Integer.parseInt(item.get("cantidad").toString());
                Double precio = "PRODUCTO".equals(tipo)
                    ? jdbc.queryForObject("SELECT precio FROM precios_dulceria WHERE id_producto = ? AND id_sucursal = ? AND fecha_fin IS NULL LIMIT 1", Double.class, idArt, idSucursal)
                    : jdbc.queryForObject("SELECT precio FROM precios_dulceria WHERE id_combo = ? AND id_sucursal = ? AND fecha_fin IS NULL LIMIT 1", Double.class, idArt, idSucursal);
                total += (precio != null ? precio : 0.0) * cant;
            }

            jdbc.update(
                "INSERT INTO ventas (id_sucursal, tipo_venta, subtotal, descuento, total, metodo_pago, estado) VALUES (?, 'DULCERIA', ?, 0, ?, 'EFECTIVO', 'COMPLETADA')",
                idSucursal, total, total
            );
            Long idVenta = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

            for (Map<String, Object> item : items) {
                String tipo = item.get("tipo").toString();
                int idArt = Integer.parseInt(item.get("idArticulo").toString());
                int cant = Integer.parseInt(item.get("cantidad").toString());
                if ("PRODUCTO".equals(tipo)) {
                    Map<String, Object> info = jdbc.queryForMap(
                        "SELECT p.nombre_producto AS nombre, pd.precio FROM productos p JOIN precios_dulceria pd ON pd.id_producto = p.id_producto WHERE p.id_producto = ? AND pd.id_sucursal = ? AND pd.fecha_fin IS NULL LIMIT 1",
                        idArt, idSucursal
                    );
                    double pUnit = Double.parseDouble(info.get("precio").toString());
                    jdbc.update(
                        "INSERT INTO detalles_venta (id_venta, id_producto, descripcion, cantidad, precio_unitario, descuento, total_linea) VALUES (?, ?, ?, ?, ?, 0, ?)",
                        idVenta, idArt, info.get("nombre"), cant, pUnit, pUnit * cant
                    );
                    Integer idEx = jdbc.queryForObject("SELECT id_existencia FROM existencias WHERE id_sucursal = ? AND id_producto = ?", Integer.class, idSucursal, idArt);
                    jdbc.update("INSERT INTO movimientos_inventario (id_existencia, tipo_movimiento, cantidad, motivo) VALUES (?, 'SALIDA', ?, ?)",
                        idEx, cant, "Venta Android/Web #" + idVenta);
                } else {
                    Map<String, Object> info = jdbc.queryForMap(
                        "SELECT c.nombre_combo AS nombre, pd.precio FROM combos c JOIN precios_dulceria pd ON pd.id_combo = c.id_combo WHERE c.id_combo = ? AND pd.id_sucursal = ? AND pd.fecha_fin IS NULL LIMIT 1",
                        idArt, idSucursal
                    );
                    double pUnit = Double.parseDouble(info.get("precio").toString());
                    jdbc.update(
                        "INSERT INTO detalles_venta (id_venta, id_combo, descripcion, cantidad, precio_unitario, descuento, total_linea) VALUES (?, ?, ?, ?, ?, 0, ?)",
                        idVenta, idArt, info.get("nombre"), cant, pUnit, pUnit * cant
                    );
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("idVenta", idVenta, "mensaje", "Venta de dulcería sincronizada en MySQL"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
