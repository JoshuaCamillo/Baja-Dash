package com.example.dash3;



import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDeviceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.ToggleButton;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private  Gyroscope gyroListener;                    //start putting class calls here
    private PhoneBattery batteryUpdater;                //create instance of phonebattery class
    private Speedometer speedUpdater;                   //create instance of speedometer class
    private USBRead usbRead;                            //create instance of usbread class
    private SpeedoUpdate speedoUpdate;

    public static final long SCREEN_DISPLAY_DURATION = 120000;      //change to durarion of message to driver on screen
    public static int LB, RB, LF, RF;                   //create variables for lpots

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationManager locationManager;
    private UsbSerialInterface usbSerialInterface; // Instantiating the UsbSerialInterface class
    private SensorManager sensorManager;
    public static boolean isSRButtonPressed = false;
    public static boolean isKphSelected = false;

    private static TextView messageLayout;
    private Sensor gyroSensor;

    public static double latitude, longitude;           //create variables for latitude and longitude

    public static int RPM = 0;
    public static String speedText ="";
    public static int speedInt = 0;
    public static int speedKPH = 0;
    public static int fuel;
    public static int battery;
    public static float Xa, Ya, Za = 0.0f;
    public static float Xpos, Ypos, Zpos = 0.0f;
    private RPMGaugeView rpmGauge;
    public static boolean speedSelect = false;
    public static String message = "Starting";
    public static volatile String data;
    public static int panic;
    public static int panicking;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    public static int mute = 0;
    public static int phoneBat;      //battery percentage

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
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);     //set the layout to activity_main
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);       //keeps the screen from turning off

        usbSerialInterface = new UsbSerialInterface();                      //create instance of usbserialinterface class
        messageLayout = findViewById(R.id.messageLayout);                   //sets the layout for displaying messages to driver

        Orientation orientation = new Orientation(this);            // initialize the orientation class for pitch, roll, yaw values
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);     //initialize gyro sensor for gyroscopic accelerations
        gyroListener = new Gyroscope();

        batteryUpdater = new PhoneBattery(this);                        //initialize class to get phone battery
        speedUpdater = new Speedometer(this);                           //initialize class to get speed and location

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); // Get the audio manager for mic control
        audioManager.setMicrophoneMute(true); // Mute the microphone by default

        usbRead = new USBRead(this);                    //initialize class to read from usb
        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);      //filter for usb device attached
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        TextView testText = (TextView)findViewById(R.id.speedText);
        TextView alternateText = (TextView)findViewById(R.id.alternateText);
        speedoUpdate = new SpeedoUpdate(testText, alternateText);


        ToggleButton changeUnitsButton = findViewById(R.id.changeUnits);
        changeUnitsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isKphSelected = b;
                updateUnitsText();
            }
        });

        ToggleButton Power = (ToggleButton) findViewById(R.id.Power);                   //remove this button


        Handling handling = new Handling(125);//create instance of handling class and set sending delay to 50 miliseconds
        ToggleButton toggle = findViewById(R.id.Send);                              //button to select if data will be sent to firebase
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    handling.start();           //start sending in the set delay
                    batteryUpdater.start();

                }else{
                    handling.stop();            //stop sending data
                    batteryUpdater.stop();
                }
            }
        });

        ToggleButton toggle2 = findViewById(R.id.DataButton);
        rpmGauge = findViewById(R.id.rpmGauge);
        toggle2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!updateRPMThread.isAlive()) {
                        // Start the thread to continuously update RPM gauge
                        updateRPMThread.start();
                    }
                    // Start the thread to continuously update RPM gauge
                    speedSelect = true;
                    speedUpdater.run();
                    usbRead.startCommunication();
                    speedoUpdate.startUpdates();
                    orientation.startListening(new Orientation.OrientationListener(){

                        public void onOrientationChanged(float pitch, float roll) {
                            // Handle the continuous orientation updates here
                            Log.d("Orientation", "Pitch: " + pitch + ", Roll: " + roll);
                            // You can update your global variables here if needed
                        }
                    });

                } else {
                    // Stop the thread when the toggle button is unchecked
                    if (updateRPMThread.isAlive()) {
                        updateRPMThread.interrupt();
                    }                                              //still needs to have functionality put in class
                    speedSelect = false;
                    speedUpdater.stop();                    //stop getting gps speed and location
                    usbRead.stopCommunication();            //stop reading from usb
                    orientation.stopListening();            //stop getting orientation
                    speedoUpdate.stopUpdates();
                }
            }
        });
        // Thread to continuously update the RPM gauge

        ToggleButton speedRPM = findViewById(R.id.SR);
        TextView Units = (TextView)findViewById(R.id.Units);
        speedRPM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isSRButtonPressed = b;
                updateUnitsText();
            }
        });

        Button sendMessage = findViewById(R.id.Message);

                                                                                        // Check if SMS permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        } else {
            sendMessage.setOnClickListener(new View.OnClickListener() {                 //sending sms message when button is pressed, will be changed to when panic is true
                @Override
                public void onClick(View view) {
                    // Send the SMS message directly
                    SMSSender.sendSMS();                               //call send sms class to send message
                }
            });
        }

        ToggleButton muteButton = findViewById(R.id.micMutetoggle);
        muteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (b) {
                    audioManager.setMicrophoneMute(false); // UnMute the microphone
                    mute = 1;
                } else {
                    audioManager.setMicrophoneMute(true); // mute the microphone
                    mute = 0;
                }
            }
        });


    }
    private void updateUnitsText() {
        TextView unitsText = findViewById(R.id.Units);              //set the Unit value to MPH or KPH
        if (isSRButtonPressed) {
            unitsText.setText("RPM");
        } else if (isKphSelected) {
            unitsText.setText("KPH");
        } else {
            unitsText.setText("MPH");
        }
    }

    // Thread to continuously update the RPM gauge and speed text
    private Thread updateRPMThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Update the RPM gauge and speed text with the current RPM value
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rpmGauge.setCurrentRPM(RPM);
                            // Continuously update the speed value on screen

                            ProgressBar fuelBar = findViewById(R.id.FuelGuage);
                            fuelBar.setProgress(fuel);
                            ProgressBar battBar = findViewById(R.id.BatteryGuage);
                            battBar.setProgress(battery);
                        }
                    });

                    // Adjust the delay time as needed
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                // Handle the interruption, if needed
            }
        }
    });

    public static void updateMessage(String newMessage) {
        Log.d("UpdateMessage", "New Message: " + newMessage);

        if (!newMessage.equals(message)) {
            messageLayout.setVisibility(View.VISIBLE);
            messageLayout.setText(newMessage);

            // Set a delayed runnable to hide the layout after a specific duration (e.g., 5000 milliseconds)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    messageLayout.setVisibility(View.INVISIBLE);
                }
            }, SCREEN_DISPLAY_DURATION); // Use the constant SCREEN_DISPLAY_DURATION or adjust the duration as needed

            message = newMessage;
            Log.d("Updated Message", newMessage);
        }
    }

}
