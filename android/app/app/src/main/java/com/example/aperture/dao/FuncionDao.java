package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Funcion;
import java.util.ArrayList;

public class FuncionDao {
    private final DatabaseHelper dbHelper;

    public FuncionDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Funcion funcion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_cartelera", funcion.getIdCartelera());
        values.put("id_pelicula", funcion.getIdPelicula());
        values.put("id_sala", funcion.getIdSala());
        values.put("fecha_hora", funcion.getFechaHora());
        values.put("idioma", funcion.getIdioma());
        values.put("formato", funcion.getFormato());
        values.put("estado", funcion.getEstado());
        return db.insert("funciones_cine", null, values);
    }

    public ArrayList<Funcion> obtenerTodas() {
        return consultar(
                "SELECT * FROM funciones_cine ORDER BY fecha_hora",
                null
        );
    }

    public ArrayList<Funcion> obtenerProgramadas() {
        return consultar(
                "SELECT * FROM funciones_cine " +
                "WHERE estado = 'Programada' " +
                "ORDER BY fecha_hora",
                null
        );
    }

    public ArrayList<Funcion> obtenerPorFecha(String fecha) {
        return consultar(
                "SELECT * FROM funciones_cine " +
                "WHERE fecha_hora LIKE ? ORDER BY fecha_hora",
                new String[]{fecha + "%"}
        );
    }

    public ArrayList<Funcion> obtenerPorCartelera(int idCartelera) {
        return consultar(
                "SELECT * FROM funciones_cine " +
                "WHERE id_cartelera = ? ORDER BY fecha_hora",
                new String[]{String.valueOf(idCartelera)}
        );
    }

    public ArrayList<Funcion> obtenerPorSala(int idSala) {
        return consultar(
                "SELECT * FROM funciones_cine " +
                "WHERE id_sala = ? ORDER BY fecha_hora",
                new String[]{String.valueOf(idSala)}
        );
    }

    public Funcion obtenerPorId(int idFuncion) {
        ArrayList<Funcion> lista = consultar(
                "SELECT * FROM funciones_cine WHERE id_funcion = ?",
                new String[]{String.valueOf(idFuncion)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(Funcion funcion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_cartelera", funcion.getIdCartelera());
        values.put("id_pelicula", funcion.getIdPelicula());
        values.put("id_sala", funcion.getIdSala());
        values.put("fecha_hora", funcion.getFechaHora());
        values.put("idioma", funcion.getIdioma());
        values.put("formato", funcion.getFormato());
        values.put("estado", funcion.getEstado());
        return db.update(
                "funciones_cine",
                values,
                "id_funcion = ?",
                new String[]{String.valueOf(funcion.getIdFuncion())}
        );
    }

    public int cambiarEstado(int idFuncion, String estado) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("estado", estado);
        return db.update(
                "funciones_cine",
                values,
                "id_funcion = ?",
                new String[]{String.valueOf(idFuncion)}
        );
    }

    private ArrayList<Funcion> consultar(String sql, String[] argumentos) {
        ArrayList<Funcion> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new Funcion(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_funcion")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_cartelera")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_pelicula")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_sala")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora")),
                        cursor.getString(cursor.getColumnIndexOrThrow("idioma")),
                        cursor.getString(cursor.getColumnIndexOrThrow("formato")),
                        cursor.getString(cursor.getColumnIndexOrThrow("estado"))
                ));
            }
        }
        return lista;
    }
}
