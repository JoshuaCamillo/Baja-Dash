package com.example.dash3;

import static com.example.dash3.MainActivity.isKphSelected;
import static com.example.dash3.MainActivity.isSRButtonPressed;


import static java.lang.Math.abs;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class ModeSelect {
    private final TextView unitsText;
    private final TextView lastLap;
    private final TextView diffLap;
    private int RPM = MainActivity.RPM;
    private double timer;
    public static double laptime = 0;
    private Handler handler;
    private String timedisp = "";       //time of the current lap
    public static boolean enduro;
    private int delay = 50;
    private int resetTimer = MainActivity.laptimeReset;
    private long refTime;
    private long elapsedTime;
    private boolean firstTime = true;
    private String lastLapTime = "";        //time of the last lap
    public static long lastLapVal = 0;
    private long lastLastLap = 0;
    private String diffLapTime = "";    //difference in lap times
    private int minutes;
    private int seconds;


    public ModeSelect(TextView unitsText, TextView lastLap, TextView diffLap){
        this.unitsText = unitsText;
        this.lastLap = lastLap;
        this.diffLap = diffLap;
        handler = new Handler();

    }

    public void startUpdates() {
        handler.post(updateRunnable);
    }

    public void stopUpdates() {
        handler.removeCallbacks(updateRunnable);
    }


    private Runnable updateRunnable = new Runnable() {                              //need to change formatting so it will show only up to 60 secs and minutes
        @Override
        public void run() {
            resetTimer = MainActivity.laptimeReset;
            Log.d("enduroselect", String.valueOf(enduro));
            // Update the UI with speed and RPM values
            if(enduro) {                                    //if teh enduro button is pressed, allow driver to reset their timer
                if(resetTimer != MainActivity.oldLaptimeReset){             // checks if there was a change in the input form esp
                    MainActivity.oldLaptimeReset = resetTimer;      // set the old value to current so change is known
                    if(firstTime){
                        lastLapTime = "";
                        firstTime = false;                  //not done doing the last time thing
                        laptime = -111;
                    }else {
                        lastLapVal = Math.round(laptime);       //get the value of the last lap

                        int lastMin = (int) (lastLapVal / 60000);
                        int lastSec = (int) ((lastLapVal % 60000) / 1000);
                        lastLapTime = String.format("%02d:%02d", lastMin, lastSec);     //format the last lap time

                        float lapDiff = (float) (laptime - lastLastLap);                //get the difference in lap times
                        int secDiff = (int) ((abs(lapDiff) % 60000) / 1000);
                        int minDiff = (int) (abs(lapDiff) / 60000);

                        diffLapTime = String.format("%02d:%02d", minDiff, secDiff);     //format the difference in lap times
                        diffLap.setText(diffLapTime);

                        if (lapDiff < 0) {
                            diffLap.setTextColor(Color.GREEN); // Set text color to green for negative lap difference
                        } else if (lapDiff > 0) {
                            diffLap.setTextColor(Color.RED); // Set text color to red for positive lap difference
                        } else {
                            // Set default text color if lap difference is 0
                            diffLap.setTextColor(Color.WHITE);
                        }
                        lastLastLap = lastLapVal;                       //set the refrenece value to determine the difference in lap times
                    }
                    laptime = 0;
                    Log.d("testres", String.valueOf(resetTimer));
                    refTime = System.currentTimeMillis();
                }else  {
                    laptime = System.currentTimeMillis() - refTime;

                }
                if (refTime == 0) {
                    laptime = 0;
                }else {
                    laptime = Math.round(laptime);
                }
                laptime = Math.round(laptime);
                Log.d("Laptime", String.valueOf(laptime));

                // Calculate minutes and seconds
                minutes = (int) (laptime / 60000);
                seconds = (int) ((laptime % 60000) / 1000);


                // Format the time string
                Log.d("Minutes", String.valueOf(minutes));
                Log.d("Seconds", String.valueOf(seconds));
                timedisp = String.format("%02d:%02d", minutes, seconds);
                unitsText.setText(timedisp);
                lastLap.setText(lastLapTime);
                Log.d("Laptimer", timedisp);

            }
            else {
                lastLap.setText("");                //make the last lap invisible when not in enduro
                diffLap.setText("");                //make the difference in lap times invisible when not in enduro
                if (isSRButtonPressed) {
                    unitsText.setText("RPM");
                } else if (isKphSelected) {
                    unitsText.setText("KPH");
                } else {
                    unitsText.setText("MPH");
                }
            }

            handler.postDelayed(this, delay);                      //change refresh rate of speedo display

        }
    };

}
