/*
FYP : Smart phone controlled omnidirectional wheel driven robot

Module:
fyp001 the smartphone application that controls the robot

Class name: fyp001_VoiceControl

Purpose:
    handle voice input from users

States:
    no states

Description:
fyp001_VoiceControl is the class that implements the voice control activity
this activity has a cameraView at the background to show the real time environment
a button switching to joystick control
and a mic image that used to trigger speechrecognizer provided by Google

Public functions:
    public void toJoystickControl(View view)
    public void onCameraViewStarted(int width, int height)
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)

 */
package com.example.fyp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class fyp001_VoiceControl extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2
{
    /*private declarations*/

    private static final String TAG = "VoiceControl";                       /*TAG for log output*/

    private CameraBridgeViewBase mOpenCvCameraView;                         /*openCV camera*/
    private boolean MIC_ON = false;                                         /*openCV camera*/
    private boolean AUTO_DETECT_VOICE = false;                              /*openCV camera*/

    private fyp001_BluetoothConnectionRobotApp bcra;                        /*reference for application*/

    private SpeechRecognizer sr;                                            /*variables for speechrecognizer*/
    private Intent srIntent;                                                /*variables for speechrecognizer*/
    private String instruction = "forward, backward, left, right, stop";    /*variables for speechrecognizer*/
    private int SpeechRecognizerInt = 3000;                                 /*variables for speechrecognizer*/
    private ImageView mic;                                                  /*variables for speechrecognizer*/

    /*
    Protected function definitions

    Function Name: void onCreate
                    Bundle savedInstanceState

    Description:
        first called when activity launched
        set up references to application class, toolbar, mic image, floating action button and speech recognizer

    Import:
        savedInstanceState, Bundle, initial or state of this activity before it is paused

    Export:
        no export

    Return:
        no return value
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fyp001_activity_voice_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mic = findViewById(R.id.micImg);
        FloatingActionButton fab = findViewById(R.id.fab);

        sr = SpeechRecognizer.createSpeechRecognizer(this);

        /*
        check if necessary permissions granted
         */
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }

        /*
        make a snackbar when clicked and go to joystick control after users confirm their actions
         */
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Switch to Joystick Control", Snackbar.LENGTH_LONG)
                        .setAction("Go", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                to_JoystickControl();
                            }
                        }).show();
            }
        });

        bcra = (fyp001_BluetoothConnectionRobotApp) getApplication();

        /*
        mic listener, when clicked, change image and call speechrecognizer and wait for response
         */
        set_mic_image(MIC_ON);
        mic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MIC_ON = !MIC_ON;
                set_mic_image(MIC_ON);
                startListening();
            }

        });

        //openCV camera
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.voice_cameraView);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.enableView();
            Log.i(TAG,"Camera Start!");
        }
    }
    /*
    Private function definitions

    Function Name: void startListening

    Description:
        call the speech recognizer

    Import:
        no import

    Export:
        call speech recognizer

    Return:
        no return value
     */
    private void startListening ()
    {
        srIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //srIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, instruction);
        sr.startListening(srIntent);
        startActivityForResult(srIntent, SpeechRecognizerInt);
    }
    /*
    Protected function definitions

    Function Name: void onActivityResult
                        int requestCode
                        int resultCde
                        Intent data

    Description:
        handle the data returned after the speechrecognizer has processed the voice input of the user

    Import:
        requestCode, int, integer that specify what the request is
        resultCode, int, integer that specify whether the result has been successfully proceeded

    Export:
        identify the user's word from the data then call the helper functions from the application class bcra
        the helper function will then send instructions to the robot
        the mic icon will also be changed as well

    Return:
        no return value
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeechRecognizerInt)
        {
            if (resultCode==RESULT_OK)
            {
                String command = "init";
                try
                {
                     command = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                    Log.i("VoiceControlActivity", "result = " + command);
                }catch(Exception e)
                {
                    /*empty body*/
                }
                switch (command.toUpperCase())
                {
                    case "FORWARD" : bcra.blewriteCharacteristic_byte(bcra.get_forward_byte_array());
                        break;
                    case "BACKWARD" : bcra.blewriteCharacteristic_byte(bcra.get_backward_byte_array());
                        break;
                    case "LEFT" : bcra.blewriteCharacteristic_byte(bcra.get_left_byte_array());
                        break;
                    case "RIGHT" : bcra.blewriteCharacteristic_byte(bcra.get_right_byte_array());
                        break;
                    case "STOP" : //do nothing
                        break;
                    default: //do nothing
                        break;
                }
            }
            MIC_ON = !MIC_ON;
            set_mic_image(MIC_ON);
        }
    }

    /*
    Public function definitions

    Function Name: void toJoystickControl

    Description:
        go to joystick control

    Import:
        no import

    Export:
        go to joystick control

    Return:
        no return value
     */
    public void to_JoystickControl()
    {
        Intent i = new Intent(this, fyp001_JoystickControl.class);
        startActivity(i);
    }

    /*
    private function definitions

    Function Name: void set_mic_image
                        boolean onoff

    Description:
        set the mic image according to the input boolean

    Import:
        onoff, boolean, determine which image should be used

    Export:
        set the image using seImageResource

    Return:
        no return value
     */
    private void set_mic_image(boolean onoff)
    {
        if (onoff)
        {
            mic.setImageResource(R.drawable.fyp001_ic_mic_on_press);
        } else
        {
            mic.setImageResource(R.drawable.fyp001_ic_mic_on);
        }
    }

    /*
    Public function definitions

    Function Name: void onCameraViewStarted
                        int width
                        int height

    Description:
        override function for openCV camera view

    Import:
        width, int, as required
        height, int, as required

    Export:
        log to show the camera has been started functioning

    Return:
        no return value
     */
    @Override
    public void onCameraViewStarted(int width, int height)
    {
        Log.i(TAG,"openCV voice camera started!");
    }

    /*
    Public function definitions

    Function Name: void onCameraViewStopped

    Description:
        override function for openCV camera view

    Import:
        no import

    Export:
        no export

    Return:
        no return value
     */
    @Override
    public void onCameraViewStopped()
    {
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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        return bcra.processFrame(inputFrame);
    }
}
