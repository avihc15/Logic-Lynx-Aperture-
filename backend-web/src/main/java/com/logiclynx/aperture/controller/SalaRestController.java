package com.logiclynx.aperture.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salas")
@CrossOrigin(origins = "*")
public class SalaRestController {

    private final JdbcTemplate jdbc;

    public SalaRestController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return jdbc.queryForList(
            "SELECT sa.*, su.nombre_sucursal FROM salas sa JOIN sucursales su ON su.id_sucursal = sa.id_sucursal ORDER BY sa.id_sala"
        );
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            jdbc.update(
                "INSERT INTO salas (id_sucursal, nombre_sala, capacidad, tipo_sala, activo) VALUES (?, ?, ?, ?, ?)",
                Integer.parseInt(body.getOrDefault("id_sucursal", 1).toString()),
                body.get("nombre_sala"),
                Integer.parseInt(body.get("capacidad").toString()),
                body.getOrDefault("tipo_sala", "ESTANDAR"),
                Boolean.TRUE.equals(body.getOrDefault("activo", true)) ? 1 : 0
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Sala registrada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            int rows = jdbc.update(
                "UPDATE salas SET id_sucursal = ?, nombre_sala = ?, capacidad = ?, tipo_sala = ?, activo = ? WHERE id_sala = ?",
                Integer.parseInt(body.getOrDefault("id_sucursal", 1).toString()),
                body.get("nombre_sala"),
                Integer.parseInt(body.get("capacidad").toString()),
                body.getOrDefault("tipo_sala", "ESTANDAR"),
                Boolean.TRUE.equals(body.getOrDefault("activo", true)) ? 1 : 0,
                id
            );
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Sala actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable int id) {
        try {
            int rows = jdbc.update("DELETE FROM salas WHERE id_sala = ?", id);
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Sala eliminada correctamente"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Integridad referencial: No se puede eliminar la sala porque tiene asientos o funciones vinculadas."));
        }
    }
}
