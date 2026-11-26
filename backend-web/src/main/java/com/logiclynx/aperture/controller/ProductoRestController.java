package com.logiclynx.aperture.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoRestController {

    private final JdbcTemplate jdbc;

    public ProductoRestController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return jdbc.queryForList(
            "SELECT p.*, COALESCE(pd.precio, 0) AS precio, COALESCE(e.cantidad_actual, 0) AS stock " +
            "FROM productos p " +
            "LEFT JOIN precios_dulceria pd ON pd.id_producto = p.id_producto AND pd.id_sucursal = 1 AND pd.fecha_fin IS NULL " +
            "LEFT JOIN existencias e ON e.id_producto = p.id_producto AND e.id_sucursal = 1 " +
            "ORDER BY p.id_producto DESC"
        );
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            jdbc.update(
                "INSERT INTO productos (nombre_producto, descripcion, categoria, unidad_medida, activo) VALUES (?, ?, ?, ?, ?)",
                body.get("nombre_producto"),
                body.getOrDefault("descripcion", ""),
                body.getOrDefault("categoria", "BOTANAS"),
                body.getOrDefault("unidad_medida", "PIEZA"),
                1
            );
            Integer idProd = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
            double precio = Double.parseDouble(body.getOrDefault("precio", 50).toString());
            double stock = Double.parseDouble(body.getOrDefault("stock", 50).toString());

            jdbc.update("INSERT INTO precios_dulceria (id_sucursal, id_producto, precio, fecha_inicio) VALUES (1, ?, ?, CURDATE())", idProd, precio);
            jdbc.update("INSERT INTO existencias (id_sucursal, id_producto, cantidad_actual, cantidad_minima) VALUES (1, ?, ?, 10)", idProd, stock);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Producto registrado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            int rows = jdbc.update(
                "UPDATE productos SET nombre_producto = ?, descripcion = ?, categoria = ?, activo = ? WHERE id_producto = ?",
                body.get("nombre_producto"),
                body.getOrDefault("descripcion", ""),
                body.getOrDefault("categoria", "BOTANAS"),
                Boolean.TRUE.equals(body.getOrDefault("activo", true)) ? 1 : 0,
                id
            );
            if (rows == 0) return ResponseEntity.notFound().build();
            if (body.containsKey("precio")) {
                jdbc.update("UPDATE precios_dulceria SET precio = ? WHERE id_producto = ? AND id_sucursal = 1 AND fecha_fin IS NULL",
                    Double.parseDouble(body.get("precio").toString()), id);
            }
            return ResponseEntity.ok(Map.of("mensaje", "Producto actualizado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable int id) {
        try {
            jdbc.update("DELETE FROM existencias WHERE id_producto = ? AND NOT EXISTS (SELECT 1 FROM movimientos_inventario m JOIN existencias e ON m.id_existencia = e.id_existencia WHERE e.id_producto = ?)", id, id);
            jdbc.update("DELETE FROM precios_dulceria WHERE id_producto = ?", id);
            int rows = jdbc.update("DELETE FROM productos WHERE id_producto = ?", id);
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Producto eliminado correctamente"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Integridad referencial: No se puede eliminar el producto porque tiene movimientos de inventario, combos o ventas asociadas."));
        }
    }
}
