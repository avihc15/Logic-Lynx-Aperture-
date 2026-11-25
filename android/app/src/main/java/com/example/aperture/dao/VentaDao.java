package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Venta;
import java.util.ArrayList;

public class VentaDao {
    private final DatabaseHelper dbHelper;

    public VentaDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Venta venta) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_sucursal", venta.getIdSucursal());
        values.put("fecha_hora", venta.getFechaHora());
        values.put("total", venta.getTotal());
        values.put("estado", venta.getEstado());
        return db.insert("ventas", null, values);
    }

    public ArrayList<Venta> obtenerTodas() {
        return consultar("SELECT * FROM ventas ORDER BY fecha_hora DESC", null);
    }

    public ArrayList<Venta> obtenerPorSucursal(int idSucursal) {
        return consultar(
                "SELECT * FROM ventas WHERE id_sucursal = ? ORDER BY fecha_hora DESC",
                new String[]{String.valueOf(idSucursal)}
        );
    }

    public Venta obtenerPorId(int idVenta) {
        ArrayList<Venta> lista = consultar(
                "SELECT * FROM ventas WHERE id_venta = ?",
                new String[]{String.valueOf(idVenta)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizarTotal(int idVenta, double nuevoTotal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("total", nuevoTotal);
        return db.update(
                "ventas",
                values,
                "id_venta = ?",
                new String[]{String.valueOf(idVenta)}
        );
    }

    public int cambiarEstado(int idVenta, String estado) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("estado", estado);
        return db.update(
                "ventas",
                values,
                "id_venta = ?",
                new String[]{String.valueOf(idVenta)}
        );
    }

    private ArrayList<Venta> consultar(String sql, String[] argumentos) {
        ArrayList<Venta> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new Venta(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_venta")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_sucursal")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("total")),
                        cursor.getString(cursor.getColumnIndexOrThrow("estado"))
                ));
            }
        }
        return lista;
    }
}
