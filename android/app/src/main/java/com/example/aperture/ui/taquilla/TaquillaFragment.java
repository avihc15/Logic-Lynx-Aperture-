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
import com.example.aperture.dao.CarteleraDao;
import com.example.aperture.dao.FuncionDao;
import com.example.aperture.dao.PeliculaDao;
import com.example.aperture.dao.PromocionDao;
import com.example.aperture.dao.SucursalDao;
import com.example.aperture.dao.TarifaBoletoDao;
import com.example.aperture.dao.VentaDao;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.network.ApiSyncClient;
import com.example.aperture.service.VentaService;
import com.example.aperture.model.Asiento;
import com.example.aperture.model.Boleto;
import com.example.aperture.model.Funcion;
import com.example.aperture.model.Pelicula;
import com.example.aperture.model.Promocion;
import com.example.aperture.model.TarifaBoleto;
import com.example.aperture.utils.FechaUtils;
import java.util.ArrayList;
import java.math.BigDecimal;

public class TaquillaFragment extends Fragment {

    private FuncionDao funcionDao;
    private PeliculaDao peliculaDao;
    private AsientoDao asientoDao;
    private TarifaBoletoDao tarifaDao;
    private PromocionDao promocionDao;
    private BoletoDao boletoDao;
    private VentaDao ventaDao;
    private SucursalDao sucursalDao;
    private CarteleraDao carteleraDao;
    private VentaService ventaService;

    private Spinner spFuncion;
    private ListView lvAsientos;
    private Spinner spTarifa;
    private Spinner spPromocion;
    private TextView tvTotal;
    private ListView lvBoletos;

    private ArrayList<Funcion> listaFunciones;
    private ArrayList<Asiento> listaAsientos;
    private ArrayList<TarifaBoleto> listaTarifas;
    private ArrayList<Promocion> listaPromociones;
    private ArrayList<Boleto> listaBoletosFuncion;
    private final java.util.HashSet<Integer> asientosOcupados = new java.util.HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taquilla, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        ventaService = new VentaService(dbHelper);
        carteleraDao = new CarteleraDao(dbHelper);
        funcionDao = new FuncionDao(dbHelper);
        peliculaDao = new PeliculaDao(dbHelper);
        asientoDao = new AsientoDao(dbHelper);
        tarifaDao = new TarifaBoletoDao(dbHelper);
        promocionDao = new PromocionDao(dbHelper);
        boletoDao = new BoletoDao(dbHelper);
        ventaDao = new VentaDao(dbHelper);
        sucursalDao = new SucursalDao(dbHelper);

        spFuncion = view.findViewById(R.id.spTaquillaFuncion);
        lvAsientos = view.findViewById(R.id.lvTaquillaAsientos);
        lvAsientos.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
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
        lvAsientos.setOnItemClickListener((parent, itemView, position, id) -> {
            if (position >= 0 && position < listaAsientos.size()
                    && asientosOcupados.contains(listaAsientos.get(position).getIdAsiento())) {
                lvAsientos.setItemChecked(position, false);
                Toast.makeText(requireContext(), "Ese asiento ya está vendido.", Toast.LENGTH_SHORT).show();
            }
            calcularPrecioFinal();
        });
        if (!listaFunciones.isEmpty()) {
            cargarAsientosPorFuncion(spFuncion.getSelectedItemPosition());
            cargarBoletosFuncion(spFuncion.getSelectedItemPosition());
        }

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
        asientosOcupados.clear();
        for (Boleto boleto : boletoDao.obtenerPorFuncion(f.getIdFuncion())) {
            if (!"Cancelado".equalsIgnoreCase(boleto.getEstado())) {
                asientosOcupados.add(boleto.getIdAsiento());
            }
        }
        ArrayList<String> nombresAsientos = new ArrayList<>();
        for (Asiento a : listaAsientos) {
            String estado = asientosOcupados.contains(a.getIdAsiento()) ? " - VENDIDO" : "";
            nombresAsientos.add("Fila " + a.getFila() + " - Asiento " + a.getNumero()
                    + " (" + a.getTipo() + ")" + estado);
        }
        lvAsientos.clearChoices();
        lvAsientos.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_multiple_choice, nombresAsientos));
        calcularPrecioFinal();
    }

    private double calcularPrecioFinal() {
        if (listaTarifas == null || listaTarifas.isEmpty() || spTarifa.getSelectedItemPosition() < 0) {
            tvTotal.setText("Total a pagar: $0.00");
            return 0.0;
        }
        TarifaBoleto tarifa = listaTarifas.get(spTarifa.getSelectedItemPosition());
        double precioPorBoleto = tarifa.getPrecio();
        int cantidadSeleccionada = obtenerAsientosSeleccionados().size();

        int posPromo = spPromocion.getSelectedItemPosition();
        if (posPromo > 0 && posPromo - 1 < listaPromociones.size()) {
            Promocion promo = listaPromociones.get(posPromo - 1);
            String hoy = FechaUtils.obtenerFechaActual();
            boolean vigente = hoy.compareTo(promo.getFechaInicio()) >= 0
                    && hoy.compareTo(promo.getFechaFin()) <= 0;
            if (vigente) {
                if ("Porcentaje".equalsIgnoreCase(promo.getTipo())) {
                    precioPorBoleto -= precioPorBoleto * (promo.getValor() / 100.0);
                } else if ("2x1".equalsIgnoreCase(promo.getTipo())) {
                    if (cantidadSeleccionada >= 2) {
                        double totalDosPorUno = Math.ceil(cantidadSeleccionada / 2.0) * precioPorBoleto;
                        precioPorBoleto = totalDosPorUno / cantidadSeleccionada;
                    }
                } else {
                    precioPorBoleto = Math.max(0.0, precioPorBoleto - promo.getValor());
                }
            }
        }

        double total = precioPorBoleto * cantidadSeleccionada;
        tvTotal.setText("Total a pagar: $" + String.format("%.2f", total)
                + " (" + cantidadSeleccionada + " boleto(s))");
        return precioPorBoleto;
    }

    private ArrayList<Integer> obtenerAsientosSeleccionados() {
        ArrayList<Integer> posiciones = new ArrayList<>();
        if (lvAsientos == null) return posiciones;
        android.util.SparseBooleanArray marcados = lvAsientos.getCheckedItemPositions();
        for (int i = 0; i < marcados.size(); i++) {
            int posicion = marcados.keyAt(i);
            if (marcados.valueAt(i) && posicion >= 0 && posicion < listaAsientos.size()) {
                posiciones.add(posicion);
            }
        }
        return posiciones;
    }

    private void venderBoleto() {
        if (listaFunciones == null || listaFunciones.isEmpty()
                || listaAsientos == null || listaAsientos.isEmpty()
                || listaTarifas == null || listaTarifas.isEmpty()
                || spFuncion.getSelectedItemPosition() < 0
                || spTarifa.getSelectedItemPosition() < 0) {
            Toast.makeText(requireContext(), "Selecciona función y tarifa", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<Integer> seleccionados = obtenerAsientosSeleccionados();
        if (seleccionados.isEmpty()) {
            Toast.makeText(requireContext(), "Selecciona al menos un asiento.", Toast.LENGTH_SHORT).show();
            return;
        }

        Funcion funcion = listaFunciones.get(spFuncion.getSelectedItemPosition());
        TarifaBoleto tarifa = listaTarifas.get(spTarifa.getSelectedItemPosition());
        int posPromo = spPromocion.getSelectedItemPosition();
        Integer idPromo = (posPromo > 0 && posPromo - 1 < listaPromociones.size())
                ? listaPromociones.get(posPromo - 1).getIdPromocion() : null;
        double precioFinal = calcularPrecioFinal();

        com.example.aperture.model.Cartelera cartelera = carteleraDao.obtenerPorId(funcion.getIdCartelera());
        if (cartelera == null) {
            Toast.makeText(requireContext(), "No se encontró la cartelera de la función.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<VentaService.BoletoSolicitud> solicitudes = new ArrayList<>();
        for (int posicion : seleccionados) {
            Asiento asiento = listaAsientos.get(posicion);
            solicitudes.add(new VentaService.BoletoSolicitud(
                    funcion.getIdFuncion(), asiento.getIdAsiento(), tarifa.getIdTarifa(),
                    idPromo, BigDecimal.valueOf(precioFinal)));
        }

        try {
            long idVenta = ventaService.registrarVentaBoletos(cartelera.getIdSucursal(), solicitudes);
            ArrayList<Integer> idsAsientosSync = new ArrayList<>();
            for (int pos : seleccionados) idsAsientosSync.add(listaAsientos.get(pos).getIdAsiento());
            ApiSyncClient.sincronizarVentaTaquilla(cartelera.getIdSucursal(), funcion.getIdFuncion(), tarifa.getIdTarifa(), idPromo, precioFinal, idsAsientosSync);
            Toast.makeText(requireContext(), "Venta registrada. Folio: " + idVenta,
                    Toast.LENGTH_SHORT).show();
            lvAsientos.clearChoices();
            cargarBoletosFuncion(spFuncion.getSelectedItemPosition());
            calcularPrecioFinal();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No se pudo completar la venta: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
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
