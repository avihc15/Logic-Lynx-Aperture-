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
        values.put("numero_asiento", asiento.getNumero());
        values.put("activo", 1);
        return db.insertWithOnConflict("asientos", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public ArrayList<Asiento> obtenerPorSala(int idSala) {
        ArrayList<Asiento> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT id_asiento, id_sala, fila, numero_asiento FROM asientos WHERE id_sala = ? AND activo = 1 ORDER BY fila, numero_asiento",
                new String[]{String.valueOf(idSala)})) {
            while (cursor.moveToNext()) {
                lista.add(new Asiento(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getInt(3),
                        "ESTANDAR"
                ));
            }
        }
        return lista;
    }
}
