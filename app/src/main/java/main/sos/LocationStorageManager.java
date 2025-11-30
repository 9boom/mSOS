// LocationStorageManager.java
package com.yourapp.locationlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import androidx.annotation.Nullable;

/**
 * Class สำหรับจัดการเก็บและดึง Location จาก SharedPreferences
 * เพื่อให้ได้ตำแหน่งล่าสุดแม้หลังจากปิดแอปแล้วเปิดใหม่
 */
public class LocationStorageManager {
    
    private static final String PREF_NAME = "LocationStorage";
    private static final String KEY_LATITUDE = "last_latitude";
    private static final String KEY_LONGITUDE = "last_longitude";
    private static final String KEY_ACCURACY = "last_accuracy";
    private static final String KEY_ALTITUDE = "last_altitude";
    private static final String KEY_SPEED = "last_speed";
    private static final String KEY_BEARING = "last_bearing";
    private static final String KEY_PROVIDER = "last_provider";
    private static final String KEY_TIMESTAMP = "last_timestamp";
    private static final String KEY_HAS_LOCATION = "has_location";
    
    private SharedPreferences preferences;
    private Context context;
    
    /**
     * Constructor
     */
    public LocationStorageManager(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * บันทึก Location ลง SharedPreferences
     */
    public void saveLocation(Location location) {
        if (location == null) return;
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_LATITUDE, (float) location.getLatitude());
        editor.putFloat(KEY_LONGITUDE, (float) location.getLongitude());
        editor.putFloat(KEY_ACCURACY, location.hasAccuracy() ? location.getAccuracy() : -1f);
        editor.putFloat(KEY_ALTITUDE, (float) (location.hasAltitude() ? location.getAltitude() : 0));
        editor.putFloat(KEY_SPEED, location.hasSpeed() ? location.getSpeed() : 0f);
        editor.putFloat(KEY_BEARING, location.hasBearing() ? location.getBearing() : 0f);
        editor.putString(KEY_PROVIDER, location.getProvider());
        editor.putLong(KEY_TIMESTAMP, location.getTime());
        editor.putBoolean(KEY_HAS_LOCATION, true);
        editor.apply();
    }
    
    /**
     * บันทึก Location จาก latitude และ longitude
     */
    public void saveLocation(double latitude, double longitude) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(KEY_LATITUDE, (float) latitude);
        editor.putFloat(KEY_LONGITUDE, (float) longitude);
        editor.putLong(KEY_TIMESTAMP, System.currentTimeMillis());
        editor.putBoolean(KEY_HAS_LOCATION, true);
        editor.apply();
    }
    
    /**
     * ดึง Location จาก SharedPreferences
     * @return Location object หรือ null ถ้าไม่มีข้อมูล
     */
    @Nullable
    public Location getLastSavedLocation() {
        if (!hasLocation()) {
            return null;
        }
        
        Location location = new Location("stored");
        location.setLatitude(preferences.getFloat(KEY_LATITUDE, 0f));
        location.setLongitude(preferences.getFloat(KEY_LONGITUDE, 0f));
        
        float accuracy = preferences.getFloat(KEY_ACCURACY, -1f);
        if (accuracy >= 0) {
            location.setAccuracy(accuracy);
        }
        
        float altitude = preferences.getFloat(KEY_ALTITUDE, 0f);
        if (altitude != 0) {
            location.setAltitude(altitude);
        }
        
        float speed = preferences.getFloat(KEY_SPEED, 0f);
        if (speed > 0) {
            location.setSpeed(speed);
        }
        
        float bearing = preferences.getFloat(KEY_BEARING, 0f);
        if (bearing > 0) {
            location.setBearing(bearing);
        }
        
        location.setTime(preferences.getLong(KEY_TIMESTAMP, System.currentTimeMillis()));
        
        return location;
    }
    
    /**
     * ดึงเฉพาะ Latitude
     */
    public double getLatitude() {
        return preferences.getFloat(KEY_LATITUDE, 0f);
    }
    
    /**
     * ดึงเฉพาะ Longitude
     */
    public double getLongitude() {
        return preferences.getFloat(KEY_LONGITUDE, 0f);
    }
    
    /**
     * ดึง Latitude และ Longitude เป็น array [lat, lng]
     */
    public double[] getLatLng() {
        return new double[]{
            preferences.getFloat(KEY_LATITUDE, 0f),
            preferences.getFloat(KEY_LONGITUDE, 0f)
        };
    }
    
    /**
     * ตรวจสอบว่ามีข้อมูล Location เก็บไว้หรือไม่
     */
    public boolean hasLocation() {
        return preferences.getBoolean(KEY_HAS_LOCATION, false);
    }
    
    /**
     * ดึงเวลาที่บันทึกล่าสุด (timestamp)
     */
    public long getLastUpdateTime() {
        return preferences.getLong(KEY_TIMESTAMP, 0);
    }
    
    /**
     * ตรวจสอบว่าข้อมูลเก่าเกินกำหนดหรือไม่
     * @param maxAgeMillis อายุสูงสุดที่ยอมรับได้ (milliseconds)
     * @return true ถ้าข้อมูลเก่าเกินไป
     */
    public boolean isLocationTooOld(long maxAgeMillis) {
        if (!hasLocation()) return true;
        
        long age = System.currentTimeMillis() - getLastUpdateTime();
        return age > maxAgeMillis;
    }
    
    /**
     * ดึง accuracy ล่าสุด
     */
    public float getAccuracy() {
        return preferences.getFloat(KEY_ACCURACY, -1f);
    }
    
    /**
     * ดึง provider ล่าสุด
     */
    public String getProvider() {
        return preferences.getString(KEY_PROVIDER, "unknown");
    }
    
    /**
     * ลบข้อมูล Location ทั้งหมด
     */
    public void clearLocation() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
    
    /**
     * ลบข้อมูลถ้าเก่าเกินกำหนด
     * @param maxAgeMillis อายุสูงสุดที่ยอมรับได้
     */
    public void clearIfTooOld(long maxAgeMillis) {
        if (isLocationTooOld(maxAgeMillis)) {
            clearLocation();
        }
    }
    
    /**
     * ดึงข้อมูลเป็น String สำหรับแสดงผล
     */
    public String getLocationString() {
        if (!hasLocation()) {
            return "No location saved";
        }
        
        return String.format("%.6f, %.6f", getLatitude(), getLongitude());
    }
    
    /**
     * ดึงข้อมูลแบบละเอียด
     */
    public String getDetailedLocationString() {
        if (!hasLocation()) {
            return "No location data";
        }
        
        long age = System.currentTimeMillis() - getLastUpdateTime();
        long ageSeconds = age / 1000;
        long ageMinutes = ageSeconds / 60;
        long ageHours = ageMinutes / 60;
        
        String ageString;
        if (ageHours > 0) {
            ageString = ageHours + " hours ago";
        } else if (ageMinutes > 0) {
            ageString = ageMinutes + " minutes ago";
        } else {
            ageString = ageSeconds + " seconds ago";
        }
        
        return String.format(
            "Lat: %.6f\nLng: %.6f\nAccuracy: %.2f m\nProvider: %s\nUpdated: %s",
            getLatitude(),
            getLongitude(),
            getAccuracy(),
            getProvider(),
            ageString
        );
    }
}


// ===== LocationStorageHelper.java - Helper สำหรับใช้ร่วมกับ LocationManager =====

/**
 * Helper class ที่รวม LocationManager และ LocationStorageManager เข้าด้วยกัน
 * เพื่อให้ใช้งานง่ายและจัดการ location แบบอัตโนมัติ
 */
class LocationStorageHelper {
    
    private LocationManager locationManager;
    private LocationStorageManager storageManager;
    private Context context;
    
    // กำหนดอายุสูงสุดของ location ที่ยอมรับได้
    private static final long MAX_LOCATION_AGE = 30 * 60 * 1000; // 30 นาที
    
    public interface LocationResultListener {
        void onLocationReceived(double latitude, double longitude, Location location, boolean isFromCache);
        void onLocationError(String error);
    }
    
    public LocationStorageHelper(Context context) {
        this.context = context.getApplicationContext();
        this.locationManager = new LocationManager(this.context);
        this.storageManager = new LocationStorageManager(this.context);
    }
    
    /**
     * ดึง location แบบอัจฉริยะ:
     * 1. ตรวจสอบ SharedPreferences ก่อน (ถ้ายังไม่เก่ามาก)
     * 2. ถ้าไม่มีหรือเก่าเกินไป ค่อยดึงจาก GPS/Network
     * 3. บันทึกลง SharedPreferences อัตโนมัติ
     */
    public void getSmartLocation(LocationResultListener listener) {
        // ตรวจสอบ permission ก่อน
        if (!locationManager.hasLocationPermission()) {
            listener.onLocationError("ไม่มีสิทธิ์เข้าถึง Location");
            return;
        }
        
        // ตรวจสอบว่ามี location ใน SharedPreferences หรือไม่
        if (storageManager.hasLocation() && 
            !storageManager.isLocationTooOld(MAX_LOCATION_AGE)) {
            
            // มี location ที่ยังไม่เก่ามาก ส่งกลับทันที
            Location cachedLocation = storageManager.getLastSavedLocation();
            if (cachedLocation != null) {
                listener.onLocationReceived(
                    cachedLocation.getLatitude(),
                    cachedLocation.getLongitude(),
                    cachedLocation,
                    true // มาจาก cache
                );
                
                // แต่ยังดึงตำแหน่งใหม่ใน background เพื่ออัพเดท
                fetchFreshLocation(listener);
                return;
            }
        }
        
        // ไม่มี cache หรือ cache เก่าเกินไป ดึงใหม่
        fetchFreshLocation(listener);
    }
    
    /**
     * ดึง location ใหม่จาก GPS/Network และบันทึกลง SharedPreferences
     */
    private void fetchFreshLocation(LocationResultListener listener) {
        locationManager.setLocationListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude, Location location) {
                // บันทึกลง SharedPreferences
                storageManager.saveLocation(location);
                
                // ส่งกลับให้ listener
                if (listener != null) {
                    listener.onLocationReceived(latitude, longitude, location, false);
                }
            }
            
            @Override
            public void onLocationError(String error) {
                if (listener != null) {
                    listener.onLocationError(error);
                }
            }
        });
        
        locationManager.getLastLocation();
    }
    
    /**
     * บังคับดึง location ใหม่ (ไม่ใช้ cache)
     */
    public void forceRefreshLocation(LocationResultListener listener) {
        if (!locationManager.hasLocationPermission()) {
            listener.onLocationError("ไม่มีสิทธิ์เข้าถึง Location");
            return;
        }
        
        fetchFreshLocation(listener);
    }
    
    /**
     * เริ่ม tracking แบบต่อเนื่องและบันทึกทุกครั้งที่อัพเดท
     */
    public void startTrackingWithAutoSave(LocationResultListener listener) {
        locationManager.setLocationListener(new LocationManager.LocationListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude, Location location) {
                // บันทึกลง SharedPreferences ทุกครั้ง
                storageManager.saveLocation(location);
                
                if (listener != null) {
                    listener.onLocationReceived(latitude, longitude, location, false);
                }
            }
            
            @Override
            public void onLocationError(String error) {
                if (listener != null) {
                    listener.onLocationError(error);
                }
            }
        });
        
        locationManager.startLocationUpdates();
    }
    
    public void stopTracking() {
        locationManager.stopLocationUpdates();
    }
    
    public LocationStorageManager getStorageManager() {
        return storageManager;
    }
    
    public LocationManager getLocationManager() {
        return locationManager;
    }
    
    public void destroy() {
        locationManager.destroy();
    }
}


// ===== ตัวอย่างการใช้งาน =====

/*
public class MainActivity extends AppCompatActivity {
    
    private LocationStorageHelper locationHelper;
    private TextView tvLocation, tvCacheInfo;
    private Button btnGetLocation, btnRefresh, btnClear;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tvLocation = findViewById(R.id.tvLocation);
        tvCacheInfo = findViewById(R.id.tvCacheInfo);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnClear = findViewById(R.id.btnClear);
        
        locationHelper = new LocationStorageHelper(this);
        
        // แสดงข้อมูล cache ที่มีอยู่
        displayCachedLocation();
        
        // ปุ่มดึง location อัจฉริยะ (จาก cache หรือ GPS)
        btnGetLocation.setOnClickListener(v -> {
            tvLocation.setText("Loading...");
            locationHelper.getSmartLocation(new LocationStorageHelper.LocationResultListener() {
                @Override
                public void onLocationReceived(double lat, double lng, Location location, boolean isFromCache) {
                    String source = isFromCache ? "📦 From Cache" : "🛰️ From GPS/Network";
                    tvLocation.setText(String.format(
                        "%s\n\nLat: %.6f\nLng: %.6f\nAccuracy: %.2f m\nProvider: %s",
                        source, lat, lng,
                        LocationManager.getAccuracy(location),
                        location.getProvider()
                    ));
                    
                    displayCachedLocation();
                }
                
                @Override
                public void onLocationError(String error) {
                    tvLocation.setText("Error: " + error);
                }
            });
        });
        
        // ปุ่ม Refresh - บังคับดึงใหม่
        btnRefresh.setOnClickListener(v -> {
            tvLocation.setText("Refreshing from GPS...");
            locationHelper.forceRefreshLocation(new LocationStorageHelper.LocationResultListener() {
                @Override
                public void onLocationReceived(double lat, double lng, Location location, boolean isFromCache) {
                    tvLocation.setText(String.format(
                        "🛰️ Fresh Location\n\nLat: %.6f\nLng: %.6f\nAccuracy: %.2f m",
                        lat, lng, LocationManager.getAccuracy(location)
                    ));
                    displayCachedLocation();
                }
                
                @Override
                public void onLocationError(String error) {
                    tvLocation.setText("Error: " + error);
                }
            });
        });
        
        // ปุ่มลบ cache
        btnClear.setOnClickListener(v -> {
            locationHelper.getStorageManager().clearLocation();
            displayCachedLocation();
            Toast.makeText(this, "Cache cleared", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void displayCachedLocation() {
        LocationStorageManager storage = locationHelper.getStorageManager();
        if (storage.hasLocation()) {
            tvCacheInfo.setText("💾 Cached Location:\n" + 
                storage.getDetailedLocationString());
        } else {
            tvCacheInfo.setText("💾 No cached location");
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHelper.destroy();
    }
}
*/
