package com.smartpark.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.smartpark.app.R;
import com.smartpark.app.data.CsvHelper;
import com.smartpark.app.data.SmartParkDbHelper;
import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.util.BottomNavHelper;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SavedActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ListView listView;
    private TextView tvEmpty;
    private MaterialButton btnExportCsv, btnImportCsv;

    private SmartParkDbHelper dbHelper;
    private List<HashMap<String, String>> currentList = new ArrayList<>();
    private boolean isFavouritesTab = true;

    private ActivityResultLauncher<String> csvPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        dbHelper = new SmartParkDbHelper(this);

        tabLayout = findViewById(R.id.tab_layout_saved);
        listView = findViewById(R.id.list_view_saved);
        tvEmpty = findViewById(R.id.tv_empty_saved);
        btnExportCsv = findViewById(R.id.btn_export_csv);
        btnImportCsv = findViewById(R.id.btn_import_csv);

        // CSV File Picker Launcher
        csvPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        importCsvFile(uri);
                    }
                }
        );

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isFavouritesTab = (tab.getPosition() == 0);
                loadTabData();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < currentList.size()) {
                HashMap<String, String> item = currentList.get(position);
                showItemOptionsDialog(item);
                return true;
            }
            return false;
        });

        btnExportCsv.setOnClickListener(v -> exportCsvHistory());
        btnImportCsv.setOnClickListener(v -> csvPickerLauncher.launch("*/*"));

        BottomNavHelper.setupBottomNav(this, R.id.nav_saved);
        loadTabData();
    }

    private void loadTabData() {
        if (isFavouritesTab) {
            currentList = dbHelper.getFavourites();
            btnExportCsv.setVisibility(View.GONE);
            btnImportCsv.setVisibility(View.GONE);
        } else {
            currentList = dbHelper.getHistory();
            btnExportCsv.setVisibility(View.VISIBLE);
            btnImportCsv.setVisibility(View.VISIBLE);
        }

        renderListView();
    }

    private void renderListView() {
        if (currentList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(isFavouritesTab ? "No saved favourites yet" : "No viewing history yet");
            listView.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);

        List<HashMap<String, String>> data = new ArrayList<>();
        for (HashMap<String, String> map : currentList) {
            HashMap<String, String> item = new HashMap<>();
            item.put("title", map.get("name"));

            if (isFavouritesTab) {
                item.put("subtitle", map.get("address") + " • " + map.get("price"));
                String note = map.get("note");
                item.put("extra", note != null && !note.isEmpty() ? "Note: " + note : "Saved: " + map.get("saved_at"));
            } else {
                item.put("subtitle", "Price: " + map.get("price"));
                item.put("extra", "Viewed: " + map.get("viewed_at"));
            }
            data.add(item);
        }

        String[] from = {"title", "subtitle", "extra"};
        int[] to = {R.id.tv_saved_title, R.id.tv_saved_subtitle, R.id.tv_saved_extra};

        SimpleAdapter adapter = new SimpleAdapter(this, data, R.layout.item_saved, from, to);
        listView.setAdapter(adapter);
    }

    private void showItemOptionsDialog(HashMap<String, String> item) {
        String lotId = item.get("lot_id");
        String name = item.get("name");

        if (isFavouritesTab) {
            String[] options = {"Edit Note", "Delete Favourite"};
            new AlertDialog.Builder(this)
                    .setTitle(name)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            showEditNoteDialog(lotId, item.get("note"));
                        } else {
                            dbHelper.deleteFavourite(lotId);
                            Toast.makeText(SavedActivity.this, "Deleted from Favourites", Toast.LENGTH_SHORT).show();
                            loadTabData();
                        }
                    })
                    .show();
        } else {
            String histId = item.get("id");
            new AlertDialog.Builder(this)
                    .setTitle(name)
                    .setMessage("Delete this history item?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (histId != null) {
                            dbHelper.deleteHistoryEntry(histId);
                            Toast.makeText(SavedActivity.this, "Deleted entry", Toast.LENGTH_SHORT).show();
                            loadTabData();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void showEditNoteDialog(String lotId, String currentNote) {
        final EditText input = new EditText(this);
        if (currentNote != null) input.setText(currentNote);
        input.setHint("Enter note (e.g., Best spot for mall)");

        new AlertDialog.Builder(this)
                .setTitle("Edit Note")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newNote = input.getText().toString().trim();
                    dbHelper.updateFavouriteNote(lotId, newNote);
                    Toast.makeText(SavedActivity.this, "Note updated!", Toast.LENGTH_SHORT).show();
                    loadTabData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportCsvHistory() {
        try {
            List<HashMap<String, String>> history = dbHelper.getHistory();
            if (history.isEmpty()) {
                Toast.makeText(this, "History is empty. Nothing to export.", Toast.LENGTH_SHORT).show();
                return;
            }

            File csvFile = CsvHelper.exportHistoryToCsv(this, history);
            Toast.makeText(this, "Exported: " + csvFile.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Share via FileProvider
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", csvFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share History CSV"));

        } catch (Exception e) {
            Toast.makeText(this, "CSV Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void importCsvFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                List<HashMap<String, String>> imported = CsvHelper.importHistoryFromCsv(inputStream);
                int count = 0;
                for (HashMap<String, String> item : imported) {
                    ParkingLot lot = new ParkingLot();
                    lot.setLotId(item.get("lot_id"));
                    lot.setName(item.get("name"));
                    String pStr = item.get("price") != null ? item.get("price").replaceAll("[^0-9]", "") : "0";
                    lot.setPricePerHour(pStr.isEmpty() ? 0 : Integer.parseInt(pStr));

                    dbHelper.addHistory(lot);
                    count++;
                }
                Toast.makeText(this, "Successfully imported " + count + " history rows!", Toast.LENGTH_LONG).show();
                loadTabData();
            }
        } catch (Exception e) {
            Toast.makeText(this, "CSV Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
