package com.example.dash3;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import com.example.dash3.PhoneBattery;
import android.view.View;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

//class to handle all the data being sent to the firebase database
public class Handling {
    private double oldlatitude =-1;
    private double oldlongitude =-1;
    private int RPM = MainActivity.RPM;
    private int oldRPM = -1;
    private int oldspeed = 0;        //placeholder for old speed test, will be changed when started
    private int Fuel = MainActivity.fuel;
    private int oldFuel = -1;
    private int oldbattery = -1;
    private int oldLB = -1;
    private int oldRB = -1;
    private int oldLF = -1;
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

    private int oldpanicking = 0;
    private int oldphoneBat = -1;
    private double oldphoneTemp = -1;
    private boolean firstRun  = true;
    private int oldremaining = -1;
    private int oldECVTBat = -1;
    public static String CSVName;
    private String oldCSVName = "old";
    private boolean sendFile = false;
    public static int datalog = 0;
    private int oldDatalog = 0;
    private boolean firstName = true;

    // Set this duration according to your requirements
    private static final long PANIC_DURATION = 120000; // 60000 milliseconds (60 seconds)
    private double oldlaptime = 0;
    private long oldLastLap = 0;
    private String oldCarNum= "old";
    private String oldScrapedLap = "old";
    private String oldBestLap = "old";
    private int bestLapVal = -1;
    private int lastLapVal = -1;
    private int diff = 0;

    private final float chargeAMPs = MainActivity.chargeAmps;
    private float oldchargeAMPS = -1;
    private Context context;


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
    DatabaseReference LFRef = database.getReference("LF");
    DatabaseReference LBRef = database.getReference("LB");
    DatabaseReference RFRef = database.getReference("RF");
    DatabaseReference RBRef = database.getReference("RB");
    DatabaseReference messRef = database.getReference("/Message to driver/message");
    DatabaseReference panicRef = database.getReference("Panic");
    DatabaseReference phoneBattRef = database.getReference("phoneBattery");
    DatabaseReference phonneTempRef = database.getReference("phoneTemperature");
    DatabaseReference raceTimerRef = database.getReference("raceTimer");
    DatabaseReference startTimerRef = database.getReference("Start Timer");
    DatabaseReference remainingTime = database.getReference("remaining Time");
    DatabaseReference lapTimeRef = database.getReference("currentLapTime");
    DatabaseReference lastLapTimeRef = database.getReference("previousLapTime");
    DatabaseReference ECVTBatRef = database.getReference("ECVTBattery");
    DatabaseReference CSVRef = database.getReference("CSVName");
    DatabaseReference DatalogRef = database.getReference("DataLog");
    DatabaseReference carAheadRef = database.getReference("/scraped/carAheadNumber");
    DatabaseReference scrapedLapRef = database.getReference("/scraped/lastLap/0");
    DatabaseReference bestLapScrapedRef = database.getReference("/scraped/bestLap");
    DatabaseReference chargeCurrentRef = database.getReference("ChargeAmps");

    private String oldMessage = "old";

    public Handling(Context context, long intervalMillis) {      // call this method with a set time period to continuously repeat
        this.intervalMillis = intervalMillis;
        handler = new Handler();
        createRunnable();
        this.context = context;
    }
    private void readMessageFromFirebase() {
        messRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newMessage = dataSnapshot.getValue(String.class);
                if (newMessage != null && !newMessage.equals(oldMessage)) {
                    // Update the message variable in MainActivity or perform other actions
                    if(firstRun){
                        oldMessage = newMessage;
                        firstRun = false;
                    }else {
                        MainActivity.updateMessage(newMessage);
                        oldMessage = newMessage;
                    }

                    //Log.d("Firebase", "New Message: " + newMessage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                //Log.e("Firebase", "Error reading value from Firebase", error.toException());
            }
        });
    }
    private void readCSVNameFromFirebase() {
        CSVRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newCSVName = dataSnapshot.getValue(String.class);
                if (newCSVName != null && !newCSVName.equals(oldCSVName)) {
                    if (firstName) {
                        oldCSVName = newCSVName;
                        firstName = false;
                    } else {
                        oldCSVName = newCSVName;
                        CSVName = newCSVName;
                    }

                    //Log.d("Firebase", "New Message: " + newCSVName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                //Log.e("Firebase", "Error reading value from Firebase", error.toException());
            }
        });
    }
    private void readStartDatalogFromFirebase() {
        DatalogRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer storing = dataSnapshot.getValue(Integer.class);
                if (storing != null) {
                    if (storing != oldDatalog) {
                        if (storing == 1) {
                            saveData(CSVName);
                        } else if (storing == 0) {
                            sendFile = true;
                            saveData.removeCallbacksAndMessages(null);
                        }
                        oldDatalog = datalog; // Update oldDatalog with the current value of datalog
                        datalog = storing;    // Update datalog with the new value from Firebase

                        //Log.d("Firebase", "New Datalog Value: " + datalog);
                    }
                } else {
                    //Log.e("Firebase", "Datalog value is null");
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                //Log.e("Firebase", "Error reading value from Firebase", error.toException());
            }
        });
    }

    private void getRaceTimer() {                       //function to get the race timer from the database
        raceTimerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Long value = dataSnapshot.getValue(Long.class);
                if (value != null) {
                    MainActivity.raceTime = value.intValue();
                    //Log.d("Firebase", "Timer: " + MainActivity.raceTime);
                } else {
                    //Log.e("Firebase", "Race timer value is null");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                //Log.e("Firebase", "Error reading timer from Firebase", error.toException());
            }
        });
    }
    private void startTimer() {                       //function to get the race timer from the database
        startTimerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Integer startTimer = dataSnapshot.getValue(Integer.class);
                    if (startTimer != null) {
                        MainActivity.started = startTimer;
                        //Log.d("Firebase", "Start Timer: " + MainActivity.started);
                    } else {
                        //Log.e("Firebase", "Start timer value is null");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                //Log.e("Firebase", "Error reading timer from Firebase", error.toException());
            }
        });
    }
    private void getCarNum() {
        carAheadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newCar = dataSnapshot.getValue(String.class);
                if (newCar !=null && !newCar.equals(oldCarNum)) {
                    MainActivity.carNum = newCar;
                    oldCarNum = newCar;
                    //Log.d("Car Number: " , newCar);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                //Log.e("Firebase", "Error reading value from Firebase", error.toException());
            }
        });
    }

    private void getScrapedLastLap() {
        scrapedLapRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newLap = dataSnapshot.getValue(String.class);
                if (newLap != null && !newLap.equals(oldScrapedLap)) {
                    oldScrapedLap = newLap;
                    MainActivity.scrapedLastLap = newLap;
                    Log.d("Last Lap: ", newLap); // Optional logging
                    lastLapVal = scrapeTimeMill(newLap);

                    if (lastLapVal != -1 && bestLapVal != -1) {
                        diff = lastLapVal - bestLapVal; // this should be negative when the new lap is faster
                        MainActivity.scrapedDiff = diff;
                        Log.d("Time Difference", "Difference in milliseconds: " + MainActivity.scrapedDiff);
                    } else {
                        MainActivity.scrapedDiff = 0; // Handle case when parsing fails
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                // Log.e("Firebase", "Error reading value from Firebase", error.toException());
            }
        });
    }

    private String removeBracketSection(String lapTime) {
        return lapTime.replaceAll("\\s*\\(.*?\\)\\s*", "");
    }

    private int scrapeTimeMill(String lapTime) {
        try {
            String[] time = lapTime.split(":");
            if (time.length != 2) {
                return -1; // Invalid format
            }

            int minutes = Integer.parseInt(time[0]);
            String[] seconds = time[1].split("\\.");
            if (seconds.length != 2) {
                return -1; // Invalid format
            }

            int secondsInt = Integer.parseInt(seconds[0]);
            int milli = Integer.parseInt(seconds[1]);

            return (minutes * 60000) + (secondsInt * 1000) + milli;
        } catch (NumberFormatException e) {
            return -1; // Parsing error, invalid format
        }
    }


    private void getBestScrapedLap() {
        bestLapScrapedRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String newBestLap = dataSnapshot.getValue(String.class);
                if (newBestLap != null && !newBestLap.equals(oldBestLap)) {
                    String cleanedLap = removeBracketSection(newBestLap);
                    oldBestLap = cleanedLap;
                    bestLapVal = scrapeTimeMill(cleanedLap);
                    MainActivity.scrapedBestLap = cleanedLap;
                     //Log.d("Best Lap: ", cleanedLap); // Optional logging
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
                // Log.e("Firebase", "Error reading value from Firebase", error.toException());
            }
        });
    }


    private void createRunnable() {
        runnable = new Runnable() {
            @Override
            public void run() {

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
                }if(MainActivity.phoneTemp != oldphoneTemp) {
                    phonneTempRef.setValue(MainActivity.phoneTemp);
                    oldphoneTemp = MainActivity.phoneTemp;
                }if(MainActivity.decreasingTime != oldremaining) {                  //sending remaining time of timer to site so we can check it is on time
                    remainingTime.setValue(MainActivity.decreasingTime);
                    oldremaining = (int)(MainActivity.decreasingTime);
                }if (MainActivity.completed){
                    startTimerRef.setValue(0);
                    MainActivity.completed = false;
                }if (ModeSelect.laptime != oldlaptime) {
                    lapTimeRef.setValue(ModeSelect.laptime);
                    oldlaptime = ModeSelect.laptime;
                }if (ModeSelect.lastLapVal != oldLastLap) {
                    lastLapTimeRef.setValue(ModeSelect.lastLapVal);
                    oldLastLap = ModeSelect.lastLapVal;
                }if(MainActivity.panicking != oldpanicking) {
                    panicRef.setValue(MainActivity.panicking);
                    oldpanicking = MainActivity.panic;
                }
                if((System.currentTimeMillis() - MainActivity.panicStart)> PANIC_DURATION){
                     MainActivity.panicking = 0;
                }if (MainActivity.ECVTBat != oldECVTBat) {
                    ECVTBatRef.setValue(MainActivity.ECVTBat);
                    oldECVTBat = MainActivity.ECVTBat;
                }if (MainActivity.chargeAmps != oldchargeAMPS){
                    chargeCurrentRef.setValue(String.format("%.3f", MainActivity.chargeAmps));
                    oldchargeAMPS = MainActivity.chargeAmps;
                }
                Log.d("PhoneBattery", "Battery Charge Current: " + oldchargeAMPS+ " mA");


                readMessageFromFirebase();
                readCSVNameFromFirebase();
                readStartDatalogFromFirebase();
                getRaceTimer();
                startTimer();
                getCarNum();
                getScrapedLastLap();
                getBestScrapedLap();

                handler.postDelayed(this, intervalMillis);
            }
        };
    }

    private static Handler saveData = new Handler();
    private int count = 0;

    public void saveData(String fileName) {
        // Specify the folder name
        String folderName = "Documents/Data log";
        if (fileName != null) {

            saveData.postDelayed(new Runnable() {
                File myExternalFile = new File(Environment.getExternalStorageDirectory() + "/" + folderName, fileName);

                @Override
                public void run() {
                    try {

                        // Create folders if they don't exist
                        myExternalFile.getParentFile().mkdirs();

                        boolean fileExists = myExternalFile.exists();

                        // Add column headers if the file doesn't exist or is empty
                        if (!fileExists || myExternalFile.length() == 0) {
                            String headers = "Count,Timestamp,Speed,RPM,Fuel,Battery,LF,RF,LB,RB,Longitude,Latitude\n";
                            FileOutputStream headerFos = new FileOutputStream(myExternalFile, false); // Overwrite mode
                            headerFos.write(headers.getBytes());
                            headerFos.close();
                        }

                        String data = count + "," + System.currentTimeMillis() + "," + MainActivity.speedInt
                                + "," + MainActivity.RPM + "," + MainActivity.fuel + "," + MainActivity.battery +
                                "," + MainActivity.LF + "," + MainActivity.RF + "," + MainActivity.LB + "," +
                                MainActivity.RB + "," + MainActivity.longitude + "," + MainActivity.latitude + "\n";

                        FileOutputStream fos = new FileOutputStream(myExternalFile, true); // Append mode
                        fos.write(data.getBytes());
                        fos.close();
                        //Log.d("File", "Data appended to file");

                        count++; // Increment count after appending data

                        // Check if storing value is 0, and upload the file if it is
                        if (datalog == 0 && sendFile) {
                            sendFile = false; // Prevent multiple uploads
                            //Log.d("File", "Uploading file to Firebase Storage");
                            uploadFileToFirebaseStorage(myExternalFile);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Reschedule the task to run again after 100 milliseconds
                    saveData.postDelayed(this, 100);
                }
            }, 10); // Initial delay before the first execution (10 milliseconds)
        }
    }


    private void uploadFileToFirebaseStorage(File file) {
        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        //Log.d("Firebase", "Storage reference created");

        // Create a reference to 'Data log/fileName'
        StorageReference dataLogRef = storageRef.child("Data log/" + file.getName());

        // Upload file to Firebase Storage
        dataLogRef.putFile(Uri.fromFile(file))
                .addOnSuccessListener(taskSnapshot -> {
                    // File uploaded successfully
                    //Log.d("Firebase", "File uploaded: " + dataLogRef.getPath());
                })
                .addOnFailureListener(e -> {
                    // Handle unsuccessful uploads
                    //Log.e("Firebase", "Upload failed", e);
                });
    }







    public void start() {       // call this with the Handling function in anotehr class to start repeating
        handler.post(runnable);

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
