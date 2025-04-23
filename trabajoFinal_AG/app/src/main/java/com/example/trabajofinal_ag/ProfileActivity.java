package com.example.trabajofinal_ag;

import android.content.Intent;
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

public class ProfileActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private ImageView toggle;

    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

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
        TextView registrarse = findViewById(R.id.registrarse);

        // Acciones
        registrarse.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, RegisterActivity.class);
            startActivity(intent);
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

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.error_no_values, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
