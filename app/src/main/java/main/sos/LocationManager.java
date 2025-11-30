// LocationManager.java
package com.yourapp.locationlib;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Library class สำหรับจัดการดึง Location จาก GPS และ GMS Location
 * รองรับทั้ง GPS ดาวเทียมและ Network Location
 */
public class LocationManager {
    
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationListener listener;
    
    // Configuration
    private long updateInterval = 10000; // 10 วินาที
    private long fastestInterval = 5000; // 5 วินาที
    private int priority = Priority.PRIORITY_HIGH_ACCURACY;
    
    /**
     * Interface สำหรับ callback เมื่อได้รับ location
     */
    public interface LocationListener {
        void onLocationReceived(double latitude, double longitude, Location location);
        void onLocationError(String error);
    }
    
    /**
     * Constructor
     */
    public LocationManager(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
        setupLocationCallback();
    }
    
    /**
     * ตั้งค่า LocationCallback
     */
    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    if (listener != null) {
                        listener.onLocationError("ไม่สามารถรับตำแหน่งได้");
                    }
                    return;
                }
                
                for (Location location : locationResult.getLocations()) {
                    if (location != null && listener != null) {
                        listener.onLocationReceived(
                            location.getLatitude(),
                            location.getLongitude(),
                            location
                        );
                    }
                }
            }
        };
    }
    
    /**
     * ตั้งค่า listener
     */
    public void setLocationListener(LocationListener listener) {
        this.listener = listener;
    }
    
    /**
     * ตั้งค่า update interval (milliseconds)
     */
    public void setUpdateInterval(long intervalMs) {
        this.updateInterval = intervalMs;
    }
    
    /**
     * ตั้งค่า fastest interval (milliseconds)
     */
    public void setFastestInterval(long intervalMs) {
        this.fastestInterval = intervalMs;
    }
    
    /**
     * ตั้งค่า priority
     * Priority.PRIORITY_HIGH_ACCURACY - GPS ความแม่นยำสูง
     * Priority.PRIORITY_BALANCED_POWER_ACCURACY - สมดุลระหว่างแบตและความแม่นยำ
     * Priority.PRIORITY_LOW_POWER - ประหยัดแบต
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    /**
     * ตรวจสอบ permission
     */
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * เริ่มรับ location updates แบบต่อเนื่อง
     */
    public void startLocationUpdates() {
        if (!hasLocationPermission()) {
            if (listener != null) {
                listener.onLocationError("ไม่มีสิทธิ์เข้าถึง Location");
            }
            return;
        }
        
        LocationRequest locationRequest = new LocationRequest.Builder(priority, updateInterval)
            .setMinUpdateIntervalMillis(fastestInterval)
            .setWaitForAccurateLocation(false)
            .build();
        
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            if (listener != null) {
                listener.onLocationError("Security Exception: " + e.getMessage());
            }
        }
    }
    
    /**
     * หยุดรับ location updates
     */
    public void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
    
    /**
     * ดึง location ล่าสุดครั้งเดียว (Last Known Location)
     */
    public void getLastLocation() {
        if (!hasLocationPermission()) {
            if (listener != null) {
                listener.onLocationError("ไม่มีสิทธิ์เข้าถึง Location");
            }
            return;
        }
        
        try {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null && listener != null) {
                        listener.onLocationReceived(
                            location.getLatitude(),
                            location.getLongitude(),
                            location
                        );
                    } else if (listener != null) {
                        listener.onLocationError("ไม่พบ Last Known Location");
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onLocationError("Error: " + e.getMessage());
                    }
                });
        } catch (SecurityException e) {
            if (listener != null) {
                listener.onLocationError("Security Exception: " + e.getMessage());
            }
        }
    }
    
    /**
     * คำนวณระยะทางระหว่าง 2 จุด (เมตร)
     */
    public static float calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0];
    }
    
    /**
     * ตรวจสอบว่า location มาจาก GPS หรือไม่
     */
    public static boolean isFromGPS(Location location) {
        return location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER);
    }
    
    /**
     * ตรวจสอบว่า location มาจาก Network หรือไม่
     */
    public static boolean isFromNetwork(Location location) {
        return location.getProvider().equals(android.location.LocationManager.NETWORK_PROVIDER);
    }
    
    /**
     * ดึงข้อมูลความแม่นยำ (accuracy) ของ location
     */
    public static float getAccuracy(Location location) {
        return location.hasAccuracy() ? location.getAccuracy() : -1;
    }
    
    /**
     * ดึงข้อมูล altitude (ความสูง) ของ location
     */
    public static double getAltitude(Location location) {
        return location.hasAltitude() ? location.getAltitude() : 0;
    }
    
    /**
     * ดึงข้อมูลความเร็ว (m/s)
     */
    public static float getSpeed(Location location) {
        return location.hasSpeed() ? location.getSpeed() : 0;
    }
    
    /**
     * ดึงข้อมูลทิศทาง (degree)
     */
    public static float getBearing(Location location) {
        return location.hasBearing() ? location.getBearing() : 0;
    }
    
    /**
     * ทำลาย instance และหยุดการทำงาน
     */
    public void destroy() {
        stopLocationUpdates();
        listener = null;
        fusedLocationClient = null;
        locationCallback = null;
    }
}


// ===== ความแตกต่างระหว่าง getLastLocation() vs startLocationUpdates() =====

/*
┌─────────────────────────────────────────────────────────────────────────┐
│ getLastLocation()                    │ startLocationUpdates()           │
├──────────────────────────────────────┼──────────────────────────────────┤
│ ✓ ดึงตำแหน่งครั้งเดียว               │ ✓ รับตำแหน่งแบบต่อเนื่อง         │
│ ✓ ได้ผลลัพธ์ทันที (ถ้ามี cache)     │ ✓ อัพเดทตาม interval ที่ตั้งค่า  │
│ ✓ ประหยัดแบตมาก                     │ ✓ ได้ข้อมูลแบบ real-time         │
│ ✓ อาจได้ตำแหน่งเก่า (ไม่แม่นยำ)     │ ✓ ความแม่นยำสูงกว่า              │
│ ✓ เหมาะสำหรับแสดงตำแหน่งเริ่มต้น   │ ✓ กินแบตมากกว่า                  │
│ ✓ ไม่ต้อง stopLocationUpdates()     │ ✓ ต้อง stopLocationUpdates()     │
│                                      │                                  │
│ Use case:                            │ Use case:                        │
│ - แสดงตำแหน่งปัจจุบันครั้งเดียว     │ - Tracking GPS แบบต่อเนื่อง      │
│ - เลือกตำแหน่งบนแผนที่              │ - Navigation app                 │
│ - Check-in ร้านอาหาร                │ - Fitness tracking               │
│                                      │ - Delivery tracking              │
└──────────────────────────────────────┴──────────────────────────────────┘
*/

// ===== ตัวอย่างที่ 1: ใช้ getLastLocation() - ดึงครั้งเดียว =====

/*
public class SingleLocationActivity extends AppCompatActivity {
    
    private LocationManager locationManager;
    private TextView tvLocation;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvLocation = findViewById(R.id.tvLocation);
        Button btnGetLocation = findViewById(R.id.btnGetLocation);
        
        locationManager = new LocationManager(this);
        locationManager.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        
        // ตั้งค่า callback แบบ anonymous class
        locationManager.setLocationListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude, Location location) {
                String info = String.format(
                    "Latitude: %.6f\nLongitude: %.6f\n" +
                    "Accuracy: %.2f meters\n" +
                    "Provider: %s\n" +
                    "From GPS: %s",
                    latitude, longitude,
                    LocationManager.getAccuracy(location),
                    location.getProvider(),
                    LocationManager.isFromGPS(location) ? "Yes" : "No"
                );
                tvLocation.setText(info);
                
                // ได้ตำแหน่งแล้ว ไม่ต้องทำอะไรเพิ่ม
            }
            
            @Override
            public void onLocationError(String error) {
                tvLocation.setText("Error: " + error);
                Toast.makeText(SingleLocationActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // กดปุ่มเพื่อดึงตำแหน่ง
        btnGetLocation.setOnClickListener(v -> {
            if (locationManager.hasLocationPermission()) {
                tvLocation.setText("Getting location...");
                locationManager.getLastLocation(); // ดึงครั้งเดียว
            } else {
                requestLocationPermission();
            }
        });
    }
    
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }, 100);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.destroy();
    }
}
*/


// ===== ตัวอย่างที่ 2: ใช้ startLocationUpdates() - อัพเดทต่อเนื่อง =====

/*
public class ContinuousTrackingActivity extends AppCompatActivity {
    
    private LocationManager locationManager;
    private TextView tvLocation, tvSpeed, tvDistance;
    private Button btnStartStop;
    private boolean isTracking = false;
    
    private Location lastLocation = null;
    private float totalDistance = 0f;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        
        tvLocation = findViewById(R.id.tvLocation);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvDistance = findViewById(R.id.tvDistance);
        btnStartStop = findViewById(R.id.btnStartStop);
        
        locationManager = new LocationManager(this);
        
        // ตั้งค่าสำหรับ tracking
        locationManager.setUpdateInterval(3000);  // อัพเดททุก 3 วินาที
        locationManager.setFastestInterval(1000); // เร็วสุดทุก 1 วินาที
        locationManager.setPriority(Priority.PRIORITY_HIGH_ACCURACY); // ใช้ GPS
        
        // ตั้งค่า callback
        locationManager.setLocationListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude, Location location) {
                // อัพเดทตำแหน่งปัจจุบัน
                tvLocation.setText(String.format("%.6f, %.6f", latitude, longitude));
                
                // อัพเดทความเร็ว
                float speed = LocationManager.getSpeed(location);
                tvSpeed.setText(String.format("Speed: %.2f m/s (%.2f km/h)", 
                    speed, speed * 3.6));
                
                // คำนวณระยะทางรวม
                if (lastLocation != null) {
                    float distance = LocationManager.calculateDistance(
                        lastLocation.getLatitude(),
                        lastLocation.getLongitude(),
                        latitude, longitude
                    );
                    totalDistance += distance;
                    tvDistance.setText(String.format("Distance: %.2f meters (%.2f km)", 
                        totalDistance, totalDistance / 1000));
                }
                
                lastLocation = location;
                
                Log.d("Tracking", "Location updated - GPS: " + 
                    LocationManager.isFromGPS(location));
            }
            
            @Override
            public void onLocationError(String error) {
                Toast.makeText(ContinuousTrackingActivity.this, 
                    error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // ปุ่ม Start/Stop tracking
        btnStartStop.setOnClickListener(v -> {
            if (!isTracking) {
                startTracking();
            } else {
                stopTracking();
            }
        });
    }
    
    private void startTracking() {
        if (locationManager.hasLocationPermission()) {
            locationManager.startLocationUpdates(); // เริ่มรับ updates ต่อเนื่อง
            isTracking = true;
            btnStartStop.setText("Stop Tracking");
            btnStartStop.setBackgroundColor(Color.RED);
            totalDistance = 0f;
            lastLocation = null;
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, 100);
        }
    }
    
    private void stopTracking() {
        locationManager.stopLocationUpdates(); // หยุดรับ updates
        isTracking = false;
        btnStartStop.setText("Start Tracking");
        btnStartStop.setBackgroundColor(Color.GREEN);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // หยุดเมื่อออกจาก app เพื่อประหยัดแบต
        if (isTracking) {
            locationManager.stopLocationUpdates();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // เริ่มใหม่เมื่อกลับมา app
        if (isTracking) {
            locationManager.startLocationUpdates();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.destroy();
    }
}
*/


// ===== ตัวอย่างที่ 3: Hybrid - ใช้ทั้ง 2 แบบร่วมกัน =====

/*
public class HybridLocationActivity extends AppCompatActivity {
    
    private LocationManager locationManager;
    private GoogleMap map;
    private Marker currentMarker;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        SupportMapFragment mapFragment = (SupportMapFragment) 
            getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(googleMap -> {
            map = googleMap;
            setupLocationManager();
        });
    }
    
    private void setupLocationManager() {
        locationManager = new LocationManager(this);
        locationManager.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        
        locationManager.setLocationListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationReceived(double lat, double lng, Location location) {
                LatLng position = new LatLng(lat, lng);
                
                if (currentMarker == null) {
                    // ครั้งแรก: สร้าง marker และ zoom
                    currentMarker = map.addMarker(new MarkerOptions()
                        .position(position)
                        .title("You are here"));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
                } else {
                    // อัพเดท: เลื่อน marker
                    currentMarker.setPosition(position);
                }
            }
            
            @Override
            public void onLocationError(String error) {
                Log.e("Location", error);
            }
        });
        
        if (locationManager.hasLocationPermission()) {
            // Step 1: ดึงตำแหน่งเริ่มต้นครั้งเดียวเพื่อแสดงบนแผนที่เร็ว ๆ
            locationManager.getLastLocation();
            
            // Step 2: เริ่ม tracking แบบต่อเนื่องเพื่ออัพเดท real-time
            locationManager.startLocationUpdates();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.stopLocationUpdates();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager.hasLocationPermission()) {
            locationManager.startLocationUpdates();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.destroy();
    }
}
*/


// ===== เพิ่มใน AndroidManifest.xml =====
/*
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
*/


// ===== เพิ่มใน build.gradle (Module: app) =====
/*
dependencies {
    implementation 'com.google.android.gms:play-services-location:21.0.1'
}
*/
