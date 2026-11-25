package com.example.aperture.ui.taquilla;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.aperture.R;
import com.example.aperture.dao.AsientoDao;
import com.example.aperture.dao.BoletoDao;
import com.example.aperture.dao.FuncionDao;
import com.example.aperture.dao.PeliculaDao;
import com.example.aperture.dao.PromocionDao;
import com.example.aperture.dao.SucursalDao;
import com.example.aperture.dao.TarifaBoletoDao;
import com.example.aperture.dao.VentaDao;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Asiento;
import com.example.aperture.model.Boleto;
import com.example.aperture.model.Funcion;
import com.example.aperture.model.Pelicula;
import com.example.aperture.model.Promocion;
import com.example.aperture.model.Sucursal;
import com.example.aperture.model.TarifaBoleto;
import com.example.aperture.model.Venta;
import com.example.aperture.utils.FechaUtils;
import java.util.ArrayList;

public class TaquillaFragment extends Fragment {

    private FuncionDao funcionDao;
    private PeliculaDao peliculaDao;
    private AsientoDao asientoDao;
    private TarifaBoletoDao tarifaDao;
    private PromocionDao promocionDao;
    private BoletoDao boletoDao;
    private VentaDao ventaDao;
    private SucursalDao sucursalDao;

    private Spinner spFuncion;
    private Spinner spAsiento;
    private Spinner spTarifa;
    private Spinner spPromocion;
    private TextView tvTotal;
    private ListView lvBoletos;

    private ArrayList<Funcion> listaFunciones;
    private ArrayList<Asiento> listaAsientos;
    private ArrayList<TarifaBoleto> listaTarifas;
    private ArrayList<Promocion> listaPromociones;
    private ArrayList<Boleto> listaBoletosFuncion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taquilla, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        funcionDao = new FuncionDao(dbHelper);
        peliculaDao = new PeliculaDao(dbHelper);
        asientoDao = new AsientoDao(dbHelper);
        tarifaDao = new TarifaBoletoDao(dbHelper);
        promocionDao = new PromocionDao(dbHelper);
        boletoDao = new BoletoDao(dbHelper);
        ventaDao = new VentaDao(dbHelper);
        sucursalDao = new SucursalDao(dbHelper);

        spFuncion = view.findViewById(R.id.spTaquillaFuncion);
        spAsiento = view.findViewById(R.id.spTaquillaAsiento);
        spTarifa = view.findViewById(R.id.spTaquillaTarifa);
        spPromocion = view.findViewById(R.id.spTaquillaPromocion);
        tvTotal = view.findViewById(R.id.tvTaquillaPrecioFinal);
        Button btnVender = view.findViewById(R.id.btnVenderBoleto);
        lvBoletos = view.findViewById(R.id.lvBoletosVendidos);

        cargarDatosIniciales();

        spFuncion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                cargarAsientosPorFuncion(position);
                cargarBoletosFuncion(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        AdapterView.OnItemSelectedListener recalcularListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                calcularPrecioFinal();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spTarifa.setOnItemSelectedListener(recalcularListener);
        spPromocion.setOnItemSelectedListener(recalcularListener);

        btnVender.setOnClickListener(v -> venderBoleto());

        return view;
    }

    private void cargarDatosIniciales() {
        listaFunciones = funcionDao.obtenerProgramadas();
        ArrayList<String> nombresFunciones = new ArrayList<>();
        for (Funcion f : listaFunciones) {
            Pelicula p = peliculaDao.obtenerPorId(f.getIdPelicula());
            String titulo = (p != null) ? p.getTitulo() : "Película #" + f.getIdPelicula();
            nombresFunciones.add(titulo + " - " + f.getFechaHora() + " (" + f.getFormato() + ")");
        }
        spFuncion.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresFunciones));

        listaTarifas = tarifaDao.obtenerActivas();
        ArrayList<String> nombresTarifas = new ArrayList<>();
        for (TarifaBoleto t : listaTarifas) {
            nombresTarifas.add(t.getNombre() + " ($" + t.getPrecio() + ")");
        }
        spTarifa.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresTarifas));

        listaPromociones = promocionDao.obtenerActivas();
        ArrayList<String> nombresPromos = new ArrayList<>();
        nombresPromos.add("Sin Promoción");
        for (Promocion pr : listaPromociones) {
            nombresPromos.add(pr.getNombre() + " (" + pr.getTipo() + ")");
        }
        spPromocion.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresPromos));
    }

    private void cargarAsientosPorFuncion(int posFuncion) {
        if (listaFunciones.isEmpty() || posFuncion < 0) return;
        Funcion f = listaFunciones.get(posFuncion);
        listaAsientos = asientoDao.obtenerPorSala(f.getIdSala());
        ArrayList<String> nombresAsientos = new ArrayList<>();
        for (Asiento a : listaAsientos) {
            nombresAsientos.add("Fila " + a.getFila() + " - Asiento " + a.getNumero() + " (" + a.getTipo() + ")");
        }
        spAsiento.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresAsientos));
    }

    private double calcularPrecioFinal() {
        if (listaTarifas.isEmpty() || spTarifa.getSelectedItemPosition() < 0) {
            tvTotal.setText("Total a pagar: $0.00");
            return 0.0;
        }
        TarifaBoleto tarifa = listaTarifas.get(spTarifa.getSelectedItemPosition());
        double precio = tarifa.getPrecio();

        int posPromo = spPromocion.getSelectedItemPosition();
        if (posPromo > 0 && (posPromo - 1) < listaPromociones.size()) {
            Promocion promo = listaPromociones.get(posPromo - 1);
            if ("Porcentaje".equalsIgnoreCase(promo.getTipo())) {
                precio = precio - (precio * (promo.getValor() / 100.0));
            } else {
                precio = Math.max(0.0, precio - promo.getValor());
            }
        }
        tvTotal.setText("Total a pagar: $" + String.format("%.2f", precio));
        return precio;
    }

    private void venderBoleto() {
        if (listaFunciones.isEmpty() || listaAsientos == null || listaAsientos.isEmpty() || listaTarifas.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona función, asiento y tarifa", Toast.LENGTH_SHORT).show();
            return;
        }

        Funcion funcion = listaFunciones.get(spFuncion.getSelectedItemPosition());
        Asiento asiento = listaAsientos.get(spAsiento.getSelectedItemPosition());
        TarifaBoleto tarifa = listaTarifas.get(spTarifa.getSelectedItemPosition());

        int posPromo = spPromocion.getSelectedItemPosition();
        Integer idPromo = (posPromo > 0) ? listaPromociones.get(posPromo - 1).getIdPromocion() : null;
        double precioFinal = calcularPrecioFinal();

        ArrayList<Sucursal> sucursales = sucursalDao.obtenerActivas();
        int idSucursal = sucursales.isEmpty() ? 1 : sucursales.get(0).getIdSucursal();

        long idVenta = ventaDao.insertar(new Venta(
                0,
                idSucursal,
                FechaUtils.obtenerFechaHoraActual(),
                precioFinal,
                "Completada"
        ));

        Boleto boleto = new Boleto(
                0,
                funcion.getIdFuncion(),
                asiento.getIdAsiento(),
                tarifa.getIdTarifa(),
                idPromo,
                (int) idVenta,
                precioFinal,
                "Vendido"
        );

        long idBoleto = boletoDao.insertar(boleto);
        if (idBoleto > 0) {
            Toast.makeText(requireContext(), "Boleto vendido exitosamente", Toast.LENGTH_SHORT).show();
            cargarBoletosFuncion(spFuncion.getSelectedItemPosition());
        } else {
            Toast.makeText(requireContext(), "Error al vender boleto", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarBoletosFuncion(int posFuncion) {
        if (listaFunciones.isEmpty() || posFuncion < 0) return;
        Funcion f = listaFunciones.get(posFuncion);
        listaBoletosFuncion = boletoDao.obtenerPorFuncion(f.getIdFuncion());
        ArrayList<String> items = new ArrayList<>();
        for (Boleto b : listaBoletosFuncion) {
            items.add("Boleto #" + b.getIdBoleto() + " | Asiento ID: " + b.getIdAsiento() +
                    " | $" + String.format("%.2f", b.getPrecioFinal()) + " [" + b.getEstado() + "]");
        }
        lvBoletos.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items));
    }
}
