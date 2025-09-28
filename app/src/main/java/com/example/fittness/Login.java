package com.example.fittness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();
                boolean correct = true;
                if (email.isEmpty()) {
                    editEmail.setError("email is required");
                    correct = false;
                }
                if (password.isEmpty()) {
                    editPassword.setError("password is required");
                    correct = false;
                }

                if (correct) {
                    Intent intent = new Intent(Login.this,Home.class);
                    startActivity(intent);
                }

            }
        });
    }
}