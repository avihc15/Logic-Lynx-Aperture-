package com.example.aperture.network;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cliente HTTP REST que sincroniza las operaciones de Android con el backend Spring Boot en Azure.
 * Cambia BASE_URL por la IP publica de tu VM en Azure (ej. "http://20.120.45.10:8080/api").
 */
public class ApiSyncClient {
    private static final String TAG = "ApertureApiSync";
    // 10.0.2.2 apunta al localhost de tu PC desde el emulador Android.
    // Para Azure en vivo, cambia a: "http://TU_IP_PUBLICA_AZURE:8080/api"
    public static String BASE_URL = "http://68.211.179.58:8080/api";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void enviarPostAsync(String endpoint, JSONObject payload) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                Log.i(TAG, "POST " + endpoint + " -> HTTP " + code);
            } catch (Exception e) {
                Log.w(TAG, "Modo offline local (servidor no alcanzable en " + endpoint + "): " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    public static void sincronizarVentaTaquilla(int idSucursal, int idFuncion, int idTarifa,
                                                Integer idPromocion, double precioUnitario,
                                                java.util.List<Integer> idAsientos) {
        try {
            JSONObject body = new JSONObject();
            body.put("idSucursal", idSucursal);
            body.put("idFuncion", idFuncion);
            body.put("idTarifa", idTarifa);
            if (idPromocion != null) body.put("idPromocion", idPromocion);
            body.put("precioUnitario", precioUnitario);
            JSONArray arr = new JSONArray();
            for (Integer idA : idAsientos) arr.put(idA);
            body.put("asientos", arr);
            enviarPostAsync("/ventas/taquilla", body);
        } catch (Exception e) {
            Log.e(TAG, "Error construyendo JSON taquilla", e);
        }
    }

    public static void sincronizarVentaDulceria(int idSucursal, java.util.List<JSONObject> items) {
        try {
            JSONObject body = new JSONObject();
            body.put("idSucursal", idSucursal);
            JSONArray arr = new JSONArray();
            for (JSONObject item : items) arr.put(item);
            body.put("items", arr);
            enviarPostAsync("/ventas/dulceria", body);
        } catch (Exception e) {
            Log.e(TAG, "Error construyendo JSON dulceria", e);
        }
    }

    public static void sincronizarFuncion(int idCartelera, int idPelicula, int idSala, String fechaHora) {
        try {
            JSONObject body = new JSONObject();
            body.put("idCartelera", idCartelera);
            body.put("idPelicula", idPelicula);
            body.put("idSala", idSala);
            body.put("fechaInicio", fechaHora.length() == 16 ? fechaHora + ":00" : fechaHora);
            body.put("estado", "PROGRAMADA");
            enviarPostAsync("/funciones", body);
        } catch (Exception e) {
            Log.e(TAG, "Error construyendo JSON funcion", e);
        }
    }

    public static void sincronizarPromocion(String nombre, String descripcion, String tipo,
                                            double valor, String fechaInicio, String fechaFin) {
        try {
            JSONObject body = new JSONObject();
            body.put("nombrePromocion", nombre);
            body.put("descripcion", descripcion);
            body.put("tipoDescuento", "Porcentaje".equalsIgnoreCase(tipo) ? "PORCENTAJE" : "CANTIDAD_FIJA");
            body.put("valorDescuento", valor);
            body.put("fechaInicio", fechaInicio + " 00:00:00");
            body.put("fechaFin", fechaFin + " 23:59:59");
            body.put("activo", true);
            enviarPostAsync("/promociones", body);
        } catch (Exception e) {
            Log.e(TAG, "Error construyendo JSON promocion", e);
        }
    }
}
