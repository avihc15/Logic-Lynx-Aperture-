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
        lvPromociones = view.findViewById(R.id.lvPromociones);

        String[] tipos = {"Porcentaje", "Monto Fijo", "2x1"};
        spTipo.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, tipos));

        cargarPromociones();

        btnGuardar.setOnClickListener(v -> guardarPromocion());

        lvPromociones.setOnItemLongClickListener((parent, view1, position, id) -> {
            Promocion promo = listaPromociones.get(position);
            boolean nuevoEstado = !promo.isActivo();
            promocionDao.cambiarEstado(promo.getIdPromocion(), nuevoEstado);
            Toast.makeText(requireContext(),
                    nuevoEstado ? "Promoción activada" : "Promoción desactivada",
                    Toast.LENGTH_SHORT).show();
            cargarPromociones();
            return true;
        });

        return view;
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

        Promocion p = new Promocion(0, nombre, descripcion, tipo, valor, fInicio, fFin, true);
        long id = promocionDao.insertar(p);
        if (id > 0) {
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
