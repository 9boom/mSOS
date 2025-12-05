package main.sos;

import android.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import com.google.android.material.color.utilities.MathUtils;
import com.google.common.graph.PredecessorsFunction;

public class PreferenceManager {
    private Context context;
    public static final String SECTION = "MAIN_PREF";
    private SharedPreferences prefs;
    public ReportPreferencesManager report;

    public PreferenceManager(Context context) {
        this.context = context;
        report = new ReportPreferencesManager(this);
    }

    public void addData(String key, String value) {
        prefs = context.getSharedPreferences(SECTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String readData(String key) {
        prefs = context.getSharedPreferences(SECTION, Context.MODE_PRIVATE);
        return prefs.getString(key, null);
    }
    
    public Boolean isKeyAvalible(String key){
        prefs = context.getSharedPreferences(SECTION, Context.MODE_PRIVATE);
        String check = prefs.getString(key, null);
        if (check != null){
            return true;
        }
        return false;
    }

    public void deleteData(String key) {
        prefs = context.getSharedPreferences(SECTION, Context.MODE_PRIVATE);
        prefs.edit().remove(key).apply();
    }

    public void clear() {
        prefs = context.getSharedPreferences(SECTION, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    public static class ReportPreferencesManager {
        PreferenceManager preferencesManager;
        public ReportPreferencesManager(PreferenceManager preferencesManager) {
            this.preferencesManager = preferencesManager;
        }

        public boolean storageReport(MainActivity.Report report) {
            preferencesManager.addData("ID", String.valueOf(report.id));
            preferencesManager.addData("NAME", report.name);
            preferencesManager.addData("CONTACT", report.contact);
            preferencesManager.addData("DETAIL", report.details);
            preferencesManager.addData("LOCAION_LAT", String.valueOf(report.location.lat));
            preferencesManager.addData("LOCAION_LNG", String.valueOf(report.location.lng));
            preferencesManager.addData("TIMESTAMP", report.timestamp);
            preferencesManager.addData("LEVEL", report.level);
            preferencesManager.addData("TYPE", report.type);
            preferencesManager.addData("STATUS", report.status);
            preferencesManager.addData("RELAYED", String.valueOf(report.relayed));
            if (isReported()){
            return true;}
            return false;
        }
        
        public Boolean isReported() {
            if (preferencesManager.isKeyAvalible("ID")){
                return true;
            }
            return false;
        }

        public Integer getId() {
            return Integer.parseInt(preferencesManager.readData("ID"));
        }
        public String getName() {
            return (preferencesManager.readData("NAME"));
        }
        
        public String getContact() {
            return (preferencesManager.readData("CONTACT"));
        }
        
        public String getDetails() {
            return (preferencesManager.readData("DETAIL"));
        }
        
        public Float getLat() {
            return Float.parseFloat(preferencesManager.readData("LOCATION_LAT"));
        }
        
        public Float getLng() {
            return Float.parseFloat(preferencesManager.readData("LOCATION_LNG"));
        }
        
        public String getTimestamp() {
            return (preferencesManager.readData("TIMESTAMP"));
        }
        
        public String getLevel() {
            return (preferencesManager.readData("LEVEL"));
        }
        
        public String getType() {
            return (preferencesManager.readData("TYPE"));
        }
        
        public String getStatus() {
            return (preferencesManager.readData("STATUS"));
        }
        
        public Boolean getRelayed() {
            return Boolean.parseBoolean(preferencesManager.readData("RELAYED"));
        }
    }

    //        readData("NAME");
    //        readData("CONTACT");
    //        readData("DETAIL");
    //        readData("LOCAION_LAT");
    //        readData("LOCAION_LNG");
    //        readData("TIMESTAMP");
    //        readData("LEVEL");
    //        readData("TYPE");
    //        readData("STATUS");
    //        readData("RELAYED");
}
