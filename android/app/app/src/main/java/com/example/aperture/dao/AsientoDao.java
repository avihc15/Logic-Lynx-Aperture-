package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Asiento;
import java.util.ArrayList;

public class AsientoDao {
    private final DatabaseHelper dbHelper;

    public AsientoDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Asiento asiento) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_sala", asiento.getIdSala());
        values.put("fila", asiento.getFila());
        values.put("numero", asiento.getNumero());
        values.put("tipo", asiento.getTipo());
        return db.insertWithOnConflict("asientos", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public ArrayList<Asiento> obtenerPorSala(int idSala) {
        ArrayList<Asiento> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT * FROM asientos WHERE id_sala = ? ORDER BY fila, numero",
                new String[]{String.valueOf(idSala)})) {
            while (cursor.moveToNext()) {
                lista.add(new Asiento(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_asiento")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_sala")),
                        cursor.getString(cursor.getColumnIndexOrThrow("fila")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("numero")),
                        cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                ));
            }
        }
        return lista;
    }
}
