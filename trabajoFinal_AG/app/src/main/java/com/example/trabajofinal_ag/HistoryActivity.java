package com.example.trabajofinal_ag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Api.ApiCallback;
import Api.ApiUtils;
import Database.DownloadHistory;
import Database.HistoryAdapter;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<DownloadHistory> downloadHistoryList;
    private ImageButton back;

    private ImageView deleteHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        /*getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new WaterMarkFragment())
                .commit();*/

        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.rcView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        back = findViewById(R.id.myImageButton);
        deleteHistoryButton = findViewById(R.id.ic_delete);
        // Obtener los datos del historial desde la base de datos
        SharedPreferences pref = getSharedPreferences("user_data", MODE_PRIVATE);
        String userName = pref.getString("user_name", "null");
        if (userName.equals("null")) {
            downloadHistoryList = DownloadActivity.getDatabase().downloadHistoryDao().getAllHistory();

            // Configurar el adaptador con los datos
            adapter = new HistoryAdapter(downloadHistoryList, this);
            recyclerView.setAdapter(adapter);

            // Configurar el botón de "Atrás"
            back.setOnClickListener(v -> {
                Intent intent = new Intent(HistoryActivity.this, DownloadActivity.class);
                startActivity(intent);
            });
            deleteHistoryButton.setOnClickListener(v -> deleteHistory());

        } else {
            ApiUtils.fetchDownloadHistory(userName, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        JSONArray historyArray = response.getJSONArray("history");

                        downloadHistoryList = new ArrayList<>();
                        for (int i = 0; i < historyArray.length(); i++) {
                            JSONObject obj = historyArray.getJSONObject(i);
                            DownloadHistory item = new DownloadHistory();
                            item.setFileName(obj.getString("filename"));
                            item.setFormat(obj.getString("formato"));
                            item.setUrl(obj.getString("video_url"));
                            item.setDate(obj.getString("fecha_descarga"));
                            downloadHistoryList.add(item);
                        }

                        // Actualizar la interfaz de usuario en el hilo principal
                        runOnUiThread(() -> {
                            adapter = new HistoryAdapter(downloadHistoryList, HistoryActivity.this);
                            recyclerView.setAdapter(adapter);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(HistoryActivity.this, "Error al obtener historial", Toast.LENGTH_SHORT).show()
                    );
                }
            });
            deleteHistoryButton.setOnClickListener(v -> deleteHistoryApi(userName));
        }
        // Configurar el botón de "Atrás"
        back.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, DownloadActivity.class);
            startActivity(intent);
        });
    }

    // Método para borrar el historial
    private void deleteHistory() {
        // Borrar todos los registros del historial desde la base de datos
        DownloadActivity.getDatabase().downloadHistoryDao().deleteAllHistory();

        // Actualizar la lista en la interfaz
        downloadHistoryList.clear();
        adapter.notifyDataSetChanged();

        // Mostrar un mensaje al usuario
        Toast.makeText(this, this.getText(R.string.deleteHistory), Toast.LENGTH_SHORT).show();
    }
    private void deleteHistoryApi(String username) {
        ApiUtils.deleteDownloadHistory(username, new ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_SHORT).show();
                            downloadHistoryList.clear();
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(HistoryActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}