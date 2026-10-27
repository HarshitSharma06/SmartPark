package com.smartpark.app.activities;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartpark.app.R;
import com.smartpark.app.ai.ParkingPredictor;
import com.smartpark.app.ai.PredictionResult;
import com.smartpark.app.data.FirebaseRepository;
import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.receiver.ConnectivityReceiver;
import com.smartpark.app.util.BottomNavHelper;
import com.smartpark.app.util.LocationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_LOCATION_REQUEST_CODE = 101;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    private EditText etSearch;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabMyLocation;
    private MaterialCardView cardPreview;
    private TextView tvPreviewName, tvPreviewStatus, tvPreviewDetails, tvPreviewAi;

    private List<ParkingLot> allLots = new ArrayList<>();
    private ParkingLot selectedLot = null;
    private ConnectivityReceiver connectivityReceiver;

    // Default location: Chandigarh Sector 17
    private static final LatLng DEFAULT_LOCATION = new LatLng(30.7398, 76.7826);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etSearch = findViewById(R.id.et_search);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        fabMyLocation = findViewById(R.id.fab_my_location);
        cardPreview = findViewById(R.id.card_parking_preview);
        tvPreviewName = findViewById(R.id.tv_preview_name);
        tvPreviewStatus = findViewById(R.id.tv_preview_status);
        tvPreviewDetails = findViewById(R.id.tv_preview_details);
        tvPreviewAi = findViewById(R.id.tv_preview_ai);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fabMyLocation.setOnClickListener(v -> zoomToUserLocation());
        cardPreview.setOnClickListener(v -> openDetailsForSelectedLot());

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        // Filter chips setup
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> applyFilters());

        BottomNavHelper.setupBottomNav(this, R.id.nav_map);
        connectivityReceiver = new ConnectivityReceiver();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        checkLocationPermission();

        mMap.setOnMarkerClickListener(marker -> {
            ParkingLot lot = (ParkingLot) marker.getTag();
            if (lot != null) {
                selectedLot = lot;
                showPreviewCard(lot);
            }
            return false;
        });

        mMap.setOnMapClickListener(latLng -> cardPreview.setVisibility(View.GONE));

        mMap.setOnMapLongClickListener(latLng -> {
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Destination Pin")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            Toast.makeText(MapActivity.this, "Destination set! Searching nearby parking...", Toast.LENGTH_SHORT).show();
            filterNearbyLots(latLng);
        });

        loadParkingLots();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("SmartPark needs location access to find parking spots near you and compute distances.")
                        .setPositiveButton("OK", (dialog, which) ->
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE))
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Permission denied. Showing default city view.", Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 13f));
            }
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f));
                    recalculateDistances();
                } else {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 13f));
                }
            });
        }
    }

    private void zoomToUserLocation() {
        if (currentLocation != null) {
            LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
        } else {
            checkLocationPermission();
        }
    }

    private void loadParkingLots() {
        FirebaseRepository.fetchParkingLots(this, new FirebaseRepository.ParkingLotsCallback() {
            @Override
            public void onDataLoaded(List<ParkingLot> lots) {
                allLots = lots;
                recalculateDistances();
                renderMarkers(allLots);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MapActivity.this, "Error loading lots: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recalculateDistances() {
        if (allLots == null) return;
        double userLat = currentLocation != null ? currentLocation.getLatitude() : DEFAULT_LOCATION.latitude;
        double userLng = currentLocation != null ? currentLocation.getLongitude() : DEFAULT_LOCATION.longitude;

        for (ParkingLot lot : allLots) {
            double dist = LocationHelper.calculateDistanceKm(userLat, userLng, lot.getLat(), lot.getLng());
            lot.setDistanceKm(dist);
            PredictionResult res = ParkingPredictor.predictImmediate(lot);
            lot.setAiChance(res.getChancePercent());
        }
    }

    private void renderMarkers(List<ParkingLot> lots) {
        if (mMap == null) return;
        mMap.clear();

        for (ParkingLot lot : lots) {
            LatLng pos = new LatLng(lot.getLat(), lot.getLng());
            float markerColor;

            if ("AVAILABLE".equalsIgnoreCase(lot.getStatus())) {
                markerColor = BitmapDescriptorFactory.HUE_GREEN;
            } else if ("FILLING".equalsIgnoreCase(lot.getStatus())) {
                markerColor = BitmapDescriptorFactory.HUE_YELLOW;
            } else {
                markerColor = BitmapDescriptorFactory.HUE_RED;
            }

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(lot.getName())
                    .snippet(lot.getFormattedPrice() + " • " + lot.getStatus())
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

            if (marker != null) {
                marker.setTag(lot);
            }
        }
    }

    private void showPreviewCard(ParkingLot lot) {
        tvPreviewName.setText(lot.getName());
        tvPreviewStatus.setText(lot.getStatus());

        if ("AVAILABLE".equalsIgnoreCase(lot.getStatus())) {
            tvPreviewStatus.setTextColor(ContextCompat.getColor(this, R.color.status_available));
        } else if ("FILLING".equalsIgnoreCase(lot.getStatus())) {
            tvPreviewStatus.setTextColor(ContextCompat.getColor(this, R.color.status_filling));
        } else {
            tvPreviewStatus.setTextColor(ContextCompat.getColor(this, R.color.status_full));
        }

        tvPreviewDetails.setText(lot.getFormattedDistance() + " • " + lot.getFormattedPrice() + " • " + lot.getFreeSlots() + " slots left");
        tvPreviewAi.setText("🤖 AI Chance: " + lot.getAiChance() + "%");

        cardPreview.setVisibility(View.VISIBLE);
    }

    private void openDetailsForSelectedLot() {
        if (selectedLot != null) {
            Intent intent = new Intent(this, DetailsActivity.class);
            intent.putExtra("parkingLot", selectedLot);
            startActivity(intent);
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            renderMarkers(allLots);
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng destLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLatLng, 14f));
                filterNearbyLots(destLatLng);
            } else {
                // Filter by name locally
                List<ParkingLot> filtered = new ArrayList<>();
                for (ParkingLot lot : allLots) {
                    if (lot.getName().toLowerCase().contains(query.toLowerCase()) ||
                            lot.getAddress().toLowerCase().contains(query.toLowerCase())) {
                        filtered.add(lot);
                    }
                }
                renderMarkers(filtered);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Search error or Geocoder unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterNearbyLots(LatLng dest) {
        List<ParkingLot> nearby = new ArrayList<>();
        for (ParkingLot lot : allLots) {
            double dist = LocationHelper.calculateDistanceKm(dest.latitude, dest.longitude, lot.getLat(), lot.getLng());
            if (dist <= 3.0) { // Within 3 km
                nearby.add(lot);
            }
        }
        renderMarkers(nearby.isEmpty() ? allLots : nearby);
    }

    private void applyFilters() {
        if (allLots == null) return;
        int checkedId = chipGroupFilters.getCheckedChipId();
        List<ParkingLot> filtered = new ArrayList<>();

        if (checkedId == R.id.chip_cheapest) {
            for (ParkingLot lot : allLots) {
                if (lot.getPricePerHour() <= 20) filtered.add(lot);
            }
        } else if (checkedId == R.id.chip_open) {
            for (ParkingLot lot : allLots) {
                if (!"FULL".equalsIgnoreCase(lot.getStatus())) filtered.add(lot);
            }
        } else if (checkedId == R.id.chip_ev) {
            for (ParkingLot lot : allLots) {
                if (lot.isEv()) filtered.add(lot);
            }
        } else { // Nearby / default
            filtered.addAll(allLots);
        }

        renderMarkers(filtered);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(connectivityReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(connectivityReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connectivityReceiver != null) {
            try {
                unregisterReceiver(connectivityReceiver);
            } catch (Exception ignored) {
            }
        }
    }
}
