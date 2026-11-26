package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Combo;
import java.util.ArrayList;

public class ComboDao {
    private final DatabaseHelper dbHelper;

    public ComboDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Combo combo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre_combo", combo.getNombre());
        values.put("descripcion", combo.getDescripcion());
        values.put("activo", combo.isActivo() ? 1 : 0);
        long id = db.insert("combos", null, values);
        if (id > 0) {
            ContentValues precio = new ContentValues();
            precio.put("id_sucursal", 1);
            precio.put("id_combo", id);
            precio.put("precio", combo.getPrecio());
            precio.put("fecha_inicio", "2026-01-01");
            db.insert("precios_dulceria", null, precio);
        }
        return id;
    }

    public ArrayList<Combo> obtenerTodos() {
        return consultar(
                "SELECT c.id_combo, c.nombre_combo, c.descripcion, COALESCE(pd.precio, 0) AS precio, c.activo " +
                "FROM combos c LEFT JOIN precios_dulceria pd ON pd.id_combo = c.id_combo AND pd.fecha_fin IS NULL " +
                "ORDER BY c.nombre_combo", null);
    }

    public ArrayList<Combo> obtenerActivos() {
        return consultar(
                "SELECT c.id_combo, c.nombre_combo, c.descripcion, COALESCE(pd.precio, 0) AS precio, c.activo " +
                "FROM combos c LEFT JOIN precios_dulceria pd ON pd.id_combo = c.id_combo AND pd.fecha_fin IS NULL " +
                "WHERE c.activo = 1 ORDER BY c.nombre_combo", null);
    }

    public boolean agregarProductoACombo(int idCombo, int idProducto, int cantidad) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_combo", idCombo);
        values.put("id_producto", idProducto);
        values.put("cantidad", cantidad);
        return db.insertWithOnConflict("combo_productos", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1;
    }

    private ArrayList<Combo> consultar(String sql, String[] args) {
        ArrayList<Combo> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                lista.add(new Combo(
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
