package com.example.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.makerlab.bt.BluetoothConnect;
import com.makerlab.protocol.Mobile;
import com.makerlab.protocol.Turret;
import com.makerlab.ui.BluetoothDevListActivity;

import static com.example.fyp.fyp001_BluetoothConnectionRobotApp.mBluetoothMobileConnect;
import static com.example.fyp.fyp001_BluetoothConnectionRobotApp.mBluetoothTurretConnect;

public class fyp001_MainActivity extends AppCompatActivity {
    static public final int REQUEST_BT_GET_DEVICE_MOBILE = 1;       /*mobile flag*/
    static public final int REQUEST_BT_GET_DEVICE_TURRET = 0;       /*turret flag*/

    /*
    Protected function definitions

    Function Name: void onCreate
                    Bundle savedInstanceState

    Description:
        first called when activity launched
        set up the layout

    Import:
        savedInstanceState, Bundle, initial or state of this activity before it is paused

    Export:
        no export

    Return:
        no return value
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBluetoothMobileConnect = new BluetoothConnect(this);
        mBluetoothTurretConnect = new BluetoothConnect(this);

    }
    /*
    Public function definitions

    Function Name: void startControl
                    View view

    Description:
        called when user pressed the get start button
        get user to the joystick control application

    Import:
        view, View

    Export:
        no export

    Return:
        no return value
    */
    public void startControl(View view) {
        Intent i = new Intent(this, fyp001_JoystickControl.class);
        startActivity(i);
    }

    /*
    Public function definitions

    Function Name: void startControl
                    View view

    Description:
        called when user pressed the connect to robot button
        connect to robot

    Import:
        view, View

    Export:
        call intent to return with found device

    Return:
        no return value
    */
    public void bluetoothMobileConnection(View view) {
        if (mBluetoothMobileConnect.isConnected()){
            mBluetoothMobileConnect.disconnectBluetooth();
        }
        Intent intent = new Intent(this, BluetoothDevListActivity.class);
        startActivityForResult(intent, REQUEST_BT_GET_DEVICE_MOBILE);
    }

    /*
    Public function definitions

    Function Name: void bluetoothTurretConnection
                    View view

    Description:
        called when user pressed the connect to turret button
        connect to robot arm(turret)

    Import:
        view, View

    Export:
        call intent to return with found device

    Return:
        no return value
    */
    public void bluetoothTurretConnection(View view) {
        if (mBluetoothTurretConnect.isConnected()){
            mBluetoothTurretConnect.disconnectBluetooth();
        }
        Intent intent = new Intent(this, BluetoothDevListActivity.class);
        startActivityForResult(intent, REQUEST_BT_GET_DEVICE_TURRET);
    }

    /*
    Public function definitions

    Function Name: void onActivityResult
                    int requestCode
                    int resultCode
                    Intent resultIntent

    Description:
        get the bluetooth device and connect to it

    Import:
        requestCode, int, integer that specify what the request is
        resultCode, int, integer that specify whether the result has been successfully proceeded
        data, Intent, intent that contains data

    Export:
        connect the mobile/turret to the returned bluetooth device

    Return:
        no return value
    */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (resultCode != RESULT_OK){
            return;
        }
        if (requestCode == REQUEST_BT_GET_DEVICE_MOBILE) {
            final BluetoothDevice mBluetoothDevice;
            mBluetoothDevice = resultIntent.getParcelableExtra(BluetoothDevListActivity.EXTRA_KEY_DEVICE);
            if (mBluetoothDevice == null) {
                return;
            }
            if (mBluetoothMobileConnect.isConnected()) {
                mBluetoothMobileConnect.disconnectBluetooth();
            }
            fyp001_BluetoothConnectionRobotApp.set_mobile_device(mBluetoothDevice);
            mBluetoothMobileConnect.connectBluetooth(mBluetoothDevice);
            fyp001_BluetoothConnectionRobotApp.create_new_mobile(mBluetoothMobileConnect);
            Toast.makeText(this, "Mobile connected to " + mBluetoothDevice.getName(), Toast.LENGTH_LONG).show();
        } else {
            final BluetoothDevice mBluetoothDevice;
            mBluetoothDevice = resultIntent.getParcelableExtra(BluetoothDevListActivity.EXTRA_KEY_DEVICE);
            if (mBluetoothDevice == null) {
                return;
            }
            if (mBluetoothTurretConnect.isConnected()) {
                mBluetoothTurretConnect.disconnectBluetooth();
            }
            fyp001_BluetoothConnectionRobotApp.set_turret_device(mBluetoothDevice);
            mBluetoothTurretConnect.connectBluetooth(mBluetoothDevice);
            fyp001_BluetoothConnectionRobotApp.create_new_turret(mBluetoothTurretConnect);
            Toast.makeText(this,"Turret connected to " + mBluetoothDevice.getName(),Toast.LENGTH_LONG).show();
        }
    }
}
