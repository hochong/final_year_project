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
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConnectRobot extends AppCompatActivity {
    private BluetoothAdapter mBluetooth;
    private static final int ENABLE_BLUETOOTH = 1;
    private static final int REQUEST_LOC = 2;
    private String TAG = "ConnectRobot";
    private final String[] bluetoothList = new String[5];

    //recyclerView var
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_robot);

        //TODO
        //connect Bluetooth
        //temp
        bluetoothList[0] = "Bluetooth0";
        bluetoothList[1] = "Bluetooth1";
        bluetoothList[2] = "Bluetooth2";
        bluetoothList[3] = "Bluetooth3";
        bluetoothList[4] = "Bluetooth4";
        //show list of connections in ui
        recyclerView = (RecyclerView) findViewById(R.id.bluetooth_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ConnectRobot_BluetoothListAdapter(this, bluetoothList);
        recyclerView.setAdapter(mAdapter);

        //set uuid
        //return connection

    }

    private void initBluetooth(){
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



    private void returnConnection () {

    }
}
