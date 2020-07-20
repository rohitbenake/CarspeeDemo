package com.cc.carspeedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.car.Car;
import android.car.CarNotConnectedException;
import android.car.hardware.CarSensorEvent;
import android.car.hardware.CarSensorManager;
import android.car.hardware.hvac.CarHvacManager;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.ServiceConnection;
import android.widget.TextView;

import com.cc.carspeedemo.listener.ISensorMsgListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Car mCarService;
    private int carSpeed = 0;
    private final String[] permissions = new String[]{Car.PERMISSION_SPEED};

    private IBinder mCarManager;
    private CarHvacManager mCarHvacManager;
    private CarSensorManager mCarSensorManager;
    public TextView carSpeedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        carSpeedTextView = findViewById(R.id.car_speed_text_view);

        String text = getString(R.string.car_speed_text,carSpeed);

        carSpeedTextView.setText(text);

        Log.i(TAG, "onCreate: calling EstablishCarServiceConnection");
        EstablishCarServiceConnection();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        startService();
    }

    private void startService() {
        if(checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "startService: Permission for"+permissions[0]+" is GRANTED");

            if(mCarService == null) {
                Log.d(TAG, "EstablishCarServiceConnection:  mCarService is NULL");
                return;
            }

            if (!mCarService.isConnected() && !mCarService.isConnecting()) {
                mCarService.connect();
            }
        }
        else
        {
            Log.d(TAG, "startService: Permission for "+permissions[0]+" is NOT GRANTED");
            requestPermissions(permissions, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissions[0].equals(Car.PERMISSION_SPEED )&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //if (permissions[0] == Car.PERMISSION_CONTROL_CAR_CLIMATE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult: Permission for"+ permissions[0]+" GRANTED");
            startService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: Activity in OnPause");
        
        if(mCarService == null) {
            Log.d(TAG, "EstablishCarServiceConnection:  mCarService is NULL");
            return;
        }
        
        if(mCarService.isConnected()) {
            Log.d(TAG, "onPause: Disconnecting Car Service");
            mCarService.disconnect();
        }
    }

    private ServiceConnection mConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected: Connected to car service");
            mCarManager = iBinder;//not needed
            onCarServiceReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected: Disconnected from car service");
        }
    };

    private void onCarServiceReady() {
        Log.d(TAG, "onCarServiceReady: entry");

        onCarSpeedChange();

    }


    //ISensorMsgListener mSenserListener = new ISensorMsgListener() {
    CarSensorManager.OnSensorChangedListener   mSenserListener = new CarSensorManager.OnSensorChangedListener(){
        @Override
        public void onSensorChanged(CarSensorEvent carSensorEvent) {
            Log.d(TAG, "onSensorChanged: carSensorEvent :"+carSensorEvent.toString());
            carSpeed = (int)carSensorEvent.floatValues[0];

            // In order to modify the UI, we have to make sure the code is running on the "UI thread.
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    String text = getString(R.string.car_speed_text,carSpeed);
                    carSpeedTextView.setText(text);

                }
            });

        }

//        public void onSensorValueChanged(CarSensorEvent carSensorEvent)
//        {
//            Log.d(TAG, "onSensorValueChanged: carSensorEvent :"+carSensorEvent.toString());
//            carSpeed = (int)carSensorEvent.floatValues[0];
//            String text = getString(R.string.car_speed_text,carSpeed);
//            carSpeedTextView.setText(text);
//        }
    };

    private void onCarSpeedChange() {

        if(mCarService == null) {
            Log.d(TAG, "onCarSpeedChange:  mCarService is NULL");
            return;
        }

        try {
            mCarSensorManager = (CarSensorManager) mCarService.getCarManager(Car.SENSOR_SERVICE);
            mCarSensorManager.registerListener(mSenserListener,CarSensorManager.SENSOR_TYPE_CAR_SPEED,CarSensorManager.SENSOR_RATE_NORMAL);
        } catch (CarNotConnectedException e) {
            e.printStackTrace();
        }
//        try {
//            mCarHvacManager =(CarHvacManager) mCarService.getCarManager(Car.HVAC_SERVICE);
//        } catch (CarNotConnectedException e) {
//            Log.e(TAG, "onCarSpeedChange: CarNotConnectedException");
//            e.printStackTrace();
//        }
    }

    private void EstablishCarServiceConnection() {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)) {
            Log.w(TAG, "EstablishCarServiceConnection: FEATURE_AUTOMOTIVE not available");
            return;
        }

        if(mCarService == null) {
            Log.d(TAG, "EstablishCarServiceConnection:  mCarService is NULL");
            mCarService = Car.createCar(this,mConnection);
        }
        else{
            Log.d(TAG, "EstablishCarServiceConnection: mCarService is already created");
        }
    }
}