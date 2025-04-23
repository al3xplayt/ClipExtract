package com.example.trabajofinal_ag;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.List;

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
        downloadHistoryList = MainActivity.getDatabase().downloadHistoryDao().getAllHistory();

        // Configurar el adaptador con los datos
        adapter = new HistoryAdapter(downloadHistoryList, this);
        recyclerView.setAdapter(adapter);

        // Configurar el botón de "Atrás"
        back.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
            startActivity(intent);
        });
        deleteHistoryButton.setOnClickListener(v -> deleteHistory());
    }

    // Método para borrar el historial
    private void deleteHistory() {
        // Borrar todos los registros del historial desde la base de datos
        MainActivity.getDatabase().downloadHistoryDao().deleteAllHistory();

        // Actualizar la lista en la interfaz
        downloadHistoryList.clear();
        adapter.notifyDataSetChanged();

        // Mostrar un mensaje al usuario
        Toast.makeText(this, this.getText(R.string.deleteHistory), Toast.LENGTH_SHORT).show();
    }
}




