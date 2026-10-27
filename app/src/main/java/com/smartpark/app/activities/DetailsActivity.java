package com.smartpark.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.smartpark.app.R;
import com.smartpark.app.ai.ParkingPredictor;
import com.smartpark.app.ai.PredictionResult;
import com.smartpark.app.data.CsvHelper;
import com.smartpark.app.data.SmartParkDbHelper;
import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.util.IntentHelper;

import java.io.File;
public class DetailsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvName, tvStatus, tvAddress;
    private TextView tvDistance, tvPrice, tvSlots;
    private TextView tvAiChance, tvAiRec;
    private ProgressBar progressAiChance;

    private MaterialButton btnAiWhatIf, btnNavigate, btnCall, btnShare, btnSave;
    private TextView tvLinkFeeCalc, tvLinkReport;

    private ParkingLot lot;
    private SmartParkDbHelper dbHelper;
    private boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        dbHelper = new SmartParkDbHelper(this);

        lot = (ParkingLot) getIntent().getSerializableExtra("parkingLot");
        if (lot == null) {
            Toast.makeText(this, "Parking lot data error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /*History data for sqlite*/
        dbHelper.addHistory(lot);

        btnBack = findViewById(R.id.btn_back);
        tvName = findViewById(R.id.tv_details_name);
        tvStatus = findViewById(R.id.tv_details_status);
        tvAddress = findViewById(R.id.tv_details_address);

        tvDistance = findViewById(R.id.tv_details_distance);
        tvPrice = findViewById(R.id.tv_details_price);
        tvSlots = findViewById(R.id.tv_details_slots);

        tvAiChance = findViewById(R.id.tv_details_ai_chance);
        tvAiRec = findViewById(R.id.tv_details_ai_rec);
        progressAiChance = findViewById(R.id.progress_ai_chance);

        btnAiWhatIf = findViewById(R.id.btn_ai_whatif);
        btnNavigate = findViewById(R.id.btn_navigate);
        btnCall = findViewById(R.id.btn_call);
        btnShare = findViewById(R.id.btn_share);
        btnSave = findViewById(R.id.btn_save);

        tvLinkFeeCalc = findViewById(R.id.tv_link_fee_calculator);
        tvLinkReport = findViewById(R.id.tv_link_report);

        populateUi();

        btnBack.setOnClickListener(v -> finish());
        btnAiWhatIf.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, PredictorActivity.class);
            intent.putExtra("parkingLot", lot);
            startActivity(intent);
        });

        btnNavigate.setOnClickListener(v -> IntentHelper.openNavigation(this, lot.getLat(), lot.getLng(), lot.getName()));
        btnCall.setOnClickListener(v -> IntentHelper.makePhoneCall(this, lot.getPhone()));
        btnShare.setOnClickListener(v -> IntentHelper.shareParkingLot(this, lot.getName(), lot.getAddress(), lot.getPricePerHour(), lot.getLat(), lot.getLng()));

        btnSave.setOnClickListener(v -> toggleFavourite());
        tvLinkFeeCalc.setOnClickListener(v -> showFeeCalculatorDialog());
        tvLinkReport.setOnClickListener(v -> {
            Intent intent = new Intent(DetailsActivity.this, ReportActivity.class);
            intent.putExtra("parkingLot", lot);
            startActivity(intent);
        });
    }

    private void populateUi() {
        tvName.setText(lot.getName());
        tvAddress.setText(lot.getAddress());

        tvStatus.setText(lot.getStatus());
        if ("AVAILABLE".equalsIgnoreCase(lot.getStatus())) {
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available));
        } else if ("FILLING".equalsIgnoreCase(lot.getStatus())) {
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_filling));
        } else {
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_full));
        }

        tvDistance.setText(lot.getFormattedDistance());
        tvPrice.setText(lot.getFormattedPrice());
        tvSlots.setText(lot.getFreeSlots() + " / " + lot.getTotalSlots());

        /* Calculates prediction */
        PredictionResult pred = ParkingPredictor.predictImmediate(lot);
        tvAiChance.setText(pred.getChancePercent() + "% Chance");
        tvAiRec.setText(pred.getRecommendation());
        progressAiChance.setProgress(pred.getChancePercent());

        isSaved = dbHelper.isFavourite(lot.getLotId());
        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        if (isSaved) {
            btnSave.setText(R.string.btn_saved);
        } else {
            btnSave.setText(R.string.btn_save);
        }
    }

    private void toggleFavourite() {
        if (isSaved) {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Favourite")
                    .setMessage("Remove " + lot.getName() + " from your saved list?")
                    .setPositiveButton("Remove", (dialog, which) -> {
                        dbHelper.deleteFavourite(lot.getLotId());
                        isSaved = false;
                        updateSaveButtonState();
                        Toast.makeText(DetailsActivity.this, "Removed from favourites", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            dbHelper.insertFavourite(lot, "Saved from details");
            isSaved = true;
            updateSaveButtonState();
            Toast.makeText(this, "Saved to favourites!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFeeCalculatorDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_fee_calculator, null);
        TextView tvRate = dialogView.findViewById(R.id.tv_calc_rate);
        TextView tvResult = dialogView.findViewById(R.id.tv_calc_result);
        EditText etHours = dialogView.findViewById(R.id.et_hours);
        MaterialButton btnMinus = dialogView.findViewById(R.id.btn_minus_hour);
        MaterialButton btnPlus = dialogView.findViewById(R.id.btn_plus_hour);

        tvRate.setText("Rate: ₹" + lot.getPricePerHour() + "/hr");

        final int[] hours = {2};
        final Runnable updateFee = () -> {
            int total = hours[0] * lot.getPricePerHour();
            tvResult.setText("₹" + total);
            etHours.setText(String.valueOf(hours[0]));
        };

        updateFee.run();

        btnMinus.setOnClickListener(v -> {
            if (hours[0] > 1) {
                hours[0]--;
                updateFee.run();
            }
        });

        btnPlus.setOnClickListener(v -> {
            hours[0]++;
            updateFee.run();
        });

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save Receipt (.txt)", (dialog, which) -> {
                    try {
                        int total = hours[0] * lot.getPricePerHour();
                        File receipt = CsvHelper.saveParkingReceipt(DetailsActivity.this, lot, hours[0], total);
                        Toast.makeText(DetailsActivity.this, "Receipt saved: " + receipt.getName(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(DetailsActivity.this, "Error saving receipt", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }
}
