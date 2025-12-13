package com.example.fittness;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    private EditText editFullName, editEmail, editPassword, editConfirmPassword;
    private Button btnSignup;
    private TextView textLogin;

    // Minimum 8 chars, at least 1 letter and 1 number
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Init Views
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        textLogin = findViewById(R.id.textLogin);

        // Sign Up Click
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    performSignup();
                }
            }
        });

        // Login Link Click
        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Return to Login
            }
        });
    }

    private boolean validateInputs() {
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty()) {
            editFullName.setError("Full name is required");
            return false;
        }

        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Invalid email address");
            return false;
        }

        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            return false;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            editPassword.setError("Password must be at least 8 chars (letters + numbers)");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void performSignup() {
        // Here you would normally save the user to a database
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

        // Redirect to Login
        finish();
    }
}