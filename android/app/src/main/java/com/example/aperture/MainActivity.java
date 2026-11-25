package com.example.aperture;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aperture.ui.dulceria.DulceriaFragment;
import com.example.aperture.ui.funciones.FuncionesFragment;
import com.example.aperture.ui.promociones.PromocionesFragment;
import com.example.aperture.ui.taquilla.TaquillaFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnTaquilla = findViewById(R.id.btnTaquilla);
        Button btnPromocion = findViewById(R.id.btnPromocion);
        Button btnDulceria = findViewById(R.id.btnDulceria);
        Button btnFuncion = findViewById(R.id.btnFuncion);

        btnTaquilla.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TaquillaFragment.class)));

        btnPromocion.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PromocionesFragment.class)));

        btnDulceria.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DulceriaFragment.class)));

        btnFuncion.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FuncionesFragment.class)));
    }
}