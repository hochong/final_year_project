package com.example.fyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConnectRobot extends AppCompatActivity {
    private BluetoothAdapter mBluetooth;
    private static final int ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_LOC = 2;
    private String TAG = "ConnectRobot";
    private String[] bluetoothList = null;
    private ConnectRobot_BluetoothListAdapter crbla;

    //recyclerView var
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;


    private BluetoothSocket mBluetoothSocket;
    private Context context;
    private UUID uuid;
    private ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private ArrayList<String> deviceListname = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_robot);

        //TODO
        //connect Bluetooth
        Log.d(TAG, "new bcra");
        BluetoothConnectionRobotApp bcra= new BluetoothConnectionRobotApp();
        //temp
        mBluetooth = bcra.getmBluetooth();
        initBluetooth();
        //monitorDiscovery();
        startDiscovery();

        deviceListname.add("test1");
        //show list of connections in ui
        recyclerView = (RecyclerView) findViewById(R.id.bluetooth_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        crbla = new ConnectRobot_BluetoothListAdapter(this, deviceListname, bcra);
        mAdapter = crbla;
        recyclerView.setAdapter(mAdapter);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //return connection
    }

    private void initBluetooth(){
        //comment out for simulator testing
        if (mBluetooth == null){
            return;
        }
        if (!mBluetooth.isEnabled()) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(i, ENABLE_BLUETOOTH);
        } else {
            Log.d(TAG, "Bluetooth Started...");
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

        } else {
            Log.d(TAG, "Permissions not granted");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOC);
        }
    }
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        if (requestCode == ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK){
                Log.d(TAG, "Bluetooth Started...");
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetooth != null) {
            mBluetooth.cancelDiscovery();
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(discoveryResult);
        unregisterReceiver(discoveryMonitor);
    }

    private final BroadcastReceiver discoveryMonitor = new BroadcastReceiver() {
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
    private final BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        private ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
        private ArrayList<String> deviceListname = new ArrayList<String>();
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Bluetooth result received ");
            String remoteDeviceName =
                    intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice remoteDevice =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] uuidExtra =
                    intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
            deviceList.add(remoteDevice);
            if (remoteDeviceName == null){
                deviceListname.add("null device name");
            }
            deviceListname.add(remoteDeviceName);
            Log.d(TAG, "Discovered " + remoteDevice.getName() +" /// "+ remoteDeviceName);


            crbla.updateData(deviceListname);
            Log.d(TAG, "deviceListname " + deviceListname);
        }
    };

    private void monitorDiscovery() {
        this.registerReceiver(discoveryMonitor,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        this.registerReceiver(discoveryMonitor,
                new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }
    private void startDiscovery() {
        mBluetooth = BluetoothAdapter.getDefaultAdapter();
        this.registerReceiver(discoveryResult,
                new IntentFilter(BluetoothDevice.ACTION_FOUND));
        if (mBluetooth.isEnabled() && !mBluetooth.isDiscovering()) {
            deviceList.clear();
            mBluetooth.startDiscovery();
        }
        Log.d(TAG, "mBluetooth.isEnabled() = " + mBluetooth.isEnabled());
        Log.d(TAG, "mBluetooth.isDiscovering() = " + mBluetooth.isDiscovering());

    }
    public static void connectToRobotSocket(int pos){
        //TODO
        //getpos
        //call connecttoserversocket
    }
    private BluetoothSocket connectToServerSocket(BluetoothDevice device, UUID uuid) {
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
}
