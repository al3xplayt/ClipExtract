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
import android.widget.ProgressBar;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Api.ApiCallback;
import Api.ApiUtils;
import Database.AppDatabase;
import Models.DownloadHistory;
import okhttp3.*;
import okio.BufferedSink;
import okio.Okio;

public class DownloadActivity extends AppCompatActivity {

    EditText url;
    Button download;
    Spinner formatSpinner;
    OkHttpClient client = new OkHttpClient();
    BottomNavigationView bottomNavigationView;

    ProgressBar downloadProgressBar;

    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 1;
    private static AppDatabase db;

    private static final String API_URL = ApiUtils.getBaseUrl();

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
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        if (bundle != null) {
            String url = bundle.getString("url");
            String format = bundle.getString("format");
            startDownload(url, format);
        }

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "download_database")
                .allowMainThreadQueries() // Solo para pruebas, no recomendado en producción
                .build();

        // Inicialización de vistas
        url = findViewById(R.id.Url);
        download = findViewById(R.id.btt_desc);
        formatSpinner = findViewById(R.id.formatSpinner);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);

        // Configurar Spinner con opciones
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.format_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);

        // Verificar permisos de escritura en almacenamiento externo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
            }
        }

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String userName = prefs.getString("user_name", null);

        // Acción del botón descargar
        download.setOnClickListener(v -> {
            String urlText = url.getText().toString().trim();

            if (urlText.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_url_empty), Toast.LENGTH_SHORT).show();
            } else if (!isValidUrl(urlText)) {
                Toast.makeText(this, getString(R.string.error_url_invalid), Toast.LENGTH_SHORT).show();
            } else {
                String selectedFormat = formatSpinner.getSelectedItem().toString().toLowerCase();
                startDownload(urlText, selectedFormat);
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_history) {
                startActivity(new Intent(DownloadActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
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

    private void startDownload(String urlText, String format) {
        String json = "{\"url\":\"" + urlText + "\", \"format\":\"" + format + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Enviando solicitud...", Toast.LENGTH_SHORT).show());

        Request request = new Request.Builder()
                .url(API_URL + "download")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Error al descargar", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null && response.body().contentLength() > 0) {
                    runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Iniciando descarga...", Toast.LENGTH_SHORT).show());
                    String fileName;
                    String contentDisposition = response.header("Content-Disposition");

                    if (contentDisposition != null && contentDisposition.contains("filename=")) {
                        fileName = contentDisposition.split("filename=")[1].replace("\"", "");
                    } else {
                        fileName = "video_descargado.mp4";
                    }

                    File downloadsDir = new File(getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

                    try (BufferedSink sink = Okio.buffer(Okio.sink(downloadsDir))) {
                        sink.writeAll(response.body().source());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Archivo guardado en descargas", Toast.LENGTH_SHORT).show());

                    SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                    String userName = prefs.getString("user_name", "null");

                    if (userName.equals("null")) {
                        String currentDate = getCurrentDate();
                        saveDownloadHistory(fileName, format.toUpperCase(), currentDate, urlText);
                        notifyServerFileDownloaded(fileName);
                        return;
                    }

                    ApiUtils.registerDownload(userName, urlText, fileName, format, new ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            runOnUiThread(() -> {
                                try {
                                    boolean success = response.getBoolean("success");
                                    String message = response.getString("message");

                                    if (success) {
                                        Intent intent = new Intent(DownloadActivity.this, DownloadActivity.class);
                                        notifyServerFileDownloaded(fileName);
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
                } else {
                    runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Error en la descarga", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void notifyServerFileDownloaded(String fileName) {
        String json = "{\"file_name\":\"" + fileName + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL + "delete_file")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { }

            @Override
            public void onResponse(Call call, Response response) throws IOException { }
        });
    }

    private boolean isValidUrl(String url) {
        return android.util.Patterns.WEB_URL.matcher(url).matches();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, puedes manejar lógica adicional aquí si quieres
            }
        }
    }

    private void saveDownloadHistory(String fileName, String format, String date, String url) {
        DownloadHistory downloadHistory = new DownloadHistory(fileName, format, date, url);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            this.getDatabase().downloadHistoryDao().insert(downloadHistory);
            runOnUiThread(() -> Toast.makeText(DownloadActivity.this, "Descarga guardada en historial local", Toast.LENGTH_LONG).show());
        });
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String extractYoutubeId(String url) {
        String pattern = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.be)/(watch\\?v=)?([^&]+)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(5);
        }
        return null;
    }
}
