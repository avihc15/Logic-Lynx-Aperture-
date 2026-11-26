package com.example.aperture.utils;

import com.example.aperture.dao.AsientoDao;
import com.example.aperture.dao.CarteleraDao;
import com.example.aperture.dao.ComboDao;
import com.example.aperture.dao.ExistenciaDao;
import com.example.aperture.dao.PeliculaDao;
import com.example.aperture.dao.ProductoDao;
import com.example.aperture.dao.PromocionDao;
import com.example.aperture.dao.SalaDao;
import com.example.aperture.dao.SucursalDao;
import com.example.aperture.dao.TarifaBoletoDao;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Asiento;
import com.example.aperture.model.Cartelera;
import com.example.aperture.model.Combo;
import com.example.aperture.model.Existencia;
import com.example.aperture.model.Pelicula;
import com.example.aperture.model.Producto;
import com.example.aperture.model.Promocion;
import com.example.aperture.model.Sala;
import com.example.aperture.model.Sucursal;
import com.example.aperture.model.TarifaBoleto;

public class DataSeeder {

    public static void cargarDatosIniciales(DatabaseHelper dbHelper) {
        SucursalDao sucursalDao = new SucursalDao(dbHelper);
        if (!sucursalDao.obtenerTodas().isEmpty()) {
            return;
        }

        long idSuc1 = sucursalDao.insertar(new Sucursal(0, "Aperture Centro", "Av. Principal 100", "555-0101", true));
        long idSuc2 = sucursalDao.insertar(new Sucursal(0, "Aperture Norte", "Plaza Norte Local 4", "555-0202", true));

        SalaDao salaDao = new SalaDao(dbHelper);
        long idSala1 = salaDao.insertar(new Sala(0, (int) idSuc1, "Sala 1 Tradicional", 50, "Tradicional", true));
        long idSala2 = salaDao.insertar(new Sala(0, (int) idSuc1, "Sala 2 VIP", 30, "VIP", true));
        long idSala3 = salaDao.insertar(new Sala(0, (int) idSuc2, "Sala 1 IMAX", 80, "IMAX", true));

        AsientoDao asientoDao = new AsientoDao(dbHelper);
        String[] filas = {"A", "B", "C"};
        for (String fila : filas) {
            for (int num = 1; num <= 5; num++) {
                asientoDao.insertar(new Asiento(0, (int) idSala1, fila, num, "Estándar"));
                asientoDao.insertar(new Asiento(0, (int) idSala2, fila, num, "VIP"));
                asientoDao.insertar(new Asiento(0, (int) idSala3, fila, num, "Estándar"));
            }
        }

        PeliculaDao peliculaDao = new PeliculaDao(dbHelper);
        long idPel1 = peliculaDao.insertar(new Pelicula(0, "Interstellar", 169, "B", "Ciencia Ficción", "Viaje espacial a través de un agujero de gusano", "interstellar.jpg", true));
        long idPel2 = peliculaDao.insertar(new Pelicula(0, "Inception", 148, "B", "Acción", "Robo de secretos a través de los sueños", "inception.jpg", true));
        long idPel3 = peliculaDao.insertar(new Pelicula(0, "Dune: Parte Dos", 166, "B", "Aventura", "Paul Atreides se une a los Fremen", "dune2.jpg", true));

        CarteleraDao carteleraDao = new CarteleraDao(dbHelper);
        long idCart1 = carteleraDao.insertar(new Cartelera(0, (int) idSuc1, "Cartelera Estelar Centro", "2026-01-01", "2026-12-31", true));
        long idCart2 = carteleraDao.insertar(new Cartelera(0, (int) idSuc2, "Cartelera Estelar Norte", "2026-01-01", "2026-12-31", true));

        carteleraDao.asociarPelicula((int) idCart1, (int) idPel1);
        carteleraDao.asociarPelicula((int) idCart1, (int) idPel2);
        carteleraDao.asociarPelicula((int) idCart1, (int) idPel3);
        carteleraDao.asociarPelicula((int) idCart2, (int) idPel1);
        carteleraDao.asociarPelicula((int) idCart2, (int) idPel3);

        TarifaBoletoDao tarifaDao = new TarifaBoletoDao(dbHelper);
        tarifaDao.insertar(new TarifaBoleto(0, "Adulto General", 85.0, "Boleto estándar adulto", true));
        tarifaDao.insertar(new TarifaBoleto(0, "Niño / Tercera Edad", 65.0, "Tarifa preferencial", true));
        tarifaDao.insertar(new TarifaBoleto(0, "VIP", 150.0, "Acceso a sala VIP", true));

        PromocionDao promocionDao = new PromocionDao(dbHelper);
        promocionDao.insertar(new Promocion(0, "Miércoles 2x1", "Descuento de 50% en boleto", "Porcentaje", 50.0, "2026-01-01", "2026-12-31", true));
        promocionDao.insertar(new Promocion(0, "Estudiante -$20", "Descuento directo con credencial", "Monto Fijo", 20.0, "2026-01-01", "2026-12-31", true));

        ProductoDao productoDao = new ProductoDao(dbHelper);
        long idProd1 = productoDao.insertar(new Producto(0, "Palomitas Grandes", "Palomitas de mantequilla 120g", 90.0, true));
        long idProd2 = productoDao.insertar(new Producto(0, "Refresco Grande", "Vaso de refresco 900ml", 70.0, true));
        long idProd3 = productoDao.insertar(new Producto(0, "Nachos con Queso", "Totopos con queso cheddar", 85.0, true));

        ComboDao comboDao = new ComboDao(dbHelper);
        long idCombo1 = comboDao.insertar(new Combo(0, "Combo Pareja", "1 Palomitas Grandes + 2 Refrescos Grandes", 210.0, true));
        comboDao.agregarProductoACombo((int) idCombo1, (int) idProd1, 1);
        comboDao.agregarProductoACombo((int) idCombo1, (int) idProd2, 2);

        ExistenciaDao existenciaDao = new ExistenciaDao(dbHelper);
        existenciaDao.insertar(new Existencia(0, (int) idSuc1, (int) idProd1, 100));
        existenciaDao.insertar(new Existencia(0, (int) idSuc1, (int) idProd2, 100));
        existenciaDao.insertar(new Existencia(0, (int) idSuc1, (int) idProd3, 50));
    }
}
