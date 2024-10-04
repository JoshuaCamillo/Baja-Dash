package com.example.dash3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.ToggleButton;
import android.hardware.usb.UsbManager;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// main class where everything is called and initialized
public class MainActivity extends AppCompatActivity{

    private  Gyroscope gyroListener;                    //start putting class calls here
    private PhoneBattery batteryUpdater;                //create instance of phonebattery class
    private Speedometer speedUpdater;                   //create instance of speedometer class
    private USBRead usbRead;                            //create instance of usbread class
    private SpeedoUpdate speedoUpdate;                  //create instance of speedoupdate class
    private ModeSelect enduroSelect;                      //instance of mode select to choose enduro mode for timers
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    public static int LB, RB, LF, RF;                   //create variables for lpots
    private UsbSerialInterface usbSerialInterface; // Instantiating the UsbSerialInterface class
    private SensorManager sensorManager;                //create instance of sensormanager
    public static boolean speedRPMSelect = false;        //flag for when speed is on screen or RPM
    public static boolean isKphSelected = false;        //flag for when speed is in KPH or MPH
    public static boolean enduro = false;           //flag for when enduro mode is selected
    private static TextView messageLayout;        //create instance of textview for messages to driver
    public static TextView alternateText;        //create instance of textview for alternate text
    private Sensor gyroSensor;
    public static double latitude, longitude;           //create variables for latitude and longitude
    public static boolean startup = true;
    public static boolean onlyOnce = true;          //makes sure it only happens once
    private static TextView intro;

    public static int RPM = 0;
    public static String speedText ="";
    public static int speedInt = 0;         //integer value of speed in MPH
    public static int speedKPH = 0;         //conversion to KPH for screen display
    public static int fuel;         //fuel percentage
    public static int battery;          //dewalt battery percentage on car
    public static int ECVTBat;          //battery percentage of ECVT battery
    public static float Xa, Ya, Za = 0.0f;
    public static float Xpos, Ypos, Zpos = 0.0f;
    private RPMGaugeView rpmGauge;
    public static boolean speedSelect = false;
    public static String message = "Starting";
    public static boolean messageFlag = false;
    public static volatile String data;
    public static int panic;
    public static int oldPanic;
    public static boolean panicFlag = false;
    public static boolean firstRound = true;
    public static long panicStart;
    public static int panicking;
    public static boolean firstPanic = true;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    public static int mute = 0;
    public static boolean muteStateChange = false;
    public boolean muteFlag = false;
    public static int phoneBat;      //battery percentage
    public static double phoneTemp;      //battery temperature
    private int sleepTime = 50;
    public static int raceTime;
    public static long refTime;
    public static long elapsedTime;

    public static long decreasingTime;
    public static int started = 0;              //flag to start timer from firebase
    public static boolean completed = false;
    public static int laptimeReset;
    public static int oldLaptimeReset;
    public static int launch = 0;
    public static String carNum;
    public static String scrapedLastLap;
    public static String scrapedBestLap;
    public static int scrapedDiff;
    public static float chargeAmps;

    protected void onResume() {
        super.onResume();                             //when app is open, the gyroscopic accelerations are measured
        // Register the gyroscope sensor listener
        if (gyroSensor != null) {
            sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the gyroscope sensor listener to conserve battery
        sensorManager.unregisterListener(gyroListener);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setMicrophoneMute(false); // Unmute the microphone
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);     //set the layout to activity_main
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Check if the app has permission to write to external storage
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE);
        }//keeps the screen from turning off

        ToggleButton toggle2 = findViewById(R.id.DataButton);
        ToggleButton sendButton = findViewById(R.id.Send);                              //button to select if data will be sent to firebase

        UsbSerialInterface usbSerialInterface = new UsbSerialInterface(toggle2, sendButton);
        //create instance of usbserialinterface class
        messageLayout = findViewById(R.id.messageLayout);                   //sets the layout for displaying messages to driver

        Orientation orientation = new Orientation(this);            // initialize the orientation class for pitch, roll, yaw values
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);     //initialize gyro sensor for gyroscopic accelerations
        gyroListener = new Gyroscope();

        batteryUpdater = new PhoneBattery(this);                        //initialize class to get phone battery
        speedUpdater = new Speedometer(this);                           //initialize class to get speed and location

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); // Get the audio manager for mic control
        audioManager.setMicrophoneMute(false); // Mute the microphone by default           trying setting this when data is turned on so when we dont have the button from wheel, mic still works

        usbRead = new USBRead(this);                    //initialize class to read from usb
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);      //filter for usb device attached
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        TextView testText = (TextView) findViewById(R.id.speedText);
        alternateText = (TextView) findViewById(R.id.alternateText);
        TextView carNumText = (TextView) findViewById(R.id.carAhead);
        speedoUpdate = new SpeedoUpdate(testText, alternateText, carNumText);        //initialize class to update speed and RPM on screen

        TextView launchText = findViewById(R.id.launchdisp);

        TextView unitsText = findViewById(R.id.Units);
        TextView lastLap = findViewById(R.id.lastLap);
        TextView diffLap = findViewById(R.id.lapDifference);
        intro = findViewById(R.id.intro);

        enduroSelect = new ModeSelect(unitsText, lastLap, diffLap);            //initialize class to select enduro mode

        ToggleButton modeSelect = findViewById(R.id.ModeSelect);                        //buton to select enduro race mode
        modeSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    enduro = true;
                    //Log.d("Mainenduro", String.valueOf(enduro));
                    refTime = System.currentTimeMillis();
                    ModeSelect.enduro = enduro;
                    //make this button change the display mode from rpm, to enduro timer and lap timer
                } else {
                    enduro = false;
                    ModeSelect.enduro = enduro;
                }
            }
        });


        ToggleButton changeUnitsButton = findViewById(R.id.changeUnits);
        changeUnitsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isKphSelected = b;
            }
        });

        Handling handling = new Handling( this,125);// starts getting phone battery and sending to furebase
        sendButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b) {
                    handling.start();           //start sending in the set delay
                    batteryUpdater.start();

                } else {
                    handling.stop();            //stop sending data
                    batteryUpdater.stop();
                }
            }
        });
        rpmGauge = findViewById(R.id.rpmGauge);
        toggle2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    audioManager.setMicrophoneMute(false); // unMute the microphone by default
                    //Log.d("DataButton", "Data Button is checked");
                    // Create a new updateRPMThread and start it
                    updateRPMThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                while (!Thread.currentThread().isInterrupted()) {
                                    // Update the RPM gauge and speed text with the current RPM value
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(launch == 1){
                                                launchText.setVisibility(View.VISIBLE);
                                                launchText.setText("LAUNCH");
                                            }else{
                                                launchText.setVisibility(View.INVISIBLE);
                                            }

                                            if(!startup && onlyOnce) {
                                                onlyOnce = false;
                                                introAnimation();
                                            }
                                            /*      Commented out mute control due to request for always open mic
                                            if (mute == 1) {
                                                messageFlag = false;
                                                if (!muteFlag) {     //flag to make sure it doesnt repeat the call to unmute the microphone
                                                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);     // mute the microphone
                                                    audioManager.setMicrophoneMute(false);
                                                    muteFlag = true;
                                                }
                                                messageLayout.setVisibility(View.VISIBLE);          //show the message for being unmuted
                                                if (!panicFlag) {
                                                    if(!flashing){
                                                        messageLayout.setText("Unmuted");
                                                    }

                                                }
                                            } else {
                                                if (!audioManager.isMicrophoneMute()) {     //checks to make sure its not repeating this call for mute
                                                    audioManager.setMicrophoneMute(true);       //mute the microphone
                                                }
                                                muteFlag = false;
                                                if (!panicFlag) {
                                                    if(!messageFlag) {
                                                        messageLayout.setVisibility(View.INVISIBLE);        //hide the message
                                                    }
                                                }

                                            }
                                             */
                                            if (panic != oldPanic) {
                                                oldPanic = panic;
                                                checkAndSendSMS();
                                                panicking = 1;
                                                panicStart = System.currentTimeMillis();
                                                panicFlag = true;

                                                // Set a delayed runnable to hide the layout after 5 seconds
                                                new Handler().postDelayed(new Runnable() {
                                                    private boolean visible = true;

                                                    @Override
                                                    public void run() {

                                                        if (visible) {
                                                            messageLayout.setText("PANIC!!");
                                                            messageLayout.setVisibility(View.VISIBLE);
                                                        } else {
                                                            if (mute == 1) {
                                                                messageLayout.setVisibility(View.VISIBLE);
                                                                messageLayout.setText("Unmuted");
                                                            } else {
                                                                if(!messageFlag) {
                                                                    messageLayout.setVisibility(View.INVISIBLE);
                                                                }
                                                            }
                                                        }
                                                        visible = !visible;
                                                        if (System.currentTimeMillis() - panicStart < 10000) {  // Repeat until 5 seconds have passed
                                                            new Handler().postDelayed(this, 500); // Toggle every second
                                                        } else {
                                                            panicFlag = false;
                                                            messageLayout.setVisibility(View.INVISIBLE);
                                                        }
                                                    }
                                                }, 1000); // Start toggling after 1 second
                                            }

                                            if (enduro) {                        //changing number in bar for timer in SpeedoUpdate
                                                elapsedTime = System.currentTimeMillis() - refTime;

                                                if (started == 1) {                    //when the race timer is updated, starts the value for decreasing , and reset the flags tfor interacting with firebase
                                                    decreasingTime = raceTime;
                                                    completed = true;
                                                    //Log.d("dec", String.valueOf(decreasingTime));
                                                    ;
                                                    refTime = System.currentTimeMillis();
                                                } else if ((raceTime - elapsedTime) > 0) {     // if the timer is greater than 0, decrease it by the amount of the delay
                                                    decreasingTime = raceTime - elapsedTime;
                                                    rpmGauge.setCurrentValue((int) (decreasingTime));     //display the timer on the bar
                                                }
                                            } else {
                                                rpmGauge.setCurrentValue(RPM);                //if not in enduro, display rpm on bar
                                            }
                                            // Handle normal mode
                                            ProgressBar fuelBar = findViewById(R.id.FuelGuage);
                                            fuelBar.setProgress(fuel);
                                            ProgressBar battBar = findViewById(R.id.BatteryGuage);
                                            battBar.setProgress(battery);
                                        }

                                    });

                                    // Adjust the delay time as needed
                                    Thread.sleep(sleepTime);
                                }
                            } catch (InterruptedException e) {
                                // Handle the interruption, if needed
                            }
                        }
                    });
                    updateRPMThread.start();
                    // Start the thread to continuously update RPM gauge
                    speedSelect = true;
                    speedUpdater.run();
                    usbRead.startCommunication();
                    speedoUpdate.startUpdates();
                    enduroSelect.startUpdates();
                    orientation.startListening(new Orientation.OrientationListener() {

                        public void onOrientationChanged(float pitch, float roll) {
                            // Handle the continuous orientation updates here
                            //Log.d("Orientation", "Pitch: " + pitch + ", Roll: " + roll);
                            // You can update your global variables here if needed
                        }
                    });

                } else {
                    // Stop the current updateRPMThread
                    if (updateRPMThread != null && updateRPMThread.isAlive()) {
                        updateRPMThread.interrupt();
                    }                                           //still needs to have functionality put in class
                    speedSelect = false;
                    speedUpdater.stop();                    //stop getting gps speed and location
                    usbRead.stopCommunication();            //stop reading from usb
                    orientation.stopListening();            //stop getting orientation
                    speedoUpdate.stopUpdates();
                    enduroSelect.stopUpdates();
                }
            }
        });

        ToggleButton speedRPM = findViewById(R.id.SR);
        speedRPM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                speedRPMSelect = b;
            }
        });

        // Set the initial state of the toggle buttons
        toggle2.setChecked(true); // Turn on toggle2
        sendButton.setChecked(true); // Turn on sendButton

    }

    // Thread to continuously update the RPM gauge and speed text
    private Thread updateRPMThread = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //Log.d("muted", String.valueOf(mute));
                            if(enduro) {                        //changing number in bar for timer in SpeedoUpdate
                                elapsedTime = System.currentTimeMillis() - refTime;

                                if(started ==1){                    //when the race timer is updated, starts the value for decreasing , and reset the flags tfor interacting with firebase
                                    decreasingTime = raceTime;
                                    completed = true;
                                    //Log.d("dec", String.valueOf(decreasingTime));;
                                    refTime = System.currentTimeMillis();
                                }
                                else if ((raceTime-elapsedTime)>0){     // if the timer is greater than 0, decrease it by the amount of the delay
                                    decreasingTime = raceTime - elapsedTime;
                                    rpmGauge.setCurrentValue((int)(decreasingTime));     //display the timer on the bar
                                }
                            }else {
                                rpmGauge.setCurrentValue(RPM);                //if not in enduro, display rpm on bar
                            }
                            // Continuously update the speed value on screen

                            ProgressBar fuelBar = findViewById(R.id.FuelGuage);
                            fuelBar.setProgress(fuel);
                            ProgressBar battBar = findViewById(R.id.BatteryGuage);
                            battBar.setProgress(battery);
                        }
                    });

                    // Adjust the delay time as needed
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                // Handle the interruption, if needed
            }
        }
    });
    private void checkAndSendSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        } else {
            // Send the SMS message directly
            SMSSender.sendSMS(); // Call sendSMS method from SMSSender class to send message
        }
    }

    // Define the Handler as a class variable
    private static Handler messageHandler = new Handler();
    public static boolean flashing = false; // Flag to indicate if flashing is active

    public static void updateMessage(String newMessage) {
        //Log.d("UpdateMessage", "New Message: " + newMessage);

        if (!newMessage.equals(message)) {
            messageFlag = true;
            messageLayout.setVisibility(View.VISIBLE);
            messageLayout.setText(newMessage);

            // Check if mute state has changed
            if (mute == 0 && flashing) {
                // Stop the flashing
                messageHandler.removeCallbacksAndMessages(null);
                flashing = false;
                messageLayout.setVisibility(View.INVISIBLE);
            }

            // Start flashing if mute is 1
            if (mute == 1 && !flashing) {
                flashing = true;
                messageHandler.postDelayed(new Runnable() {
                    private boolean visible = true;

                    @Override
                    public void run() {
                        if (visible) {
                            messageLayout.setText(newMessage);
                        } else {
                            messageLayout.setText("Unmuted");
                        }
                        visible = !visible;
                        if (mute == 1) { // Repeat until mute changes to 0
                            messageHandler.postDelayed(this, 1000); // Toggle every 500 milliseconds
                        } else {
                            flashing = false; // Reset flashing flag
                            messageLayout.setVisibility(View.INVISIBLE);
                        }
                    }
                }, 1000); // Start toggling after 1 second
            }
        }

        message = newMessage;
        //Log.d("Updated Message", newMessage);
    }

    private static Handler introHandler = new Handler();

    public static void introAnimation() {
        intro.setVisibility(View.VISIBLE);
        alternateText.setVisibility(View.INVISIBLE);
        intro.setText(""); // Start with an empty text

        final String textToDisplay = "LETS GO RACING";
        final int delayPerLetter = 100; // Delay in milliseconds between each letter
        final int totalDuration = 5000; // Total duration in milliseconds for the animation

        introHandler.postDelayed(new Runnable() {
            private int currentIndex = 0;
            private long startTime = System.currentTimeMillis();
            private boolean animationComplete = false;

            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= totalDuration) {
                    // Animation finished, hide the text
                    if (!animationComplete) {
                        intro.setVisibility(View.GONE);
                        alternateText.setVisibility(View.VISIBLE);
                        animationComplete = true;
                    }
                    return;
                }

                // Check if intro is still visible before updating text
                if (intro.getVisibility() == View.VISIBLE && currentIndex < textToDisplay.length()) {
                    intro.setText(textToDisplay.substring(0, currentIndex + 1));
                }

                currentIndex++;
                introHandler.postDelayed(this, delayPerLetter); // Delay before showing the next letter
            }
        }, delayPerLetter); // Start with the delay for the first letter
    }
}
