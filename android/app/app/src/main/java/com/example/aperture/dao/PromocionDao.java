package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Promocion;
import java.util.ArrayList;

public class PromocionDao {
    private final DatabaseHelper dbHelper;

    public PromocionDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Promocion promocion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", promocion.getNombre());
        values.put("descripcion", promocion.getDescripcion());
        values.put("tipo", promocion.getTipo());
        values.put("valor", promocion.getValor());
        values.put("fecha_inicio", promocion.getFechaInicio());
        values.put("fecha_fin", promocion.getFechaFin());
        values.put("activo", promocion.isActivo() ? 1 : 0);
        return db.insert("promociones", null, values);
    }

    public ArrayList<Promocion> obtenerTodas() {
        return consultar("SELECT * FROM promociones ORDER BY fecha_inicio DESC", null);
    }

    public ArrayList<Promocion> obtenerActivas() {
        return consultar("SELECT * FROM promociones WHERE activo = 1 ORDER BY nombre", null);
    }

    public Promocion obtenerPorId(int id) {
        ArrayList<Promocion> lista = consultar(
                "SELECT * FROM promociones WHERE id_promocion = ?",
                new String[]{String.valueOf(id)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(Promocion promocion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", promocion.getNombre());
        values.put("descripcion", promocion.getDescripcion());
        values.put("tipo", promocion.getTipo());
        values.put("valor", promocion.getValor());
        values.put("fecha_inicio", promocion.getFechaInicio());
        values.put("fecha_fin", promocion.getFechaFin());
        return db.update(
                "promociones",
                values,
                "id_promocion = ?",
                new String[]{String.valueOf(promocion.getIdPromocion())}
        );
    }

    public int cambiarEstado(int id, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update(
                "promociones",
                values,
                "id_promocion = ?",
                new String[]{String.valueOf(id)}
        );
    }

    private ArrayList<Promocion> consultar(String sql, String[] argumentos) {
        ArrayList<Promocion> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new Promocion(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_promocion")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                        cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("valor")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fecha_inicio")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fecha_fin")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
                ));
            }
        }
        return lista;
    }
}
