package com.example.aperture.ui.dulceria;

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
import com.example.aperture.model.Combo;
import com.example.aperture.model.Existencia;
import com.example.aperture.model.Producto;
import com.example.aperture.model.Sucursal;
import com.example.aperture.model.Venta;
import com.example.aperture.utils.FechaUtils;
import java.util.ArrayList;

public class DulceriaFragment extends Fragment {

    private SucursalDao sucursalDao;
    private ProductoDao productoDao;
    private ComboDao comboDao;
    private ExistenciaDao existenciaDao;
    private VentaDao ventaDao;

    private Spinner spSucursal;
    private Spinner spProducto;
    private Spinner spCombo;
    private EditText etCantidad;
    private ListView lvCatalogo;

    private ArrayList<Sucursal> listaSucursales;
    private ArrayList<Producto> listaProductos;
    private ArrayList<Combo> listaCombos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dulceria, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
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

        cargarSpinners();
        cargarListaCatalogo();

        btnVenderProducto.setOnClickListener(v -> venderProducto());
        btnVenderCombo.setOnClickListener(v -> venderCombo());

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

    private void venderProducto() {
        if (listaSucursales.isEmpty() || listaProductos.isEmpty()) return;

        Sucursal sucursal = listaSucursales.get(spSucursal.getSelectedItemPosition());
        Producto producto = listaProductos.get(spProducto.getSelectedItemPosition());
        int cantidad = obtenerCantidad();
        double total = producto.getPrecio() * cantidad;

        long idVenta = ventaDao.insertar(new Venta(
                0,
                sucursal.getIdSucursal(),
                FechaUtils.obtenerFechaHoraActual(),
                total,
                "Completada"
        ));

        Existencia ex = existenciaDao.obtenerPorSucursalYProducto(
                sucursal.getIdSucursal(), producto.getIdProducto());
        if (ex != null) {
            int nuevoStock = Math.max(0, ex.getCantidad() - cantidad);
            existenciaDao.actualizarCantidad(ex.getIdExistencia(), nuevoStock);
        }

        if (idVenta > 0) {
            Toast.makeText(requireContext(),
                    "Venta de " + cantidad + "x " + producto.getNombre() + " ($" + total + ")",
                    Toast.LENGTH_SHORT).show();
            cargarListaCatalogo();
        }
    }

    private void venderCombo() {
        if (listaSucursales.isEmpty() || listaCombos.isEmpty()) return;

        Sucursal sucursal = listaSucursales.get(spSucursal.getSelectedItemPosition());
        Combo combo = listaCombos.get(spCombo.getSelectedItemPosition());
        int cantidad = obtenerCantidad();
        double total = combo.getPrecio() * cantidad;

        long idVenta = ventaDao.insertar(new Venta(
                0,
                sucursal.getIdSucursal(),
                FechaUtils.obtenerFechaHoraActual(),
                total,
                "Completada"
        ));

        if (idVenta > 0) {
            Toast.makeText(requireContext(),
                    "Venta de " + cantidad + "x " + combo.getNombre() + " ($" + total + ")",
                    Toast.LENGTH_SHORT).show();
            cargarListaCatalogo();
        }
    }

    private void cargarListaCatalogo() {
        ArrayList<String> items = new ArrayList<>();
        int idSuc = listaSucursales.isEmpty() ? 1 : listaSucursales.get(0).getIdSucursal();

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
