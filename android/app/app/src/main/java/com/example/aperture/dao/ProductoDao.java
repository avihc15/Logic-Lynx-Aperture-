package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Producto;
import java.util.ArrayList;

public class ProductoDao {
    private final DatabaseHelper dbHelper;

    public ProductoDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Producto producto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", producto.getNombre());
        values.put("descripcion", producto.getDescripcion());
        values.put("precio", producto.getPrecio());
        values.put("activo", producto.isActivo() ? 1 : 0);
        return db.insert("productos", null, values);
    }

    public ArrayList<Producto> obtenerTodos() {
        return consultar("SELECT * FROM productos ORDER BY nombre", null);
    }

    public ArrayList<Producto> obtenerActivos() {
        return consultar("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre", null);
    }

    public Producto obtenerPorId(int id) {
        ArrayList<Producto> lista = consultar(
                "SELECT * FROM productos WHERE id_producto = ?",
                new String[]{String.valueOf(id)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(Producto producto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", producto.getNombre());
        values.put("descripcion", producto.getDescripcion());
        values.put("precio", producto.getPrecio());
        return db.update(
                "productos",
                values,
                "id_producto = ?",
                new String[]{String.valueOf(producto.getIdProducto())}
        );
    }

    public int cambiarEstado(int id, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update(
                "productos",
                values,
                "id_producto = ?",
                new String[]{String.valueOf(id)}
        );
    }

    private ArrayList<Producto> consultar(String sql, String[] argumentos) {
        ArrayList<Producto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new Producto(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_producto")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("precio")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
                ));
            }
        }
        return lista;
    }
}
