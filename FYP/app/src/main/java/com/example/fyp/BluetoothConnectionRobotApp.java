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
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private List<String> deviceListname = new ArrayList<>();
    private String TAG = "BluetoothConnectionRobotApp";
    private BluetoothSocket mBluetoothSocket;
    private Context context;
    private UUID uuid;
    public BluetoothConnectionRobotApp(Context c){
        this.context = c;
        //TODO
        //get param
        //wrap up interfaces
        monitorDiscovery();
        startDiscovery();

        uuid = new UUID(0,64);
    }

    public void connectToRobotSocket(int i) {
        connectToServerSocket(deviceList.get(i),UUID.randomUUID());
    }

    public String[] getBluetoothList() {
        return (String[]) deviceListname.toArray();
    }
    public String create_protocol_message(String button, String rockerformat, String rocker1, String rocker2, String rocker3, String rocker4) {
        String header = hextostring("55");
        String headerempty = hextostring("AA");
        String address = hextostring("11");
        String buttonquantity = hextostring("01");
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
    public String hextostring(String value) {
        return new BigInteger(value.substring(0,1), 16).toString() + new BigInteger(value.substring(1,2), 16).toString();

    }
    public void sendtorobot(String message){
        sendMessage(mBluetoothSocket, message);
        Log.i(TAG, "message sent: "+ message);
    }

    private void monitorDiscovery() {
        context.registerReceiver(discoveryMonitor,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        context.registerReceiver(discoveryMonitor,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
                    .equals(intent.getAction())) {
                // Discovery has started.
                Log.d(TAG, "Discovery Started...");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(intent.getAction())) {
                // Discovery has completed.
                Log.d(TAG, "Discovery Complete.");
            }
        }
    };
    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String remoteDeviceName =
                    intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice remoteDevice =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            deviceList.add(remoteDevice);
            deviceListname.add(remoteDeviceName);
            Log.d(TAG, "Discovered " + remoteDeviceName);
        }
    };


    private void startDiscovery() {
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        context.registerReceiver(discoveryResult,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
        if (mBluetooth.isEnabled() && !mBluetooth.isDiscovering()) {
            deviceList.clear();
            mBluetooth.startDiscovery();
        }

    }

    public BluetoothSocket connectToServerSocket(BluetoothDevice device, UUID uuid) {
        BluetoothSocket returnshocket = null;
        try{
            BluetoothSocket clientSocket
                    = device.createRfcommSocketToServiceRecord(uuid);
            // Block until server connection accepted.
            clientSocket.connect();

            // Add a reference to the socket used to send messages.
            this.mBluetoothSocket = clientSocket;
            returnshocket = this.mBluetoothSocket;

        } catch (IOException e) {
            Log.e(TAG, "Bluetooth client I/O Exception.", e);
        }
        return returnshocket;
    }


    private void sendMessage(BluetoothSocket socket, String message) {
        OutputStream outputStream;

        try {
            outputStream = socket.getOutputStream();

            // Add a stop character.
            byte[] byteArray = (message + " ").getBytes();
            byteArray[byteArray.length-1] = 0;

            outputStream.write(byteArray);
        } catch (IOException e) {
            Log.e(TAG, "Failed to send message: " + message, e);
        }
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
