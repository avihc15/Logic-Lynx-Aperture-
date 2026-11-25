package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.TarifaBoleto;
import java.util.ArrayList;

public class TarifaBoletoDao {
    private final DatabaseHelper dbHelper;

    public TarifaBoletoDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(TarifaBoleto tarifa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", tarifa.getNombre());
        values.put("precio", tarifa.getPrecio());
        values.put("descripcion", tarifa.getDescripcion());
        values.put("activo", tarifa.isActivo() ? 1 : 0);
        return db.insert("precios_boletos", null, values);
    }

    public ArrayList<TarifaBoleto> obtenerTodas() {
        return consultar("SELECT * FROM precios_boletos ORDER BY nombre", null);
    }

    public ArrayList<TarifaBoleto> obtenerActivas() {
        return consultar("SELECT * FROM precios_boletos WHERE activo = 1 ORDER BY nombre", null);
    }

    public TarifaBoleto obtenerPorId(int id) {
        ArrayList<TarifaBoleto> lista = consultar(
                "SELECT * FROM precios_boletos WHERE id_tarifa = ?",
                new String[]{String.valueOf(id)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(TarifaBoleto tarifa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", tarifa.getNombre());
        values.put("precio", tarifa.getPrecio());
        values.put("descripcion", tarifa.getDescripcion());
        return db.update(
                "precios_boletos",
                values,
                "id_tarifa = ?",
                new String[]{String.valueOf(tarifa.getIdTarifa())}
        );
    }

    public int cambiarEstado(int id, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update(
                "precios_boletos",
                values,
                "id_tarifa = ?",
                new String[]{String.valueOf(id)}
        );
    }

    private ArrayList<TarifaBoleto> consultar(String sql, String[] argumentos) {
        ArrayList<TarifaBoleto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new TarifaBoleto(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_tarifa")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("precio")),
                        cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
                ));
            }
        }
        return lista;
    }
}
