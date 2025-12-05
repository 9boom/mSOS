package main.sos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private MaterialButton btnVictimMode;
    private MaterialButton btnRescuerMode;
    private ImageView ivConnectionStatus;
    private View victimModeContent;
    private View rescuerModeContent;

    // Victim Mode Components
    private MaterialCardView cardConnectionStatus;
    private ImageView ivConnectionIcon;
    private TextView tvConnectionTitle;
    private TextView tvConnectionDescription;
    private MaterialButton btnToggleConnection;
    private View sosButtonContainer;
    private View btnSendSOS;
    private View btnSendGotHelp;
    private View sosFormContainer;

    // Form Components
    private TextInputEditText etName;
    private TextInputEditText etContact;
    private TextInputEditText etDetails;
    private AutoCompleteTextView etLevel;
    private AutoCompleteTextView etType;
    private MaterialButton btnGetLocation;
    private TextView tvLocationInfo;
    private MaterialButton btnSubmitSOS;
    private ImageView btnCloseForm;

    // SharedPreferences Manager
    private PreferenceManager preferencesManager;

    // Rescuer Mode
    private TextView tvReportsHeader;
    private RecyclerView rvReports;
    private ReportsAdapter reportsAdapter;

    // Location
    private LocationLib locationLib;
    private boolean isLocationRequestInProgress = false;

    // Data
    private Mode currentMode = Mode.VICTIM;
    private boolean isOnline = true;
    private MainActivity.Location currentLocation = null;
    private List<Report> reports = new ArrayList<>();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public enum Mode {
        VICTIM,
        RESCUER
    }

    public static class Location {
        public double lat;
        public double lng;

        public Location(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    public static class Report {
        public int id;
        public String name;
        public String contact;
        public String details;
        public Location location;
        public String timestamp;
        public String level;
        public String type;
        public String status;
        public boolean relayed;

        public Report(
                int id,
                String name,
                String contact,
                String details,
                Location location,
                String timestamp,
                String level,
                String status,
                String type,
                boolean relayed) {
            this.id = id;
            this.name = name;
            this.contact = contact;
            this.details = details;
            this.location = location;
            this.timestamp = timestamp;
            this.status = status;
            this.type = type;
            this.level = level;
            this.relayed = relayed;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize
        locationLib = new LocationLib(this);
        preferencesManager = new PreferenceManager(this);

        // Initialize views
        initializeViews();
        checkForReportAvalible();

        // Setup listeners
        setupListeners();

        // Load sample data
        loadSampleReports();

        // Setup RecyclerView
        setupRecyclerView();

        // Set initial mode
        updateMode(Mode.VICTIM);
        updateConnectionStatus(isOnline);

        // Check for saved location when app starts
        checkSavedLocation();
    }

    private void checkForReportAvalible() {
        if (preferencesManager.report.isReported()) {
            showSOSForm();
            showGotHelpBtn();
        } else {
            hideSOSForm();
            hideGotHelpBtn();
        }
    }

    private void hideGotHelpBtn() {
        btnSendGotHelp.setVisibility(View.GONE);
    }

    private void showGotHelpBtn() {
        btnSendGotHelp.setVisibility(View.VISIBLE);
    }
    
    private void showSOSForm() {
        sosButtonContainer.setVisibility(View.GONE);
        sosFormContainer.setVisibility(View.VISIBLE);
        updateForm();
    }

    private void hideSOSForm() {
        sosButtonContainer.setVisibility(View.VISIBLE);
        sosFormContainer.setVisibility(View.GONE);
        clearForm();
    }

    private void updateForm() {
        etName.setText(preferencesManager.report.getName());
        etContact.setText(preferencesManager.report.getContact());
        etDetails.setText(preferencesManager.report.getDetails());
        etLevel.setText(preferencesManager.report.getLevel());
        etType.setText(preferencesManager.report.getType());
        tvLocationInfo.setVisibility(View.VISIBLE);
    }

    private void clearForm() {
        etName.getText().clear();
        etContact.getText().clear();
        etDetails.getText().clear();
        etLevel.getText().clear();
        etType.getText().clear();
        currentLocation = null;
        tvLocationInfo.setVisibility(View.GONE);
        btnGetLocation.setText(getString(R.string.get_location));
    }
    
    private void initializeViews() {
        // Mode buttons
        btnVictimMode = findViewById(R.id.btnVictimMode);
        btnRescuerMode = findViewById(R.id.btnRescuerMode);
        ivConnectionStatus = findViewById(R.id.ivConnectionStatus);

        // Content containers
        victimModeContent = findViewById(R.id.victimModeContent);
        rescuerModeContent = findViewById(R.id.rescuerModeContent);

        // Victim mode views
        cardConnectionStatus = findViewById(R.id.cardConnectionStatus);
        ivConnectionIcon = findViewById(R.id.ivConnectionIcon);
        tvConnectionTitle = findViewById(R.id.tvConnectionTitle);
        tvConnectionDescription = findViewById(R.id.tvConnectionDescription);
        btnToggleConnection = findViewById(R.id.btnToggleConnection);
        sosButtonContainer = findViewById(R.id.sosButtonContainer);
        btnSendSOS = findViewById(R.id.btnSendSOS);
        btnSendGotHelp = findViewById(R.id.btnSendGotHelp);
        sosFormContainer = findViewById(R.id.sosFormContainer);

        // Form views
        etName = findViewById(R.id.etName);
        etContact = findViewById(R.id.etContact);
        etDetails = findViewById(R.id.etDetails);
        etLevel = findViewById(R.id.etLevel);
        etType = findViewById(R.id.etType);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        tvLocationInfo = findViewById(R.id.tvLocationInfo);
        btnSubmitSOS = findViewById(R.id.btnSubmitSOS);
        btnCloseForm = findViewById(R.id.btnCloseForm);

        // Rescuer mode views
        tvReportsHeader = findViewById(R.id.tvReportsHeader);
        rvReports = findViewById(R.id.rvReports);
    }

    private void setupSeverityDropdown() {
        etLevel = findViewById(R.id.etLevel);
        String[] severityLevels = {
            "🟢 ต่ำ - ไม่เร่งด่วน",
            "🟡 ปานกลาง - ต้องการความช่วยเหลือ",
            "🟠 สูง - เร่งด่วน",
            "🔴 วิกฤติ - อันตรายถึงชีวิต"
        };
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this, android.R.layout.simple_dropdown_item_1line, severityLevels);
        etLevel.setAdapter(adapter);
        etLevel.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        String selectedSeverity = severityLevels[position];
                        Toast.makeText(
                                        MainActivity.this,
                                        "เลือก: " + selectedSeverity,
                                        Toast.LENGTH_SHORT)
                                .show();

                        // สามารถเก็บค่าไว้ใช้ตอนส่งข้อมูลได้
                        // เช่น int severityLevel = position;
                    }
                });
    }

    private void setupTypeDropdown() {
        etType = findViewById(R.id.etType);

        String[] type = {
            "แผ่นดินไหว 🌍🔊",
            "น้ำท่วม 🌊🏠",
            "สึนามิ 🌊🌴",
            "ถูกลักพาตัว 🚨👤",
            "เหตุกราดยิง 🔫😱",
            "อุบัติเหตุ 💥🚑",
            "อื่นๆ"
        };
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, type);
        etType.setAdapter(adapter);

        // จัดการเมื่อมีการเลือกรายการ
        etType.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        String selectedSeverity = type[position];
                        Toast.makeText(
                                        MainActivity.this,
                                        "เลือก: " + selectedSeverity,
                                        Toast.LENGTH_SHORT)
                                .show();

                        // สามารถเก็บค่าไว้ใช้ตอนส่งข้อมูลได้
                        // เช่น int severityLevel = position;
                    }
                });
    }

    private void setupListeners() {
        setupSeverityDropdown();
        setupTypeDropdown();
        // Mode switching
        btnVictimMode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateMode(Mode.VICTIM);
                    }
                });

        btnRescuerMode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateMode(Mode.RESCUER);
                    }
                });

        // Connection toggle
        btnToggleConnection.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isOnline = !isOnline;
                        updateConnectionStatus(isOnline);
                    }
                });

        // SOS button
        btnSendSOS.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSOSForm();
                    }
                });

        // Form actions
        btnCloseForm.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideSOSForm();
                    }
                });

        btnGetLocation.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestLocation();
                    }
                });

        btnSubmitSOS.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submitSOSReport();
                    }
                });
    }

    private void updateMode(Mode mode) {
        currentMode = mode;

        switch (mode) {
            case VICTIM:
                // Update button styles
                btnVictimMode.setBackgroundColor(ContextCompat.getColor(this, R.color.red_600));
                btnVictimMode.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                btnRescuerMode.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_100));
                btnRescuerMode.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

                // Show/hide content
                victimModeContent.setVisibility(View.VISIBLE);
                rescuerModeContent.setVisibility(View.GONE);
                break;

            case RESCUER:
                // Update button styles
                btnVictimMode.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_100));
                btnVictimMode.setTextColor(ContextCompat.getColor(this, R.color.gray_700));

                btnRescuerMode.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_600));
                btnRescuerMode.setTextColor(ContextCompat.getColor(this, android.R.color.white));

                // Show/hide content
                victimModeContent.setVisibility(View.GONE);
                rescuerModeContent.setVisibility(View.VISIBLE);

                // Update reports count
                updateReportsHeader();
                break;
        }
    }

    private void updateConnectionStatus(boolean online) {
        isOnline = online;

        if (online) {
            // Online mode
            ivConnectionStatus.setImageResource(R.drawable.ic_wifi);
            ivConnectionStatus.setColorFilter(ContextCompat.getColor(this, R.color.green_600));

            ivConnectionIcon.setImageResource(R.drawable.ic_wifi);
            ivConnectionIcon.setColorFilter(ContextCompat.getColor(this, R.color.green_700));

            cardConnectionStatus.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.green_50));
            cardConnectionStatus.setStrokeColor(ContextCompat.getColor(this, R.color.green_200));

            tvConnectionTitle.setText(getString(R.string.connection_online));
            tvConnectionDescription.setText(getString(R.string.connection_online_desc));
            btnToggleConnection.setText(getString(R.string.test_offline));
        } else {
            // Offline mode
            ivConnectionStatus.setImageResource(R.drawable.ic_wifi_off);
            ivConnectionStatus.setColorFilter(ContextCompat.getColor(this, R.color.gray_400));

            ivConnectionIcon.setImageResource(R.drawable.ic_radio);
            ivConnectionIcon.setColorFilter(ContextCompat.getColor(this, R.color.yellow_600));

            cardConnectionStatus.setCardBackgroundColor(
                    ContextCompat.getColor(this, R.color.yellow_50));
            cardConnectionStatus.setStrokeColor(ContextCompat.getColor(this, R.color.yellow_200));

            tvConnectionTitle.setText(getString(R.string.connection_offline));
            tvConnectionDescription.setText(getString(R.string.connection_offline_desc));
            btnToggleConnection.setText(getString(R.string.back_online));
        }
    }

    private void requestLocation() {
        if (isLocationRequestInProgress) return;

        isLocationRequestInProgress = true;
        btnGetLocation.setText("กำลังดึงตำแหน่ง...");
        btnGetLocation.setEnabled(false);

        // ตั้งค่า listener
        locationLib.setLocationListener(
                new LocationLib.LocationListener() {
                    @Override
                    public void onLocationReceived(android.location.Location location) {
                        runOnUiThread(
                                () -> {
                                    isLocationRequestInProgress = false;
                                    btnGetLocation.setEnabled(true);
                                    locationLib.stopLocationService(); // หยุด service

                                    if (location != null) {
                                        currentLocation =
                                                new MainActivity.Location(
                                                        location.getLatitude(),
                                                        location.getLongitude());

                                        tvLocationInfo.setText(
                                                getString(
                                                        R.string.coordinates,
                                                        String.format(
                                                                Locale.getDefault(),
                                                                "%.6f",
                                                                currentLocation.lat),
                                                        String.format(
                                                                Locale.getDefault(),
                                                                "%.6f",
                                                                currentLocation.lng)));
                                        tvLocationInfo.setVisibility(View.VISIBLE);
                                        btnGetLocation.setText(getString(R.string.location_set));

                                        Toast.makeText(
                                                        MainActivity.this,
                                                        "ได้ตำแหน่งแล้ว: "
                                                                + location.getLatitude()
                                                                + ", "
                                                                + location.getLongitude(),
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        btnGetLocation.setText(getString(R.string.get_location));
                                    }
                                });
                    }

                    @Override
                    public void onLocationError(String error) {
                        runOnUiThread(
                                () -> {
                                    isLocationRequestInProgress = false;
                                    btnGetLocation.setEnabled(true);
                                    btnGetLocation.setText(getString(R.string.get_location));

                                    Toast.makeText(
                                                    MainActivity.this,
                                                    "ผิดพลาด: " + error,
                                                    Toast.LENGTH_SHORT)
                                            .show();

                                    useSavedLocation();
                                });
                    }

                    @Override
                    public void onGPSEnabled() {
                        runOnUiThread(
                                () -> {
                                    Toast.makeText(
                                                    MainActivity.this,
                                                    "GPS เปิดแล้ว กำลังดึงตำแหน่ง...",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                });
                    }
                });

        // ตรวจสอบ permission
        if (!locationLib.hasLocationPermission()) {
            locationLib.requestLocationPermission(this);
            // จะดำเนินการต่อใน onRequestPermissionsResult
            return;
        }

        // ตรวจสอบ GPS
        if (!locationLib.isLocationEnabled()) {
            locationLib.requestEnableGPS(this);
            // จะดำเนินการต่อใน onActivityResult
            return;
        }

        // ทุกอย่างพร้อม เริ่มดึงตำแหน่ง
        locationLib.startLocationService();
    }

    private void useSavedLocation() {
        // ลองใช้ตำแหน่งที่บันทึกไว้ใน SharedPreferences
        android.location.Location savedLocation = locationLib.getLastLocationFromPrefs();

        if (savedLocation != null) {
            currentLocation =
                    new MainActivity.Location(
                            savedLocation.getLatitude(), savedLocation.getLongitude());

            tvLocationInfo.setText(
                    getString(
                                    R.string.coordinates,
                                    String.format(Locale.getDefault(), "%.6f", currentLocation.lat),
                                    String.format(Locale.getDefault(), "%.6f", currentLocation.lng))
                            + " (ตำแหน่งที่บันทึกไว้)");
            tvLocationInfo.setVisibility(View.VISIBLE);
            btnGetLocation.setText(getString(R.string.location_set));

            Toast.makeText(this, "ใช้ตำแหน่งที่บันทึกไว้ล่าสุด", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkSavedLocation() {
        // ตรวจสอบว่ามีตำแหน่งบันทึกไว้หรือไม่
        if (locationLib.hasSavedLocation()) {
            android.location.Location savedLocation = locationLib.getLastLocationFromPrefs();
            if (savedLocation != null) {
                currentLocation =
                        new MainActivity.Location(
                                savedLocation.getLatitude(), savedLocation.getLongitude());

                // แจ้งเตือนว่ามีตำแหน่งบันทึกไว้
                Toast.makeText(this, "มีตำแหน่งที่บันทึกไว้พร้อมใช้งาน", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitSOSReport() {
        String name = etName.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String details = etDetails.getText().toString().trim();
        String level = etLevel.getText().toString().trim();
        String type = etType.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกชื่อ-นามสกุล", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contact.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกช่องทางติดต่อ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (details.isEmpty()) {
            Toast.makeText(this, "กรุณากรอกรายละเอียดสถานการณ์", Toast.LENGTH_SHORT).show();
            return;
        }

        if (level.isEmpty()) {
            Toast.makeText(this, "กรุณาเลือกระดับการร้องขอของคุณ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type.isEmpty()) {
            Toast.makeText(this, "กรุณาเลือกประเภท SOS", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentLocation == null) {
            Toast.makeText(this, "กรุณาระบุตำแหน่ง", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create new report
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm น.", new Locale("th", "TH"));
        Report newReport =
                new Report(
                        reports.size() + 1,
                        name,
                        contact,
                        details,
                        currentLocation,
                        timeFormat.format(new Date()),
                        level,
                        "รอความช่วยเหลือ",
                        type,
                        !isOnline);

        reports.add(0, newReport);
        reportsAdapter.notifyItemInserted(0);
        updateReportsHeader();

        preferencesManager.report.storageReport(newReport);

        Toast.makeText(this, "ส่งสัญญาณ SOS สำเร็จ", Toast.LENGTH_SHORT).show();
        hideSOSForm();
    }

    private void loadSampleReports() {
        reports.add(
                new Report(
                        1,
                        "สมชาย ใจดี",
                        "081-234-5678",
                        "ติดอยู่บนหลังคาบ้าน น้ำท่วมสูง ต้องการความช่วยเหลือด่วน",
                        new Location(13.7563, 100.5018),
                        "10:30 น.",
                        "🔴 วิกฤติ - อันตรายถึงชีวิต",
                        "รอความช่วยเหลือ",
                        "แผ่นดินไหว 🌍🔊",
                        false));

        reports.add(
                new Report(
                        2,
                        "สมหญิง รักดี",
                        "089-876-5432",
                        "มีผู้สูงอายุและเด็กเล็ก ต้องการอาหารและน้ำดื่ม",
                        new Location(13.7465, 100.5341),
                        "09:15 น.",
                        "🔴 วิกฤติ - อันตรายถึงชีวิต",
                        "รอความช่วยเหลือ",
                        "แผ่นดินไหว 🌍🔊",
                        true));
    }

    private void setupRecyclerView() {
        reportsAdapter =
                new ReportsAdapter(
                        reports,
                        new ReportsAdapter.OnViewMapClickListener() {
                            @Override
                            public void onViewMapClick(Report report) {
                                Toast.makeText(
                                                MainActivity.this,
                                                "เปิดแผนที่สำหรับ " + report.name,
                                                Toast.LENGTH_SHORT)
                                        .show();
                                // TODO: Open map with location
                            }
                        });

        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(reportsAdapter);
    }

    private void updateReportsHeader() {
        tvReportsHeader.setText(getString(R.string.all_reports, reports.size()));
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LocationLib.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "ได้รับสิทธิ์การเข้าถึงตำแหน่งแล้ว", Toast.LENGTH_SHORT)
                        .show();

                // ตรวจสอบ GPS
                if (!locationLib.isLocationEnabled()) {
                    locationLib.requestEnableGPS(this);
                } else {
                    // เริ่มดึงตำแหน่ง
                    locationLib.startLocationService();
                }
            } else {
                isLocationRequestInProgress = false;
                btnGetLocation.setEnabled(true);
                btnGetLocation.setText(getString(R.string.get_location));
                Toast.makeText(this, "ต้องการสิทธิ์เข้าถึงตำแหน่ง", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LocationLib.REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "GPS เปิดแล้ว กำลังดึงตำแหน่ง...", Toast.LENGTH_SHORT).show();
                locationLib.startLocationService();
            } else {
                isLocationRequestInProgress = false;
                btnGetLocation.setEnabled(true);
                btnGetLocation.setText(getString(R.string.get_location));
                Toast.makeText(this, "ต้องการ GPS เพื่อระบุตำแหน่ง", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // หยุดการติดตามตำแหน่งเมื่อ Activity ถูกทำลาย
        if (locationLib != null) {
            locationLib.stopLocationService();
        }
    }
}
