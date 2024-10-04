package com.example.dash3;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class PhoneBattery implements Runnable {
    private Context context;
    private volatile boolean isRunning; // Use volatile for thread safety

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
            updateBatteryStatus();
            try {
                Thread.sleep(1000); // Update battery status every second
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
            }
        }
    }

    private void updateBatteryStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            MainActivity.phoneBat = Math.round((level / (float) scale) * 100);

            int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            MainActivity.phoneTemp = temperature / 10.0;

            MainActivity.chargeAmps = getBatteryCurrentAmps();
        }
    }

    private float getBatteryCurrentAmps() {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (batteryManager != null) {
            int currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
            if (currentNow != Integer.MIN_VALUE) {
                return currentNow / 1000.0f; // Convert microamperes to milliamperes
            }
        }
        return 0.0f; // If the battery manager is null or the property is not available, return 0
    }
}
