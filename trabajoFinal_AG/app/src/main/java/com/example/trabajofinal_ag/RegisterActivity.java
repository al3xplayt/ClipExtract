package com.example.trabajofinal_ag;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import Api.ApiCallback;
import Api.ApiUtils;

public class RegisterActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etSurname, etUser;
    Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etSurname = findViewById(R.id.etApellidos);
        etUser = findViewById(R.id.etUsuario);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        EditText passwordEditText = findViewById(R.id.etPassword);
        ImageView toggle = findViewById(R.id.showPasswordToggle);

        toggle.setOnClickListener(v -> {
            if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Mostrar contraseña
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggle.setImageResource(R.drawable.ic_eye); // ojo abierto
            } else {
                // Ocultar contraseña
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggle.setImageResource(R.drawable.ic_eye_off); // ojo cerrado
            }
            // Mantener cursor al final
            passwordEditText.setSelection(passwordEditText.length());
        });

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String surname = etSurname.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String username = etUser.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Formato de correo inválido", Toast.LENGTH_SHORT).show();
                return;
            }
            ApiUtils.registerUser(name,surname, email, password, username, new ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    runOnUiThread(() -> {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success){
                                Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                finish();
                            } else{
                                String errorMessage = response.getString("message");
                                Toast.makeText(RegisterActivity.this, errorMessage , Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "Error de red", Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}
