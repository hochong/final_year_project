package com.example.fyp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnectionRobotApp implements Serializable{
    private BluetoothAdapter mBluetooth;

    private String TAG = "BluetoothConnectionRobotApp";

    public BluetoothConnectionRobotApp(){
        Log.d(TAG, "Creating bluetooth connection robot app instance");

        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        //TODO
        //get param
        //wrap up interfaces
        //comment out for simulator testing

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
        //TODO
        //add checksum

        String returnString = header + headerempty + address + buttonquantity + rockerposition + b + r1 + r2 + r3 + r4/*+ checksum*/;
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
        //sendMessage(mBluetoothSocket, message);
        Log.i(TAG, "message sent: "+ message);
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
