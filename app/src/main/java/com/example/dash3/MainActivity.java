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
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
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

// main class where everything is called and initialized
public class MainActivity extends AppCompatActivity{

    private  Gyroscope gyroListener;                    //start putting class calls here
    private PhoneBattery batteryUpdater;                //create instance of phonebattery class
    private Speedometer speedUpdater;                   //create instance of speedometer class
    private USBRead usbRead;                            //create instance of usbread class
    private SpeedoUpdate speedoUpdate;
    private ModeSelect enduroSelect;                      //instance of mode select to choose enduro mode for timers

    public static final long SCREEN_DISPLAY_DURATION = 120000;      //change to durarion of message to driver on screen
    public static int LB, RB, LF, RF;                   //create variables for lpots

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationManager locationManager;
    private UsbSerialInterface usbSerialInterface; // Instantiating the UsbSerialInterface class
    private SensorManager sensorManager;
    public static boolean isSRButtonPressed = false;
    public static boolean isKphSelected = false;
    public static boolean enduro = false;
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
    public static int oldPanic;
    public static boolean firstRound = true;
    public static long panicStart;
    public static int panicking;
    public static long readPanic;
    public static long betweenPanic;
    public static long debugStart;
    public static long debugEnd;
    public static long debugBetween;
    private static final int SMS_PERMISSION_REQUEST_CODE = 1;
    public static int mute = 0;
    public boolean muteFlag = false;
    public static int phoneBat;      //battery percentage
    private int sleepTime = 50;
    public static int raceTime;
    public static long refTime;
    public static long elapsedTime;

    public static long decreasingTime;
    public static int started = 0;              //flag to start timer from firebase
    public static boolean completed = false;
    public static int laptimeReset;
    public static int oldLaptimeReset;

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

        TextView unitsText = findViewById(R.id.Units);
        TextView lastLap = findViewById(R.id.lastLap);
        TextView diffLap = findViewById(R.id.lapDifference);
        ProgressBar fuelBar = findViewById(R.id.FuelGuage);
        ProgressBar battBar = findViewById(R.id.BatteryGuage);

        enduroSelect = new ModeSelect(unitsText, lastLap, diffLap);            //initialize class to select enduro mode

        ToggleButton modeSelect = findViewById(R.id.ModeSelect);                        //buton to select enduro race mode
        modeSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    enduro = true;
                    Log.d("Mainenduro", String.valueOf(enduro));
                    refTime = System.currentTimeMillis();
                    ModeSelect.enduro = enduro;
                                                                                        //make this button change the display mode from rpm, to enduro timer and lap timer
                }else{
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

        Handling handling = new Handling(125);// starts getting phone battery and sending to furebase
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
                    Log.d("DataButton", "Data Button is checked");
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
                                            if(mute == 1){
                                                if (!muteFlag){     //flag to make sure it doesnt repeat the call to unmute the microphone
                                                    AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);     // mute the microphone
                                                    audioManager.setMicrophoneMute(false);
                                                    muteFlag = true;
                                                }
                                                messageLayout.setVisibility(View.VISIBLE);          //show the message for being unmuted
                                                messageLayout.setText("Unmuted");
                                            } else {
                                                if (!audioManager.isMicrophoneMute()) {     //checks to make sure its not repeating this call for mute
                                                    audioManager.setMicrophoneMute(true);       //mute the microphone
                                                }
                                                muteFlag = false;
                                                messageLayout.setVisibility(View.INVISIBLE);        //hide the message
                                            }
                                            if(panic != oldPanic){                 //runs when panic value changes coming from esp32
                                                oldPanic = panic;
                                                checkAndSendSMS();               //calls function to send sms message using SMSsender
                                                panicking = 1;
                                                panicStart = System.currentTimeMillis();     //start time for panic button being sent to website

                                                // Show "Sent" message
                                                messageLayout.setVisibility(View.VISIBLE);
                                                messageLayout.setText("Sent");

                                                // Set a delayed runnable to hide the layout after 5 seconds
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        messageLayout.setVisibility(View.INVISIBLE);
                                                    }
                                                }, 5000); // 5000 milliseconds = 5 seconds
                                            }
                                            if(enduro) {                        //changing number in bar for timer in SpeedoUpdate
                                                elapsedTime = System.currentTimeMillis() - refTime;

                                                if(started ==1){                    //when the race timer is updated, starts the value for decreasing , and reset the flags tfor interacting with firebase
                                                    decreasingTime = raceTime;
                                                    completed = true;
                                                    Log.d("dec", String.valueOf(decreasingTime));;
                                                    refTime = System.currentTimeMillis();
                                                }
                                                else if ((raceTime-elapsedTime)>0){     // if the timer is greater than 0, decrease it by the amount of the delay
                                                    decreasingTime = raceTime - elapsedTime;
                                                    rpmGauge.setCurrentValue((int)(decreasingTime));     //display the timer on the bar
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
                    orientation.startListening(new Orientation.OrientationListener(){

                        public void onOrientationChanged(float pitch, float roll) {
                            // Handle the continuous orientation updates here
                            Log.d("Orientation", "Pitch: " + pitch + ", Roll: " + roll);
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
                isSRButtonPressed = b;
            }
        });

        /*Button sendMessage = findViewById(R.id.Message);
                                                                        // Check if SMS permission is not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
             Request SMS permission
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
                 */                 //remove this after testing button works with sending sos

        ToggleButton muteButton = findViewById(R.id.micMutetoggle);
        muteButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mute = 1;
                    messageLayout.setVisibility(View.VISIBLE);
                    messageLayout.setText("Unmuted");
                } else {
                    mute = 0;
                    messageLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        Button timerRest = findViewById(R.id.testTimer);
        timerRest.setOnClickListener(new View.OnClickListener() {                //testing timer in enduro
            @Override
            public void onClick(View view) {
                // Send the SMS message directly
                laptimeReset = 1;                               //resets the lap timer for enduro
                                                //remove when buttons are made, functionality should be taken care of if button sends 1 to lapTimer to phone
            }
        });
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

                            Log.d("muted", String.valueOf(mute));
                            if(enduro) {                        //changing number in bar for timer in SpeedoUpdate
                                elapsedTime = System.currentTimeMillis() - refTime;

                                if(started ==1){                    //when the race timer is updated, starts the value for decreasing , and reset the flags tfor interacting with firebase
                                    decreasingTime = raceTime;
                                    completed = true;
                                    Log.d("dec", String.valueOf(decreasingTime));;
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

