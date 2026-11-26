package com.example.aperture.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Promocion;
import java.util.ArrayList;

public class PromocionDao {
    private final DatabaseHelper dbHelper;

    public PromocionDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insertar(Promocion promocion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre_promocion", promocion.getNombre());
        values.put("descripcion", promocion.getDescripcion());
        values.put("tipo_descuento", mapTipoToDb(promocion.getTipo()));
        values.put("valor_descuento", promocion.getValor());
        values.put("fecha_inicio", promocion.getFechaInicio());
        values.put("fecha_fin", promocion.getFechaFin());
        values.put("activo", promocion.isActivo() ? 1 : 0);
        return db.insert("promociones", null, values);
    }

    private String mapTipoToDb(String tipo) {
        if ("Porcentaje".equalsIgnoreCase(tipo) || "PORCENTAJE".equalsIgnoreCase(tipo)) return "PORCENTAJE";
        return "CANTIDAD_FIJA";
    }

    public ArrayList<Promocion> obtenerTodas() {
        return consultar("SELECT * FROM promociones ORDER BY fecha_inicio DESC", null);
    }

    public ArrayList<Promocion> obtenerActivas() {
        return consultar("SELECT * FROM promociones WHERE activo = 1 ORDER BY nombre_promocion", null);
    }

    public int actualizar(Promocion promocion) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre_promocion", promocion.getNombre());
        values.put("descripcion", promocion.getDescripcion());
        values.put("tipo_descuento", mapTipoToDb(promocion.getTipo()));
        values.put("valor_descuento", promocion.getValor());
        values.put("fecha_inicio", promocion.getFechaInicio());
        values.put("fecha_fin", promocion.getFechaFin());
        return db.update("promociones", values, "id_promocion = ?", new String[]{String.valueOf(promocion.getIdPromocion())});
    }

    public int cambiarEstado(int id, boolean activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activo", activo ? 1 : 0);
        return db.update("promociones", values, "id_promocion = ?", new String[]{String.valueOf(id)});
    }

    private ArrayList<Promocion> consultar(String sql, String[] args) {
        ArrayList<Promocion> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, args)) {
            while (cursor.moveToNext()) {
                String tipoDb = cursor.getString(cursor.getColumnIndexOrThrow("tipo_descuento"));
                String tipoUi = "PORCENTAJE".equalsIgnoreCase(tipoDb) ? "Porcentaje" : "Monto Fijo";
                String fIni = cursor.getString(cursor.getColumnIndexOrThrow("fecha_inicio"));
                String fFin = cursor.getString(cursor.getColumnIndexOrThrow("fecha_fin"));
                if (fIni != null && fIni.length() > 10) fIni = fIni.substring(0, 10);
                if (fFin != null && fFin.length() > 10) fFin = fFin.substring(0, 10);
                lista.add(new Promocion(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_promocion")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre_promocion")),
                        cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                        tipoUi,
                        cursor.getDouble(cursor.getColumnIndexOrThrow("valor_descuento")),
                        fIni,
                        fFin,
                        cursor.getInt(cursor.getColumnIndexOrThrow("activo")) == 1
                ));
            }
        }
        return lista;
    }
}
