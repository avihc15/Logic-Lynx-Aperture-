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
        values.put("nombre", combo.getNombre());
        values.put("descripcion", combo.getDescripcion());
        values.put("precio", combo.getPrecio());
        values.put("activo", combo.isActivo() ? 1 : 0);
        return db.insert("combos", null, values);
    }

    public ArrayList<Combo> obtenerTodos() {
        return consultar("SELECT * FROM combos ORDER BY nombre", null);
    }

    public ArrayList<Combo> obtenerActivos() {
        return consultar("SELECT * FROM combos WHERE activo = 1 ORDER BY nombre", null);
    }

    public Combo obtenerPorId(int id) {
        ArrayList<Combo> lista = consultar(
                "SELECT * FROM combos WHERE id_combo = ?",
                new String[]{String.valueOf(id)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(Combo combo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", combo.getNombre());
        values.put("descripcion", combo.getDescripcion());
        values.put("precio", combo.getPrecio());
        return db.update(
                "combos",
                values,
                "id_combo = ?",
                new String[]{String.valueOf(combo.getIdCombo())}
        );
    }

    public int cambiarEstado(int id, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update(
                "combos",
                values,
                "id_combo = ?",
                new String[]{String.valueOf(id)}
        );
    }

    public boolean agregarProductoACombo(int idCombo, int idProducto, int cantidad) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_combo", idCombo);
        values.put("id_producto", idProducto);
        values.put("cantidad", cantidad);
        return db.insertWithOnConflict(
                "combo_productos",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        ) != -1;
    }

    private ArrayList<Combo> consultar(String sql, String[] argumentos) {
        ArrayList<Combo> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new Combo(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_combo")),
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
