package com.example.dash3;


import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class Speedometer implements Runnable, LocationListener {
    private Context context;

    public Speedometer(Context context) {
        this.context = context;
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Get the speed from the location object in meters per second
        float speedMps = location.getSpeed();
        MainActivity.latitude = location.getLatitude();
        MainActivity.longitude = location.getLongitude();
        // Convert the speed to miles per hour
        float speedMph = speedMps * 2.23694f;
        float speedKph = speedMps * 3.6f;
        MainActivity.speedInt = Math.round(speedMph);
        MainActivity.speedKPH = Math.round(speedKph);

        Log.d("SpeedDebug", "Speed in MPH: " + speedMph);
        Log.d("SpeedDebug", "Speed in KPH: " + speedMps * 3.6f);

        // Format and display the speed in mph
        if (speedMph == 0.0f) {
            MainActivity.speedText = "0";
        } else {
            if(MainActivity.isKphSelected)
                MainActivity.speedText = String.format("%.0f", speedKph);
            else
                MainActivity.speedText = String.format("%.0f", speedMph);
        }

        //MainActivity.updateUI();

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Toast.makeText(context, "GPS is disabled.", Toast.LENGTH_SHORT).show();
    }

    public String updateLoc(boolean bol) {
        return MainActivity.speedText;
    }


    @Override
    public void run() {
// Create a new LocationManager object
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Check if location permission is granted before requesting updates
        if (context.checkSelfPermission(
                android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,                                                    //change first value for time between reading
                    50, 0, this);
        } else {
            Toast.makeText(context, "Location permission denied. Speedometer will not work.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void stop() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
    }
}
