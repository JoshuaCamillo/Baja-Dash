package com.example.dash3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;
public class USBRead {
    private Context context;
    private volatile boolean isReading = false;
    public USBRead(Context context) {
        this.context = context;
    }
    String toESP = "";

    public void startCommunication() {
        isReading = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                UsbSerialPort port = null;
                UsbDeviceConnection connection = null;

                // Find all available drivers from attached devices.
                UsbSort usbsort = new UsbSort();
                UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
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
                    port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);       //set baud to 9600 for arduino, 115200 for esp32
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                final int MAX_READ_ATTEMPTS = 10;
                final int READ_WAIT_MILLIS = 1000; // Define your read timeout

                StringBuilder partialData = new StringBuilder();

                // Continuously read data
                while (isReading) { // You need to set and manage the "isReading" boolean flag
                    byte[] response = new byte[1024]; // Create a buffer to read the response                   chack if this is issue for esp32
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
    }

    public void stopCommunication() {
        isReading = false;
    }


}
