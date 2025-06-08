package com.example.trabajofinal_ag;

import static android.os.Environment.getExternalStoragePublicDirectory;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Api.ApiCallback;
import Api.ApiUtils;
import Database.AppDatabase;
import Models.DownloadHistory;
import okhttp3.*;

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

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "download_database")
                .allowMainThreadQueries()
                .build();

        url = findViewById(R.id.Url);
        download = findViewById(R.id.btt_desc);
        formatSpinner = findViewById(R.id.formatSpinner);
        downloadProgressBar = findViewById(R.id.downloadProgressBar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.format_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE_PERMISSION);
            }
        }

        SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String userName = prefs.getString("user_name", null);

        download.setOnClickListener(v -> {
            String urlText = url.getText().toString().trim();
            if (urlText.isEmpty()) {
                Toast.makeText(this, R.string.error_url_empty, Toast.LENGTH_SHORT).show();
            } else if (!isValidUrl(urlText)) {
                Toast.makeText(this, R.string.error_url_invalid, Toast.LENGTH_SHORT).show();
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
                if (userName == null || userName.equals("null")) {
                    startActivity(new Intent(this, LogInActivity.class));
                } else {
                    startActivity(new Intent(this, ProfileActivity.class));
                }
                return true;
            } else if (itemId == R.id.nav_clip) {
                if (userName == null) {
                    new AlertDialog.Builder(this)
                            .setTitle("Acceso restringido")
                            .setMessage("Debes iniciar sesión para acceder a esta función.")
                            .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                            .setNegativeButton("Iniciar sesión", (dialog, which) -> {
                                startActivity(new Intent(this, LogInActivity.class));
                                dialog.dismiss();
                            }).show();
                    return true;
                } else {
                    startActivity(new Intent(this, GenerateClipsActivity.class));
                    return true;
                }
            }
            return false;
        });
    }

    public static AppDatabase getDatabase() {
        return db;
    }

    private void startDownload(String urlText, String format) {
        runOnUiThread(() -> {
            downloadProgressBar.setVisibility(ProgressBar.VISIBLE);
            downloadProgressBar.setProgress(0);
        });

        String json = "{\"url\":\"" + urlText + "\", \"format\":\"" + format + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL + "download")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(DownloadActivity.this, "Error al descargar", Toast.LENGTH_SHORT).show();
                    downloadProgressBar.setVisibility(ProgressBar.GONE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(DownloadActivity.this, "Error en la descarga", Toast.LENGTH_SHORT).show();
                        downloadProgressBar.setVisibility(ProgressBar.GONE);
                    });
                    return;
                }

                String fileName = "video_descargado.mp4";
                String contentDisposition = response.header("Content-Disposition");

                if (contentDisposition != null) {
                    Matcher matcher = Pattern.compile("filename\\*=UTF-8''([^;]+)").matcher(contentDisposition);
                    if (matcher.find()) {
                        fileName = java.net.URLDecoder.decode(matcher.group(1), "UTF-8");
                    } else if (contentDisposition.contains("filename=")) {
                        fileName = contentDisposition.split("filename=")[1].replace("\"", "").trim();
                    }
                }

                File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

                try (InputStream inputStream = response.body().byteStream();
                     FileOutputStream outputStream = new FileOutputStream(downloadsDir)) {

                    long fileSize = response.body().contentLength();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytesRead = 0;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        int progress = (int) ((totalBytesRead * 100) / fileSize);
                        int finalProgress = progress;
                        runOnUiThread(() -> downloadProgressBar.setProgress(finalProgress));
                    }

                    runOnUiThread(() -> {
                        downloadProgressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(DownloadActivity.this, "Archivo guardado en descargas", Toast.LENGTH_SHORT).show();
                    });

                } catch (IOException e) {
                    runOnUiThread(() -> {
                        downloadProgressBar.setVisibility(ProgressBar.GONE);
                        Log.e("DownloadActivity", "Error guardando archivo", e);
                        Toast.makeText(DownloadActivity.this, "Error guardando archivo", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                String userName = prefs.getString("user_name", null);
                String currentDate = getCurrentDate();

                if (userName == null || userName.equals("null")) {
                    saveDownloadHistory(fileName, format.toUpperCase(), currentDate, urlText);
                    notifyServerFileDownloaded(fileName);
                    return;
                }

                String finalFileName = fileName;
                ApiUtils.registerDownload(userName, urlText, fileName, format, new ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        runOnUiThread(() -> {
                            notifyServerFileDownloaded(finalFileName);
                            Toast.makeText(DownloadActivity.this, "Descarga registrada", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            Log.e("DownloadActivity", "Error al registrar descarga", e);
                            Toast.makeText(DownloadActivity.this, "Error al registrar descarga", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
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
            @Override public void onFailure(Call call, IOException e) { }
            @Override public void onResponse(Call call, Response response) { }
        });
    }

    private boolean isValidUrl(String url) {
        return android.util.Patterns.WEB_URL.matcher(url).matches();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE_PERMISSION && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveDownloadHistory(String fileName, String format, String date, String url) {
        DownloadHistory downloadHistory = new DownloadHistory(fileName, format, date, url);

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            getDatabase().downloadHistoryDao().insert(downloadHistory);
            runOnUiThread(() -> Toast.makeText(this, "Historial local guardado", Toast.LENGTH_SHORT).show());
        });
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
