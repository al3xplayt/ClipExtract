package com.example.trabajofinal_ag;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import Adapters.ClipAdapter;
import Api.ApiUtils;
import Api.ApiCallback;
import Models.Clip;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GenerateClipsActivity extends AppCompatActivity {

    // Declaración de adapter como variable de clase
    private ClipAdapter clipAdapter;

    // ... tus variables actuales
    private static final int PICK_FILE_REQUEST = 1;

    BottomNavigationView bottomNavigationView;
    ProgressBar uploadProgress;
    Button extractClipsButton;
    ImageView preview;
    private File uploadedFile = null;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_clips);
        EdgeToEdge.enable(this);

        // Configuración del edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        uploadProgress = findViewById(R.id.uploadProgress);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_clip);
        extractClipsButton = findViewById(R.id.extractClip);
        extractClipsButton.setEnabled(false);

        recyclerView = findViewById(R.id.clipRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Inicializa adapter vacío
        clipAdapter = new ClipAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(clipAdapter);
        recyclerView.setVisibility(GONE);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, DownloadActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, LogInActivity.class));
                return true;
            }
            return false;
        });

        ImageView uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(v -> openFilePicker());

        extractClipsButton.setOnClickListener(v -> {
            if (uploadedFile != null) {
                extractClips(uploadedFile.getName());
            } else {
                Toast.makeText(this, "Primero sube un video", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Resto de métodos existentes sin cambios
    // openFilePicker(), onActivityResult(), showVideoThumbnail(), getFileFromUri(), getFileNameFromUri(), uploadFile()

    private void extractClips(String fileName) {
        uploadProgress.setVisibility(VISIBLE);

        ApiUtils.extractClipsFromFile(fileName, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    uploadProgress.setVisibility(GONE);
                    try {
                        String fileName = response.getString("filename");
                        JSONArray clipsArray = response.getJSONArray("clips");
                        List<Clip> clips = new ArrayList<>();
                        for (int i = 0; i < clipsArray.length(); i++) {
                            JSONObject obj = clipsArray.getJSONObject(i);
                            double start = obj.getDouble("start");
                            double end = obj.getDouble("end");
                            clips.add(new Clip(fileName, start, end));
                        }

                        if (clips.isEmpty()) {
                            recyclerView.setVisibility(GONE);
                            Toast.makeText(getApplicationContext(), "No se detectaron clips", Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Liberar players antiguos antes de asignar nuevo adapter
                        if (clipAdapter != null) {
                            clipAdapter.releasePlayers();
                        }

                        clipAdapter = new ClipAdapter(clips, GenerateClipsActivity.this);
                        recyclerView.setAdapter(clipAdapter);
                        recyclerView.setVisibility(VISIBLE);
                        preview.setVisibility(GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error al leer clips", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    uploadProgress.setVisibility(GONE);
                    Toast.makeText(getApplicationContext(), "Error al extraer: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/mp4");
        startActivityForResult(Intent.createChooser(intent, "Selecciona un video MP4"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedUri = data.getData();
            File file = getFileFromUri(selectedUri);

            if (file != null) {
                showVideoThumbnail(file);
                uploadedFile = file;
                uploadFile(file);
            } else {
                Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showVideoThumbnail(File file) {
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
                file.getAbsolutePath(),
                MediaStore.Video.Thumbnails.MINI_KIND
        );
        runOnUiThread(() -> {
            preview = findViewById(R.id.previewThumbnail);
            preview.setImageBitmap(thumbnail);
        });
    }

    private File getFileFromUri(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        File file = new File(getCacheDir(), fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) result = cursor.getString(index);
                }
            }
        }
        if (result == null) result = uri.getLastPathSegment();
        return result;
    }

    private void uploadFile(File file) {
        uploadProgress.setVisibility(VISIBLE);
        ApiUtils.uploadFile(file, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    uploadProgress.setVisibility(GONE);
                    extractClipsButton.setEnabled(true); // habilitar botón
                    Toast.makeText(getApplicationContext(), "Video subido correctamente", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    uploadProgress.setVisibility(GONE);
                    Toast.makeText(getApplicationContext(), "Error al subir: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
