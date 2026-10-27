package com.smartpark.app.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.smartpark.app.model.ParkingLot;

import java.util.ArrayList;
import java.util.List;
public class DatabaseSeeder {

    public static List<ParkingLot> getSampleLots() {
        List<ParkingLot> lots = new ArrayList<>();

        lots.add(new ParkingLot("lot_001", "City Mall Parking", "SCO 12, Sector 17, Chandigarh", "Chandigarh",
                30.7398, 76.7826, 20, 120, 42, "AVAILABLE", true, true, "24 hours", "+911722700000", "Car,Bike", ""));

        lots.add(new ParkingLot("lot_002", "Sector 17 Basement", "Sector 17 Plaza, Chandigarh", "Chandigarh",
                30.7410, 76.7850, 15, 200, 15, "FILLING", true, false, "08:00 AM - 10:00 PM", "+911722700001", "Car", ""));

        lots.add(new ParkingLot("lot_003", "Elante Mall Parking", "Industrial Area Phase I, Chandigarh", "Chandigarh",
                30.7055, 76.8013, 30, 500, 0, "FULL", true, true, "10:00 AM - 11:00 PM", "+911722700002", "Car,Bike", ""));

        lots.add(new ParkingLot("lot_004", "Rock Garden Lot", "Sector 1, Chandigarh", "Chandigarh",
                30.7525, 76.8101, 20, 80, 55, "AVAILABLE", false, false, "09:00 AM - 07:00 PM", "+911722700003", "Car,Bike", ""));

        lots.add(new ParkingLot("lot_005", "Railway Station P2", "Chandigarh Railway Station, Daria", "Chandigarh",
                30.7082, 76.8236, 10, 150, 30, "FILLING", false, false, "24 hours", "+911722700004", "Car,Bike", ""));

        lots.add(new ParkingLot("lot_006", "Sukhna Lake Lot", "Sector 6, Chandigarh", "Chandigarh",
                30.7422, 76.8188, 20, 100, 80, "AVAILABLE", false, true, "05:00 AM - 10:00 PM", "+911722700005", "Car,Bike", ""));

        lots.add(new ParkingLot("lot_007", "PGIMER Visitor Parking", "Sector 12, Chandigarh", "Chandigarh",
                30.7628, 76.7766, 10, 300, 8, "FILLING", true, false, "24 hours", "+911722700006", "Car,Bike", ""));

        lots.add(new ParkingLot("lot_008", "Sector 22 Market Lot", "Main Market, Sector 22B, Chandigarh", "Chandigarh",
                30.7320, 76.7710, 25, 90, 0, "FULL", false, false, "09:00 AM - 10:00 PM", "+911722700007", "Car,Bike", ""));

        return lots;
    }

    public static void seedIfEmpty() {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("parking_lots");
            ref.get().addOnSuccessListener(dataSnapshot -> {
                if (!dataSnapshot.exists() || dataSnapshot.getChildrenCount() == 0) {
                    for (ParkingLot lot : getSampleLots()) {
                        ref.child(lot.getLotId()).setValue(lot);
                    }
                }
            });
        } catch (Exception e) {
            // Firebase might not be initialized if google-services.json is missing; handle gracefully
            e.printStackTrace();
        }
    }
}
