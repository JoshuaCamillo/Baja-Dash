package com.example.dash3;

import android.util.Log;

public class UsbSort {


    public void processResponse(byte[] response, int len) {
        String data = new String(response, 0, len);
        Log.d("UsbSort", "Received data: " + data);

        // Split the data into an array of strings using the comma as a delimiter
        String[] values = data.split(",");

        for (String value : values) {
            // Trim any leading or trailing whitespaces
            value = value.trim();

            // Assuming the first element is the identifier (e.g., "RPM:")
            // and the second element is the value
            String[] parts = value.split(":");

            if (parts.length == 2) {
                String identifier = parts[0].trim();
                String valueStr = parts[1].trim();

                if (identifier.equals("RPM")) {
                    MainActivity.RPM = Integer.parseInt(valueStr);
                } else if (identifier.equals("Speed")) {
                    MainActivity.speedInt = Integer.parseInt(valueStr);
                }
            }
        }
    }
}