package com.example.aperture.ui;

import android.os.Bundle;
import android.widget.Button;
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

        Button btnRegresar = findViewById(R.id.btnRegresar);
        btnRegresar.setOnClickListener(v -> finish());

        if (savedInstanceState == null) {
            String modulo = getIntent().getStringExtra("MODULO");
            Fragment fragmentSeleccionado;

            if ("TAQUILLA".equals(modulo)) {
                fragmentSeleccionado = new TaquillaFragment();
            } else if ("PROMOCION".equals(modulo)) {
                fragmentSeleccionado = new PromocionesFragment();
            } else if ("DULCERIA".equals(modulo)) {
                fragmentSeleccionado = new DulceriaFragment();
            } else {
                fragmentSeleccionado = new FuncionesFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragment, fragmentSeleccionado)
                    .commit();
        }
    }
}