package com.example.aperture.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.utils.Constantes;
import com.example.aperture.utils.FechaUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DulceriaService {
    private final DatabaseHelper dbHelper;

    public DulceriaService(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long registrarVentaDulceria(int idSucursal, List<ArticuloSolicitud> articulos) {
        if (idSucursal <= 0) {
            throw new IllegalArgumentException("Sucursal no válida.");
        }
        if (articulos == null || articulos.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar al menos un artículo.");
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            BigDecimal total = BigDecimal.ZERO;
            Map<Integer, Integer> productosNecesarios = new HashMap<>();
            List<ArticuloProcesado> articulosProcesados = new ArrayList<>();

            for (ArticuloSolicitud solicitud : articulos) {
                validarSolicitud(solicitud);
                InfoArticulo info = obtenerInfoYPrecio(db, idSucursal, solicitud.tipo, solicitud.idArticulo);

                BigDecimal subtotal = info.precio.multiply(BigDecimal.valueOf(solicitud.cantidad))
                        .setScale(2, RoundingMode.HALF_UP);
                total = total.add(subtotal);

                articulosProcesados.add(new ArticuloProcesado(
                        solicitud.tipo,
                        solicitud.idArticulo,
                        info.nombre,
                        solicitud.cantidad,
                        info.precio,
                        subtotal
                ));

                if ("PRODUCTO".equals(solicitud.tipo)) {
                    agregarCantidad(productosNecesarios, solicitud.idArticulo, solicitud.cantidad);
                } else {
                    agregarProductosDelCombo(db, solicitud.idArticulo, solicitud.cantidad, productosNecesarios);
                }
            }

            total = total.setScale(2, RoundingMode.HALF_UP);

            for (Map.Entry<Integer, Integer> entrada : productosNecesarios.entrySet()) {
                verificarExistencia(db, idSucursal, entrada.getKey(), entrada.getValue());
            }

            ContentValues venta = new ContentValues();
            venta.put("id_sucursal", idSucursal);
            venta.put("tipo_venta", Constantes.TIPO_VENTA_DULCERIA);
            venta.put("fecha_venta", FechaUtils.obtenerFechaHoraActual());
            venta.put("subtotal", total.doubleValue());
            venta.put("descuento", 0.0);
            venta.put("total", total.doubleValue());
            venta.put("metodo_pago", "EFECTIVO");
            venta.put("estado", Constantes.VENTA_COMPLETADA);

            long idVenta = db.insertOrThrow("ventas", null, venta);

            for (ArticuloProcesado articulo : articulosProcesados) {
                ContentValues detalle = new ContentValues();
                detalle.put("id_venta", idVenta);
                detalle.put("descripcion", articulo.nombre);
                detalle.put("cantidad", articulo.cantidad);
                detalle.put("precio_unitario", articulo.precio.doubleValue());
                detalle.put("descuento", 0.0);
                detalle.put("total_linea", articulo.subtotal.doubleValue());

                if ("PRODUCTO".equals(articulo.tipo)) {
                    detalle.put("id_producto", articulo.idArticulo);
                    detalle.putNull("id_combo");
                } else {
                    detalle.putNull("id_producto");
                    detalle.put("id_combo", articulo.idArticulo);
                }
                detalle.putNull("id_boleto");

                db.insertOrThrow("detalles_venta", null, detalle);
            }

            for (Map.Entry<Integer, Integer> entrada : productosNecesarios.entrySet()) {
                descontarExistencia(db, idSucursal, entrada.getKey(), entrada.getValue());
            }

            db.setTransactionSuccessful();
            return idVenta;
        } finally {
            db.endTransaction();
        }
    }

    private void validarSolicitud(ArticuloSolicitud solicitud) {
        if (solicitud == null || (!"PRODUCTO".equals(solicitud.tipo) && !"COMBO".equals(solicitud.tipo))) {
            throw new IllegalArgumentException("El tipo de artículo no es válido.");
        }
        if (solicitud.idArticulo <= 0 || solicitud.cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad o el artículo no son válidos.");
        }
    }

    private InfoArticulo obtenerInfoYPrecio(SQLiteDatabase db, int idSucursal, String tipo, int idArticulo) {
        String sql;
        if ("PRODUCTO".equals(tipo)) {
            sql = "SELECT p.nombre_producto, p.activo, pd.precio " +
                    "FROM productos p " +
                    "INNER JOIN precios_dulceria pd ON pd.id_producto = p.id_producto " +
                    "WHERE p.id_producto = ? AND pd.id_sucursal = ? AND pd.fecha_fin IS NULL " +
                    "ORDER BY pd.id_precio DESC LIMIT 1";
        } else {
            sql = "SELECT c.nombre_combo, c.activo, pd.precio " +
                    "FROM combos c " +
                    "INNER JOIN precios_dulceria pd ON pd.id_combo = c.id_combo " +
                    "WHERE c.id_combo = ? AND pd.id_sucursal = ? AND pd.fecha_fin IS NULL " +
                    "ORDER BY pd.id_precio DESC LIMIT 1";
        }

        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(idArticulo), String.valueOf(idSucursal)});
        try {
            if (!cursor.moveToFirst()) {
                throw new IllegalArgumentException("El artículo seleccionado no tiene precio registrado en esta sucursal.");
            }
            String nombre = cursor.getString(0);
            int activo = cursor.getInt(1);
            double precio = cursor.getDouble(2);

            if (activo != 1) {
                throw new IllegalArgumentException("El artículo seleccionado está inactivo.");
            }
            return new InfoArticulo(nombre, BigDecimal.valueOf(precio).setScale(2, RoundingMode.HALF_UP));
        } finally {
            cursor.close();
        }
    }

    private void agregarCantidad(Map<Integer, Integer> productos, int idProducto, int cantidad) {
        int actual = productos.containsKey(idProducto) ? productos.get(idProducto) : 0;
        productos.put(idProducto, actual + cantidad);
    }

    private void agregarProductosDelCombo(SQLiteDatabase db, int idCombo, int cantidadCombos, Map<Integer, Integer> productosNecesarios) {
        Cursor cursor = db.rawQuery(
                "SELECT id_producto, cantidad FROM combo_productos WHERE id_combo = ?",
                new String[]{String.valueOf(idCombo)}
        );
        try {
            if (!cursor.moveToFirst()) {
                throw new IllegalArgumentException("El combo no tiene productos registrados.");
            }
            do {
                int idProducto = cursor.getInt(0);
                int cantidadPorCombo = (int) cursor.getDouble(1);
                agregarCantidad(productosNecesarios, idProducto, cantidadPorCombo * cantidadCombos);
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }
    }

    private void verificarExistencia(SQLiteDatabase db, int idSucursal, int idProducto, int cantidadNecesaria) {
        Cursor cursor = db.rawQuery(
                "SELECT cantidad_actual FROM existencias WHERE id_sucursal = ? AND id_producto = ?",
                new String[]{String.valueOf(idSucursal), String.valueOf(idProducto)}
        );
        try {
            if (!cursor.moveToFirst()) {
                throw new IllegalArgumentException("No existe registro de inventario para el producto " + idProducto + ".");
            }
            double disponible = cursor.getDouble(0);
            if (disponible < cantidadNecesaria) {
                throw new IllegalArgumentException("Existencias insuficientes para el producto " + idProducto + ".");
            }
        } finally {
            cursor.close();
        }
    }

    private void descontarExistencia(SQLiteDatabase db, int idSucursal, int idProducto, int cantidad) {
        SQLiteStatement sentencia = db.compileStatement(
                "UPDATE existencias SET cantidad_actual = cantidad_actual - ? " +
                        "WHERE id_sucursal = ? AND id_producto = ? AND cantidad_actual >= ?"
        );
        sentencia.bindLong(1, cantidad);
        sentencia.bindLong(2, idSucursal);
        sentencia.bindLong(3, idProducto);
        sentencia.bindLong(4, cantidad);
        int filas = sentencia.executeUpdateDelete();
        sentencia.close();

        if (filas == 0) {
            throw new IllegalArgumentException("No fue posible actualizar las existencias.");
        }
    }

    public static class ArticuloSolicitud {
        private final String tipo;
        private final int idArticulo;
        private final int cantidad;

        public ArticuloSolicitud(String tipo, int idArticulo, int cantidad) {
            this.tipo = tipo;
            this.idArticulo = idArticulo;
            this.cantidad = cantidad;
        }
    }

    private static class InfoArticulo {
        final String nombre;
        final BigDecimal precio;

        InfoArticulo(String nombre, BigDecimal precio) {
            this.nombre = nombre;
            this.precio = precio;
        }
    }

    private static class ArticuloProcesado {
        final String tipo;
        final int idArticulo;
        final String nombre;
        final int cantidad;
        final BigDecimal precio;
        final BigDecimal subtotal;

        ArticuloProcesado(String tipo, int idArticulo, String nombre, int cantidad, BigDecimal precio, BigDecimal subtotal) {
            this.tipo = tipo;
            this.idArticulo = idArticulo;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
            this.subtotal = subtotal;
        }
    }
}