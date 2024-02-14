package com.example.dash3;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

public class SpeedoUpdate {
    private Handler handler;
    private TextView testText;
    private TextView alternateText;
    private String speedText;

    public SpeedoUpdate(TextView testText, TextView alternateText) {
        this.testText = testText;
        this.alternateText = alternateText;
        handler = new Handler();
    }

    public void startUpdates() {
        handler.post(updateRunnable);
    }

    public void stopUpdates() {
        handler.removeCallbacks(updateRunnable);
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // Update the UI with speed and RPM values

            if (MainActivity.isSRButtonPressed) {
               testText.setText(String.valueOf(MainActivity.RPM));
               alternateText.setText(MainActivity.speedText);
            } else {
                testText.setText(MainActivity.speedText);
                alternateText.setText(String.valueOf(MainActivity.RPM));
            }


            // Log other data
            Log.d("Speed", MainActivity.speedText);
            Log.d("Lat", String.valueOf(MainActivity.latitude));
            Log.d("Long", String.valueOf(MainActivity.longitude));
            Log.d("GyroX", String.valueOf(MainActivity.Xa));
            Log.d("GyroY", String.valueOf(MainActivity.Ya));
            Log.d("GyroZ", String.valueOf(MainActivity.Za));
            Log.d("Data", String.valueOf(MainActivity.data));
            Log.d("LF", String.valueOf(MainActivity.LF));
            Log.d("RF", String.valueOf(MainActivity.RF));
            Log.d("LB", String.valueOf(MainActivity.LB));
            Log.d("RB", String.valueOf(MainActivity.RB));
            Log.d("Panic", String.valueOf(MainActivity.panic));
            Log.d("PhoneBat", String.valueOf(MainActivity.phoneBat));
            Log.d("KPHselect", String.valueOf(MainActivity.isKphSelected));
            Log.d("SRButton", String.valueOf(MainActivity.isSRButtonPressed));

            // Repeat the update after 50 milliseconds
            handler.postDelayed(this, 25);
        }
    };
}
