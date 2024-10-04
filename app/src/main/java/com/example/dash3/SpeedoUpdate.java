package com.example.dash3;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

//class to take care of displaying speed and RPM on the UI as numbers (not the bar)
public class SpeedoUpdate {
    private Handler handler;
    private TextView testText;
    private TextView alternateText;
    private TextView carInfrontText;
    private int remainingTime;
    private String timedisp;
    private int hours, minutes, seconds;

    public SpeedoUpdate(TextView testText, TextView alternateText, TextView carInfrontText) {
        this.testText = testText;
        this.alternateText = alternateText;
        this.carInfrontText = carInfrontText;
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

            if (MainActivity.enduro) {
                carInfrontText.setVisibility(TextView.VISIBLE);
                carInfrontText.setText(MainActivity.carNum);
                //need to add logic for showing the last lap and difference
            }else {
                carInfrontText.setVisibility(TextView.INVISIBLE);
            }

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

            handler.postDelayed(this, 25);                      //change refresh rate of speedo display
        }
    };
}
