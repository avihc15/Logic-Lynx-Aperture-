package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Pelicula;
import java.util.ArrayList;

public class PeliculaDao {
    private final DatabaseHelper dbHelper;

    public PeliculaDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Pelicula pelicula) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", pelicula.getTitulo());
        values.put("duracion_minutos", pelicula.getDuracion());
        values.put("clasificacion", pelicula.getClasificacion());
        values.put("genero", pelicula.getGenero());
        values.put("sinopsis", pelicula.getSinopsis());
        values.put("idioma_original", "Español");
        values.put("activo", pelicula.isActivo() ? 1 : 0);
        return db.insert("peliculas", null, values);
    }

    public ArrayList<Pelicula> obtenerTodas() {
        return consultar("SELECT * FROM peliculas ORDER BY titulo", null);
    }

    public ArrayList<Pelicula> obtenerActivas() {
        return consultar("SELECT * FROM peliculas WHERE activo = 1 ORDER BY titulo", null);
    }

    public Pelicula obtenerPorId(int id) {
        ArrayList<Pelicula> lista = consultar("SELECT * FROM peliculas WHERE id_pelicula = ?", new String[]{String.valueOf(id)});
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(Pelicula pelicula) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("titulo", pelicula.getTitulo());
        values.put("duracion_minutos", pelicula.getDuracion());
        values.put("clasificacion", pelicula.getClasificacion());
        values.put("genero", pelicula.getGenero());
        values.put("sinopsis", pelicula.getSinopsis());
        return db.update("peliculas", values, "id_pelicula = ?", new String[]{String.valueOf(pelicula.getIdPelicula())});
    }

    public int cambiarEstado(int id, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update("peliculas", values, "id_pelicula = ?", new String[]{String.valueOf(id)});
    }

    private ArrayList<Pelicula> consultar(String sql, String[] args) {
        ArrayList<Pelicula> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                lista.add(new Pelicula(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_pelicula")),
                        cursor.getString(cursor.getColumnIndexOrThrow("titulo")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("duracion_minutos")),
                        cursor.getString(cursor.getColumnIndexOrThrow("clasificacion")),
                        cursor.getString(cursor.getColumnIndexOrThrow("genero")),
                        cursor.getString(cursor.getColumnIndexOrThrow("sinopsis")),
                        cursor.getString(cursor.getColumnIndexOrThrow("idioma_original")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
                ));
            }
        }
        return lista;
    }
}
