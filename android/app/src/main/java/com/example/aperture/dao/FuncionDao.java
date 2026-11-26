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
        String inicio = funcion.getFechaHora();
        if (inicio.length() == 16) inicio = inicio + ":00";
        values.put("fecha_inicio", inicio);
        values.put("fecha_fin", calcularFechaFin(inicio));
        values.put("estado", funcion.getEstado().toUpperCase());
        return db.insert("funciones_cine", null, values);
    }

    private String calcularFechaFin(String fechaInicio) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date d = sdf.parse(fechaInicio);
            if (d != null) {
                return sdf.format(new java.util.Date(d.getTime() + 135L * 60L * 1000L));
            }
        } catch (Exception ignored) {}
        return fechaInicio;
    }

    public ArrayList<Funcion> obtenerTodas() {
        return consultar("SELECT * FROM funciones_cine ORDER BY fecha_inicio", null);
    }

    public ArrayList<Funcion> obtenerProgramadas() {
        return consultar("SELECT * FROM funciones_cine WHERE UPPER(estado) = 'PROGRAMADA' ORDER BY fecha_inicio", null);
    }

    public Funcion obtenerPorId(int idFuncion) {
        ArrayList<Funcion> lista = consultar("SELECT * FROM funciones_cine WHERE id_funcion = ?", new String[]{String.valueOf(idFuncion)});
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(Funcion funcion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_cartelera", funcion.getIdCartelera());
        values.put("id_pelicula", funcion.getIdPelicula());
        values.put("id_sala", funcion.getIdSala());
        String inicio = funcion.getFechaHora();
        if (inicio.length() == 16) inicio = inicio + ":00";
        values.put("fecha_inicio", inicio);
        values.put("fecha_fin", calcularFechaFin(inicio));
        values.put("estado", funcion.getEstado().toUpperCase());
        return db.update("funciones_cine", values, "id_funcion = ?", new String[]{String.valueOf(funcion.getIdFuncion())});
    }

    public int cambiarEstado(int idFuncion, String estado) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("estado", estado.toUpperCase());
        return db.update("funciones_cine", values, "id_funcion = ?", new String[]{String.valueOf(idFuncion)});
    }

    private ArrayList<Funcion> consultar(String sql, String[] args) {
        ArrayList<Funcion> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                lista.add(new Funcion(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_funcion")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_cartelera")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_pelicula")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_sala")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fecha_inicio")),
                        "Español",
                        "2D",
                        cursor.getString(cursor.getColumnIndexOrThrow("estado"))
                ));
            }
        }
        return lista;
    }
}
