package com.example.dash3;



import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDeviceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CompoundButton;
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

import java.io.IOException;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private LocationManager locationManager;
    private UsbSerialInterface usbSerialInterface; // Instantiating the UsbSerialInterface class
    private SensorManager sensorManager;

    private Orientation orientation;
    private Sensor gyroSensor;
    private TextView textX, textY, textZ;
    public static double latitude;
    public static double longitude;
    public static int RPM;
    public static String speedText ="";
    public static int speedInt;
    public static double fuel;
    public static float Xa, Ya, Za = 0.0f;
    public static float Xpos, Ypos, Zpos = 0.0f;
    private RPMGaugeView rpmGauge;
    private boolean speedSelect = false;

    public static volatile String data;

    private final SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Update global variables
            Xa = x;
            Ya = y;
            Za = z;
        }
    };
    protected void onResume() {
        super.onResume();
        // Register the gyroscope sensor listener
        sensorManager.registerListener(gyroListener, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the gyroscope sensor listener to conserve battery
        sensorManager.unregisterListener(gyroListener);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Test");

        usbSerialInterface = new UsbSerialInterface();

        Orientation orientation = new Orientation(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        ToggleButton Power = (ToggleButton) findViewById(R.id.Power);
        TextView testText = (TextView)findViewById(R.id.testText);
        Handler handler = new Handler();
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (speedSelect == false){
                    testText.setText(speedText);//continuously update the speed value on screen
                }

                Log.d("Speed", speedText);
                Log.d("Lat", String.valueOf(latitude));
                Log.d("Long", String.valueOf(longitude));
                Log.d("GyroX", String.valueOf(Xa));
                Log.d("GyroY", String.valueOf(Ya));
                Log.d("GyroZ", String.valueOf(Za));
                Log.d("Data", String.valueOf(data));

                handler.postDelayed(this, 50); // Delay for 50 milliseconds
            }
        };
        Power.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {         //check if power button is pressed
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    handler.post(updateRunnable);           //tells handler to continuously update speed on screen
                    orientation.startListening(new Orientation.OrientationListener(){

                        public void onOrientationChanged(float pitch, float roll) {
                            // Handle the continuous orientation updates here
                            Log.d("Orientation", "Pitch: " + pitch + ", Roll: " + roll);
                            // You can update your global variables here if needed
                        }
                    });
                    handler.post(updateRunnable);

                }else{
                    orientation.stopListening();
                    handler.removeCallbacks(updateRunnable);        //tells handler to stop updating speed on screen
                }
            }
        });

        Switch mySwitch = findViewById(R.id.TestSwitch);                                    //just a switch to test the firebase, will be removed
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Your code to handle the switch change goes here
                if (isChecked) {
                    // Switch is ON
                    myRef.setValue("on");
                } else {
                    // Switch is OFF
                    myRef.setValue("off");
                }
            }
        });


        Handling handling = new Handling(125);//create instance of handling class and set sending delay to 50 miliseconds
        ToggleButton toggle = findViewById(R.id.Send);                              //button to select if data will be sent to firebase
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    handling.start();           //start sending in the set delay
                }else{
                    handling.stop();            //stop sending data
                }
            }
        });

        TextView la = (TextView)findViewById(R.id.lat);
        TextView lo = (TextView)findViewById(R.id.longe);

        ToggleButton toggle2 = findViewById(R.id.RPM);
        rpmGauge = findViewById(R.id.rpmGauge);
        toggle2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    // Start the thread to continuously update RPM gauge
                    updateRPMThread.start();
                    speedSelect = true;

                } else {
                    // Stop the thread when the toggle button is unchecked
                    updateRPMThread.interrupt();
                    speedSelect = false;
                }
            }
        });
        // Thread to continuously update the RPM gauge

        ToggleButton toggle3 = findViewById(R.id.USB);
        toggle3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {

                    isReading = true; // Start the continuous reading
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UsbSerialPort port = null;
                            UsbDeviceConnection connection = null;

                            // Find all available drivers from attached devices.
                            UsbSort usbsort = new UsbSort();
                            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
                            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
                            if (availableDrivers.isEmpty()) {
                                return;
                            }

                            // Open a connection to the first available driver.
                            UsbSerialDriver driver = availableDrivers.get(0);
                            connection = manager.openDevice(driver.getDevice());
                            if (connection == null) {
                                // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                                return;
                            }

                            port = driver.getPorts().get(0);
                            try {
                                port.open(connection);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            final int MAX_READ_ATTEMPTS = 10;
                            final int READ_WAIT_MILLIS = 1000; // Define your read timeout

                            StringBuilder partialData = new StringBuilder();

                            // Continuously read data
                            while (isReading) { // You need to set and manage the "isReading" boolean flag
                                byte[] response = new byte[1024]; // Create a buffer to read the response
                                int bytesRead = 0;
                                int readAttempts = 0;

                                // Repeat the read attempts until data is received or the maximum attempts are reached
                                while (bytesRead == 0 && readAttempts < MAX_READ_ATTEMPTS) {
                                    try {
                                        bytesRead = port.read(response, READ_WAIT_MILLIS);
                                    } catch (IOException e) {
                                        e.printStackTrace(); // Handle or log the exception
                                    }
                                    readAttempts++;
                                }

                                // Convert the read bytes into a String
                                synchronized (MainActivity.class) {
                                    String receivedData = new String(response, 0, bytesRead);
                                    Log.d("syncdata", "Received data: " + receivedData);

                                    // Handle split data
                                    String fullData = partialData + receivedData;
                                    String[] dataParts = fullData.split(",");

                                    if (dataParts.length > 1) {
                                        partialData = new StringBuilder(dataParts[dataParts.length - 1]);
                                        for (int i = 0; i < dataParts.length - 1; i++) {
                                            byte[] processedData = dataParts[i].getBytes();  // Convert each part back to byte[]
                                            int len = processedData.length;
                                            usbsort.processResponse(processedData, len);
                                        }
                                    } else {
                                        partialData = new StringBuilder(fullData);
                                    }
                                }

                                // Add a delay before the next read attempt if needed
                                try {
                                    Thread.sleep(10); // Adjust the sleep duration as needed
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Close USB communication
                            if (port != null) {
                                try {
                                    port.close(); // Close the port
                                } catch (IOException e) {
                                    e.printStackTrace(); // Handle or log the exception
                                }
                            }
                            if (connection != null) {
                                connection.close(); // Close the connection
                            }
                        }
                    }).start();
                } else {
                    // Handle the case when the toggle button is unchecked
                    isReading = false; // Stop the continuous reading
                }
            }

            // Declare a boolean flag to control the continuous reading
            private volatile boolean isReading = false;

        });
        ToggleButton toggle4 = findViewById(R.id.Fuel);
        toggle4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //will need to have call to class for the fuel sending method woth parameter yes
                }else{
                    //send Fuel method will need to have parameter no
                }
            }
        });



        // Check and request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        // Initialize location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 0, locationListener);
        }
    }

    private final LocationListener locationListener = new LocationListener() {

        @Override

        public void onLocationChanged(@NonNull Location location) {
            // Get the speed from the location object in meters per second
            float speedMps = location.getSpeed();
            MainActivity.latitude = location.getLatitude();
            MainActivity.longitude = location.getLongitude();
            // Convert the speed to miles per hour
            float speedMph = speedMps * 2.23694f;
            speedInt = Math.round(speedMph);            // assign the speed int value to the speed to send to firebase

            Log.d("SpeedDebug", "Speed in MPH: " + speedMph);
            // Format and display the speed in mph
            if (speedMph == 0.0f) {
                speedText = "0 MPH";

            } else {
                // Format and display the speed in mph on phone screen
                speedText = String.format("%.0f MPH", speedMph);

            }
        }
        public String updateLoc(boolean bol){
            return speedText;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Toast.makeText(MainActivity.this, "GPS is disabled.", Toast.LENGTH_SHORT).show();

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start listening for location updates
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            100000, 0, locationListener);
                }
            } else {
                Toast.makeText(this, "Location permission denied. Speedometer will not work.",
                        Toast.LENGTH_SHORT).show();
            }
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

                            TextView testText = findViewById(R.id.testText);
                            speedText = String.format("%d MPH", MainActivity.speedInt);
                            testText.setText(speedText); // Continuously update the speed value on screen
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
}
