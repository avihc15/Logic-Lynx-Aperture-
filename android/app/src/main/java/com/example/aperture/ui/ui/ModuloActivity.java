package com.example.aperture.ui.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.aperture.R;
import com.example.aperture.ui.dulceria.DulceriaFragment;
import com.example.aperture.ui.funciones.FuncionesFragment;
import com.example.aperture.ui.promociones.PromocionesFragment;
import com.example.aperture.ui.taquilla.TaquillaFragment;

public class ModuloActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulo);

        String nombre = getIntent().getStringExtra("nombre_modulo");
        String descripcion = getIntent().getStringExtra("descripcion_modulo");
        if (nombre == null) nombre = "Aperture";
        if (descripcion == null) descripcion = "Sistema de gestión de cine";

        ((TextView) findViewById(R.id.txtNombreModulo)).setText(nombre);
        ((TextView) findViewById(R.id.txtDescripcionModulo)).setText(descripcion);
        Button regresar = findViewById(R.id.btnRegresar);
        regresar.setOnClickListener(v -> finish());

        if (savedInstanceState == null) {
            Fragment fragment = crearFragment(nombre);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedorModulo, fragment)
                    .commit();
        }
    }

    private Fragment crearFragment(String nombre) {
        if ("Taquilla".equalsIgnoreCase(nombre)) return new TaquillaFragment();
        if ("Promoción".equalsIgnoreCase(nombre) || "Promocion".equalsIgnoreCase(nombre)) {
            return new PromocionesFragment();
        }
        if ("Dulcería".equalsIgnoreCase(nombre) || "Dulceria".equalsIgnoreCase(nombre)) {
            return new DulceriaFragment();
        }
        return new FuncionesFragment();
    }
}
