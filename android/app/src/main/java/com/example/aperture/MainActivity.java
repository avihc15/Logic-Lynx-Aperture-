package com.example.aperture;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aperture.ui.ModuloActivity;

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
                abrirModulo("Taquilla", "Venta y consulta de boletos."));

        btnPromocion.setOnClickListener(v ->
                abrirModulo("Promoción", "Administración de promociones."));

        btnDulceria.setOnClickListener(v ->
                abrirModulo("Dulcería", "Venta de productos y combos."));

        btnFuncion.setOnClickListener(v ->
                abrirModulo("Función", "Administración de películas, carteleras y funciones."));
    }

    private void abrirModulo(String nombre, String descripcion) {
        Intent intent = new Intent(MainActivity.this, ModuloActivity.class);
        intent.putExtra("nombre_modulo", nombre);
        intent.putExtra("descripcion_modulo", descripcion);
        startActivity(intent);
    }
}
