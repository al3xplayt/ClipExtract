package com.example.trabajofinal_ag;
import static android.os.Environment.getExternalStoragePublicDirectory;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import Api.ApiCallback;
import Api.ApiUtils;
import Database.AppDatabase;
import Database.DownloadHistory;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;

public class DownloadActivity extends AppCompatActivity {
    EditText url;
    Button download;
    Spinner formatSpinner;
    OkHttpClient client = new OkHttpClient();
    BottomNavigationView bottomNavigationView;
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 1;
    private static AppDatabase db;

    private static final String API_URL = "http://192.168.1.14:50010/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_download);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Bundle bundle = getIntent().getExtras();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bundle != null) {
            String url = bundle.getString("url");
            String format = bundle.getString("format");
            startDownload(url, format);
            //Toast.makeText(this, "Descargando: " + url + " en formato " + format, Toast.LENGTH_SHORT).show();
        }
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "download_database")
                .allowMainThreadQueries()  // Permite hacer consultas en el hilo principal (para pruebas, en producción es mejor hacerlas en segundo plano)
                .build();

        // Inicialización de las vistas
        url = findViewById(R.id.Url);
        download = findViewById(R.id.btt_desc);
        formatSpinner = findViewById(R.id.formatSpinner);

        // Configurar el Spinner con los formatos
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.format_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);

        // Verificar permisos de escritura en almacenamiento
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
            }
        }
        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String userName = prefs.getString("user_name", null);

        // Acción del botón de descarga
        download.setOnClickListener(v -> {
            String urlText = url.getText().toString().trim();

            // Verificar que la URL no esté vacía
            if (urlText.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_url_empty), Toast.LENGTH_SHORT).show();
            }
            // Verificar que la URL sea válida
            else if (!isValidUrl(urlText)) {
                Toast.makeText(this, getString(R.string.error_url_invalid), Toast.LENGTH_SHORT).show();
            } else {
                // Obtener el formato seleccionado
                String selectedFormat = formatSpinner.getSelectedItem().toString().toLowerCase();
                // Enviar la URL y el formato al servidor Flask para la descarga
                startDownload(urlText, selectedFormat);

            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_history) {
                // Código para "Historial"
                startActivity(new Intent(DownloadActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                // Código para "Perfil"
                startActivity(new Intent(this, LogInActivity.class));
                return true;
            } else if (itemId == R.id.nav_clip) {
                startActivity(new Intent(this, GenerateClipsActivity.class));
                return true;
            }
            return false;
        });
    }

    public static AppDatabase getDatabase() {
        return db;
    }

    // Método para enviar la URL al servidor Flask
    private void startDownload(String urlText, String format) {
        // Crear la solicitud JSON
        String json = "{\"url\":\"" + urlText + "\", \"format\":\"" + format + "\"}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        // Crear la solicitud POST
        Request request = new Request.Builder()
                .url(API_URL + "download")  // Asegúrate de que Flask esté escuchando en esta ruta
                .post(body)
                .build();

        // Ejecutar la solicitud en un hilo en segundo plano
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null && response.body().contentLength() > 0) {
                    // Intentar obtener el nombre del archivo desde el encabezado "Content-Disposition"
                    String fileName; // Nombre por defecto

                    String contentDisposition = response.header("Content-Disposition");
                    if (contentDisposition != null && contentDisposition.contains("filename=")) {
                        // Extraer el nombre del archivo del encabezado
                        fileName = contentDisposition.split("filename=")[1].replace("\"", "");
                    } else {
                        fileName = "video_descargado.mp4";
                    }

                    File downloadsDir = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

                    // Escribir el archivo en el directorio de "Descargas"
                    try (BufferedSink sink = Okio.buffer(Okio.sink(downloadsDir))) {
                        runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Iniciando descarga...", Toast.LENGTH_SHORT).show());
                        sink.writeAll(response.body().source());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Notificar al usuario sobre la descarga
                    String finalFileName = fileName;
                    runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Archivo guardado en descargas", Toast.LENGTH_SHORT).show());
                    SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                    String userName = prefs.getString("user_name", "null");

                    if (userName.equals("null")) {
                        String currentDate = getCurrentDate();
                        saveDownloadHistory(finalFileName, format.toUpperCase(), currentDate, urlText);
                        notifyServerFileDownloaded(finalFileName);
                        return;
                    }
                    ApiUtils.registerDownload(userName, urlText, finalFileName, format, new ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            runOnUiThread(() -> {
                                try {
                                    boolean success = response.getBoolean("success");
                                    String message = response.getString("message");

                                    if (success) {
                                        Toast.makeText(DownloadActivity.this, message, Toast.LENGTH_SHORT).show();
                                        Toast.makeText(DownloadActivity.this, "Se esta registrando desde la api", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(DownloadActivity.this, DownloadActivity.class);
                                        notifyServerFileDownloaded(finalFileName);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(DownloadActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }

                                } catch (Exception e) {
                                    Toast.makeText(DownloadActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> {
                                Toast.makeText(DownloadActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                    // Notificar al servidor que la descarga ha terminado

                } else {
                    runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Error en la descarga", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Método para notificar al servidor que la descarga ha finalizado
    private void notifyServerFileDownloaded(String fileName) {
        // Crear la solicitud JSON
        String json = "{\"file_name\":\"" + fileName + "\"}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        // Crear la solicitud POST
        Request request = new Request.Builder()
                .url(API_URL + "delete_file")  // La URL del servidor Flask para eliminar archivos
                .post(body)
                .build();

        // Ejecutar la solicitud en un hilo en segundo plano
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Error al notificar al servidor", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "El servidor ha recibido la notificación para eliminar el archivo", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Error al notificar al servidor", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // Método para validar si la URL es correcta
    private boolean isValidUrl(String url) {
        return android.util.Patterns.WEB_URL.matcher(url).matches();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, proceder con la descarga
            }
        }
    }

    private void saveDownloadHistory(String fileName, String format, String date, String url) {
        DownloadHistory downloadHistory = new DownloadHistory(fileName, format, date, url);

        // Usar un ExecutorService para manejar la inserción en segundo plano
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Insertar el historial en la base de datos
            this.getDatabase().downloadHistoryDao().insert(downloadHistory);
            runOnUiThread(() -> {
                Toast.makeText(DownloadActivity.this, "Descarga guardada en historial local", Toast.LENGTH_LONG).show();
            });
        });
    }


    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

}
