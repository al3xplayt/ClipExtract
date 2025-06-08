package com.example.trabajofinal_ag;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import Api.ApiUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {

    private EditText etUserName;
    private EditText etEmail;
    private Button btnSave;

    private final OkHttpClient client = new OkHttpClient();

    private final String API_BASE_URL = ApiUtils.getBaseUrl();

    private Button btnLogout;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        EdgeToEdge.enable(this);

        etUserName = findViewById(R.id.etUserName);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        btnLogout = findViewById(R.id.logout);

        // Obtener datos pasados desde DownloadActivity (opcional)
        prefs = getSharedPreferences("user_data", MODE_PRIVATE);
        String userName = prefs.getString("user_name", null);

        // Cargar perfil desde backend
        fetchUserProfile(userName);

        btnSave.setOnClickListener(v -> {
            String updatedUserName = etUserName.getText().toString().trim();
            String updatedEmail = etEmail.getText().toString().trim();

            if (updatedUserName.isEmpty()) {
                etUserName.setError("El nombre no puede estar vacío");
                return;
            }

            updateUserProfile(userName ,updatedUserName, updatedEmail);
        });

        btnLogout.setOnClickListener(v -> {
            prefs.edit().putString("user_name", null).apply();
            Toast.makeText(ProfileActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void fetchUserProfile(String userName) {
        Request request = new Request.Builder()
                .url(API_BASE_URL + "user/profile?username="+userName)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ProfileActivity.this, "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String respBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(respBody);
                        final String userName = json.getString("user_name");
                        final String email = json.getString("email");

                        runOnUiThread(() -> {
                            etUserName.setText(userName);
                            etEmail.setText(email);
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(ProfileActivity.this, "Error al cargar perfil", Toast.LENGTH_LONG).show()
                    );
                }
            }
        });
    }

    private void updateUserProfile(String username, String NewuserName, String email) {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String jsonBody = "{"
                + "\"username\":\"" + username + "\","
                + "\"user_name\":\"" + NewuserName + "\","
                + "\"email\":\"" + email + "\""
                + "}";

        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(API_BASE_URL + "user/profile") // Cambia por tu URL real
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ProfileActivity.this, "Error al conectar: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String respBody = response.body().string();

                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        prefs.edit().putString("user_name", NewuserName).apply();
                        Toast.makeText(ProfileActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error al actualizar: " + respBody, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}
