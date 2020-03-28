package com.example.fyp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.nfc.Tag;
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
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

public class VoiceControl extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "VoiceControl";
    //openCV camera
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean MIC_ON = false;
    private boolean AUTO_DETECT_VOICE = false;

    //camera
    //private Camera mCamera;
    //private CameraControlApp.CameraPreview mPreview;
    private BluetoothConnectionRobotApp bcra;
    //speech
    private SpeechRecognizer sr;
    private Intent srIntent;
    private String instruction = "forward, backward, left, right, stop";
    private int SpeechRecognizerInt = 3000;
    private ImageView mic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView mic = findViewById(R.id.micImg);
        FloatingActionButton fab = findViewById(R.id.fab);
        Switch auto_voice_detection = findViewById(R.id.auto_voice_detection);

        sr = SpeechRecognizer.createSpeechRecognizer(this);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Switch to Joystick Control", Snackbar.LENGTH_LONG)
                        .setAction("Go", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                toJoystickControl(v);
                            }
                        }).show();
            }
        });

        bcra = (BluetoothConnectionRobotApp) getApplication();

        //mic
        set_mic_image(MIC_ON);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MIC_ON = !MIC_ON;
                set_mic_image(MIC_ON);
                startListening();
            }

        });
        /*auto_voice_detection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                set_mic_image(isChecked);
                if (isChecked){
                    //TODO
                    //auto detect on
                    startListening();

                } else{
                    //TODO
                    //turn off
                }
            }
        });*/
        //openCV camera
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.voice_cameraView);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.enableView();
            Log.i(TAG,"Camera Start!");
        }

        /*camera
        CameraControlApp cca = new CameraControlApp();
        if (cca.checkCameraHardware(this)){
            mCamera = cca.getCameraInstance();
            mCamera.setDisplayOrientation(90);
            mPreview = new CameraControlApp.CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.voice_cameraView);
            preview.addView(mPreview);
        }*/
    }
    private void startListening () {
        srIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        srIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //srIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, instruction);
        sr.startListening(srIntent);
        startActivityForResult(srIntent, SpeechRecognizerInt);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SpeechRecognizerInt){
            if (resultCode==RESULT_OK){
                String command = "init";
                try {
                     command = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                    Log.i("VoiceControlActivity", "result = " + command);
                }catch(Exception e){ }
                switch (command.toUpperCase()){
                    case "FORWARD" : //TODO bcra.blewriteCharacteristic_byte(blist);
                        break;
                    case "BACKWARD" : //TODO bcra.blewriteCharacteristic_byte(blist);
                        break;
                    case "LEFT" : //TODO bcra.blewriteCharacteristic_byte(blist);
                        break;
                    case "RIGHT" : //TODO bcra.blewriteCharacteristic_byte(blist);
                        break;
                    case "STOP" : //TODO bcra.blewriteCharacteristic_byte(blist);
                        break;
                    default:
                        break;
                }
            }
            MIC_ON = !MIC_ON;
            set_mic_image(MIC_ON);
        }
    }

    public void toJoystickControl(View view) {
        Intent i = new Intent(this, JoystickControl.class);
        startActivity(i);
    }
    private void set_mic_image(boolean onoff) {
        if (onoff) {
            mic.setImageResource(R.drawable.ic_mic_on_press);
        } else{
            mic.setImageResource(R.drawable.ic_mic_on);
        }
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG,"openCV voice camera started!");
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return bcra.processFrame(inputFrame);
    }
}
