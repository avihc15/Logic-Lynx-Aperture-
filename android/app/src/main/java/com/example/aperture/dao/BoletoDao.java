package com.example.aperture.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Boleto;
import java.util.ArrayList;

public class BoletoDao {
    private final DatabaseHelper dbHelper;

    public BoletoDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public ArrayList<Boleto> obtenerPorFuncion(int idFuncion) {
        ArrayList<Boleto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT id_boleto, id_funcion, id_asiento, id_precio_boleto, id_promocion, id_venta, precio_pagado, estado FROM boletos WHERE id_funcion = ?",
                new String[]{String.valueOf(idFuncion)})) {
            while (cursor.moveToNext()) {
                Integer idPromo = cursor.isNull(4) ? null : cursor.getInt(4);
                Integer idVenta = cursor.isNull(5) ? null : cursor.getInt(5);
                lista.add(new Boleto(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getInt(3),
                        idPromo,
                        idVenta,
                        cursor.getDouble(6),
                        cursor.getString(7)
                ));
            }
        }
        return lista;
    }
}
