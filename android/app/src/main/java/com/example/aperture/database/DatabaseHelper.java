package com.example.aperture.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "aperture_entrega.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        crearTablas(db);
        insertarDatosIniciales(db);
    }

    private void crearTablas(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS sucursales (" +
                "id_sucursal INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre_sucursal TEXT NOT NULL," +
                "direccion TEXT NOT NULL," +
                "telefono TEXT," +
                "fecha_apertura TEXT," +
                "activo INTEGER NOT NULL DEFAULT 1," +
                "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS salas (" +
                "id_sala INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_sucursal INTEGER NOT NULL," +
                "nombre_sala TEXT NOT NULL," +
                "capacidad INTEGER NOT NULL CHECK(capacidad > 0)," +
                "tipo_sala TEXT DEFAULT 'ESTANDAR'," +
                "activo INTEGER NOT NULL DEFAULT 1," +
                "UNIQUE(id_sucursal, nombre_sala)," +
                "FOREIGN KEY(id_sucursal) REFERENCES sucursales(id_sucursal))");

        db.execSQL("CREATE TABLE IF NOT EXISTS asientos (" +
                "id_asiento INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_sala INTEGER NOT NULL," +
                "fila TEXT NOT NULL," +
                "numero_asiento INTEGER NOT NULL CHECK(numero_asiento > 0)," +
                "activo INTEGER DEFAULT 1," +
                "UNIQUE(id_sala, fila, numero_asiento)," +
                "FOREIGN KEY(id_sala) REFERENCES salas(id_sala))");

        db.execSQL("CREATE TABLE IF NOT EXISTS empleados (" +
                "id_empleado INTEGER PRIMARY KEY AUTOINCREMENT," +
                "numero_empleado TEXT NOT NULL UNIQUE," +
                "nombre_empleado TEXT NOT NULL," +
                "apellido_paterno TEXT NOT NULL," +
                "apellido_materno TEXT NOT NULL," +
                "telefono TEXT," +
                "correo TEXT UNIQUE," +
                "fecha_contrato TEXT NOT NULL," +
                "activo INTEGER NOT NULL DEFAULT 1," +
                "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE IF NOT EXISTS usuarios (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_empleado INTEGER NOT NULL UNIQUE," +
                "nombre_usuario TEXT NOT NULL UNIQUE," +
                "hash_contrasena TEXT NOT NULL," +
                "activo INTEGER NOT NULL DEFAULT 1," +
                "ultimo_acceso TEXT," +
                "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(id_empleado) REFERENCES empleados(id_empleado))");

        db.execSQL("CREATE TABLE IF NOT EXISTS peliculas (" +
                "id_pelicula INTEGER PRIMARY KEY AUTOINCREMENT," +
                "titulo TEXT NOT NULL," +
                "sinopsis TEXT," +
                "duracion_minutos INTEGER NOT NULL CHECK(duracion_minutos > 0)," +
                "clasificacion TEXT NOT NULL," +
                "genero TEXT," +
                "idioma_original TEXT," +
                "fecha_estreno TEXT," +
                "activo INTEGER NOT NULL DEFAULT 1)");

        db.execSQL("CREATE TABLE IF NOT EXISTS carteleras (" +
                "id_cartelera INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_sucursal INTEGER NOT NULL," +
                "fecha_inicio TEXT NOT NULL," +
                "fecha_fin TEXT NOT NULL," +
                "estado TEXT DEFAULT 'BORRADOR'," +
                "FOREIGN KEY(id_sucursal) REFERENCES sucursales(id_sucursal))");

        db.execSQL("CREATE TABLE IF NOT EXISTS cartelera_pelicula (" +
                "id_cartelera INTEGER NOT NULL," +
                "id_pelicula INTEGER NOT NULL," +
                "PRIMARY KEY(id_cartelera, id_pelicula)," +
                "FOREIGN KEY(id_cartelera) REFERENCES carteleras(id_cartelera)," +
                "FOREIGN KEY(id_pelicula) REFERENCES peliculas(id_pelicula))");

        db.execSQL("CREATE TABLE IF NOT EXISTS funciones_cine (" +
                "id_funcion INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_cartelera INTEGER NOT NULL," +
                "id_pelicula INTEGER NOT NULL," +
                "id_sala INTEGER NOT NULL," +
                "fecha_inicio TEXT NOT NULL," +
                "fecha_fin TEXT NOT NULL," +
                "estado TEXT DEFAULT 'PROGRAMADA'," +
                "creada_por INTEGER," +
                "fecha_creacion TEXT DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(id_cartelera) REFERENCES carteleras(id_cartelera)," +
                "FOREIGN KEY(id_pelicula) REFERENCES peliculas(id_pelicula)," +
                "FOREIGN KEY(id_sala) REFERENCES salas(id_sala))");

        db.execSQL("CREATE TABLE IF NOT EXISTS precios_boletos (" +
                "id_precio_boleto INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_sucursal INTEGER NOT NULL," +
                "nombre_precio TEXT NOT NULL," +
                "cantidad REAL NOT NULL CHECK(cantidad >= 0)," +
                "fecha_inicio TEXT NOT NULL," +
                "fecha_fin TEXT," +
                "activo INTEGER NOT NULL DEFAULT 1," +
                "FOREIGN KEY(id_sucursal) REFERENCES sucursales(id_sucursal))");

        db.execSQL("CREATE TABLE IF NOT EXISTS promociones (" +
                "id_promocion INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre_promocion TEXT NOT NULL," +
                "descripcion TEXT," +
                "tipo_descuento TEXT NOT NULL," +
                "valor_descuento REAL NOT NULL CHECK(valor_descuento >= 0)," +
                "fecha_inicio TEXT NOT NULL," +
                "fecha_fin TEXT NOT NULL," +
                "texto_condiciones TEXT," +
                "activo INTEGER DEFAULT 1," +
                "creada_por INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS promocion_sucursal (" +
                "id_promocion INTEGER NOT NULL," +
                "id_sucursal INTEGER NOT NULL," +
                "PRIMARY KEY(id_promocion, id_sucursal)," +
                "FOREIGN KEY(id_promocion) REFERENCES promociones(id_promocion) ON DELETE CASCADE," +
                "FOREIGN KEY(id_sucursal) REFERENCES sucursales(id_sucursal) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE IF NOT EXISTS productos (" +
                "id_producto INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre_producto TEXT NOT NULL UNIQUE," +
                "descripcion TEXT," +
                "categoria TEXT," +
                "unidad_medida TEXT DEFAULT 'PIEZA'," +
                "activo INTEGER NOT NULL DEFAULT 1)");

        db.execSQL("CREATE TABLE IF NOT EXISTS combos (" +
                "id_combo INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre_combo TEXT NOT NULL UNIQUE," +
                "descripcion TEXT," +
                "activo INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE IF NOT EXISTS combo_productos (" +
                "id_combo INTEGER NOT NULL," +
                "id_producto INTEGER NOT NULL," +
                "cantidad REAL NOT NULL CHECK(cantidad > 0)," +
                "PRIMARY KEY(id_combo, id_producto)," +
                "FOREIGN KEY(id_combo) REFERENCES combos(id_combo) ON DELETE CASCADE," +
                "FOREIGN KEY(id_producto) REFERENCES productos(id_producto))");

        db.execSQL("CREATE TABLE IF NOT EXISTS precios_dulceria (" +
                "id_precio INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_sucursal INTEGER NOT NULL," +
                "id_producto INTEGER," +
                "id_combo INTEGER," +
                "precio REAL NOT NULL CHECK(precio >= 0)," +
                "fecha_inicio TEXT NOT NULL," +
                "fecha_fin TEXT," +
                "FOREIGN KEY(id_sucursal) REFERENCES sucursales(id_sucursal)," +
                "FOREIGN KEY(id_producto) REFERENCES productos(id_producto)," +
                "FOREIGN KEY(id_combo) REFERENCES combos(id_combo))");

        db.execSQL("CREATE TABLE IF NOT EXISTS promocion_producto (" +
                "id_promocion_producto INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_promocion INTEGER NOT NULL," +
                "id_producto INTEGER," +
                "id_combo INTEGER," +
                "FOREIGN KEY(id_promocion) REFERENCES promociones(id_promocion) ON DELETE CASCADE," +
                "FOREIGN KEY(id_producto) REFERENCES productos(id_producto)," +
                "FOREIGN KEY(id_combo) REFERENCES combos(id_combo))");

        db.execSQL("CREATE TABLE IF NOT EXISTS ventas (" +
                "id_venta INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_sucursal INTEGER NOT NULL," +
                "id_usuario INTEGER," +
                "tipo_venta TEXT NOT NULL," +
                "fecha_venta TEXT DEFAULT CURRENT_TIMESTAMP," +
                "subtotal REAL NOT NULL DEFAULT 0," +
                "descuento REAL NOT NULL DEFAULT 0," +
                "total REAL NOT NULL DEFAULT 0," +
                "metodo_pago TEXT DEFAULT 'EFECTIVO'," +
                "estado TEXT DEFAULT 'COMPLETADA'," +
                "FOREIGN KEY(id_sucursal) REFERENCES sucursales(id_sucursal))");

        db.execSQL("CREATE TABLE IF NOT EXISTS boletos (" +
                "id_boleto INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_funcion INTEGER NOT NULL," +
                "id_asiento INTEGER NOT NULL," +
                "id_precio_boleto INTEGER NOT NULL," +
                "id_promocion INTEGER," +
                "id_venta INTEGER NOT NULL," +
                "precio_pagado REAL NOT NULL," +
                "estado TEXT DEFAULT 'VENDIDO'," +
                "fecha_venta TEXT DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY(id_funcion) REFERENCES funciones_cine(id_funcion)," +
                "FOREIGN KEY(id_asiento) REFERENCES asientos(id_asiento)," +
                "FOREIGN KEY(id_precio_boleto) REFERENCES precios_boletos(id_precio_boleto)," +
                "FOREIGN KEY(id_venta) REFERENCES ventas(id_venta))");

        db.execSQL("CREATE TABLE IF NOT EXISTS detalles_venta (" +
                "id_detalle INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_venta INTEGER NOT NULL," +
                "id_producto INTEGER," +
                "id_combo INTEGER," +
                "id_boleto INTEGER," +
                "descripcion TEXT NOT NULL," +
                "cantidad INTEGER NOT NULL DEFAULT 1," +
                "precio_unitario REAL NOT NULL," +
                "descuento REAL NOT NULL DEFAULT 0," +
                "total_linea REAL NOT NULL," +
                "FOREIGN KEY(id_venta) REFERENCES ventas(id_venta))");

        db.execSQL("CREATE TABLE IF NOT EXISTS existencias (" +
                "id_existencia INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_sucursal INTEGER NOT NULL," +
                "id_producto INTEGER NOT NULL," +
                "cantidad_actual REAL NOT NULL DEFAULT 0," +
                "cantidad_minima REAL NOT NULL DEFAULT 5," +
                "fecha_actualizacion TEXT DEFAULT CURRENT_TIMESTAMP," +
                "UNIQUE(id_sucursal, id_producto)," +
                "FOREIGN KEY(id_sucursal) REFERENCES sucursales(id_sucursal)," +
                "FOREIGN KEY(id_producto) REFERENCES productos(id_producto))");
    }

    private void insertarDatosIniciales(SQLiteDatabase db) {
        // Sucursal piloto
        db.execSQL("INSERT INTO sucursales (id_sucursal, nombre_sucursal, direccion, activo) VALUES " +
                "(1, 'Aperture - sede piloto (Cine Tonalá)', 'Tonalá 261, Col. Roma Sur, CDMX', 1)");

        // 6 Salas
        db.execSQL("INSERT INTO salas (id_sala, id_sucursal, nombre_sala, capacidad, tipo_sala) VALUES " +
                "(1, 1, 'Sala 1', 24, 'ESTANDAR')," +
                "(2, 1, 'Sala 2', 28, 'ESTANDAR')," +
                "(3, 1, 'Sala 3', 32, 'ESTANDAR')," +
                "(4, 1, 'Sala 4', 24, 'ESTANDAR')," +
                "(5, 1, 'Sala 5', 36, 'ESTANDAR')," +
                "(6, 1, 'Sala 6', 28, 'ESTANDAR')");

        // Generar los 172 asientos exactamente como en el script SQL
        int[] filasPorSala = {6, 7, 8, 6, 9, 7};
        for (int sala = 1; sala <= 6; sala++) {
            int numFilas = filasPorSala[sala - 1];
            for (int f = 0; f < numFilas; f++) {
                String letraFila = String.valueOf((char) ('A' + f));
                for (int num = 1; num <= 4; num++) {
                    db.execSQL("INSERT INTO asientos (id_sala, fila, numero_asiento, activo) VALUES (" +
                            sala + ", '" + letraFila + "', " + num + ", 1)");
                }
            }
        }

        // Películas principales y de cartelera vigente
        db.execSQL("INSERT INTO peliculas (id_pelicula, titulo, duracion_minutos, clasificacion, genero, idioma_original, activo) VALUES " +
                "(1, 'The Shawshank Redemption', 142, 'SC', 'Drama', 'Inglés', 1)," +
                "(2, 'The Godfather', 175, 'SC', 'Crimen / Drama', 'Inglés', 1)," +
                "(3, 'The Dark Knight', 152, 'SC', 'Acción / Crimen', 'Inglés', 1)," +
                "(43, 'Amélie', 122, 'SC', 'Comedia / Romance', 'Francés', 1)," +
                "(44, 'Cinema Paradiso', 155, 'SC', 'Drama', 'Italiano', 1)," +
                "(45, 'Pan’s Labyrinth', 118, 'SC', 'Fantasía / Drama', 'Español', 1)," +
                "(46, 'Roma', 135, 'SC', 'Drama', 'Español', 1)," +
                "(47, 'Y tu mamá también', 106, 'SC', 'Drama', 'Español', 1)," +
                "(48, 'Amores perros', 154, 'SC', 'Drama', 'Español', 1)," +
                "(49, 'The Grand Budapest Hotel', 99, 'SC', 'Comedia / Drama', 'Inglés', 1)," +
                "(50, 'La La Land', 128, 'SC', 'Musical / Romance', 'Inglés', 1)");

        // Carteleras publicadas
        db.execSQL("INSERT INTO carteleras (id_cartelera, id_sucursal, fecha_inicio, fecha_fin, estado) VALUES " +
                "(7, 1, '2026-11-16', '2026-11-29', 'PUBLICADA')," +
                "(8, 1, '2026-11-23', '2026-12-06', 'PUBLICADA')");

        // Funciones programadas
        db.execSQL("INSERT INTO funciones_cine (id_funcion, id_cartelera, id_pelicula, id_sala, fecha_inicio, fecha_fin, estado) VALUES " +
                "(44, 7, 44, 2, '2026-11-25 16:00:00', '2026-11-25 18:55:00', 'PROGRAMADA')," +
                "(45, 7, 45, 3, '2026-11-25 19:00:00', '2026-11-25 21:18:00', 'PROGRAMADA')," +
                "(46, 7, 46, 4, '2026-11-26 13:00:00', '2026-11-26 15:35:00', 'PROGRAMADA')," +
                "(47, 7, 47, 5, '2026-11-26 16:00:00', '2026-11-26 18:06:00', 'PROGRAMADA')," +
                "(48, 7, 48, 6, '2026-11-27 19:00:00', '2026-11-27 21:54:00', 'PROGRAMADA')," +
                "(49, 7, 49, 1, '2026-11-28 13:00:00', '2026-11-28 14:59:00', 'PROGRAMADA')," +
                "(50, 8, 50, 2, '2026-11-29 16:00:00', '2026-11-29 18:28:00', 'PROGRAMADA')");

        // Precios de boletos
        db.execSQL("INSERT INTO precios_boletos (id_precio_boleto, id_sucursal, nombre_precio, cantidad, fecha_inicio, activo) VALUES " +
                "(1, 1, 'General', 70.00, '2026-01-01', 1)," +
                "(2, 1, 'Estudiante', 50.00, '2026-01-01', 1)," +
                "(3, 1, 'Adulto mayor', 50.00, '2026-01-01', 1)," +
                "(4, 1, 'Infantil', 50.00, '2026-01-01', 1)");

        // Promociones
        db.execSQL("INSERT INTO promociones (id_promocion, nombre_promocion, descripcion, tipo_descuento, valor_descuento, fecha_inicio, fecha_fin, activo) VALUES " +
                "(1, 'Martes cultural', 'Promoción para funciones culturales', 'PORCENTAJE', 15.0, '2026-10-01 00:00:00', '2027-02-28 23:59:59', 1)," +
                "(2, 'Combo familiar', 'Descuento directo en combo familiar', 'CANTIDAD_FIJA', 20.0, '2026-10-01 00:00:00', '2027-02-28 23:59:59', 1)," +
                "(3, 'Ciclo de clásicos', 'Descuento en películas clásicas', 'PORCENTAJE', 10.0, '2026-10-01 00:00:00', '2027-02-28 23:59:59', 1)," +
                "(4, 'Dulcería especial', 'Descuento en productos seleccionados', 'PORCENTAJE', 5.0, '2026-10-01 00:00:00', '2027-02-28 23:59:59', 1)," +
                "(5, 'Promoción estudiantes', 'Descuento fijo para estudiantes', 'CANTIDAD_FIJA', 10.0, '2026-10-01 00:00:00', '2027-02-28 23:59:59', 1)");

        // Productos de dulcería
        db.execSQL("INSERT INTO productos (id_producto, nombre_producto, descripcion, categoria, unidad_medida, activo) VALUES " +
                "(1, 'Palomitas chicas', 'Palomitas recién hechas', 'PALOMITAS', 'PIEZA', 1)," +
                "(2, 'Palomitas medianas', 'Palomitas recién hechas', 'PALOMITAS', 'PIEZA', 1)," +
                "(3, 'Palomitas grandes', 'Palomitas recién hechas', 'PALOMITAS', 'PIEZA', 1)," +
                "(7, 'Refresco cola 600 ml', 'Bebida fría', 'BEBIDAS', 'PIEZA', 1)," +
                "(10, 'Agua natural 600 ml', 'Agua embotellada', 'BEBIDAS', 'PIEZA', 1)," +
                "(17, 'Nachos con queso', 'Totopos con queso caliente', 'BOTANAS', 'PIEZA', 1)," +
                "(18, 'Hot dog clásico', 'Hot dog tradicional', 'ALIMENTOS', 'PIEZA', 1)," +
                "(24, 'Gomitas surtidas', 'Dulces surtidos', 'DULCES', 'PIEZA', 1)");

        // Existencias iniciales (con stock suficiente para vender)
        db.execSQL("INSERT INTO existencias (id_sucursal, id_producto, cantidad_actual, cantidad_minima) VALUES " +
                "(1, 1, 97, 10)," +
                "(1, 2, 97, 10)," +
                "(1, 3, 97, 10)," +
                "(1, 7, 97, 10)," +
                "(1, 10, 97, 10)," +
                "(1, 17, 97, 10)," +
                "(1, 18, 97, 10)," +
                "(1, 24, 97, 10)");

        // Combos y sus componentes
        db.execSQL("INSERT INTO combos (id_combo, nombre_combo, descripcion, activo) VALUES " +
                "(1, 'Combo clásico', 'Palomitas medianas + Refresco 600 ml', 1)," +
                "(2, 'Combo grande', 'Palomitas grandes + Refresco 600 ml', 1)," +
                "(3, 'Combo doble', '2 Palomitas grandes + 2 Refrescos 600 ml', 1)," +
                "(4, 'Combo nachos', 'Nachos con queso + Refresco 600 ml', 1)");

        db.execSQL("INSERT INTO combo_productos (id_combo, id_producto, cantidad) VALUES " +
                "(1, 2, 1), (1, 7, 1)," +
                "(2, 3, 1), (2, 7, 1)," +
                "(3, 3, 2), (3, 7, 2)," +
                "(4, 17, 1), (4, 7, 1)");

        // Precios de dulcería (productos y combos)
        db.execSQL("INSERT INTO precios_dulceria (id_sucursal, id_producto, id_combo, precio, fecha_inicio) VALUES " +
                "(1, 1, NULL, 45.00, '2026-01-01')," +
                "(1, 2, NULL, 60.00, '2026-01-01')," +
                "(1, 3, NULL, 75.00, '2026-01-01')," +
                "(1, 7, NULL, 45.00, '2026-01-01')," +
                "(1, 10, NULL, 30.00, '2026-01-01')," +
                "(1, 17, NULL, 75.00, '2026-01-01')," +
                "(1, 18, NULL, 65.00, '2026-01-01')," +
                "(1, 24, NULL, 35.00, '2026-01-01')," +
                "(1, NULL, 1, 95.00, '2026-01-01')," +
                "(1, NULL, 2, 110.00, '2026-01-01')," +
                "(1, NULL, 3, 210.00, '2026-01-01')," +
                "(1, NULL, 4, 105.00, '2026-01-01')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS detalles_venta");
        db.execSQL("DROP TABLE IF EXISTS boletos");
        db.execSQL("DROP TABLE IF EXISTS ventas");
        db.execSQL("DROP TABLE IF EXISTS existencias");
        db.execSQL("DROP TABLE IF EXISTS promocion_producto");
        db.execSQL("DROP TABLE IF EXISTS promocion_sucursal");
        db.execSQL("DROP TABLE IF EXISTS promociones");
        db.execSQL("DROP TABLE IF EXISTS precios_dulceria");
        db.execSQL("DROP TABLE IF EXISTS combo_productos");
        db.execSQL("DROP TABLE IF EXISTS combos");
        db.execSQL("DROP TABLE IF EXISTS productos");
        db.execSQL("DROP TABLE IF EXISTS precios_boletos");
        db.execSQL("DROP TABLE IF EXISTS funciones_cine");
        db.execSQL("DROP TABLE IF EXISTS cartelera_pelicula");
        db.execSQL("DROP TABLE IF EXISTS carteleras");
        db.execSQL("DROP TABLE IF EXISTS peliculas");
        db.execSQL("DROP TABLE IF EXISTS asientos");
        db.execSQL("DROP TABLE IF EXISTS salas");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS empleados");
        db.execSQL("DROP TABLE IF EXISTS sucursales");
        onCreate(db);
    }
}