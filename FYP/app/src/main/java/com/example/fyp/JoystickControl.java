package com.example.fyp;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class JoystickControl extends AppCompatActivity {
    //camera
    private Camera mCamera;
    private CameraControlApp.CameraPreview mPreview;
    private BluetoothConnectionRobotApp bcra;
    BluetoothGatt mBluetoothGatt;
    UUID selectedserviceuuid;
    UUID selectedcharuuid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Switch to Voice Control", Snackbar.LENGTH_LONG)
                        .setAction("Go", new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                toVoiceControl(v);
                            }
                        }).show();
            }
        });

        /*
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            mBluetoothGatt = (BluetoothGatt) bundle.get("gatt");
            selectedserviceuuid = (UUID) bundle.get("serviceuuid");
            selectedcharuuid = (UUID) bundle.get("charuuid");
            Log.i("MainActivity", "selectedserviceuuid" + selectedserviceuuid.toString());
            Log.i("MainActivity", "selectedcharuuid" + selectedcharuuid.toString());
        }
        */
        bcra = (BluetoothConnectionRobotApp) getApplication();

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            //TODO implement actual moving through bluetooth connection
            public void onMove(int angle, int strength) {
                //move forwards 90+-20 activate over 70%
                if (angle > 70 && angle < 110 && strength >70){
                    Log.i("MainActivity", "move forward");
                    if (bcra != null) {
                        String msg = bcra.create_protocol_message(00, 02, 00, 255, 00, 00);
                        bcra.sendtorobot("55AA110100010000000015");
                        //bcra.sendtorobot(msg);
                    }
                }

                //move left 180+-20 activate over 70%
                if (angle > 160 && angle < 200 && strength >70){
                    Log.i("MainActivity", "move left");
                    if (bcra != null) {
                        String msg = bcra.create_protocol_message(00, 01,00,00,00,00);
                        bcra.sendtorobot(msg);
                    }

                }

                //move right 0+-20 activate over 70%
                if ((angle > 340 || angle < 20) && strength >70){
                    Log.i("MainActivity", "move right");
                    if (bcra != null) {
                        String msg = bcra.create_protocol_message(00, 01, 255, 00, 00, 00);
                        bcra.sendtorobot(msg);
                    }
                }

                //move backwards 270+-20 activate over 70%
                if (angle > 250 && angle < 290 && strength >70){
                    Log.i("MainActivity", "move backward");
                    if (bcra != null) {
                        String msg = bcra.create_protocol_message(00, 02, 00, 00, 00, 00);
                        bcra.sendtorobot(msg);
                    }
                }

            }
        });

        //camera
        CameraControlApp cca = new CameraControlApp();
        if (cca.checkCameraHardware(this)){
            mCamera = cca.getCameraInstance();
            mPreview = new CameraControlApp.CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.joystick_cameraView);
            preview.addView(mPreview);
        }

    }

    public void connectToOtherPhones(View view) {
    }


    public void toVoiceControl(View view) {
        Intent i = new Intent(this, VoiceControl.class);
        startActivity(i);
    }
}
