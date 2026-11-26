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
        values.put("nombre_producto", producto.getNombre());
        values.put("descripcion", producto.getDescripcion());
        values.put("categoria", "GENERAL");
        values.put("activo", producto.isActivo() ? 1 : 0);
        long id = db.insert("productos", null, values);
        if (id > 0) {
            ContentValues precio = new ContentValues();
            precio.put("id_sucursal", 1);
            precio.put("id_producto", id);
            precio.put("precio", producto.getPrecio());
            precio.put("fecha_inicio", "2026-01-01");
            db.insert("precios_dulceria", null, precio);
        }
        return id;
    }

    public ArrayList<Producto> obtenerTodos() {
        return consultar(
                "SELECT p.id_producto, p.nombre_producto, p.descripcion, COALESCE(pd.precio, 0) AS precio, p.activo " +
                "FROM productos p LEFT JOIN precios_dulceria pd ON pd.id_producto = p.id_producto AND pd.fecha_fin IS NULL " +
                "ORDER BY p.nombre_producto", null);
    }

    public ArrayList<Producto> obtenerActivos() {
        return consultar(
                "SELECT p.id_producto, p.nombre_producto, p.descripcion, COALESCE(pd.precio, 0) AS precio, p.activo " +
                "FROM productos p LEFT JOIN precios_dulceria pd ON pd.id_producto = p.id_producto AND pd.fecha_fin IS NULL " +
                "WHERE p.activo = 1 ORDER BY p.nombre_producto", null);
    }

    private ArrayList<Producto> consultar(String sql, String[] args) {
        ArrayList<Producto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                lista.add(new Producto(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3),
                        cursor.getInt(4) == 1
                ));
            }
        }
        return lista;
    }
}
