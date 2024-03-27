package com.example.dash3;

import android.util.Log;

public class UsbSort {


    public void processResponse(byte[] response, int len) {
        String data = new String(response, 0, len);
        Log.d("UsbSort", "Received data: " + data);
        Log.d("Fuel", String.valueOf(MainActivity.fuel));
        long between;

        int oldLap = 0;
        int newLap;

        int oldmute = 0;

        int oldpanic = 0;
        int newpanic;

        // Split the data into an array of strings using the comma as a delimiter
        String[] values = data.split(",");        //split based on the comma

        for (String value : values) {
            // Trim any leading or trailing whitespaces
            value = value.trim();

            // Assuming the first element is the identifier (e.g., "RPM:")
            // and the second element is the value
            String[] parts = value.split(":");          //split based on the colon

            if (parts.length == 2) {
                String identifier = parts[0].trim();
                String valueStr = parts[1].trim();

                if (identifier.equals("RPM")) {             //looks for 0-3800
                    MainActivity.RPM = Integer.parseInt(valueStr);
                } else if (identifier.equals("Speed")) {        //looks for 0-40
                    MainActivity.speedInt = Integer.parseInt(valueStr);
                } else if (identifier.equals("Battery")) {      //looks for 0-100
                    MainActivity.battery = Integer.parseInt(valueStr);
                } else if (identifier.equals("Fuel")) {         //looks for 0-100
                    MainActivity.fuel = Integer.parseInt(valueStr);
                } else if (identifier.equals("LF")) {           //looks for 0-100
                    MainActivity.LF = Integer.parseInt(valueStr);
                } else if (identifier.equals("RF")) {           //looks for 0-100
                    MainActivity.RF = Integer.parseInt(valueStr);
                } else if (identifier.equals("LB")) {           //looks for 0-100
                    MainActivity.LB = Integer.parseInt(valueStr);
                } else if (identifier.equals("RB")) {           //looks for 0-100
                    MainActivity.RB = Integer.parseInt(valueStr);
                }else if (identifier.equals("panic")) {         //looks for 0-1
                    MainActivity.panic = Integer.parseInt(valueStr);
                    if(MainActivity.firstPanic) {
                        MainActivity.oldPanic = MainActivity.panic;
                        MainActivity.firstPanic = false;
                    }

                } else if (identifier.equals("mute")) {         //looks for 0-1
                    MainActivity.mute = Integer.parseInt(valueStr);
                    if(MainActivity.mute != oldmute){
                        oldmute = MainActivity.mute;
                        MainActivity.muteStateChange = true;
                    }
                }else if (identifier.equals("lapTimer")) {                     //needs to send a value of 1 when the button is pressed on the wheel to reset lap timer
                    MainActivity.laptimeReset = Integer.parseInt(valueStr);
                    if(MainActivity.firstRound){
                        MainActivity.oldLaptimeReset = MainActivity.laptimeReset;
                        MainActivity.firstRound = false;
                    }
                }else if(identifier.equals("ECVTBattery")) {       //looks for 0-100
                    MainActivity.ECVTBat = Integer.parseInt(valueStr);
                }
            }
        }
    }
}