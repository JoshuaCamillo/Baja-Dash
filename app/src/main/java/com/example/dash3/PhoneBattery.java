package com.example.dash3;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class PhoneBattery implements Runnable {
    private Context context;
    private boolean isRunning;

    public PhoneBattery(Context context) {
        this.context = context;
        this.isRunning = false;
    }

    public void start() {
        isRunning = true;
        new Thread(this).start();
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        while (isRunning) {
            updateBatteryPercentage();
            try {
                Thread.sleep(1000); // Update battery percentage every second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateBatteryPercentage() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            MainActivity.phoneBat = Math.round((level / (float) scale) * 100);
        }
    }
}
