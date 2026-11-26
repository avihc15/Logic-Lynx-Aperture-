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
        values.put("id_sucursal", 1);
        values.put("nombre_precio", tarifa.getNombre());
        values.put("cantidad", tarifa.getPrecio());
        values.put("fecha_inicio", "2026-01-01");
        values.put("activo", tarifa.isActivo() ? 1 : 0);
        return db.insert("precios_boletos", null, values);
    }

    public ArrayList<TarifaBoleto> obtenerTodas() {
        return consultar("SELECT * FROM precios_boletos ORDER BY nombre_precio", null);
    }

    public ArrayList<TarifaBoleto> obtenerActivas() {
        return consultar("SELECT * FROM precios_boletos WHERE activo = 1 ORDER BY id_precio_boleto", null);
    }

    private ArrayList<TarifaBoleto> consultar(String sql, String[] args) {
        ArrayList<TarifaBoleto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                lista.add(new TarifaBoleto(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_precio_boleto")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre_precio")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("cantidad")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre_precio")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
                ));
            }
        }
        return lista;
    }
}
