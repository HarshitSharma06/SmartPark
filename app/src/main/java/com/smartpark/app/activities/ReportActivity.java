package com.smartpark.app.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartpark.app.R;
import com.smartpark.app.data.FirebaseRepository;
import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.model.ReportEntry;
import com.smartpark.app.util.DateTimeUtil;
import com.smartpark.app.util.FirebaseHelper;

import java.io.File;
import java.util.Calendar;


public class ReportActivity extends AppCompatActivity {

    private TextView tvLotName;
    private ImageView imgPhoto;
    private MaterialButton btnCamera, btnGallery;
    private RadioGroup rgStatus;
    private RadioButton rbAvailable, rbFilling, rbFull;
    private MaterialButton btnDate, btnTime, btnSubmit;
    private TextInputEditText etNotes;
    private CheckBox cbAttachLocation;

    private ParkingLot lot;
    private Calendar observedCal;
    private Uri tempPhotoUri;
    private String savedPhotoPath = "";

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        lot = (ParkingLot) getIntent().getSerializableExtra("parkingLot");

        tvLotName = findViewById(R.id.tv_report_lot_name);
        imgPhoto = findViewById(R.id.img_report_photo);
        btnCamera = findViewById(R.id.btn_report_camera);
        btnGallery = findViewById(R.id.btn_report_gallery);

        rgStatus = findViewById(R.id.rg_report_status);
        rbAvailable = findViewById(R.id.rb_rep_available);
        rbFilling = findViewById(R.id.rb_rep_filling);
        rbFull = findViewById(R.id.rb_rep_full);

        btnDate = findViewById(R.id.btn_rep_date);
        btnTime = findViewById(R.id.btn_rep_time);
        btnSubmit = findViewById(R.id.btn_submit_report);

        etNotes = findViewById(R.id.et_report_notes);
        cbAttachLocation = findViewById(R.id.cb_attach_location);

        observedCal = Calendar.getInstance();

        if (lot != null) {
            tvLotName.setText(lot.getName());
        }

        // Camera launcher setup
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && tempPhotoUri != null) {
                        imgPhoto.setImageURI(tempPhotoUri);
                        savedPhotoPath = tempPhotoUri.toString();
                    }
                }
        );

        // Gallery launcher setup
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imgPhoto.setImageURI(uri);
                        savedPhotoPath = uri.toString();
                    }
                }
        );

        btnCamera.setOnClickListener(v -> launchCamera());
        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnDate.setOnClickListener(v -> showDatePicker());
        btnTime.setOnClickListener(v -> showTimePicker());
        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void launchCamera() {
        try {
            File photoFile = new File(getExternalFilesDir(null), "report_" + System.currentTimeMillis() + ".jpg");
            tempPhotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(tempPhotoUri);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot launch camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    observedCal.set(Calendar.YEAR, year);
                    observedCal.set(Calendar.MONTH, month);
                    observedCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    btnDate.setText(DateTimeUtil.formatDisplayDate(observedCal.getTimeInMillis()));
                },
                observedCal.get(Calendar.YEAR),
                observedCal.get(Calendar.MONTH),
                observedCal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    observedCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    observedCal.set(Calendar.MINUTE, minute);
                    btnTime.setText(DateTimeUtil.formatTimeOnly(observedCal.getTimeInMillis()));
                },
                observedCal.get(Calendar.HOUR_OF_DAY),
                observedCal.get(Calendar.MINUTE),
                false);
        dialog.show();
    }

    private void submitReport() {
        String status = "AVAILABLE";
        int checkedId = rgStatus.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_rep_filling) {
            status = "FILLING";
        } else if (checkedId == R.id.rb_rep_full) {
            status = "FULL";
        }

        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
        String lotId = lot != null ? lot.getLotId() : "lot_001";
        double lat = lot != null ? lot.getLat() : 30.7398;
        double lng = lot != null ? lot.getLng() : 76.7826;

        FirebaseAuth auth = FirebaseHelper.auth(this);
        FirebaseUser currentUser = auth != null ? auth.getCurrentUser() : null;
        String uid = currentUser != null ? currentUser.getUid() : "guest";

        ReportEntry report = new ReportEntry(
                "", lotId, uid, status,
                DateTimeUtil.formatDisplayDate(observedCal.getTimeInMillis()),
                notes, lat, lng, savedPhotoPath
        );

        FirebaseRepository.submitReport(report);

        Toast.makeText(this, "Thank you! Report submitted successfully.", Toast.LENGTH_LONG).show();
        finish();
    }
}
