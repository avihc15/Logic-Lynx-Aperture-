package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Cartelera;
import java.util.ArrayList;

public class CarteleraDao {
    private final DatabaseHelper dbHelper;

    public CarteleraDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Cartelera cartelera) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_sucursal", cartelera.getIdSucursal());
        values.put("nombre", cartelera.getNombre());
        values.put("fecha_inicio", cartelera.getFechaInicio());
        values.put("fecha_fin", cartelera.getFechaFin());
        values.put("activo", cartelera.isActivo() ? 1 : 0);
        return db.insert("carteleras", null, values);
    }

    public ArrayList<Cartelera> obtenerTodas() {
        return consultar(
                "SELECT * FROM carteleras ORDER BY fecha_inicio DESC",
                null
        );
    }

    public ArrayList<Cartelera> obtenerActivasPorSucursal(int idSucursal) {
        return consultar(
                "SELECT * FROM carteleras WHERE id_sucursal = ? " +
                "AND activo = 1 ORDER BY fecha_inicio DESC",
                new String[]{String.valueOf(idSucursal)}
        );
    }

    public Cartelera obtenerPorId(int id) {
        ArrayList<Cartelera> lista = consultar(
                "SELECT * FROM carteleras WHERE id_cartelera = ?",
                new String[]{String.valueOf(id)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(Cartelera cartelera) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_sucursal", cartelera.getIdSucursal());
        values.put("nombre", cartelera.getNombre());
        values.put("fecha_inicio", cartelera.getFechaInicio());
        values.put("fecha_fin", cartelera.getFechaFin());
        return db.update(
                "carteleras",
                values,
                "id_cartelera = ?",
                new String[]{String.valueOf(cartelera.getIdCartelera())}
        );
    }

    public int cambiarEstado(int id, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update(
                "carteleras",
                values,
                "id_cartelera = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public boolean asociarPelicula(int idCartelera, int idPelicula) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_cartelera", idCartelera);
        values.put("id_pelicula", idPelicula);
        return db.insertWithOnConflict(
                "cartelera_pelicula",
                null,
                values,
                SQLiteDatabase.CONFLICT_IGNORE
        ) != -1;
    }

    public int quitarPelicula(int idCartelera, int idPelicula) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                "cartelera_pelicula",
                "id_cartelera = ? AND id_pelicula = ?",
                new String[]{
                        String.valueOf(idCartelera),
                        String.valueOf(idPelicula)
                }
        );
    }

    public ArrayList<Integer> obtenerPeliculas(int idCartelera) {
        ArrayList<Integer> peliculas = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT id_pelicula FROM cartelera_pelicula " +
                "WHERE id_cartelera = ?",
                new String[]{String.valueOf(idCartelera)})) {
            while (cursor.moveToNext()) {
                peliculas.add(cursor.getInt(0));
            }
        }
        return peliculas;
    }

    private ArrayList<Cartelera> consultar(String sql, String[] argumentos) {
        ArrayList<Cartelera> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new Cartelera(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_cartelera")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_sucursal")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fecha_inicio")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fecha_fin")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
                ));
            }
        }
        return lista;
    }
}
