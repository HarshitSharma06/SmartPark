package com.smartpark.app.util;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.smartpark.app.R;
import com.smartpark.app.activities.ListActivity;
import com.smartpark.app.activities.MapActivity;
import com.smartpark.app.activities.ProfileActivity;
import com.smartpark.app.activities.SavedActivity;

public class BottomNavHelper {

    public static void setupBottomNav(final Activity currentActivity, int selectedItemId) {
        BottomNavigationView bottomNav = currentActivity.findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(selectedItemId);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == selectedItemId) {
                return true;
            }

            Intent intent = null;
            if (itemId == R.id.nav_map) {
                intent = new Intent(currentActivity, MapActivity.class);
            } else if (itemId == R.id.nav_list) {
                intent = new Intent(currentActivity, ListActivity.class);
            } else if (itemId == R.id.nav_saved) {
                intent = new Intent(currentActivity, SavedActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(currentActivity, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                currentActivity.startActivity(intent);
                currentActivity.overridePendingTransition(0, 0);
                currentActivity.finish();
                return true;
            }
            return false;
        });
    }
}
