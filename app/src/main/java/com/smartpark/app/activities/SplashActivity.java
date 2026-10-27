package com.smartpark.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.smartpark.app.R;
import com.smartpark.app.data.DatabaseSeeder;
import com.smartpark.app.util.FirebaseHelper;
import com.smartpark.app.util.Prefs;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Apply saved theme preference
        AppCompatDelegate.setDefaultNightMode(new Prefs(this).isDarkTheme()
                ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Seed initial sample data into Firebase if empty (skipped when Firebase isn't configured)
        if (FirebaseHelper.isAvailable(this)) {
            DatabaseSeeder.seedIfEmpty();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Prefs prefs = new Prefs(this);
            FirebaseAuth auth = FirebaseHelper.auth(this);
            boolean isLoggedIn = auth != null && auth.getCurrentUser() != null;
            boolean isGuest = prefs.isGuest();

            Intent intent;
            if (isLoggedIn || isGuest) {
                intent = new Intent(SplashActivity.this, MapActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, 2000);
    }
}
