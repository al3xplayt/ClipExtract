package com.example.trabajofinal_ag;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import Api.ApiCallback;
import Api.ApiUtils;

public class LogInActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private ImageView toggle;
    TextView registrarse, invitado;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar vistas
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.etPassword);

        toggle = findViewById(R.id.showPasswordToggle);
        btnLogin = findViewById(R.id.iniciar_sesion);
        registrarse = findViewById(R.id.registrarse);

        // Acciones
        registrarse.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        invitado = findViewById(R.id.invitado);
        invitado.setOnClickListener(v -> {
            Intent intent = new Intent(LogInActivity.this, DownloadActivity.class);
            SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("user_name", "null");
            editor.apply();
            startActivity(intent);
            finish();
        });

        toggle.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggle.setImageResource(R.drawable.ic_eye); // Mostrar ojo abierto
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggle.setImageResource(R.drawable.ic_eye_off); // Mostrar ojo cerrado
            }
            etPassword.setSelection(etPassword.length()); // Mantener cursor al final
        });

        // Validación (ejemplo simple, debería ir en botón de login si lo tienes)
        btnLogin.setOnClickListener(
                v -> {
                    validarCampos();
                }
        );
    }

    private void validarCampos() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etEmail.getText().toString().trim();
        if ((email.isEmpty() || username.isEmpty()) || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiUtils.loginUser(email, password, username, new ApiCallback() {

            @Override
            public void onSuccess(JSONObject response) {
                runOnUiThread(() -> {
                    try {
                        boolean success = response.getBoolean("success");
                        String message = response.getString("message");

                        if (success) {
                            Toast.makeText(LogInActivity.this, message, Toast.LENGTH_SHORT).show();

                            String userName = response.getString("user_name");
                            SharedPreferences prefs = getSharedPreferences("user_data", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("user_name", userName);
                            editor.apply();

                            Intent intent = new Intent(LogInActivity.this, DownloadActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LogInActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Toast.makeText(LogInActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(LogInActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}

