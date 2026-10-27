package com.smartpark.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartpark.app.R;
import com.smartpark.app.data.FirebaseRepository;
import com.smartpark.app.model.User;
import com.smartpark.app.util.BottomNavHelper;
import com.smartpark.app.util.FirebaseHelper;
import com.smartpark.app.util.Prefs;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvAvatar, tvName, tvEmail, tvDetails;
    private MaterialSwitch switchDarkTheme, switchNotifications, switchAirplaneAlert, switchOfflineCache;
    private TextView tvMenuHelp, tvMenuAbout, tvMenuLogout;

    private Prefs prefs;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseHelper.auth(this);
        prefs = new Prefs(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);

        tvAvatar = findViewById(R.id.tv_avatar);
        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvDetails = findViewById(R.id.tv_profile_details);

        switchDarkTheme = findViewById(R.id.switch_dark_theme);
        switchNotifications = findViewById(R.id.switch_notifications);
        switchAirplaneAlert = findViewById(R.id.switch_airplane_alert);
        switchOfflineCache = findViewById(R.id.switch_offline_cache);

        tvMenuHelp = findViewById(R.id.tv_menu_help);
        tvMenuAbout = findViewById(R.id.tv_menu_about);
        tvMenuLogout = findViewById(R.id.tv_menu_logout);

        // Load saved switch states
        switchDarkTheme.setChecked(prefs.isDarkTheme());
        switchNotifications.setChecked(prefs.isNotificationsEnabled());
        switchAirplaneAlert.setChecked(prefs.isAirplaneAlertEnabled());
        switchOfflineCache.setChecked(prefs.isOfflineCacheEnabled());

        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.setDarkTheme(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setNotificationsEnabled(isChecked));
        switchAirplaneAlert.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setAirplaneAlertEnabled(isChecked));
        switchOfflineCache.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setOfflineCacheEnabled(isChecked));

        tvMenuHelp.setOnClickListener(v -> startActivity(new Intent(ProfileActivity.this, HelpActivity.class)));
        tvMenuAbout.setOnClickListener(v -> showAboutDialog());
        tvMenuLogout.setOnClickListener(v -> performLogout());

        BottomNavHelper.setupBottomNav(this, R.id.nav_profile);

        loadUserData();
    }

    private void loadUserData() {
        if (prefs.isGuest()) {
            tvAvatar.setText("G");
            tvName.setText("Guest User");
            tvEmail.setText("guest@smartpark.app");
            tvDetails.setText("🚗 Guest Mode • Limited Features");
            return;
        }

        FirebaseUser firebaseUser = mAuth != null ? mAuth.getCurrentUser() : null;
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "user@smartpark.app";
            tvEmail.setText(email);

            FirebaseRepository.fetchUserProfile(firebaseUser.getUid(), new FirebaseRepository.UserCallback() {
                @Override
                public void onUserLoaded(User user) {
                    if (user != null) {
                        tvName.setText(user.getName());
                        tvDetails.setText("🚗 " + user.getVehicle() + " • 📍 " + user.getCity());

                        if (user.getName() != null && !user.getName().trim().isEmpty()) {
                            String[] parts = user.getName().trim().split(" ");
                            if (parts.length >= 2) {
                                tvAvatar.setText(("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase());
                            } else {
                                tvAvatar.setText(("" + parts[0].charAt(0)).toUpperCase());
                            }
                        }
                    } else {
                        tvName.setText("SmartPark Driver");
                        tvAvatar.setText("SP");
                    }
                }

                @Override
                public void onError(String errorMessage) {
                    tvName.setText("SmartPark Driver");
                }
            });
        }
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About SmartPark")
                .setMessage("SmartPark v1.0\n\nFind. Predict. Park.\n\nTeam:\n• Saksham Thakur\n• Krrish Oberoi\n• Harshit Sharma\n\nDeveloped for Mobile Application Development (MAD) Mini Project.")
                .setPositiveButton("Close", null)
                .show();
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    if (mAuth != null) mAuth.signOut();
                    prefs.clearSession();
                    Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
