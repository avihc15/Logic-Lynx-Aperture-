package com.logiclynx.aperture.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/peliculas")
@CrossOrigin(origins = "*")
public class PeliculaRestController {

    private final JdbcTemplate jdbc;

    public PeliculaRestController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return jdbc.queryForList("SELECT * FROM peliculas ORDER BY id_pelicula DESC");
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            jdbc.update(
                "INSERT INTO peliculas (titulo, sinopsis, duracion_minutos, clasificacion, genero, idioma_original, activo) VALUES (?, ?, ?, ?, ?, ?, ?)",
                body.get("titulo"),
                body.getOrDefault("sinopsis", ""),
                Integer.parseInt(body.get("duracion_minutos").toString()),
                body.getOrDefault("clasificacion", "B"),
                body.getOrDefault("genero", "Drama"),
                body.getOrDefault("idioma_original", "Español"),
                Boolean.TRUE.equals(body.getOrDefault("activo", true)) ? 1 : 0
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Película registrada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            int rows = jdbc.update(
                "UPDATE peliculas SET titulo = ?, sinopsis = ?, duracion_minutos = ?, clasificacion = ?, genero = ?, idioma_original = ?, activo = ? WHERE id_pelicula = ?",
                body.get("titulo"),
                body.getOrDefault("sinopsis", ""),
                Integer.parseInt(body.get("duracion_minutos").toString()),
                body.getOrDefault("clasificacion", "B"),
                body.getOrDefault("genero", "Drama"),
                body.getOrDefault("idioma_original", "Español"),
                Boolean.TRUE.equals(body.getOrDefault("activo", true)) ? 1 : 0,
                id
            );
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Película actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable int id) {
        try {
            int rows = jdbc.update("DELETE FROM peliculas WHERE id_pelicula = ?", id);
            if (rows == 0) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(Map.of("mensaje", "Película eliminada correctamente"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Integridad referencial: No se puede eliminar la película porque está asociada a una cartelera o función."));
        }
    }
}
