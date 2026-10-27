package com.smartpark.app.util;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Firebase is only initialised when google-services.json is present at build time.
 * Calling FirebaseAuth.getInstance() without it throws IllegalStateException, which used to
 * crash the app on the splash screen. These helpers let the app degrade to guest / sample data.
 */
public class FirebaseHelper {

    public static boolean isAvailable(Context context) {
        try {
            return !FirebaseApp.getApps(context.getApplicationContext()).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /** @return FirebaseAuth, or null if Firebase is not configured. */
    public static FirebaseAuth auth(Context context) {
        return isAvailable(context) ? FirebaseAuth.getInstance() : null;
    }
}
