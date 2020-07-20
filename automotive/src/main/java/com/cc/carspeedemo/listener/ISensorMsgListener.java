package com.cc.carspeedemo.listener;

import android.car.hardware.CarSensorEvent;

public abstract class ISensorMsgListener {
    protected abstract void onSensorValueChanged(CarSensorEvent var1);
}
