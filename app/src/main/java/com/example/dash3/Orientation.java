package com.example.dash3;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.annotation.Nullable;
import android.view.Surface;
import android.view.WindowManager;
public class Orientation implements SensorEventListener {


    public interface OrientationListener {
        void onOrientationChanged(float pitch, float roll);
    }

    private static final int SENSOR_DELAY_MICROS = 50 * 1000; // 50ms

    private final WindowManager mWindowManager;
    private final SensorManager mSensorManager;
    @Nullable
    private final Sensor mRotationSensor;

    private int mLastAccuracy;
    private OrientationListener mListener;

    public Orientation(Activity activity) {
        mWindowManager = activity.getWindow().getWindowManager();
        mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);

        // Can be null if the sensor hardware is not available
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void startListening(OrientationListener listener) {
        if (mListener == listener) {
            return;
        }
        mListener = listener;
        if (mRotationSensor == null) {
            // Handle the case where the rotation vector sensor is not available
            return;
        }
        mSensorManager.registerListener(this, mRotationSensor, SENSOR_DELAY_MICROS);
    }

    public void stopListening() {
        mSensorManager.unregisterListener(this);
        mListener = null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (mLastAccuracy != accuracy) {
            mLastAccuracy = accuracy;
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if (mListener == null) {
            return;
        }
        if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }
        if (event.sensor == mRotationSensor) {
            updateOrientation(event.values);
        }
    }

    private void updateOrientation(float[] rotationVector) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        switch (mWindowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                break;
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
        }



    float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
    worldAxisForDeviceAxisY, adjustedRotationMatrix);

    float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

    float pitch = orientation[1] * -57; // Convert radians to degrees
    float roll = orientation[2] * -57;

    MainActivity.Xpos = orientation[0]*-57;
    MainActivity.Ypos = pitch;
    MainActivity.Zpos = roll;

        mListener.onOrientationChanged(pitch, roll);
    }
}
