package com.smartpark.app.data;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.model.ReportEntry;
import com.smartpark.app.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirebaseRepository {

    public interface ParkingLotsCallback {
        void onDataLoaded(List<ParkingLot> lots);
        void onError(String errorMessage);
    }

    public interface UserCallback {
        void onUserLoaded(User user);
        void onError(String errorMessage);
    }

    public static void fetchParkingLots(ParkingLotsCallback callback) {
        fetchParkingLots(null, callback);
    }

    public static void fetchParkingLots(android.content.Context context, ParkingLotsCallback callback) {
        // Firebase not configured (no google-services.json): use cached / sample data.
        if (context != null && FirebaseApp.getApps(context.getApplicationContext()).isEmpty()) {
            callback.onDataLoaded(getFallbackLots(context));
            return;
        }

        // The Realtime Database never calls back while offline with no local cache,
        // so deliver fallback data after a short timeout and let live data replace it.
        final AtomicBoolean delivered = new AtomicBoolean(false);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (delivered.compareAndSet(false, true)) {
                callback.onDataLoaded(getFallbackLots(context));
            }
        }, 4000);

        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("parking_lots");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    delivered.set(true);
                    List<ParkingLot> lots = new ArrayList<>();
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ParkingLot lot = child.getValue(ParkingLot.class);
                            if (lot != null) {
                                lots.add(lot);
                            }
                        }
                    }
                    if (lots.isEmpty()) {
                        lots = getFallbackLots(context);
                    } else if (context != null) {
                        try {
                            SmartParkDbHelper dbHelper = new SmartParkDbHelper(context);
                            dbHelper.cacheLots(lots);
                        } catch (Exception ignored) {}
                    }
                    callback.onDataLoaded(lots);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    delivered.set(true);
                    callback.onDataLoaded(getFallbackLots(context));
                }
            });
        } catch (Exception e) {
            delivered.set(true);
            callback.onDataLoaded(getFallbackLots(context));
        }
    }

    private static List<ParkingLot> getFallbackLots(android.content.Context context) {
        if (context != null) {
            try {
                SmartParkDbHelper dbHelper = new SmartParkDbHelper(context);
                List<ParkingLot> cached = dbHelper.getCachedLots();
                if (cached != null && !cached.isEmpty()) {
                    return cached;
                }
            } catch (Exception ignored) {}
        }
        return DatabaseSeeder.getSampleLots();
    }

    public static void saveUserProfile(User user) {
        try {
            if (user != null && user.getUid() != null) {
                FirebaseDatabase.getInstance().getReference("users")
                        .child(user.getUid()).setValue(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fetchUserProfile(String uid, UserCallback callback) {
        try {
            FirebaseDatabase.getInstance().getReference("users").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                callback.onUserLoaded(user);
                            } else {
                                callback.onUserLoaded(null);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            callback.onError(error.getMessage());
                        }
                    });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public static void submitReport(ReportEntry report) {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("reports");
            String pushId = ref.push().getKey();
            if (pushId != null) {
                report.setPushId(pushId);
                ref.child(pushId).setValue(report);
            }
            if (report.getLotId() != null && report.getStatus() != null) {
                FirebaseDatabase.getInstance().getReference("parking_lots")
                        .child(report.getLotId()).child("status").setValue(report.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
