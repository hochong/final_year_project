package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

public class VoiceControl extends AppCompatActivity {
    private boolean MIC_ON = false;
    private boolean AUTO_DETECT_VOICE = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_control);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView mic = findViewById(R.id.mic);
        FloatingActionButton fab = findViewById(R.id.fab);
        Switch auto_voice_detection = findViewById(R.id.auto_voice_detection);

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


        set_mic_image(MIC_ON);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MIC_ON = !MIC_ON;
                set_mic_image(MIC_ON);
            }
        });
        auto_voice_detection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                set_mic_image(isChecked);
                if (isChecked){
                    //TODO
                    //auto detect on
                } else{
                    //TODO
                    //turn off
                }
            }
        });
    }

    public void toJoystickControl(View view) {
        Intent i = new Intent(this, JoystickControl.class);
        startActivity(i);
    }
    private void set_mic_image(boolean onoff) {
        ImageView mic = findViewById(R.id.mic);
        if (MIC_ON) {
            mic.setImageResource(R.drawable.ic_mic_on_press);
        } else{
            mic.setImageResource(R.drawable.ic_mic_on);
        }
    }
}
