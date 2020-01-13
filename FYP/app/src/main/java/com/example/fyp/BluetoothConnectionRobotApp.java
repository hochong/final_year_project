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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnectionRobotApp {
    private BluetoothAdapter mBluetooth;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private String TAG = "BluetoothConnectionRobotApp";
    private BluetoothSocket mBluetoothSocket;
    private Context context;
    public BluetoothConnectionRobotApp(Context c){
        this.context = c;
        //TODO
        //get param
        //wrap up interfaces
        monitorDiscovery();
        startDiscovery();
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
