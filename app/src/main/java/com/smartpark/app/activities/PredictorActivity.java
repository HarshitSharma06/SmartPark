package com.smartpark.app.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.smartpark.app.R;
import com.smartpark.app.ai.ParkingPredictor;
import com.smartpark.app.ai.PredictionResult;
import com.smartpark.app.data.FirebaseRepository;
import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.ui.GaugeView;
import com.smartpark.app.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PredictorActivity extends AppCompatActivity {

    private TextView tvLotName, tvEta;
    private GaugeView gaugeView;
    private TextView tvStatFreeSlots, tvStatSearchTime, tvStatChance, tvRecommendation;

    private MaterialButton btnPickDate, btnPickTime;
    private ChipGroup chipGroupWhatIf;
    private Chip chip30Later, chipFindCheaper, chipWalkLimit;
    private TextView tvWalkLabel;
    private SeekBar seekbarWalk;
    private ListView listViewAlternatives;

    private ParkingLot currentLot;
    private Calendar arrivalCal;
    private List<ParkingLot> allLots = new ArrayList<>();
    private List<ParkingLot> alternativeLots = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predictor);

        currentLot = (ParkingLot) getIntent().getSerializableExtra("parkingLot");
        if (currentLot == null) {
            Toast.makeText(this, "Parking lot data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        arrivalCal = Calendar.getInstance();
        int etaMinutes = (int) Math.max(2, Math.round((currentLot.getDistanceKm() / 25.0) * 60.0));
        arrivalCal.add(Calendar.MINUTE, etaMinutes);

        tvLotName = findViewById(R.id.tv_pred_lot_name);
        tvEta = findViewById(R.id.tv_pred_eta);
        gaugeView = findViewById(R.id.gauge_view);

        tvStatFreeSlots = findViewById(R.id.tv_stat_free_slots);
        tvStatSearchTime = findViewById(R.id.tv_stat_search_time);
        tvStatChance = findViewById(R.id.tv_stat_chance);
        tvRecommendation = findViewById(R.id.tv_pred_recommendation);

        btnPickDate = findViewById(R.id.btn_pick_date);
        btnPickTime = findViewById(R.id.btn_pick_time);

        chipGroupWhatIf = findViewById(R.id.chip_group_whatif);
        chip30Later = findViewById(R.id.chip_30_later);
        chipFindCheaper = findViewById(R.id.chip_find_cheaper);
        chipWalkLimit = findViewById(R.id.chip_walk_limit);

        tvWalkLabel = findViewById(R.id.tv_walk_label);
        seekbarWalk = findViewById(R.id.seekbar_walk);
        listViewAlternatives = findViewById(R.id.list_view_alternatives);

        tvLotName.setText(currentLot.getName());

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());

        chip30Later.setOnClickListener(v -> {
            arrivalCal.add(Calendar.MINUTE, 30);
            recalculatePrediction();
        });

        chipFindCheaper.setOnClickListener(v -> recalculatePrediction());
        chipWalkLimit.setOnClickListener(v -> recalculatePrediction());

        seekbarWalk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int maxWalk = Math.max(1, progress);
                tvWalkLabel.setText("Max Walking Time: " + maxWalk + " min");
                recalculatePrediction();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        listViewAlternatives.setOnItemClickListener((parent, view, position, id) -> {
            if (position < alternativeLots.size()) {
                ParkingLot lot = alternativeLots.get(position);
                Intent intent = new Intent(PredictorActivity.this, DetailsActivity.class);
                intent.putExtra("parkingLot", lot);
                startActivity(intent);
            }
        });

        loadAllLotsAndPredict();
    }

    private void loadAllLotsAndPredict() {
        FirebaseRepository.fetchParkingLots(this, new FirebaseRepository.ParkingLotsCallback() {
            @Override
            public void onDataLoaded(List<ParkingLot> lots) {
                allLots = lots;
                recalculatePrediction();
            }

            @Override
            public void onError(String errorMessage) {
                recalculatePrediction();
            }
        });
    }

    private void recalculatePrediction() {
        PredictionResult result = ParkingPredictor.predict(currentLot, arrivalCal.getTimeInMillis());

        tvEta.setText("Arrival Time: " + DateTimeUtil.formatDisplayDate(arrivalCal.getTimeInMillis()));
        gaugeView.setPercent(result.getChancePercent());

        tvStatFreeSlots.setText(result.getFreeSlotsRangeString());
        tvStatSearchTime.setText(result.getExpectedSearchTime());
        tvStatChance.setText(result.getChancePercent() + "%");
        tvRecommendation.setText("💡 " + result.getRecommendation());

        // Update Alternative lots
        alternativeLots.clear();
        int maxWalkMinutes = seekbarWalk.getProgress();
        boolean filterCheaper = chipFindCheaper.isChecked();

        for (ParkingLot lot : allLots) {
            if (lot.getLotId().equals(currentLot.getLotId())) continue;

            double distKm = lot.getDistanceKm() > 0 ? lot.getDistanceKm() : 1.0;
            double walkMinutes = distKm * 12.0; // ~12 mins per km walking

            if (chipWalkLimit.isChecked() && walkMinutes > maxWalkMinutes) {
                continue;
            }

            PredictionResult altRes = ParkingPredictor.predict(lot, arrivalCal.getTimeInMillis());
            lot.setAiChance(altRes.getChancePercent());
            alternativeLots.add(lot);
        }

        if (filterCheaper) {
            Collections.sort(alternativeLots, Comparator.comparingInt(ParkingLot::getPricePerHour));
        } else {
            Collections.sort(alternativeLots, (a, b) -> Integer.compare(b.getAiChance(), a.getAiChance()));
        }

        renderAlternativesList();
    }

    private void renderAlternativesList() {
        List<HashMap<String, Object>> data = new ArrayList<>();
        for (ParkingLot lot : alternativeLots) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", lot.getName());
            map.put("subtext", lot.getFormattedDistance() + " • " + lot.getFormattedPrice());
            map.put("chance", "Chance: " + lot.getAiChance() + "%");
            map.put("icon", R.drawable.ic_pin);
            data.add(map);
        }

        String[] from = {"name", "subtext", "chance", "icon"};
        int[] to = {R.id.tv_item_name, R.id.tv_item_subtext, R.id.tv_item_ai_chance, R.id.img_parking_item};

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.item_parking, from, to);
        adapter.setViewBinder((view, dataItem, textRepresentation) -> {
            if (view.getId() == R.id.tv_item_status) {
                TextView tvStatus = (TextView) view;
                tvStatus.setVisibility(View.GONE);
                return true;
            }
            return false;
        });

        listViewAlternatives.setAdapter(adapter);
    }

    private void showDatePicker() {
        int year = arrivalCal.get(Calendar.YEAR);
        int month = arrivalCal.get(Calendar.MONTH);
        int day = arrivalCal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            arrivalCal.set(Calendar.YEAR, year1);
            arrivalCal.set(Calendar.MONTH, month1);
            arrivalCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            recalculatePrediction();
        }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        int hour = arrivalCal.get(Calendar.HOUR_OF_DAY);
        int minute = arrivalCal.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            arrivalCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            arrivalCal.set(Calendar.MINUTE, minute1);
            recalculatePrediction();
        }, hour, minute, false);
        timePickerDialog.show();
    }
}
