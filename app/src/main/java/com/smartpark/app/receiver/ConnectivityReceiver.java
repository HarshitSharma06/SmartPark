package com.smartpark.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            boolean isAirplaneModeOn = Settings.Global.getInt(
                    context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

            if (isAirplaneModeOn) {
                Toast.makeText(context, "✈️ Airplane Mode ON: You are offline. Showing cached parking data.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "🌐 Online: Syncing live parking data...", Toast.LENGTH_SHORT).show();
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if (!isConnected) {
                    Toast.makeText(context, "⚠️ Network Disconnected: Using cached parking data.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
