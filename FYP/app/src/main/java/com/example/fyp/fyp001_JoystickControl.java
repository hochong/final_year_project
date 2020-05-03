package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.makerlab.protocol.Mobile;
import com.makerlab.protocol.Turret;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

import io.github.controlwear.virtual.joystick.android.JoystickView;

import static com.example.fyp.fyp001_BluetoothConnectionRobotApp.mobile;
import static com.example.fyp.fyp001_BluetoothConnectionRobotApp.turret;

public class fyp001_JoystickControl extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private static final String TAG = "JoystickControl";            /*tag for log purpose*/

    private CameraBridgeViewBase mOpenCvCameraView;                 /*openCV camera*/

    private fyp001_BluetoothConnectionRobotApp bcra;                /*helper class*/

    private static final int PORT = 9020;                           /*socket*/
    private fyp001_otherPhonesHandler listener;                                  /*socket*/


    /*
    Protected function definitions

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

        Button turret_home = (Button)findViewById(R.id.button_turret_home);
        Button turret_up = (Button)findViewById(R.id.button_turret_up);
        Button turret_down = (Button)findViewById(R.id.button_turret_down);
        Button turret_left = (Button)findViewById(R.id.button_turret_left);
        Button turret_right = (Button)findViewById(R.id.button_turret_right);

        turret_home.setOnTouchListener(new View.OnTouchListener() {
            /*
            Public function definitions

            Function Name: boolean onTouch
                            View v
                            MotionEvent event

            Description:
                control the home button and the turret, when button is pressed, move the turret back to the default position

            Import:
                v, View
                event, MotionEvent, event that tells how the button is touched

            Export:
                no export

            Return:
                true
            */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    turret_home();
                }
                return true;
            }
        });

        turret_up.setOnTouchListener(new View.OnTouchListener() {
            /*
            Public function definitions

            Function Name: boolean onTouch
                            View v
                            MotionEvent event

            Description:
                control the up button and the turret
                when button is pressed, turret moves up
                when button is released, turret stops moving up

            Import:
                v, View
                event, MotionEvent, event that tells how the button is touched

            Export:
                no export

            Return:
                true
            */
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    turret_up();
                }else if (event.getAction() == MotionEvent.ACTION_UP){
                    turret_halt();
                }
                return true;
            }
        });

        turret_down.setOnTouchListener(new View.OnTouchListener() {
            /*
            Public function definitions

            Function Name: boolean onTouch
                            View v
                            MotionEvent event

            Description:
                control the down button and the turret
                when button is pressed, turret moves down
                when button is released, turret stops moving down

            Import:
                v, View
                event, MotionEvent, event that tells how the button is touched

            Export:
                no export

            Return:
                true
            */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    turret_down();
                }else if (event.getAction() == MotionEvent.ACTION_UP){
                    turret_halt();
                }
                return true;
            }
        });

        turret_left.setOnTouchListener(new View.OnTouchListener() {
            /*
            Public function definitions

            Function Name: boolean onTouch
                            View v
                            MotionEvent event

            Description:
                control the left button and the turret
                when button is pressed, turret moves left
                when button is released, turret stops moving left

            Import:
                v, View
                event, MotionEvent, event that tells how the button is touched

            Export:
                no export

            Return:
                true
            */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    turret_left();
                }else if (event.getAction() == MotionEvent.ACTION_UP){
                    turret_halt();
                }
                return true;
            }
        });

        turret_right.setOnTouchListener(new View.OnTouchListener() {
            /*
            Public function definitions

            Function Name: boolean onTouch
                            View v
                            MotionEvent event

            Description:
                control the right button and the turret
                when button is pressed, turret moves right
                when button is released, turret stops moving right

            Import:
                v, View
                event, MotionEvent, event that tells how the button is touched

            Export:
                no export

            Return:
                true
            */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    turret_right();
                }else if (event.getAction() == MotionEvent.ACTION_UP){
                    turret_halt();
                }
                return true;
            }
        });

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
                angle, int, integer that determine the polar coordinate of the joystick
                strength, int, integer that determine the polar coordinate of the joystick

            Export:
                no export

            Return:
                no return
            */
            @Override
            public void onMove(int angle, int strength) {

                //move forwards 90+-20 activate over 70%
                if (angle > 70 && angle <= 110 && strength > 70){
                    //Log.i("MainActivity", "move forward");
                    bcra.set_mobile_movement(Mobile.SIDEWAY_UP);
                }
                //move left 180+-20 activate over 70%
                else if (angle > 160 && angle <= 200 && strength > 70){
                    //Log.i("MainActivity", "move left");
                    bcra.set_mobile_movement(Mobile.SIDEWAY_RIGHT);
                }
                //move right 0+-20 activate over 70%
                else if ((angle > 340 || angle <= 20) && strength > 70){
                    //Log.i("MainActivity", "move right");
                    bcra.set_mobile_movement(Mobile.SIDEWAY_LEFT);
                }
                //move backwards 270+-20 activate over 70%
                else if (angle > 250 && angle <= 290 && strength > 70){
                    //Log.i("MainActivity", "move backward");
                    bcra.set_mobile_movement(Mobile.SIDEWAY_DOWN);
                }
                else if (angle > 120 && angle <= 160 && strength > 70){
                    //Log.i("MainActivity", "move up + left");
                    bcra.set_mobile_movement(Mobile.DIAG_UP_RIGHT);
                }
                else if (angle > 20 && angle <= 70 && strength > 70){
                    //Log.i("MainActivity", "move up + right");
                    bcra.set_mobile_movement(Mobile.DIAG_DOWN_RIGHT);
                }
                else if (angle > 200 && angle <= 250 && strength > 70){
                    //Log.i("MainActivity", "move down + left");
                    bcra.set_mobile_movement(Mobile.DIAG_UP_LEFT);
                }
                else if (angle > 290 && angle <= 340 && strength > 70){
                    //Log.i("MainActivity", "move down + right");
                    bcra.set_mobile_movement(Mobile.DIAG_DOWN_LEFT);
                }
                else{
                    //Log.i("MainActivity", "halt");
                    bcra.set_mobile_movement(Mobile.HALT);
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

        bcra.start_mobile_timer_task();
        bcra.start_turret_timer_task();
    }

    /*
    Public function definitions

    Function Name: void turret_home

    Description:
       ask turret to go back to home position
       public function for listeners to access bcra class function
       listener do not have to get util class, only need to call this public function
       from :
       bcra = getApplication();
       bcra.set_turret_movement(Turret.HOME);
       to :
       turret_home();

    Import:
        no import

    Export:
        no export

    Return:
        true
    */
    public void turret_home(){
        bcra.set_turret_movement(Turret.HOME);
    }

    /*
    Public function definitions

    Function Name: void turret_up

    Description:
       ask turret to go up
       public function for listeners to access bcra class function
       listener do not have to get util class, only need to call this public function
       from :
       bcra = getApplication();
       bcra.set_turret_movement(Turret.UP);
       to :
       turret_up();

    Import:
        no import

    Export:
        no export

    Return:
        true
    */
    public void turret_up(){
        bcra.set_turret_movement(Turret.UP);
    }

    /*
    Public function definitions

    Function Name: void turret_down

    Description:
       ask turret to go down
       public function for listeners to access bcra class function
       listener do not have to get util class, only need to call this public function
       from :
       bcra = getApplication();
       bcra.set_turret_movement(Turret.DOWN);
       to :
       turret_down();

    Import:
        no import

    Export:
        no export

    Return:
        true
    */
    public void turret_down(){
        bcra.set_turret_movement(Turret.DOWN);
    }

    /*
    Public function definitions

    Function Name: void turret_left

    Description:
       ask turret to rotate left
       public function for listeners to access bcra class function
       listener do not have to get util class, only need to call this public function
       from :
       bcra = getApplication();
       bcra.set_turret_movement(Turret.LEFT);
       to :
       turret_left();

    Import:
        no import

    Export:
        no export

    Return:
        true
    */
    public void turret_left(){
        bcra.set_turret_movement(Turret.LEFT);
    }

    /*
    Public function definitions

    Function Name: void turret_right

    Description:
       ask turret to rotate right
       public function for listeners to access bcra class function
       listener do not have to get util class, only need to call this public function
       from :
       bcra = getApplication();
       bcra.set_turret_movement(Turret.RIGHT);
       to :
       turret_right();

    Import:
        no import

    Export:
        no export

    Return:
        true
    */
    public void turret_right(){
        bcra.set_turret_movement(Turret.RIGHT);
    }

    /*
    Public function definitions

    Function Name: void turret_halt

    Description:
       ask turret to stop the current rotation
       public function for listeners to access bcra class function
       listener do not have to get util class, only need to call this public function
       from :
       bcra = getApplication();
       bcra.set_turret_movement(Turret.HALT);
       to :
       turret_halt();

    Import:
        no import

    Export:
        no export

    Return:
        true
    */
    public void turret_halt() {
        bcra.set_turret_movement(Turret.HALT);
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
        //button showing ip and port
        Button b = findViewById(R.id.connect_to_other_phone_button);
        b.setClickable(false);
        b.setText(address + ":" + Integer.toString(PORT));
        Log.e(TAG, "server address: "+ address + ":"+PORT);
        listener = new fyp001_otherPhonesHandler(PORT, bcra);

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
        listener.closethread();

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
