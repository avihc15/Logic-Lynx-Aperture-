package com.logiclynx.aperture.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/funciones")
@CrossOrigin(origins = "*")
public class FuncionRestController {

    private final JdbcTemplate jdbc;

    public FuncionRestController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return jdbc.queryForList(
            "SELECT f.id_funcion, f.id_cartelera, f.id_pelicula, f.id_sala, p.titulo AS pelicula, " +
            "sa.nombre_sala AS sala, f.fecha_inicio, f.fecha_fin, f.estado " +
            "FROM funciones_cine f " +
            "JOIN peliculas p ON p.id_pelicula = f.id_pelicula " +
            "JOIN salas sa ON sa.id_sala = f.id_sala " +
            "ORDER BY f.id_funcion DESC"
        );
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            int idCartelera = Integer.parseInt(body.getOrDefault("idCartelera", body.getOrDefault("id_cartelera", 8)).toString());
            int idPelicula = Integer.parseInt(body.getOrDefault("idPelicula", body.get("id_pelicula")).toString());
            int idSala = Integer.parseInt(body.getOrDefault("idSala", body.get("id_sala")).toString());
            String fechaInicio = body.getOrDefault("fechaInicio", body.get("fecha_inicio")).toString();
            if (fechaInicio.length() == 16) fechaInicio += ":00";

            String fechaFin = body.containsKey("fecha_fin") && body.get("fecha_fin") != null
                ? body.get("fecha_fin").toString()
                : calcularFechaFin(fechaInicio);

            jdbc.update("INSERT IGNORE INTO cartelera_pelicula (id_cartelera, id_pelicula) VALUES (?, ?)", idCartelera, idPelicula);
            jdbc.update(
                "INSERT INTO funciones_cine (id_cartelera, id_pelicula, id_sala, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, ?, ?)",
                idCartelera, idPelicula, idSala, fechaInicio, fechaFin,
                body.getOrDefault("estado", "PROGRAMADA").toString().toUpperCase()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Función programada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String calcularFechaFin(String fechaInicio) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dt = LocalDateTime.parse(fechaInicio, fmt);
        return dt.plusMinutes(140).format(fmt);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            String fechaInicio = body.get("fecha_inicio").toString();
            if (fechaInicio.length() == 16) fechaInicio += ":00";
            String fechaFin = calcularFechaFin(fechaInicio);
            int rows = jdbc.update(
                "UPDATE funciones_cine SET id_sala = ?, fecha_inicio = ?, fecha_fin = ?, estado = ? WHERE id_funcion = ?",
                Integer.parseInt(body.get("id_sala").toString()),
                fechaInicio,
                fechaFin,
                body.getOrDefault("estado", "PROGRAMADA").toString().toUpperCase(),
                id
            );
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Función actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable int id) {
        try {
            int rows = jdbc.update("DELETE FROM funciones_cine WHERE id_funcion = ?", id);
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Función eliminada correctamente"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Integridad referencial: No se puede eliminar la función porque ya tiene boletos vendidos."));
        }
    }
}
