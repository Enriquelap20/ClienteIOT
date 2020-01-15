package com.example.clienteiot;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

public class Orientation implements SensorEventListener {

    private static final int SENSOR_DELAY_MICROS = 2 * 1000000; // 2s
    private final WindowManager mWindowManager;
    private final SensorManager mSensorManager;
    private int mLastAccuracy;
    private Listener mListener;
    public static String CLASS_TAG=Orientation.class.getSimpleName();
    private final Sensor mRotationSensor;

    public interface Listener {
        void onOrientationChanged(float roll);
    }

    public Orientation(Activity activity) {
        mWindowManager = activity.getWindow().getWindowManager();

        mSensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        mRotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
    }

    public void startListening(Listener listener) {
        if (mListener == listener) {
            return;
        }
        mListener = listener;
        if (mRotationSensor == null) {
            Log.e(CLASS_TAG,"Sensor de rotación no disponible.");
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

    @Override
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

    @SuppressWarnings("SuspiciousNameCombination")
    private void updateOrientation(float[] rotationVector) {
        //Se crea una matriz de rotación a partr de los resultados del sensor (rotationVector)
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        // Ajusta la matriz de rotación de acuerdo a la orientación del telefono
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
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX, worldAxisForDeviceAxisY, adjustedRotationMatrix);

        // Transforma la matriz de rotacion azimuth/pitch/roll (Z/X/Y)
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        // Convierte radianes a grados 180/pi~57
        float roll = orientation[2] * -57;

        mListener.onOrientationChanged(roll);
    }

}
