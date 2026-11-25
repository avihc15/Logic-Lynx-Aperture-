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
        values.put("cantidad", existencia.getCantidad());
        return db.insertWithOnConflict(
                "existencias",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public ArrayList<Existencia> obtenerPorSucursal(int idSucursal) {
        return consultar(
                "SELECT * FROM existencias WHERE id_sucursal = ?",
                new String[]{String.valueOf(idSucursal)}
        );
    }

    public Existencia obtenerPorSucursalYProducto(int idSucursal, int idProducto) {
        ArrayList<Existencia> lista = consultar(
                "SELECT * FROM existencias WHERE id_sucursal = ? AND id_producto = ?",
                new String[]{String.valueOf(idSucursal), String.valueOf(idProducto)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizarCantidad(int idExistencia, int nuevaCantidad) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cantidad", nuevaCantidad);
        return db.update(
                "existencias",
                values,
                "id_existencia = ?",
                new String[]{String.valueOf(idExistencia)}
        );
    }

    private ArrayList<Existencia> consultar(String sql, String[] argumentos) {
        ArrayList<Existencia> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new Existencia(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_existencia")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_sucursal")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_producto")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("cantidad"))
                ));
            }
        }
        return lista;
    }
}
