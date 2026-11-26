package com.example.aperture.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.utils.Constantes;
import com.example.aperture.utils.FechaUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VentaService {
    private final DatabaseHelper dbHelper;

    public VentaService(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long registrarVentaBoletos(int idSucursal, List<BoletoSolicitud> boletos) {
        if (idSucursal <= 0) {
            throw new IllegalArgumentException("Sucursal no válida.");
        }
        if (boletos == null || boletos.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar al menos un boleto.");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            Set<String> asientosSeleccionados = new HashSet<>();
            BigDecimal total = BigDecimal.ZERO;

            for (BoletoSolicitud boleto : boletos) {
                validarSolicitud(boleto);
                String clave = boleto.idFuncion + "-" + boleto.idAsiento;
                if (!asientosSeleccionados.add(clave)) {
                    throw new IllegalArgumentException("No puedes seleccionar el mismo asiento dos veces.");
                }
                validarFuncion(db, boleto.idFuncion);
                validarAsiento(db, boleto.idFuncion, boleto.idAsiento);
                validarDisponibilidad(db, boleto.idFuncion, boleto.idAsiento);
                total = total.add(boleto.precioFinal);
            }

            total = total.setScale(2, RoundingMode.HALF_UP);

            ContentValues venta = new ContentValues();
            venta.put("id_sucursal", idSucursal);
            venta.put("tipo_venta", Constantes.TIPO_VENTA_TAQUILLA);
            venta.put("fecha_venta", FechaUtils.obtenerFechaHoraActual());
            venta.put("subtotal", total.doubleValue());
            venta.put("descuento", 0.0);
            venta.put("total", total.doubleValue());
            venta.put("metodo_pago", "EFECTIVO");
            venta.put("estado", Constantes.VENTA_COMPLETADA);

            long idVenta = db.insertOrThrow("ventas", null, venta);

            for (BoletoSolicitud boleto : boletos) {
                ContentValues valoresBoleto = new ContentValues();
                valoresBoleto.put("id_funcion", boleto.idFuncion);
                valoresBoleto.put("id_asiento", boleto.idAsiento);
                valoresBoleto.put("id_precio_boleto", boleto.idTarifa);
                if (boleto.idPromocion != null) {
                    valoresBoleto.put("id_promocion", boleto.idPromocion);
                } else {
                    valoresBoleto.putNull("id_promocion");
                }
                valoresBoleto.put("id_venta", idVenta);
                valoresBoleto.put("precio_pagado", boleto.precioFinal.doubleValue());
                valoresBoleto.put("estado", Constantes.BOLETO_VENDIDO);

                long idBoleto = db.insertOrThrow("boletos", null, valoresBoleto);

                ContentValues detalle = new ContentValues();
                detalle.put("id_venta", idVenta);
                detalle.putNull("id_producto");
                detalle.putNull("id_combo");
                detalle.put("id_boleto", idBoleto);
                detalle.put("descripcion", "Boleto función " + boleto.idFuncion);
                detalle.put("cantidad", 1);
                detalle.put("precio_unitario", boleto.precioFinal.doubleValue());
                detalle.put("descuento", 0.0);
                detalle.put("total_linea", boleto.precioFinal.doubleValue());

                db.insertOrThrow("detalles_venta", null, detalle);
            }

            db.setTransactionSuccessful();
            return idVenta;
        } finally {
            db.endTransaction();
        }
    }

    private void validarSolicitud(BoletoSolicitud boleto) {
        if (boleto == null || boleto.idFuncion <= 0 || boleto.idAsiento <= 0 || boleto.idTarifa <= 0) {
            throw new IllegalArgumentException("Los datos del boleto son incorrectos.");
        }
        if (boleto.precioFinal == null || boleto.precioFinal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del boleto no es válido.");
        }
    }

    private void validarFuncion(SQLiteDatabase db, int idFuncion) {
        Cursor cursor = db.rawQuery(
                "SELECT estado FROM funciones_cine WHERE id_funcion = ?",
                new String[]{String.valueOf(idFuncion)}
        );
        try {
            if (!cursor.moveToFirst()) {
                throw new IllegalArgumentException("La función seleccionada no existe.");
            }
            String estado = cursor.getString(0);
            if (!Constantes.FUNCION_PROGRAMADA.equalsIgnoreCase(estado)) {
                throw new IllegalArgumentException("La función no está disponible para venta.");
            }
        } finally {
            cursor.close();
        }
    }

    private void validarAsiento(SQLiteDatabase db, int idFuncion, int idAsiento) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM funciones_cine f " +
                        "INNER JOIN asientos a ON a.id_sala = f.id_sala " +
                        "WHERE f.id_funcion = ? AND a.id_asiento = ?",
                new String[]{String.valueOf(idFuncion), String.valueOf(idAsiento)}
        );
        try {
            if (!cursor.moveToFirst() || cursor.getInt(0) == 0) {
                throw new IllegalArgumentException("El asiento no pertenece a la sala de esta función.");
            }
        } finally {
            cursor.close();
        }
    }

    private void validarDisponibilidad(SQLiteDatabase db, int idFuncion, int idAsiento) {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM boletos WHERE id_funcion = ? AND id_asiento = ? AND estado != ?",
                new String[]{String.valueOf(idFuncion), String.valueOf(idAsiento), Constantes.BOLETO_CANCELADO}
        );
        try {
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                throw new IllegalArgumentException("El asiento seleccionado ya fue vendido.");
            }
        } finally {
            cursor.close();
        }
    }

    public static class BoletoSolicitud {
        private final int idFuncion;
        private final int idAsiento;
        private final int idTarifa;
        private final Integer idPromocion;
        private final BigDecimal precioFinal;

        public BoletoSolicitud(int idFuncion, int idAsiento, int idTarifa, Integer idPromocion, BigDecimal precioFinal) {
            this.idFuncion = idFuncion;
            this.idAsiento = idAsiento;
            this.idTarifa = idTarifa;
            this.idPromocion = idPromocion;
            this.precioFinal = precioFinal;
        }
    }
}