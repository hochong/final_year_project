package com.example.fyp;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.makerlab.protocol.Mobile;
import com.makerlab.protocol.Turret;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import static com.example.fyp.fyp001_BluetoothConnectionRobotApp.mobile;
import static com.example.fyp.fyp001_BluetoothConnectionRobotApp.turret;

public class fyp001_JoystickControl extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "JoystickControl";            /*tag for log purpose*/

    private CameraBridgeViewBase mOpenCvCameraView;                 /*openCV camera*/

    private fyp001_BluetoothConnectionRobotApp bcra;                /*helper class*/

    private static final int PORT = 9020;                           /*socket*/
    private ServerSocket listener;                                  /*socket*/

    /*
    Public function definitions

    Function Name: void onCreate
                    Bundle savedInstanceState

    Description:
        first called when activity launched
        set the layout and listeners

    Import:
        savedInstanceState, Bundle, initial or state of this activity before it is paused

    Export:
        no export

    Return:
        no return
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fyp001_activity_joystick_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            /*
            Public function definitions

            Function Name: void onClick
                            View view

            Description:
                switch to voiceControl activity

            Import:
                view View

            Export:
                no export

            Return:
                no return
            */
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
        mobile = bcra.get_mobile();
        turret = bcra.get_turret();

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            /*
            Public function definitions

            Function Name: void onMove
                            int angle
                            int strength

            Description:
                override function to control the joystick
                using angle and strength to determine the joystick's position
                direct the robot to move in the corresponding position

            Import:
                angle, int, intger that determine the polar coordinate of the joystick
                strength, int, integer that determine the polar coordinate of the joystick

            Export:
                no export

            Return:
                no return
            */
            @Override
            //TODO implement actual moving through bluetooth connection
            public void onMove(int angle, int strength) {

                //move forwards 90+-20 activate over 70%
                if (angle > 70 && angle <= 110 && strength > 70){
                    Log.i("MainActivity", "move forward");
                    bcra.set_mobile_movement(Mobile.SIDEWAY_UP);
                }
                //move left 180+-20 activate over 70%
                if (angle > 160 && angle <= 200 && strength > 70){
                    Log.i("MainActivity", "move left");
                    bcra.set_mobile_movement(Mobile.SIDEWAY_LEFT);
                }
                //move right 0+-20 activate over 70%
                if ((angle > 340 || angle <= 20) && strength > 70){
                    Log.i("MainActivity", "move right");
                    bcra.set_mobile_movement(Mobile.SIDEWAY_RIGHT);
                }
                //move backwards 270+-20 activate over 70%
                if (angle > 250 && angle <= 290 && strength > 70){
                    Log.i("MainActivity", "move backward");
                    bcra.set_mobile_movement(Mobile.SIDEWAY_DOWN);
                }
                if (angle > 120 && angle <= 160 && strength > 70){
                    Log.i("MainActivity", "move up + left");
                    bcra.set_mobile_movement(Mobile.DIAG_UP_LEFT);
                }
                if (angle > 20 && angle <= 70 && strength > 70){
                    Log.i("MainActivity", "move up + right");
                    bcra.set_mobile_movement(Mobile.DIAG_UP_RIGHT);
                }
                if (angle > 200 && angle <= 250 && strength > 70){
                    Log.i("MainActivity", "move down + left");
                    bcra.set_mobile_movement(Mobile.DIAG_DOWN_LEFT);
                }
                if (angle > 290 && angle <= 340 && strength > 70){
                    Log.i("MainActivity", "move down + right");
                    bcra.set_mobile_movement(Mobile.DIAG_DOWN_RIGHT);
                }
                else{
                    Log.i("MainActivity", "halt");
                    bcra.set_mobile_movement(Mobile.HALT);
                    bcra.set_turret_movement(Turret.HALT);
                }
                if (bcra.get_obs_at_center()){
                    Toast.makeText(fyp001_JoystickControl.this,"Obstacle(s) detected at the front of the camera",Toast.LENGTH_SHORT).show();
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
    public void turret_home(View view){
        bcra.set_turret_movement(Turret.HOME);
    }
    public void turret_up(View view){
        bcra.set_turret_movement(Turret.UP);
    }
    public void turret_down(View view){
        bcra.set_turret_movement(Turret.DOWN);
    }
    public void turret_left(View view){
        bcra.set_turret_movement(Turret.LEFT);
    }
    public void turret_right(View view){
        bcra.set_turret_movement(Turret.RIGHT);
    }
    /*
    Public function definitions

    Function Name: void toVoiceControl
                    View view

    Description:
        get user to VoiceControl activity

    Import:
        view View

    Export:
        no export

    Return:
        no return
    */
    public void connectToOtherPhones(View view) {

        String address = bcra.getIPAddress(true);
        try {
            listener = new ServerSocket(PORT);
            //button showing ip and port
            Button b = findViewById(R.id.connect_to_other_phone_button);
            b.setClickable(false);
            b.setText(address + ":" + Integer.toString(PORT));
            Log.e(TAG, "server address: "+ address + ":"+PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            Socket s = listener.accept();
            //start thread
            new fyp001_otherPhonesHandler(s, bcra).start();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
    /*
    Public function definitions

    Function Name: void toVoiceControl
                    View view

    Description:
        get user to VoiceControl activity

    Import:
        view View

    Export:
        no export

    Return:
        no return
    */

    public void toVoiceControl(View view) {
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
        Intent i = new Intent(this, fyp001_VoiceControl.class);
        startActivity(i);
    }
    /*
    Public function definitions

    Function Name: void onDestroy

    Description:
        override function
        close the socket

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    Public function definitions

    Function Name: void onCameraViewStarted
                    int width
                    int height

    Description:
        override function

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG,"openCV joystick camera started!");
    }
    /*
    Public function definitions

    Function Name: void onCameraViewStopped

    Description:
        override function

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    @Override
    public void onCameraViewStopped() {
        /*empty body*/
    }
    /*
    Public function definitions

    Function Name: Mat onCameraFrame
                        CameraBridgeViewBase.CvCameraViewFrame inputFrame

    Description:
        on each frame returned by the camera, this function will call the helper function in application class bcra
        the frame will be analysed and green boxes will be drew around the object identified
        then it will be returned to the cameraView in the layout file

    Import:
        input Frame, CvCameraViewFrame, the frame captured by the camera in each sec

    Export:
        no export

    Return:
        analysed frame will be returned to the cameraView
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return bcra.processFrame(inputFrame);
    }
}
