package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Existencia;
import java.util.ArrayList;

public class ExistenciaDao {
    private final DatabaseHelper dbHelper;

    public ExistenciaDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Existencia existencia) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_sucursal", existencia.getIdSucursal());
        values.put("id_producto", existencia.getIdProducto());
        values.put("cantidad_actual", existencia.getCantidad());
        return db.insertWithOnConflict("existencias", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Existencia obtenerPorSucursalYProducto(int idSucursal, int idProducto) {
        ArrayList<Existencia> lista = consultar(
                "SELECT id_existencia, id_sucursal, id_producto, cantidad_actual FROM existencias WHERE id_sucursal = ? AND id_producto = ?",
                new String[]{String.valueOf(idSucursal), String.valueOf(idProducto)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    private ArrayList<Existencia> consultar(String sql, String[] args) {
        ArrayList<Existencia> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                lista.add(new Existencia(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        (int) cursor.getDouble(3)
                ));
            }
        }
        return lista;
    }
}
