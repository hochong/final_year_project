package com.example.fyp;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import java.io.Serializable;

import java.util.UUID;

public class BluetoothConnectionRobotApp extends Application {
    private BluetoothAdapter mBluetooth;
    BluetoothDevice device;
    BluetoothGatt mBluetoothGatt;
    UUID selectedserviceuuid;
    UUID selectedcharuuid;
    private String TAG = "BluetoothConnectionRobotApp";

    public synchronized void setBTConnectiondevice(BluetoothDevice d) {
        device = d;
    }
    public synchronized BluetoothDevice getBTConnection(){
        return device;
    }
    public synchronized void setBTConnection(BluetoothGatt gatt){
        mBluetoothGatt = gatt;
    }
    public synchronized void setServiceuuid(UUID serviceuuid){
        selectedcharuuid = serviceuuid;
    }
    public synchronized void setCharuuid(UUID charuuid){
        selectedcharuuid = charuuid;
    }
    public BluetoothAdapter getmBluetooth() {
        return mBluetooth;
    }



    public String create_protocol_message(int button, int rockerformat, int rocker1, int rocker2, int rocker3, int rocker4) {
        String header = hextostring(0x55);
        String headerempty = hextostring(0xAA);
        String address = hextostring(0x11);
        String buttonquantity = hextostring(0x00);
        String rockerposition = hextostring(rockerformat);
        String b = hextostring(button);
        String r1 = hextostring(rocker1);
        String r2 = hextostring(rocker2);
        String r3 = hextostring(rocker3);
        String r4 = hextostring(rocker4);

        //add checksum
        int sum = 0x55 + 0xAA + 0x11 + 0x00 + button + rockerformat + rocker1 + rocker2 + rocker3 + rocker4;
        int checksum = (sum/256) + (sum % 256);


        String returnString = header + headerempty + address + buttonquantity + rockerposition + b + r1 + r2 + r3 + r4+ checksum;
        return returnString;
    }
    public String hextostring(int value) {
        String twohex = Integer.toHexString(value);
       if (value > 0x0f){
           return "0" + twohex;
       }

        return twohex;

    }
    public void sendtorobot(String message){
        blewriteCharacteristic(message);
        Log.i(TAG, "message sent: "+ message);
    }


    public boolean blewriteCharacteristic(String msg){

        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = mBluetoothGatt.getService(selectedserviceuuid);
        if (Service == null) {
            Log.e(TAG, "service not found!");
            return false;
        }
        BluetoothGattCharacteristic charac = Service
                .getCharacteristic(selectedcharuuid);
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }

        byte[] value = msg.getBytes();
        charac.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(charac);
        return status;
    }

    /* only send msg to robot but not listening to its response
    private boolean mListening = false;
    private String listenForMessages(BluetoothSocket socket,
                                     StringBuilder incoming) {
        String result = "";
        mListening = true;

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        try {
            InputStream instream = socket.getInputStream();
            int bytesRead = -1;

            while (mListening) {
                bytesRead = instream.read(buffer);
                if (bytesRead != -1) {
                    while ((bytesRead == bufferSize) &&
                            (buffer[bufferSize-1] != 0)) {
                        result = result + new String(buffer, 0, bytesRead - 1);
                        bytesRead = instream.read(buffer);
                    }
                    result = result + new String(buffer, 0, bytesRead - 1);
                    incoming.append(result);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Message receive failed.", e);
        }
        return result;
    }*/
}
