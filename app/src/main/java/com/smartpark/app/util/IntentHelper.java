package com.smartpark.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;


public class IntentHelper {

    public static void openNavigation(Context context, double lat, double lng, String name) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng + "&mode=d");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        } else {
            // Fallback to web browser or general geo intent
            Uri geoUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + Uri.encode(name));
            Intent fallback = new Intent(Intent.ACTION_VIEW, geoUri);
            try {
                context.startActivity(fallback);
            } catch (Exception e) {
                Toast.makeText(context, "No navigation app available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void makePhoneCall(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Cannot dial number", Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareParkingLot(Context context, String name, String address, int price, double lat, double lng) {
        String shareText = "Check out this parking spot on SmartPark!\n\n" +
                "📍 " + name + "\n" +
                "Address: " + address + "\n" +
                "Price: ₹" + price + "/hr\n" +
                "Location: https://maps.google.com/?q=" + lat + "," + lng;

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Parking Spot");
        context.startActivity(shareIntent);
    }
}
