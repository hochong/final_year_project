package com.makerlab.protocol;

import android.util.Log;

import com.makerlab.bt.BluetoothConnect;

public class Turret extends GoBLE {
    static public final byte HALT = 0;
    static public final byte UP = 1;
    static public final byte RIGHT = 2;
    static public final byte DOWN = 3;
    static public final byte LEFT = 4;
    static public final byte HOME = 8;
    private final int mCenterPos = 128;

    private BluetoothConnect mBluetoothConnect;
    public Turret(BluetoothConnect bluetoothConnect) {
        mBluetoothConnect = bluetoothConnect;
        setRepeat(true);
    }

    private void send(byte[] payload) {
        boolean rc = mBluetoothConnect.send(payload);
        String msg = rc ? "ok" : "bad";
        Log.e("Turret send", msg);
    }

    public void halt() {
        byte[] payload = getPayload(mCenterPos, mCenterPos, null);
        send(payload);
    }

    public void panLeft() {
        byte[] directionCmd = {LEFT};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    public void panRight() {
        byte[] directionCmd = {RIGHT};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    public void tiltUp() {
        byte[] directionCmd = {UP};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    public void tiltDown() {
        byte[] directionCmd = {DOWN};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    public void home() {
        byte[] directionCmd = {HOME};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    protected void finalize() {
        halt();
    }
}
