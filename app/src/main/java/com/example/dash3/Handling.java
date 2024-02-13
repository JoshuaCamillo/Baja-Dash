package com.example.dash3;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Handling {
    private double latitude = MainActivity.latitude;
    private double oldlatitude =-1;
    private double longitude = MainActivity.longitude;
    private double oldlongitude =-1;
    private int RPM = MainActivity.RPM;
    private int oldRPM = -1;
    private int speed = MainActivity.speedInt;
    private int oldspeed = 0;        //placeholder for old speed test, will be changed when started
    private int Fuel = MainActivity.fuel;
    private int oldFuel = -1;
    private int battery = MainActivity.battery;
    private int oldbattery = -1;
    private int LB = MainActivity.LB;
    private int oldLB = -1;
    private int RB = MainActivity.RB;
    private int oldRB = -1;
    private int LF = MainActivity.LF;
    private int oldLF = -1;
    private int RF = MainActivity.RF;
    private int oldRF = -1;
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
    private int panic = MainActivity.panic;
    private int oldpanic = 0;
    private int panicking = MainActivity.panicking;
    private int phoneBat = MainActivity.phoneBat;
    private int oldphoneBat = -1;

    // Set this duration according to your requirements
    private static final long PANIC_DURATION = 60000; // 60000 milliseconds (60 seconds)


    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference speedRef = database.getReference("Speed");
    DatabaseReference latRef = database.getReference("Latitude");
    DatabaseReference longRef = database.getReference("Longitude");
    DatabaseReference fuelRef = database.getReference("Fuel");
    DatabaseReference battRef = database.getReference("Battery");
    DatabaseReference RPMRef = database.getReference("RPM");
    DatabaseReference XRef = database.getReference("xAcceleration");
    DatabaseReference YRef = database.getReference("yAcceleration");
    DatabaseReference ZRef = database.getReference("zAcceleration");
    DatabaseReference XposRef = database.getReference("xPosition");
    DatabaseReference YposRef = database.getReference("yPosition");
    DatabaseReference ZposRef = database.getReference("zPosition");
    DatabaseReference dataRef = database.getReference("data");
    DatabaseReference LFRef = database.getReference("LF");
    DatabaseReference LBRef = database.getReference("LB");
    DatabaseReference RFRef = database.getReference("RF");
    DatabaseReference RBRef = database.getReference("RB");
    DatabaseReference messRef = database.getReference("/Message to driver/message");
    DatabaseReference panicRef = database.getReference("Panic");
    DatabaseReference phoneBattRef = database.getReference("phoneBattery");
    private String oldMessage = "old";



    public Handling(long intervalMillis) {      // call this method with a set time period to continuously repeat
        this.intervalMillis = intervalMillis;
        handler = new Handler();
        createRunnable();
    }
    private void readMessageFromFirebase() {
        messRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newMessage = dataSnapshot.getValue(String.class);
                if (newMessage != null && !newMessage.equals(oldMessage)) {
                    // Update the message variable in MainActivity or perform other actions
                    MainActivity.updateMessage(newMessage);
                    oldMessage = newMessage;

                    Log.d("Firebase", "New Message: " + newMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                Log.e("Firebase", "Error reading value from Firebase", error.toException());
            }
        });
    }

    private void createRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {
                checkAndHandlePanic();

                if(MainActivity.speedInt != oldspeed) {
                    speedRef.setValue(MainActivity.speedInt);
                    oldspeed = MainActivity.speedInt;
                }
                if(MainActivity.latitude != oldlatitude) {
                    latRef.setValue(MainActivity.latitude);
                    oldlatitude = MainActivity.latitude;
                }
                if(MainActivity.longitude != oldlongitude) {
                    longRef.setValue(MainActivity.longitude);
                    oldlongitude = MainActivity.longitude;
                }
                if(MainActivity.fuel != oldFuel) {
                    fuelRef.setValue(MainActivity.fuel);
                    oldFuel = MainActivity.fuel;
                }
                if(MainActivity.battery != oldbattery) {
                    battRef.setValue(MainActivity.battery);
                    oldbattery = MainActivity.battery;
                }
                if(MainActivity.RPM != oldRPM) {
                    RPMRef.setValue(MainActivity.RPM);
                    oldRPM = MainActivity.RPM;
                }
                if(MainActivity.Xa != Xa) {
                    XRef.setValue(Math.round(MainActivity.Xa));
                    oldXa = MainActivity.Xa;
                }
                if(MainActivity.Ya != Ya) {
                    YRef.setValue(Math.round(MainActivity.Ya));
                    OldYa = MainActivity.Ya;
                }
                if(MainActivity.Za != Za) {
                    ZRef.setValue(Math.round(MainActivity.Za));
                    oldZa = MainActivity.Za;
                }
                if(MainActivity.Xpos != Xpos) {
                    XposRef.setValue(Math.round(MainActivity.Xpos));
                    oldXpos = MainActivity.Xpos;
                }
                if(MainActivity.Ypos != Ypos) {
                    YposRef.setValue(Math.round(MainActivity.Ypos));
                    OldYpos = MainActivity.Ypos;
                }
                if(MainActivity.Zpos != Zpos) {
                    ZposRef.setValue(Math.round(MainActivity.Zpos));
                    oldZpos = MainActivity.Zpos;
                }

                if(MainActivity.data != oldData) {
                    dataRef.setValue(MainActivity.data);
                    oldData = MainActivity.data;
                }
                if(MainActivity.LF != oldLF) {
                    LFRef.setValue(MainActivity.LF);
                    oldLF = MainActivity.LF;
                }
                if(MainActivity.LB != oldLB) {
                    LBRef.setValue(MainActivity.LB);
                    oldLB = MainActivity.LB;
                }
                if(MainActivity.RF != oldRF) {
                    RFRef.setValue(MainActivity.RF);
                    oldRF = MainActivity.RF;
                }
                if(MainActivity.RB != oldRB) {
                    RBRef.setValue(MainActivity.RB);
                    oldRB = MainActivity.RB;
                }
                if(MainActivity.phoneBat != oldphoneBat) {
                    phoneBattRef.setValue(MainActivity.phoneBat);
                    oldphoneBat = MainActivity.phoneBat;
                }

                readMessageFromFirebase();


                handler.postDelayed(this, intervalMillis);
            }
        };
    }
    public void start() {       // call this with the Handling function in anotehr class to start repeating
        handler.post(runnable);

    }
    private void checkAndHandlePanic() {
        if (panic == 1 && oldpanic == 0) {
            panicking = 1;
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < PANIC_DURATION) {
                // Handle panic state here if needed
            }

            panicking = 0;
        }

        // Update oldPanic for the next check
        oldpanic = panic;
    }


    public void stop() { // call this to stop
        handler.removeCallbacks(runnable);
    }


    public void updateFuel(int fuel){
        Fuel = fuel;
    }
    public void updateRPM(int value){
        RPM = value;
    }

}
