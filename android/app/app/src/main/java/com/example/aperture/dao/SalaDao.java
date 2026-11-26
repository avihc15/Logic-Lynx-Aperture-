package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Sala;
import java.util.ArrayList;

public class SalaDao {
    private final DatabaseHelper dbHelper;

    public SalaDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Sala sala) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_sucursal", sala.getIdSucursal());
        values.put("nombre", sala.getNombre());
        values.put("capacidad", sala.getCapacidad());
        values.put("tipo", sala.getTipo());
        values.put("activo", sala.isActivo() ? 1 : 0);
        return db.insert("salas", null, values);
    }

    public ArrayList<Sala> obtenerTodas() {
        return consultar(
                "SELECT * FROM salas ORDER BY nombre", null
        );
    }

    public ArrayList<Sala> obtenerActivasPorSucursal(int idSucursal) {
        return consultar(
                "SELECT * FROM salas WHERE id_sucursal = ? " +
                "AND activo = 1 ORDER BY nombre",
                new String[]{String.valueOf(idSucursal)}
        );
    }

    public Sala obtenerPorId(int idSala) {
        ArrayList<Sala> lista = consultar(
                "SELECT * FROM salas WHERE id_sala = ?",
                new String[]{String.valueOf(idSala)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int actualizar(Sala sala) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_sucursal", sala.getIdSucursal());
        values.put("nombre", sala.getNombre());
        values.put("capacidad", sala.getCapacidad());
        values.put("tipo", sala.getTipo());
        return db.update(
                "salas",
                values,
                "id_sala = ?",
                new String[]{String.valueOf(sala.getIdSala())}
        );
    }

    public int cambiarEstado(int idSala, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update(
                "salas",
                values,
                "id_sala = ?",
                new String[]{String.valueOf(idSala)}
        );
    }

    private ArrayList<Sala> consultar(String sql, String[] argumentos) {
        ArrayList<Sala> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                lista.add(new Sala(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_sala")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_sucursal")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("capacidad")),
                        cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
                ));
            }
        }
        return lista;
    }
}
