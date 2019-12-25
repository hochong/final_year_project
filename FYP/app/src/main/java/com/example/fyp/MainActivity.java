package com.example.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startControl(View view) {
        Intent i = new Intent(this, JoystickControl.class);
        startActivity(i);
    }

    public void bluetoothConnection(View view) {
        Intent i = new Intent(this, ConnectRobot.class);
        startActivity(i);
        /**
         * startActivityForResult(i, ROBOT_CONNECTION);
         * */
    }

    /**
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Test for the right intent reply.
        if (requestCode == ROBOT_CONNECTION) {
            // Test to make sure the intent reply result was good.
            if (resultCode == RESULT_OK) {
                String reply = data.getStringExtra(ConnectRobot.EXTRA_REPLY);


            }
        }
    }
    */
}
