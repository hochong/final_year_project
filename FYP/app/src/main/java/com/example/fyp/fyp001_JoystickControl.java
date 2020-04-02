package com.example.fyp;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class fyp001_JoystickControl extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "JoystickControl";
    //openCV camera
    private CameraBridgeViewBase mOpenCvCameraView;

    //camera
    //private Camera mCamera;
    //private CameraControlApp.CameraPreview mPreview;

    private fyp001_BluetoothConnectionRobotApp bcra;
    BluetoothGatt mBluetoothGatt;
    UUID selectedserviceuuid;
    UUID selectedcharuuid;
    private static final int PORT = 9020;
    private ServerSocket listener;

    //handler to send msg to robot at regular time interval
    private final int up = 0;
    private final int right = 3;
    private final int down = 6;
    private final int left = 9;
    private final int interval = 500;//0.5sec

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fyp001_activity_joystick_control);
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

        bcra = (fyp001_BluetoothConnectionRobotApp) getApplication();

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            //TODO implement actual moving through bluetooth connection
            public void onMove(int angle, int strength) {
                //move forwards 90+-20 activate over 70%
                if (angle > 70 && angle < 110 && strength >70){
                    Log.i("MainActivity", "move forward");
                    if (bcra != null) {
                        bcra.blewriteCharacteristic_byte(bcra.get_forward_byte_array());
                    }
                }

                //move left 180+-20 activate over 70%
                if (angle > 160 && angle < 200 && strength >70){
                    Log.i("MainActivity", "move left");
                    if (bcra != null) {
                        bcra.blewriteCharacteristic_byte(bcra.get_left_byte_array());
                    }

                }

                //move right 0+-20 activate over 70%
                if ((angle > 340 || angle < 20) && strength >70){
                    Log.i("MainActivity", "move right");
                    if (bcra != null) {
                        bcra.blewriteCharacteristic_byte(bcra.get_right_byte_array());
                    }
                }

                //move backwards 270+-20 activate over 70%
                if (angle > 250 && angle < 290 && strength >70){
                    Log.i("MainActivity", "move backward");
                    if (bcra != null) {
                        bcra.blewriteCharacteristic_byte(bcra.get_backward_byte_array());
                    }
                }

            }
        });
        //openCV camera
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.joystick_cameraView);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.enableView();
            Log.i(TAG,"Camera Start!");
        }
    }

    public void connectToOtherPhones(View view) {

        try {
            listener = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            //setup socket
            Socket s = listener.accept();

            //button showing ip and port
            Button b = findViewById(R.id.connect_to_other_phone_button);
            b.setText(s.getInetAddress().getHostAddress() + " " + Integer.toString(PORT));

            //start thread
            new fyp001_otherPhonesHandler(s, bcra).start();

        } catch (Exception e) {
            try {
                listener.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }


    public void toVoiceControl(View view) {
        Intent i = new Intent(this, fyp001_VoiceControl.class);
        startActivity(i);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG,"openCV joystick camera started!");
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return bcra.processFrame(inputFrame);
    }
}
