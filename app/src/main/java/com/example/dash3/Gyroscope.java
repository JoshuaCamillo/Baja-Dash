package com.example.dash3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class Gyroscope implements SensorEventListener {
    private float Xa, Ya, Za;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Update global variables
        MainActivity.Xa = x;
        MainActivity.Ya = y;
        MainActivity.Za = z;

    }

    public float getXa() {
        return Xa;
    }

    public float getYa() {
        return Ya;
    }

    public float getZa() {
        return Za;
    }
}

