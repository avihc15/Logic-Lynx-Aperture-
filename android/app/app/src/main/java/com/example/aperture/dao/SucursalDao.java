package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Sucursal;
import java.util.ArrayList;

public class SucursalDao {
    private final DatabaseHelper dbHelper;

    public SucursalDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Sucursal sucursal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", sucursal.getNombre());
        values.put("direccion", sucursal.getDireccion());
        values.put("telefono", sucursal.getTelefono());
        values.put("activo", sucursal.isActivo() ? 1 : 0);
        return db.insert("sucursales", null, values);
    }

    public ArrayList<Sucursal> obtenerTodas() {
        ArrayList<Sucursal> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM sucursales ORDER BY nombre", null)) {
            while (cursor.moveToNext()) {
                lista.add(leerSucursal(cursor));
            }
        }
        return lista;
    }

    public ArrayList<Sucursal> obtenerActivas() {
        ArrayList<Sucursal> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM sucursales WHERE activo = 1 ORDER BY nombre",
                null)) {
            while (cursor.moveToNext()) {
                lista.add(leerSucursal(cursor));
            }
        }
        return lista;
    }

    public Sucursal obtenerPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM sucursales WHERE id_sucursal = ?",
                new String[]{String.valueOf(id)})) {
            if (cursor.moveToFirst()) {
                return leerSucursal(cursor);
            }
        }
        return null;
    }

    public int actualizar(Sucursal sucursal) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", sucursal.getNombre());
        values.put("direccion", sucursal.getDireccion());
        values.put("telefono", sucursal.getTelefono());
        return db.update(
                "sucursales",
                values,
                "id_sucursal = ?",
                new String[]{String.valueOf(sucursal.getIdSucursal())}
        );
    }

    public int cambiarEstado(int id, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update(
                "sucursales",
                values,
                "id_sucursal = ?",
                new String[]{String.valueOf(id)}
        );
    }

    private Sucursal leerSucursal(Cursor cursor) {
        return new Sucursal(
                cursor.getInt(cursor.getColumnIndexOrThrow("id_sucursal")),
                cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                cursor.getString(cursor.getColumnIndexOrThrow("direccion")),
                cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
        );
    }
}
