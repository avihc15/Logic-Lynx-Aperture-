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
        lvFunciones = view.findViewById(R.id.lvFunciones);

        cargarSpinners();
        cargarListaFunciones();

        btnGuardar.setOnClickListener(v -> guardarFuncion());

        lvFunciones.setOnItemLongClickListener((parent, view1, position, id) -> {
            Funcion f = listaFunciones.get(position);
            funcionDao.cambiarEstado(f.getIdFuncion(), "Cancelada");
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
        if (fechaHora.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa la fecha y hora (YYYY-MM-DD HH:MM)", Toast.LENGTH_SHORT).show();
            return;
        }

        Cartelera cartelera = listaCarteleras.get(spCartelera.getSelectedItemPosition());
        Pelicula pelicula = listaPeliculas.get(spPelicula.getSelectedItemPosition());
        Sala sala = listaSalas.get(spSala.getSelectedItemPosition());
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
            Toast.makeText(requireContext(), "Función registrada", Toast.LENGTH_SHORT).show();
            etFechaHora.setText("");
            cargarListaFunciones();
        } else {
            Toast.makeText(requireContext(), "Error al guardar función", Toast.LENGTH_SHORT).show();
        }
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
