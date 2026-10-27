package com.smartpark.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.smartpark.app.R;
import com.smartpark.app.data.FirebaseRepository;
import com.smartpark.app.model.User;
import com.smartpark.app.util.FirebaseHelper;
import com.smartpark.app.util.Prefs;


public class SignupActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilMobile, tilPassword;
    private TextInputEditText etName, etEmail, etMobile, etPassword;
    private RadioGroup rgVehicle;
    private Spinner spinnerCity;
    private CheckBox cbTerms;
    private MaterialButton btnSignup;
    private TextView tvLoginLink;

    private FirebaseAuth mAuth;
    private Prefs prefs;

    private static final String[] CITIES = {"Chandigarh", "Mohali", "Panchkula", "Delhi", "Mumbai"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseHelper.auth(this);
        prefs = new Prefs(this);

        tilName = findViewById(R.id.til_name);
        tilEmail = findViewById(R.id.til_email);
        tilMobile = findViewById(R.id.til_mobile);
        tilPassword = findViewById(R.id.til_password);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etMobile = findViewById(R.id.et_mobile);
        etPassword = findViewById(R.id.et_password);

        rgVehicle = findViewById(R.id.rg_vehicle);
        spinnerCity = findViewById(R.id.spinner_city);
        cbTerms = findViewById(R.id.cb_terms);
        btnSignup = findViewById(R.id.btn_signup);
        tvLoginLink = findViewById(R.id.tv_login_link);

        // Populate Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CITIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);

        btnSignup.setOnClickListener(v -> performSignup());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void performSignup() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String mobile = etMobile.getText() != null ? etMobile.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilName.setError(null);
        tilEmail.setError(null);
        tilMobile.setError(null);
        tilPassword.setError(null);

        if (name.isEmpty()) {
            tilName.setError("Name is required");
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            return;
        }

        if (mobile.length() < 10) {
            tilMobile.setError("Enter a valid 10-digit mobile number");
            return;
        }

        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedVehicleId = rgVehicle.getCheckedRadioButtonId();
        RadioButton rbVehicle = findViewById(selectedVehicleId);
        String vehicle = rbVehicle != null ? rbVehicle.getText().toString() : "Car";
        String city = spinnerCity.getSelectedItem() != null ? spinnerCity.getSelectedItem().toString() : "Chandigarh";

        if (mAuth == null) {
            Toast.makeText(this, "Sign-up is unavailable (Firebase not configured).", Toast.LENGTH_LONG).show();
            return;
        }

        btnSignup.setEnabled(false);
        btnSignup.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser() != null ? authResult.getUser().getUid() : "";
                    User user = new User(uid, name, email, mobile, vehicle, city);
                    FirebaseRepository.saveUserProfile(user);

                    prefs.setGuest(false);
                    Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignupActivity.this, MapActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSignup.setEnabled(true);
                    btnSignup.setText(getString(R.string.signup_title));
                    Toast.makeText(SignupActivity.this, "Sign Up Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
