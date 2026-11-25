package com.example.aperture.dao;

import android.content.ContentValues;
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

    public long insertar(Boleto boleto) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id_funcion", boleto.getIdFuncion());
        values.put("id_asiento", boleto.getIdAsiento());
        values.put("id_tarifa", boleto.getIdTarifa());
        if (boleto.getIdPromocion() != null) {
            values.put("id_promocion", boleto.getIdPromocion());
        } else {
            values.putNull("id_promocion");
        }
        if (boleto.getIdVenta() != null) {
            values.put("id_venta", boleto.getIdVenta());
        } else {
            values.putNull("id_venta");
        }
        values.put("precio_final", boleto.getPrecioFinal());
        values.put("estado", boleto.getEstado());
        return db.insert("boletos", null, values);
    }

    public ArrayList<Boleto> obtenerPorFuncion(int idFuncion) {
        return consultar(
                "SELECT * FROM boletos WHERE id_funcion = ?",
                new String[]{String.valueOf(idFuncion)}
        );
    }

    public ArrayList<Boleto> obtenerPorVenta(int idVenta) {
        return consultar(
                "SELECT * FROM boletos WHERE id_venta = ?",
                new String[]{String.valueOf(idVenta)}
        );
    }

    public Boleto obtenerPorId(int idBoleto) {
        ArrayList<Boleto> lista = consultar(
                "SELECT * FROM boletos WHERE id_boleto = ?",
                new String[]{String.valueOf(idBoleto)}
        );
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int cambiarEstado(int idBoleto, String estado) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("estado", estado);
        return db.update(
                "boletos",
                values,
                "id_boleto = ?",
                new String[]{String.valueOf(idBoleto)}
        );
    }

    private ArrayList<Boleto> consultar(String sql, String[] argumentos) {
        ArrayList<Boleto> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, argumentos)) {
            while (cursor.moveToNext()) {
                int idxPromo = cursor.getColumnIndexOrThrow("id_promocion");
                int idxVenta = cursor.getColumnIndexOrThrow("id_venta");
                Integer idPromo = cursor.isNull(idxPromo) ? null : cursor.getInt(idxPromo);
                Integer idVenta = cursor.isNull(idxVenta) ? null : cursor.getInt(idxVenta);

                lista.add(new Boleto(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_boleto")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_funcion")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_asiento")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_tarifa")),
                        idPromo,
                        idVenta,
                        cursor.getDouble(cursor.getColumnIndexOrThrow("precio_final")),
                        cursor.getString(cursor.getColumnIndexOrThrow("estado"))
                ));
            }
        }
        return lista;
    }
}
