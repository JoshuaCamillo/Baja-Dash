package com.example.dash3;

import android.content.Context;
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
                    port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                final int MAX_READ_ATTEMPTS = 10;
                final int READ_WAIT_MILLIS = 1000;

                StringBuilder receivedData = new StringBuilder();
                while (isReading) {
                    byte[] response = new byte[1024];
                    int bytesRead = 0;
                    int readAttempts = 0;

                    while (bytesRead == 0 && readAttempts < MAX_READ_ATTEMPTS) {
                        try {
                            bytesRead = port.read(response, READ_WAIT_MILLIS);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        readAttempts++;
                    }

                    for (int i = 0; i < bytesRead; i++) {
                        char c = (char) response[i];
                        if (c == ',') {
                            String[] words = receivedData.toString().split("\\s*,\\s*");
                            for (String word : words) {
                                usbsort.processResponse(word.getBytes(), word.length());
                                Log.d("syncdata", "Received data: " + word);
                            }
                            receivedData.setLength(0);      //this was changed to clear the stack once the data is processed
                        } else if (c != '\n') {
                            receivedData.append(c);
                        }
                    }
                }

                if (port != null) {
                    try {
                        port.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.close();
                }
            }
        }).start();
    }

    public void stopCommunication() {
        isReading = false;
    }
}
