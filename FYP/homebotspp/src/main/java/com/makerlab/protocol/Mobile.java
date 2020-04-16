package com.makerlab.protocol;

import android.util.Log;

import com.makerlab.bt.BluetoothConnect;

public class Mobile extends GoBLE {
    static public final byte HALT = 0;
    static public final byte DIAG_UP_RIGHT= 1;
    static public final byte TURN_RIGHT = 2;
    static public final byte DIAG_DOWN_RIGHT = 3;
    static public final byte TURN_LEFT = 4;
    static public final byte DIAG_UP_LEFT = 5;
    static public final byte DIAG_DOWN_LEFT= 6;
    static public final byte SIDEWAY_UP=10;
    static public final byte SIDEWAY_DOWN=11;
    static public final byte SIDEWAY_LEFT=12;
    static public final byte SIDEWAY_RIGHT=13;

    private final int mCenterPos = 128;
    private BluetoothConnect mBluetoothConnect;

    public Mobile(BluetoothConnect bluetoothConnect) {
        mBluetoothConnect=bluetoothConnect;
        setRepeat(true);
    }

    private void send(byte[] payload) {
        boolean rc = mBluetoothConnect.send(payload);
        String msg = rc ? "ok" : "bad";
        Log.e("Mobile send", msg);;
    }
    //
    public void halt() {
        byte[] payload = getPayload(mCenterPos, mCenterPos, null);
        send(payload);
    }
    //
    public void sidewayUp() {
        byte[] payload = getPayload(250, mCenterPos, null);
        send(payload);
    }

    public void sidewayDown() {
        byte[] payload = getPayload(10, mCenterPos, null);
        send(payload);
    }

    public void sidewayLeft() {
        byte[] payload = getPayload(mCenterPos, 10, null);
        send(payload);
    }

    public void sidewayRight() {
        byte[] payload = getPayload(mCenterPos, 250, null);
        send(payload);
    }
    //
    public void turnRight() {
        byte[] directionCmd = {TURN_RIGHT};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    public void turnLeft() {
        byte[] directionCmd = {TURN_LEFT};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    //
    public void diagonalUpRight() {
        byte[] directionCmd = {DIAG_UP_RIGHT};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    public void diagonalDownRight() {
        byte[] directionCmd = {DIAG_DOWN_RIGHT};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    public void diagonalUpLeft() {
        byte[] directionCmd = {DIAG_UP_LEFT};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    public void diagonalDownLeft() {
        byte[] directionCmd = {DIAG_DOWN_LEFT};
        byte[] payload = getPayload(mCenterPos, mCenterPos, directionCmd);
        send(payload);
    }

    protected void finalize() {
        halt();
    }

}
