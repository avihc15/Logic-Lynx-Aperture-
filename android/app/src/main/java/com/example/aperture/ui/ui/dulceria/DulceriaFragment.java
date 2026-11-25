package com.example.aperture.ui.ui.dulceria;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aperture.R;
import com.example.aperture.dao.ComboDao;
import com.example.aperture.dao.ExistenciaDao;
import com.example.aperture.dao.ProductoDao;
import com.example.aperture.dao.SucursalDao;
import com.example.aperture.dao.VentaDao;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.service.DulceriaService;
import com.example.aperture.model.Combo;
import com.example.aperture.model.Existencia;
import com.example.aperture.model.Producto;
import com.example.aperture.model.Sucursal;

import java.util.ArrayList;

public class DulceriaFragment extends Fragment {

    private SucursalDao sucursalDao;
    private ProductoDao productoDao;
    private ComboDao comboDao;
    private ExistenciaDao existenciaDao;
    private VentaDao ventaDao;
    private DulceriaService dulceriaService;

    private Spinner spSucursal;
    private Spinner spProducto;
    private Spinner spCombo;
    private EditText etCantidad;
    private ListView lvCatalogo;
    private ListView lvCarrito;
    private android.widget.TextView tvTotal;
    private final ArrayList<ArticuloCarrito> carrito = new ArrayList<>();
    private final ArrayList<String> carritoTexto = new ArrayList<>();

    private ArrayList<Sucursal> listaSucursales;
    private ArrayList<Producto> listaProductos;
    private ArrayList<Combo> listaCombos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dulceria, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        dulceriaService = new DulceriaService(dbHelper);
        sucursalDao = new SucursalDao(dbHelper);
        productoDao = new ProductoDao(dbHelper);
        comboDao = new ComboDao(dbHelper);
        existenciaDao = new ExistenciaDao(dbHelper);
        ventaDao = new VentaDao(dbHelper);

        spSucursal = view.findViewById(R.id.spDulceriaSucursal);
        spProducto = view.findViewById(R.id.spDulceriaProducto);
        spCombo = view.findViewById(R.id.spDulceriaCombo);
        etCantidad = view.findViewById(R.id.etDulceriaCantidad);
        Button btnVenderProducto = view.findViewById(R.id.btnVenderProducto);
        Button btnVenderCombo = view.findViewById(R.id.btnVenderCombo);
        lvCatalogo = view.findViewById(R.id.lvDulceriaCatalogo);
        lvCarrito = view.findViewById(R.id.lvCarritoDulceria);
        tvTotal = view.findViewById(R.id.tvTotalCarrito);
        Button btnQuitar = view.findViewById(R.id.btnQuitarCarrito);
        Button btnRegistrar = view.findViewById(R.id.btnRegistrarVentaDulceria);

        cargarSpinners();
        cargarListaCatalogo();
        spSucursal.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View selectedView,
                                       int position, long id) {
                cargarListaCatalogo();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        btnVenderProducto.setOnClickListener(v -> agregarProducto());
        btnVenderCombo.setOnClickListener(v -> agregarCombo());
        btnQuitar.setOnClickListener(v -> quitarDelCarrito());
        btnRegistrar.setOnClickListener(v -> registrarVenta());
        actualizarCarrito();

        return view;
    }

    private void cargarSpinners() {
        listaSucursales = sucursalDao.obtenerActivas();
        ArrayList<String> nombresSucursales = new ArrayList<>();
        for (Sucursal s : listaSucursales) {
            nombresSucursales.add(s.getNombre());
        }
        spSucursal.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresSucursales));

        listaProductos = productoDao.obtenerActivos();
        ArrayList<String> nombresProductos = new ArrayList<>();
        for (Producto p : listaProductos) {
            nombresProductos.add(p.getNombre() + " ($" + p.getPrecio() + ")");
        }
        spProducto.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresProductos));

        listaCombos = comboDao.obtenerActivos();
        ArrayList<String> nombresCombos = new ArrayList<>();
        for (Combo c : listaCombos) {
            nombresCombos.add(c.getNombre() + " ($" + c.getPrecio() + ")");
        }
        spCombo.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresCombos));
    }

    private int obtenerCantidad() {
        String cantStr = etCantidad.getText().toString().trim();
        if (cantStr.isEmpty()) return 1;
        try {
            return Math.max(1, Integer.parseInt(cantStr));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int leerCantidad() {
        String texto = etCantidad.getText().toString().trim();
        if (texto.isEmpty()) return 1;
        try {
            int cantidad = Integer.parseInt(texto);
            if (cantidad > 0) return cantidad;
        } catch (NumberFormatException ignored) { }
        Toast.makeText(requireContext(), "La cantidad debe ser un número mayor que cero.", Toast.LENGTH_SHORT).show();
        return -1;
    }

    private void agregarProducto() {
        if (listaProductos == null || listaProductos.isEmpty() || spProducto.getSelectedItemPosition() < 0) {
            Toast.makeText(requireContext(), "No hay productos disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }
        int cantidad = leerCantidad();
        if (cantidad <= 0) return;
        Producto p = listaProductos.get(spProducto.getSelectedItemPosition());
        agregarAlCarrito("PRODUCTO", p.getIdProducto(), p.getNombre(), p.getPrecio(), cantidad);
    }

    private void agregarCombo() {
        if (listaCombos == null || listaCombos.isEmpty() || spCombo.getSelectedItemPosition() < 0) {
            Toast.makeText(requireContext(), "No hay combos disponibles.", Toast.LENGTH_SHORT).show();
            return;
        }
        int cantidad = leerCantidad();
        if (cantidad <= 0) return;
        Combo c = listaCombos.get(spCombo.getSelectedItemPosition());
        agregarAlCarrito("COMBO", c.getIdCombo(), c.getNombre(), c.getPrecio(), cantidad);
    }

    private void agregarAlCarrito(String tipo, int id, String nombre, double precio, int cantidad) {
        for (ArticuloCarrito articulo : carrito) {
            if (articulo.id == id && articulo.tipo.equals(tipo)) {
                articulo.cantidad += cantidad;
                actualizarCarrito();
                etCantidad.setText("1");
                return;
            }
        }
        carrito.add(new ArticuloCarrito(tipo, id, nombre, precio, cantidad));
        actualizarCarrito();
        etCantidad.setText("1");
    }

    private void quitarDelCarrito() {
        int posicion = lvCarrito.getCheckedItemPosition();
        if (posicion < 0 || posicion >= carrito.size()) {
            Toast.makeText(requireContext(), "Selecciona un artículo del carrito.", Toast.LENGTH_SHORT).show();
            return;
        }
        carrito.remove(posicion);
        lvCarrito.clearChoices();
        actualizarCarrito();
    }

    private void actualizarCarrito() {
        carritoTexto.clear();
        double total = 0.0;
        for (ArticuloCarrito articulo : carrito) {
            double subtotal = articulo.precio * articulo.cantidad;
            total += subtotal;
            String tipo = "PRODUCTO".equals(articulo.tipo) ? "Producto" : "Combo";
            carritoTexto.add(tipo + ": " + articulo.nombre + "\nCantidad: " + articulo.cantidad
                    + " | Subtotal: $" + String.format(java.util.Locale.getDefault(), "%.2f", subtotal));
        }
        lvCarrito.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_single_choice, carritoTexto));
        lvCarrito.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        tvTotal.setText("Total: $" + String.format(java.util.Locale.getDefault(), "%.2f", total));
    }

    private void registrarVenta() {
        if (listaSucursales == null || listaSucursales.isEmpty() || spSucursal.getSelectedItemPosition() < 0) {
            Toast.makeText(requireContext(), "Selecciona una sucursal.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (carrito.isEmpty()) {
            Toast.makeText(requireContext(), "Agrega al menos un artículo.", Toast.LENGTH_SHORT).show();
            return;
        }
        Sucursal sucursal = listaSucursales.get(spSucursal.getSelectedItemPosition());
        ArrayList<DulceriaService.ArticuloSolicitud> solicitudes = new ArrayList<>();
        for (ArticuloCarrito articulo : carrito) {
            solicitudes.add(new DulceriaService.ArticuloSolicitud(articulo.tipo, articulo.id, articulo.cantidad));
        }
        try {
            long folio = dulceriaService.registrarVentaDulceria(sucursal.getIdSucursal(), solicitudes);
            Toast.makeText(requireContext(), "Venta registrada correctamente. Folio: " + folio,
                    Toast.LENGTH_LONG).show();
            carrito.clear();
            actualizarCarrito();
            cargarListaCatalogo();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No se pudo completar la venta: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private static class ArticuloCarrito {
        final String tipo;
        final int id;
        final String nombre;
        final double precio;
        int cantidad;
        ArticuloCarrito(String tipo, int id, String nombre, double precio, int cantidad) {
            this.tipo = tipo;
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
        }
    }

    private void cargarListaCatalogo() {
        ArrayList<String> items = new ArrayList<>();
        int posicionSucursal = spSucursal.getSelectedItemPosition();
        if (listaSucursales.isEmpty() || posicionSucursal < 0 || posicionSucursal >= listaSucursales.size()) {
            lvCatalogo.setAdapter(new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_list_item_1, items));
            return;
        }
        int idSuc = listaSucursales.get(posicionSucursal).getIdSucursal();

        for (Producto p : productoDao.obtenerActivos()) {
            Existencia ex = existenciaDao.obtenerPorSucursalYProducto(idSuc, p.getIdProducto());
            int stock = (ex != null) ? ex.getCantidad() : 0;
            items.add("[Producto] " + p.getNombre() + " - $" + p.getPrecio() + " (Stock: " + stock + ")");
        }

        for (Combo c : comboDao.obtenerActivos()) {
            items.add("[Combo] " + c.getNombre() + " - $" + c.getPrecio() + "\n" + c.getDescripcion());
        }

        lvCatalogo.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items));
    }
}
