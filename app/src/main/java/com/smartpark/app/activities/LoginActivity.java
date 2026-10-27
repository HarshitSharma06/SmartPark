package com.smartpark.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.smartpark.app.R;
import com.smartpark.app.util.FirebaseHelper;
import com.smartpark.app.util.Prefs;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRemember;
    private TextView tvForgotPassword, tvSignupLink;
    private MaterialButton btnLogin, btnGuest;

    private FirebaseAuth mAuth;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseHelper.auth(this);
        prefs = new Prefs(this);

        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        tvSignupLink = findViewById(R.id.tv_signup_link);
        btnLogin = findViewById(R.id.btn_login);
        btnGuest = findViewById(R.id.btn_guest);

        /* remember me email login */
        if (prefs.isRememberMe()) {
            cbRemember.setChecked(true);
            etEmail.setText(prefs.getSavedEmail());
        }

        btnLogin.setOnClickListener(v -> performLogin());
        btnGuest.setOnClickListener(v -> continueAsGuest());
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        tvSignupLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (mAuth == null) {
            Toast.makeText(this, "Sign-in is unavailable (Firebase not configured). Use Continue as Guest.", Toast.LENGTH_LONG).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    prefs.setGuest(false);
                    prefs.setRememberMe(cbRemember.isChecked());
                    if (cbRemember.isChecked()) {
                        prefs.setSavedEmail(email);
                    } else {
                        prefs.setSavedEmail("");
                    }
                    Toast.makeText(LoginActivity.this, "Welcome to SmartPark!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MapActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(getString(R.string.login_title));
                    Toast.makeText(LoginActivity.this, "Login Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void continueAsGuest() {
        prefs.setGuest(true);
        Toast.makeText(this, "Logged in as Guest", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MapActivity.class));
        finish();
    }

    private void showForgotPasswordDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter your email address");
        String currentEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (!currentEmail.isEmpty()) {
            input.setText(currentEmail);
        }

        new AlertDialog.Builder(this)
                .setTitle("Reset Password")
                .setMessage("We will send a password reset link to your email.")
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    if (mAuth == null) {
                        Toast.makeText(LoginActivity.this, "Firebase not configured", Toast.LENGTH_SHORT).show();
                    } else if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(LoginActivity.this, "Reset link sent to " + email, Toast.LENGTH_LONG).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
