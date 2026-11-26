package com.example.aperture.ui.promociones;

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
import com.example.aperture.dao.PromocionDao;
import com.example.aperture.network.ApiSyncClient;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Promocion;
import java.util.ArrayList;

public class PromocionesFragment extends Fragment {

    private PromocionDao promocionDao;
    private EditText etNombre;
    private EditText etDescripcion;
    private Spinner spTipo;
    private EditText etValor;
    private EditText etFechaInicio;
    private EditText etFechaFin;
    private ListView lvPromociones;
    private ArrayList<Promocion> listaPromociones;
    private int promocionSeleccionada = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promociones, container, false);

        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        promocionDao = new PromocionDao(dbHelper);

        etNombre = view.findViewById(R.id.etPromoNombre);
        etDescripcion = view.findViewById(R.id.etPromoDescripcion);
        spTipo = view.findViewById(R.id.spPromoTipo);
        etValor = view.findViewById(R.id.etPromoValor);
        etFechaInicio = view.findViewById(R.id.etPromoFechaInicio);
        etFechaFin = view.findViewById(R.id.etPromoFechaFin);
        Button btnGuardar = view.findViewById(R.id.btnGuardarPromo);
        Button btnModificar = view.findViewById(R.id.btnModificarPromo);
        Button btnLimpiar = view.findViewById(R.id.btnLimpiarPromo);
        lvPromociones = view.findViewById(R.id.lvPromociones);

        String[] tipos = {"Porcentaje", "Monto Fijo", "2x1"};
        spTipo.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, tipos));

        cargarPromociones();

        btnGuardar.setOnClickListener(v -> guardarPromocion());
        btnModificar.setOnClickListener(v -> modificarPromocion());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
        lvPromociones.setOnItemClickListener((parent, row, position, id) -> seleccionarPromocion(position));

        lvPromociones.setOnItemLongClickListener((parent, view1, position, id) -> {
            Promocion promo = listaPromociones.get(position);
            boolean nuevoEstado = !promo.isActivo();
            promocionDao.cambiarEstado(promo.getIdPromocion(), nuevoEstado);
            if (promocionSeleccionada == promo.getIdPromocion()) promocionSeleccionada = -1;
            Toast.makeText(requireContext(),
                    nuevoEstado ? "Promoción activada" : "Promoción desactivada",
                    Toast.LENGTH_SHORT).show();
            cargarPromociones();
            return true;
        });

        return view;
    }

    private void seleccionarPromocion(int posicion) {
        if (posicion < 0 || posicion >= listaPromociones.size()) return;
        Promocion p = listaPromociones.get(posicion);
        promocionSeleccionada = p.getIdPromocion();
        etNombre.setText(p.getNombre());
        etDescripcion.setText(p.getDescripcion());
        etValor.setText(String.valueOf(p.getValor()));
        etFechaInicio.setText(p.getFechaInicio());
        etFechaFin.setText(p.getFechaFin());
        for (int i = 0; i < spTipo.getCount(); i++) {
            if (p.getTipo().equals(spTipo.getItemAtPosition(i).toString())) { spTipo.setSelection(i); break; }
        }
        Toast.makeText(requireContext(), "Promoción seleccionada para modificar.", Toast.LENGTH_SHORT).show();
    }

    private void modificarPromocion() {
        if (promocionSeleccionada < 0) {
            Toast.makeText(requireContext(), "Selecciona una promoción de la lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String valorTexto = etValor.getText().toString().trim();
        String inicio = etFechaInicio.getText().toString().trim();
        String fin = etFechaFin.getText().toString().trim();
        if (nombre.isEmpty() || valorTexto.isEmpty() || !inicio.matches("\\d{4}-\\d{2}-\\d{2}")
                || !fin.matches("\\d{4}-\\d{2}-\\d{2}") || fin.compareTo(inicio) < 0) {
            Toast.makeText(requireContext(), "Revisa los datos y las fechas AAAA-MM-DD.", Toast.LENGTH_SHORT).show();
            return;
        }
        double valor;
        try { valor = Double.parseDouble(valorTexto); }
        catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Valor numérico inválido.", Toast.LENGTH_SHORT).show();
            return;
        }
        String tipo = spTipo.getSelectedItem().toString();
        if (valor <= 0 || ("Porcentaje".equals(tipo) && valor > 100)) {
            Toast.makeText(requireContext(), "El descuento no es válido.", Toast.LENGTH_SHORT).show();
            return;
        }
        Promocion p = new Promocion(promocionSeleccionada, nombre, descripcion, tipo, valor, inicio, fin, true);
        if (promocionDao.actualizar(p) > 0) {
            Toast.makeText(requireContext(), "Promoción modificada.", Toast.LENGTH_SHORT).show();
            limpiarFormulario();
            cargarPromociones();
        } else {
            Toast.makeText(requireContext(), "No se pudo modificar la promoción.", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarFormulario() {
        promocionSeleccionada = -1;
        etNombre.setText("");
        etDescripcion.setText("");
        etValor.setText("");
        etFechaInicio.setText("");
        etFechaFin.setText("");
        if (spTipo.getCount() > 0) spTipo.setSelection(0);
        lvPromociones.clearChoices();
    }

    private void guardarPromocion() {
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String tipo = spTipo.getSelectedItem().toString();
        String valorStr = etValor.getText().toString().trim();
        String fInicio = etFechaInicio.getText().toString().trim();
        String fFin = etFechaFin.getText().toString().trim();

        if (nombre.isEmpty() || valorStr.isEmpty() || fInicio.isEmpty() || fFin.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Valor numérico inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (valor <= 0 || ("Porcentaje".equals(tipo) && valor > 100)) {
            Toast.makeText(requireContext(), "El descuento debe ser mayor que cero y el porcentaje no puede superar 100.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!fInicio.matches("\\d{4}-\\d{2}-\\d{2}")
                || !fFin.matches("\\d{4}-\\d{2}-\\d{2}")
                || fFin.compareTo(fInicio) < 0) {
            Toast.makeText(requireContext(), "Revisa las fechas. Usa AAAA-MM-DD y una fecha final posterior al inicio.", Toast.LENGTH_SHORT).show();
            return;
        }

        Promocion p = new Promocion(0, nombre, descripcion, tipo, valor, fInicio, fFin, true);
        long id = promocionDao.insertar(p);
        if (id > 0) {
            ApiSyncClient.sincronizarPromocion(nombre, descripcion, tipo, valor, fInicio, fFin);
            Toast.makeText(requireContext(), "Promoción guardada", Toast.LENGTH_SHORT).show();
            etNombre.setText("");
            etDescripcion.setText("");
            etValor.setText("");
            cargarPromociones();
        } else {
            Toast.makeText(requireContext(), "Error al guardar promoción", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarPromociones() {
        listaPromociones = promocionDao.obtenerTodas();
        ArrayList<String> items = new ArrayList<>();
        for (Promocion p : listaPromociones) {
            String estado = p.isActivo() ? "Activa" : "Inactiva";
            items.add(p.getNombre() + " (" + p.getTipo() + ": " + p.getValor() + ")\n" +
                    p.getFechaInicio() + " a " + p.getFechaFin() + " [" + estado + "]");
        }
        lvPromociones.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items));
    }
}
