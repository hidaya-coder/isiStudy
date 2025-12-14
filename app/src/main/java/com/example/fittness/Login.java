package com.example.fittness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        TextView textSignup = findViewById(R.id.textSignup);

        textSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Signup.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();
                boolean correct = true;

                if (email.isEmpty()) {
                    editEmail.setError("Email is required");
                    correct = false;
                } else if (!email.endsWith("@gmail.com")) {
                    editEmail.setError("Must be a @gmail.com address");
                    correct = false;
                }

                if (password.isEmpty()) {
                    editPassword.setError("Password is required");
                    correct = false;
                }

                if (correct) {
                    UserManager userManager = new UserManager(Login.this);
                    
                    if (userManager.validateUser(email, password)) {
                        android.util.Log.d("Login", "Credentials valid, attempting login for: " + email);
                        // 1. SAUVEGARDER LE STATUT DE CONNEXION
                        AuthHelper.setLoggedIn(Login.this, email);

                        // 2. Créer l'intent pour Home
                        Intent intent = new Intent(Login.this, Home.class);

                        // 3. Optionnel : passer l'email en extra
                        intent.putExtra("email", email);

                        // 4. Démarrer Home
                        startActivity(intent);

                        // 5. IMPORTANT : Fermer l'activité Login
                        finish();

                        // 6. Message de confirmation
                        Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}