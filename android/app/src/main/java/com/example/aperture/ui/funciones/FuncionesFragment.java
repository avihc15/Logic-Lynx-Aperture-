package com.example.aperture.ui.funciones;

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
import com.example.aperture.dao.CarteleraDao;
import com.example.aperture.dao.FuncionDao;
import com.example.aperture.network.ApiSyncClient;
import com.example.aperture.dao.PeliculaDao;
import com.example.aperture.dao.SalaDao;
import com.example.aperture.database.DatabaseHelper;
import com.example.aperture.model.Cartelera;
import com.example.aperture.model.Funcion;
import com.example.aperture.model.Pelicula;
import com.example.aperture.model.Sala;
import java.util.ArrayList;

public class FuncionesFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private CarteleraDao carteleraDao;
    private PeliculaDao peliculaDao;
    private SalaDao salaDao;
    private FuncionDao funcionDao;

    private Spinner spCartelera;
    private Spinner spPelicula;
    private Spinner spSala;
    private Spinner spIdioma;
    private Spinner spFormato;
    private EditText etFechaHora;
    private ListView lvFunciones;

    private ArrayList<Cartelera> listaCarteleras;
    private ArrayList<Pelicula> listaPeliculas;
    private ArrayList<Sala> listaSalas;
    private ArrayList<Funcion> listaFunciones;
    private int funcionSeleccionada = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_funciones, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        carteleraDao = new CarteleraDao(dbHelper);
        peliculaDao = new PeliculaDao(dbHelper);
        salaDao = new SalaDao(dbHelper);
        funcionDao = new FuncionDao(dbHelper);

        spCartelera = view.findViewById(R.id.spFuncionCartelera);
        spPelicula = view.findViewById(R.id.spFuncionPelicula);
        spSala = view.findViewById(R.id.spFuncionSala);
        spIdioma = view.findViewById(R.id.spFuncionIdioma);
        spFormato = view.findViewById(R.id.spFuncionFormato);
        etFechaHora = view.findViewById(R.id.etFuncionFechaHora);
        Button btnGuardar = view.findViewById(R.id.btnGuardarFuncion);
        Button btnModificar = view.findViewById(R.id.btnModificarFuncion);
        Button btnLimpiar = view.findViewById(R.id.btnLimpiarFuncion);
        lvFunciones = view.findViewById(R.id.lvFunciones);

        cargarSpinners();
        cargarListaFunciones();

        btnGuardar.setOnClickListener(v -> guardarFuncion());
        btnModificar.setOnClickListener(v -> modificarFuncion());
        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
        lvFunciones.setOnItemClickListener((parent, row, position, id) -> seleccionarFuncion(position));

        lvFunciones.setOnItemLongClickListener((parent, view1, position, id) -> {
            Funcion f = listaFunciones.get(position);
            funcionDao.cambiarEstado(f.getIdFuncion(), "Cancelada");
            funcionSeleccionada = -1;
            Toast.makeText(requireContext(), "Función cancelada", Toast.LENGTH_SHORT).show();
            cargarListaFunciones();
            return true;
        });

        return view;
    }

    private void cargarSpinners() {
        listaCarteleras = carteleraDao.obtenerTodas();
        ArrayList<String> nombresCarteleras = new ArrayList<>();
        for (Cartelera c : listaCarteleras) {
            nombresCarteleras.add(c.getNombre());
        }
        spCartelera.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresCarteleras));

        listaPeliculas = peliculaDao.obtenerActivas();
        ArrayList<String> nombresPeliculas = new ArrayList<>();
        for (Pelicula p : listaPeliculas) {
            nombresPeliculas.add(p.getTitulo());
        }
        spPelicula.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresPeliculas));

        listaSalas = salaDao.obtenerTodas();
        ArrayList<String> nombresSalas = new ArrayList<>();
        for (Sala s : listaSalas) {
            nombresSalas.add(s.getNombre() + " (" + s.getTipo() + ")");
        }
        spSala.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, nombresSalas));

        String[] idiomas = {"Español", "Subtitulada", "Inglés"};
        spIdioma.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, idiomas));

        String[] formatos = {"2D", "3D", "IMAX", "VIP"};
        spFormato.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, formatos));
    }

    private void guardarFuncion() {
        if (listaCarteleras.isEmpty() || listaPeliculas.isEmpty() || listaSalas.isEmpty()) {
            Toast.makeText(requireContext(), "Faltan datos base en catálogos", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaHora = etFechaHora.getText().toString().trim();
        if (!fechaHora.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
            Toast.makeText(requireContext(), "Ingresa fecha y hora con formato AAAA-MM-DD HH:MM.", Toast.LENGTH_SHORT).show();
            return;
        }

        Cartelera cartelera = listaCarteleras.get(spCartelera.getSelectedItemPosition());
        Pelicula pelicula = listaPeliculas.get(spPelicula.getSelectedItemPosition());
        Sala sala = listaSalas.get(spSala.getSelectedItemPosition());
        if (sala.getIdSucursal() != cartelera.getIdSucursal()) {
            Toast.makeText(requireContext(), "La sala debe pertenecer a la sucursal de la cartelera.", Toast.LENGTH_SHORT).show();
            return;
        }
        String idioma = spIdioma.getSelectedItem().toString();
        String formato = spFormato.getSelectedItem().toString();

        carteleraDao.asociarPelicula(cartelera.getIdCartelera(), pelicula.getIdPelicula());

        Funcion nueva = new Funcion(
                0,
                cartelera.getIdCartelera(),
                pelicula.getIdPelicula(),
                sala.getIdSala(),
                fechaHora,
                idioma,
                formato,
                "Programada"
        );

        long id = funcionDao.insertar(nueva);
        if (id > 0) {
            ApiSyncClient.sincronizarFuncion(cartelera.getIdCartelera(), pelicula.getIdPelicula(), sala.getIdSala(), fechaHora);
            Toast.makeText(requireContext(), "Función registrada", Toast.LENGTH_SHORT).show();
            etFechaHora.setText("");
            cargarListaFunciones();
        } else {
            Toast.makeText(requireContext(), "Error al guardar función", Toast.LENGTH_SHORT).show();
        }
    }

    private void seleccionarFuncion(int posicion) {
        if (posicion < 0 || posicion >= listaFunciones.size()) return;
        Funcion f = listaFunciones.get(posicion);
        funcionSeleccionada = f.getIdFuncion();
        seleccionarSpinner(spCartelera, listaCarteleras, f.getIdCartelera(), 0);
        seleccionarSpinner(spPelicula, listaPeliculas, f.getIdPelicula(), 1);
        seleccionarSpinner(spSala, listaSalas, f.getIdSala(), 2);
        etFechaHora.setText(f.getFechaHora());
        seleccionarTexto(spIdioma, f.getIdioma());
        seleccionarTexto(spFormato, f.getFormato());
        Toast.makeText(requireContext(), "Función seleccionada para modificar.", Toast.LENGTH_SHORT).show();
    }

    private void seleccionarSpinner(Spinner spinner, ArrayList<?> lista, int id, int tipo) {
        for (int i = 0; i < lista.size(); i++) {
            Object elemento = lista.get(i);
            int idElemento = tipo == 0 ? ((Cartelera) elemento).getIdCartelera()
                    : tipo == 1 ? ((Pelicula) elemento).getIdPelicula()
                    : ((Sala) elemento).getIdSala();
            if (idElemento == id) { spinner.setSelection(i); return; }
        }
    }

    private void seleccionarTexto(Spinner spinner, String valor) {
        if (spinner.getAdapter() == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (valor.equals(spinner.getItemAtPosition(i).toString())) { spinner.setSelection(i); return; }
        }
    }

    private void modificarFuncion() {
        if (funcionSeleccionada < 0) {
            Toast.makeText(requireContext(), "Selecciona una función de la lista.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (listaCarteleras.isEmpty() || listaPeliculas.isEmpty() || listaSalas.isEmpty()
                || spCartelera.getSelectedItemPosition() < 0
                || spPelicula.getSelectedItemPosition() < 0 || spSala.getSelectedItemPosition() < 0) {
            Toast.makeText(requireContext(), "Completa los catálogos de la función.", Toast.LENGTH_SHORT).show();
            return;
        }
        String fechaHora = etFechaHora.getText().toString().trim();
        if (!fechaHora.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
            Toast.makeText(requireContext(), "Usa el formato AAAA-MM-DD HH:MM.", Toast.LENGTH_SHORT).show();
            return;
        }
        Cartelera cartelera = listaCarteleras.get(spCartelera.getSelectedItemPosition());
        Pelicula pelicula = listaPeliculas.get(spPelicula.getSelectedItemPosition());
        Sala sala = listaSalas.get(spSala.getSelectedItemPosition());
        if (sala.getIdSucursal() != cartelera.getIdSucursal()) {
            Toast.makeText(requireContext(), "La sala debe pertenecer a la sucursal de la cartelera.", Toast.LENGTH_SHORT).show();
            return;
        }
        carteleraDao.asociarPelicula(cartelera.getIdCartelera(), pelicula.getIdPelicula());
        Funcion f = new Funcion(funcionSeleccionada, cartelera.getIdCartelera(), pelicula.getIdPelicula(),
                sala.getIdSala(), fechaHora, spIdioma.getSelectedItem().toString(),
                spFormato.getSelectedItem().toString(), "Programada");
        if (funcionDao.actualizar(f) > 0) {
            Toast.makeText(requireContext(), "Función modificada correctamente.", Toast.LENGTH_SHORT).show();
            limpiarFormulario();
            cargarListaFunciones();
        } else {
            Toast.makeText(requireContext(), "No se pudo modificar la función.", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarFormulario() {
        funcionSeleccionada = -1;
        etFechaHora.setText("");
        if (spCartelera.getCount() > 0) spCartelera.setSelection(0);
        if (spPelicula.getCount() > 0) spPelicula.setSelection(0);
        if (spSala.getCount() > 0) spSala.setSelection(0);
        if (spIdioma.getCount() > 0) spIdioma.setSelection(0);
        if (spFormato.getCount() > 0) spFormato.setSelection(0);
        lvFunciones.clearChoices();
    }

    private void cargarListaFunciones() {
        listaFunciones = funcionDao.obtenerTodas();
        ArrayList<String> items = new ArrayList<>();
        for (Funcion f : listaFunciones) {
            Pelicula p = peliculaDao.obtenerPorId(f.getIdPelicula());
            Sala s = salaDao.obtenerPorId(f.getIdSala());
            String tituloPel = (p != null) ? p.getTitulo() : "Película #" + f.getIdPelicula();
            String nombreSala = (s != null) ? s.getNombre() : "Sala #" + f.getIdSala();
            items.add(tituloPel + " | " + nombreSala + "\n" +
                    f.getFechaHora() + " | " + f.getIdioma() + " " + f.getFormato() + " [" + f.getEstado() + "]");
        }
        lvFunciones.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items));
    }
}
