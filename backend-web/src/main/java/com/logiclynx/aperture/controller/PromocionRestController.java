package com.logiclynx.aperture.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promociones")
@CrossOrigin(origins = "*")
public class PromocionRestController {

    private final JdbcTemplate jdbc;

    public PromocionRestController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return jdbc.queryForList("SELECT * FROM promociones ORDER BY id_promocion DESC");
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            String nombre = body.getOrDefault("nombrePromocion", body.get("nombre_promocion")).toString();
            String desc = body.getOrDefault("descripcion", "").toString();
            String tipo = body.getOrDefault("tipoDescuento", body.getOrDefault("tipo_descuento", "PORCENTAJE")).toString();
            double valor = Double.parseDouble(body.getOrDefault("valorDescuento", body.get("valor_descuento")).toString());
            String fIni = body.getOrDefault("fechaInicio", body.get("fecha_inicio")).toString();
            String fFin = body.getOrDefault("fechaFin", body.get("fecha_fin")).toString();
            if (fIni.length() == 10) fIni += " 00:00:00";
            if (fFin.length() == 10) fFin += " 23:59:59";

            jdbc.update(
                "INSERT INTO promociones (nombre_promocion, descripcion, tipo_descuento, valor_descuento, fecha_inicio, fecha_fin, activo) VALUES (?, ?, ?, ?, ?, ?, ?)",
                nombre, desc, tipo, valor, fIni, fFin, 1
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Promoción registrada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            String fIni = body.get("fecha_inicio").toString();
            String fFin = body.get("fecha_fin").toString();
            if (fIni.length() == 10) fIni += " 00:00:00";
            if (fFin.length() == 10) fFin += " 23:59:59";
            int rows = jdbc.update(
                "UPDATE promociones SET nombre_promocion = ?, descripcion = ?, tipo_descuento = ?, valor_descuento = ?, fecha_inicio = ?, fecha_fin = ?, activo = ? WHERE id_promocion = ?",
                body.get("nombre_promocion"),
                body.getOrDefault("descripcion", ""),
                body.getOrDefault("tipo_descuento", "PORCENTAJE"),
                Double.parseDouble(body.get("valor_descuento").toString()),
                fIni, fFin,
                Boolean.TRUE.equals(body.getOrDefault("activo", true)) ? 1 : 0,
                id
            );
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Promoción actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable int id) {
        try {
            int rows = jdbc.update("DELETE FROM promociones WHERE id_promocion = ?", id);
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Promoción eliminada correctamente"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Integridad referencial: No se puede eliminar la promoción porque está vinculada a registros."));
        }
    }
}
