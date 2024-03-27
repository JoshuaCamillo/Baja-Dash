package com.example.dash3;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

//class to take care of displaying speed and RPM on the UI as numbers (not the bar)
public class SpeedoUpdate {
    private Handler handler;
    private TextView testText;
    private TextView alternateText;
    private int remainingTime;
    private String timedisp;
    private int hours, minutes, seconds;

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
            remainingTime = (int)(MainActivity.decreasingTime);

            hours = remainingTime / 3600000;
            minutes = (remainingTime % 3600000) / 60000;
            seconds = (remainingTime % 60000) / 1000;

            timedisp = String.format("%01d:%02d:%02d", hours, minutes, seconds);

            if (MainActivity.speedRPMSelect) {
               testText.setText(String.valueOf(MainActivity.RPM));
               if(!MainActivity.enduro) {
                   alternateText.setText(MainActivity.speedText);
               }else{
                   alternateText.setText(timedisp);
               }
            } else {
                testText.setText(MainActivity.speedText);
                if(!MainActivity.enduro) {
                    alternateText.setText(String.valueOf(MainActivity.RPM));
                }else{
                    alternateText.setText(timedisp);
                }
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
            Log.d("SRButton", String.valueOf(MainActivity.speedRPMSelect));


            handler.postDelayed(this, 25);                      //change refresh rate of speedo display
        }
    };
}
