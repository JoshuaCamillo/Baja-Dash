package com.example.dash3;

import android.os.Handler;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Handling {
    private double latitude = MainActivity.latitude;
    private double oldlatitude =-1;
    private double longitude = MainActivity.longitude;
    private double oldlongitude =-1;
    private int RPM = MainActivity.RPM;
    private int oldRPM = -1;
    private int speed = MainActivity.speedInt;
    private int oldspeed = 0;        //placeholder for old speed test, will be changed when started
    private double Fuel = MainActivity.fuel;
    private double oldFuel = -1;
    private Handler handler;
    private Runnable runnable;
    private long intervalMillis;
    private float Xa = MainActivity.Xa;
    private float Ya = MainActivity.Ya;
    private float Za = MainActivity.Za;
    private float oldXa,OldYa,oldZa = 9000;
    private float Xpos = MainActivity.Xpos;
    private float Ypos = MainActivity.Ypos;
    private float Zpos = MainActivity.Zpos;
    private float oldXpos,OldYpos,oldZpos = 9000;
    private String data =MainActivity.data;
    private String oldData = "wrong";

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference speedRef = database.getReference("Speed");
    DatabaseReference latRef = database.getReference("Latitude");
    DatabaseReference longRef = database.getReference("Longitude");
    DatabaseReference fuelRef = database.getReference("Fuel");
    DatabaseReference RPMRef = database.getReference("RPM");
    DatabaseReference XRef = database.getReference("xAcceleration");
    DatabaseReference YRef = database.getReference("yAcceleration");
    DatabaseReference ZRef = database.getReference("zAcceleration");
    DatabaseReference XposRef = database.getReference("xPosition");
    DatabaseReference YposRef = database.getReference("yPosition");
    DatabaseReference ZposRef = database.getReference("zPosition");
    DatabaseReference dataRef = database.getReference("data");



    public Handling(long intervalMillis) {      // call this method with a set time period to continuously repeat
        this.intervalMillis = intervalMillis;
        handler = new Handler();
        createRunnable();
    }
    private void createRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {

                if(MainActivity.speedInt != oldspeed)
                    speedRef.setValue(MainActivity.speedInt);
                    oldspeed = MainActivity.speedInt;

                if(MainActivity.latitude != oldlatitude)
                    latRef.setValue(MainActivity.latitude);
                    oldlatitude = MainActivity.latitude;

                if(MainActivity.longitude != oldlongitude)
                    longRef.setValue(MainActivity.longitude);
                oldlongitude = MainActivity.longitude;

                if(MainActivity.fuel != oldFuel)
                    fuelRef.setValue(MainActivity.fuel);
                    oldFuel = MainActivity.fuel;

                if(MainActivity.RPM != oldRPM)
                    RPMRef.setValue(MainActivity.RPM);
                    oldRPM = MainActivity.RPM;

                if(MainActivity.Xa != Xa)
                    XRef.setValue(Math.round(MainActivity.Xa * 1000.0) / 1000.0);
                    oldXa = MainActivity.Xa;
                if(MainActivity.Ya != Ya)
                    YRef.setValue(Math.round(MainActivity.Ya * 1000.0) / 1000.0);
                    OldYa = MainActivity.Ya;
                if(MainActivity.Za != Za)
                    ZRef.setValue(Math.round(MainActivity.Za * 1000.0) / 1000.0);
                    oldZa = MainActivity.Za;

                if(MainActivity.Xpos != Xpos)
                    XposRef.setValue(Math.round(MainActivity.Xpos));
                oldXpos = MainActivity.Xpos;
                if (!Float.isNaN(MainActivity.Ypos) && !Float.isInfinite(MainActivity.Ypos)) {
                    int newYpos = Math.round(MainActivity.Ypos);
                    if (newYpos != OldYpos) {
                        YposRef.setValue(newYpos);
                    }
                    OldYpos = newYpos;
                } else {
                    // Handle the case where MainActivity.Ypos is NaN or infinite
                    Log.e("Firebase", "Invalid Ypos value: " + MainActivity.Ypos);
                }
                if(MainActivity.Zpos != Zpos)
                    ZposRef.setValue(Math.round(MainActivity.Zpos));
                oldZpos = MainActivity.Zpos;

                if(MainActivity.data != oldData)
                    dataRef.setValue(MainActivity.data);
                    oldData = MainActivity.data;


                handler.postDelayed(this, intervalMillis);
            }
        };
    }
    public void start() {       // call this with the Handling function in anotehr class to start repeating
        handler.post(runnable);
    }

    public void stop() { // call this to stop
        handler.removeCallbacks(runnable);
    }


    public void updateFuel(double fuel){
        Fuel = fuel;
    }
    public void updateRPM(int value){
        RPM = value;
    }

}
