package com.example.aperture.dao;

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

    public ArrayList<Venta> obtenerTodas() {
        ArrayList<Venta> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT id_venta, id_sucursal, fecha_venta, total, estado FROM ventas ORDER BY fecha_venta DESC", null)) {
            while (cursor.moveToNext()) {
                lista.add(new Venta(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getString(4)
                ));
            }
        }
        return lista;
    }
}
