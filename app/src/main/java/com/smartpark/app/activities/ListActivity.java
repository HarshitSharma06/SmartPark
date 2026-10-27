package com.smartpark.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartpark.app.R;
import com.smartpark.app.ai.ParkingPredictor;
import com.smartpark.app.ai.PredictionResult;
import com.smartpark.app.data.FirebaseRepository;
import com.smartpark.app.data.SmartParkDbHelper;
import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.util.BottomNavHelper;
import com.smartpark.app.util.IntentHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class ListActivity extends AppCompatActivity {

    private Spinner spinnerSort;
    private CheckBox cbCovered, cbEv;
    private RadioGroup rgStatusFilter;
    private ListView listView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private List<ParkingLot> rawLotsList = new ArrayList<>();
    private List<ParkingLot> displayedLotsList = new ArrayList<>();
    private SmartParkDbHelper dbHelper;

    private static final String[] SORT_OPTIONS = {"Distance", "Price (Low to High)", "Availability", "AI Chance %"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        dbHelper = new SmartParkDbHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_list);
        setSupportActionBar(toolbar);

        spinnerSort = findViewById(R.id.spinner_sort);
        cbCovered = findViewById(R.id.cb_covered);
        cbEv = findViewById(R.id.cb_ev_list);
        rgStatusFilter = findViewById(R.id.rg_status_filter);
        listView = findViewById(R.id.list_view_parking);
        progressBar = findViewById(R.id.progress_bar_list);
        tvEmpty = findViewById(R.id.tv_empty_list);

        // Sort spinner adapter
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SORT_OPTIONS);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFiltersAndSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        cbCovered.setOnCheckedChangeListener((buttonView, isChecked) -> applyFiltersAndSort());
        cbEv.setOnCheckedChangeListener((buttonView, isChecked) -> applyFiltersAndSort());
        rgStatusFilter.setOnCheckedChangeListener((group, checkedId) -> applyFiltersAndSort());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < displayedLotsList.size()) {
                ParkingLot lot = displayedLotsList.get(position);
                Intent intent = new Intent(ListActivity.this, DetailsActivity.class);
                intent.putExtra("parkingLot", lot);
                startActivity(intent);
            }
        });

        registerForContextMenu(listView);
        BottomNavHelper.setupBottomNav(this, R.id.nav_list);

        loadParkingData();
    }

    private void loadParkingData() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseRepository.fetchParkingLots(this, new FirebaseRepository.ParkingLotsCallback() {
            @Override
            public void onDataLoaded(List<ParkingLot> lots) {
                progressBar.setVisibility(View.GONE);
                rawLotsList = lots;

                for (ParkingLot lot : rawLotsList) {
                    PredictionResult res = ParkingPredictor.predictImmediate(lot);
                    lot.setAiChance(res.getChancePercent());
                }

                applyFiltersAndSort();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFiltersAndSort() {
        if (rawLotsList == null) return;
        displayedLotsList.clear();

        boolean filterCovered = cbCovered.isChecked();
        boolean filterEv = cbEv.isChecked();
        boolean filterFreeNow = (rgStatusFilter.getCheckedRadioButtonId() == R.id.rb_free_now);

        for (ParkingLot lot : rawLotsList) {
            if (filterCovered && !lot.isCovered()) continue;
            if (filterEv && !lot.isEv()) continue;
            if (filterFreeNow && "FULL".equalsIgnoreCase(lot.getStatus())) continue;

            displayedLotsList.add(lot);
        }

        int sortType = spinnerSort.getSelectedItemPosition();
        if (sortType == 0) { // Distance
            Collections.sort(displayedLotsList, Comparator.comparingDouble(ParkingLot::getDistanceKm));
        } else if (sortType == 1) { // Price
            Collections.sort(displayedLotsList, Comparator.comparingInt(ParkingLot::getPricePerHour));
        } else if (sortType == 2) { // Availability
            Collections.sort(displayedLotsList, (a, b) -> Integer.compare(b.getFreeSlots(), a.getFreeSlots()));
        } else if (sortType == 3) { // AI Chance
            Collections.sort(displayedLotsList, (a, b) -> Integer.compare(b.getAiChance(), a.getAiChance()));
        }

        renderListView();
    }

    private void renderListView() {
        if (displayedLotsList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);

        List<HashMap<String, Object>> data = new ArrayList<>();
        for (ParkingLot lot : displayedLotsList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", lot.getName());
            map.put("subtext", lot.getFormattedDistance() + " • " + lot.getFormattedPrice());
            map.put("ai", "🤖 AI Chance: " + lot.getAiChance() + "%");
            map.put("status", lot.getStatus());
            map.put("icon", R.drawable.ic_pin);
            data.add(map);
        }

        String[] from = {"name", "subtext", "ai", "status", "icon"};
        int[] to = {R.id.tv_item_name, R.id.tv_item_subtext, R.id.tv_item_ai_chance, R.id.tv_item_status, R.id.img_parking_item};

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.item_parking, from, to);
        adapter.setViewBinder((view, dataItem, textRepresentation) -> {
            if (view.getId() == R.id.tv_item_status) {
                TextView tvStatus = (TextView) view;
                String status = (String) dataItem;
                tvStatus.setText(status);
                if ("AVAILABLE".equalsIgnoreCase(status)) {
                    tvStatus.setTextColor(ContextCompat.getColor(ListActivity.this, R.color.status_available));
                } else if ("FILLING".equalsIgnoreCase(status)) {
                    tvStatus.setTextColor(ContextCompat.getColor(ListActivity.this, R.color.status_filling));
                } else {
                    tvStatus.setTextColor(ContextCompat.getColor(ListActivity.this, R.color.status_full));
                }
                return true;
            }
            return false;
        });

        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            loadParkingData();
            return true;
        } else if (id == R.id.menu_sort_price) {
            spinnerSort.setSelection(1);
            return true;
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select Option");
        menu.add(0, 1, 0, "Save to Favourites");
        menu.add(0, 2, 1, "Navigate with Google Maps");
        menu.add(0, 3, 2, "Share Parking Spot");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info != null && info.position < displayedLotsList.size()) {
            ParkingLot lot = displayedLotsList.get(info.position);
            int id = item.getItemId();
            if (id == 1) {
                dbHelper.insertFavourite(lot, "Saved from list");
                Toast.makeText(this, "Added to Favourites!", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == 2) {
                IntentHelper.openNavigation(this, lot.getLat(), lot.getLng(), lot.getName());
                return true;
            } else if (id == 3) {
                IntentHelper.shareParkingLot(this, lot.getName(), lot.getAddress(), lot.getPricePerHour(), lot.getLat(), lot.getLng());
                return true;
            }
        }
        return super.onContextItemSelected(item);
    }
}
