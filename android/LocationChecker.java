package com.example.newmediawritingprizesubmission2.location;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.*;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.util.Log;

public class LocationChecker {
    private static final String TAG = "LocationChecker";
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;

    // London bounding box
    private static final double LONDON_LAT_MIN = 51.2868;
    private static final double LONDON_LAT_MAX = 51.6919;
    private static final double LONDON_LON_MIN = -0.5104;
    private static final double LONDON_LON_MAX = 0.3340;

    public LocationChecker(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void isInLondon(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            callback.onLocationResult(false);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        boolean isInLondon = location.getLatitude() >= LONDON_LAT_MIN &&
                                location.getLatitude() <= LONDON_LAT_MAX &&
                                location.getLongitude() >= LONDON_LON_MIN &&
                                location.getLongitude() <= LONDON_LON_MAX;

                        Log.d(TAG, String.format("Location: %.4f, %.4f - In London: %b",
                                location.getLatitude(), location.getLongitude(), isInLondon));

                        callback.onLocationResult(isInLondon);
                    } else {
                        Log.w(TAG, "Location is null");
                        callback.onLocationResult(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location", e);
                    callback.onLocationResult(false);
                });
    }

    public interface LocationCallback {
        void onLocationResult(boolean isInLondon);
    }
}
