package com.makerlab.ui;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.makerlab.bt.BluetoothConnect;
import com.makerlab.protocol.Mobile;
import com.makerlab.protocol.Turret;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        BluetoothConnect.ConnectionHandler {
    static public final int REQUEST_BT_GET_DEVICE = 1;

    //
    private Button startStopButton;
    private Button turretBluetoothButton;
    private Button mobileBluetoothButton;
    private TextView turretBluetoothAddr;
    private TextView mobileBluetoothAddr;
    //
    private BluetoothConnect mBluetoothTurretConnect;
    private BluetoothConnect mBluetoothMobileConnect;
    //
    private Timer timerTurret = null;
    private Timer timerMobile = null;
    private Turret turret;
    private Mobile mobile;

    //
    private int mPressedBluetoothScanButtonId = -1;
//    String turretBtAddress = "D4:36:39:C2:5D:74";
//    String mobileBtAddress = "D4:36:39:C6:F5:60";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        turretBluetoothButton = findViewById(R.id.buttonTurretBluetooth);
        turretBluetoothButton.setOnClickListener(this);
        mobileBluetoothButton = findViewById(R.id.buttonMobileBluetooth);
        mobileBluetoothButton.setOnClickListener(this);
        startStopButton = findViewById(R.id.buttonControl);
        startStopButton.setOnClickListener(this);
        mobileBluetoothAddr = findViewById(R.id.textViewMobileBtAddr);
        turretBluetoothAddr = findViewById(R.id.textViewTurretBtAddr);
        //
        mBluetoothMobileConnect = new BluetoothConnect(this);
        mBluetoothMobileConnect.setConnectionHandler(this);
        //
        mBluetoothTurretConnect = new BluetoothConnect(this);
        mBluetoothTurretConnect.setConnectionHandler(this);
        //
        turret = new Turret(mBluetoothTurretConnect);
        mobile = new Mobile(mBluetoothMobileConnect);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.runFinalization();
        if (timerTurret != null)
            timerTurret.cancel();
        if (timerMobile != null)
            timerMobile.cancel();
        timerTurret = timerMobile = null;
        mBluetoothTurretConnect.disconnectBluetooth();
        mBluetoothMobileConnect.disconnectBluetooth();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.buttonTurretBluetooth || view.getId() == R.id.buttonMobileBluetooth) {
            if (startStopButton.getText().equals("stop")) return;
            //
            mPressedBluetoothScanButtonId = view.getId();
            if (mPressedBluetoothScanButtonId == R.id.buttonMobileBluetooth && mBluetoothMobileConnect.isConnected()) {
                mBluetoothMobileConnect.disconnectBluetooth();
                mobileBluetoothAddr.setText("ff:ff:ff:ff:ff:ff");
                if (!mBluetoothTurretConnect.isConnected()) {
                    startStopButton.setText("start");
                    startStopButton.setEnabled(false);
                }
                return;
            } else if (mPressedBluetoothScanButtonId == R.id.buttonTurretBluetooth && mBluetoothTurretConnect.isConnected()) {
                mBluetoothTurretConnect.disconnectBluetooth();
                turretBluetoothAddr.setText("ff:ff:ff:ff:ff:ff");
                if (!mBluetoothMobileConnect.isConnected()) {
                    startStopButton.setText("start");
                    startStopButton.setEnabled(false);
                }
                return;
            }
            //
            Intent intent = new Intent(this, BluetoothDevListActivity.class);
            startActivityForResult(intent, REQUEST_BT_GET_DEVICE);

        } else if (view.getId() == R.id.buttonControl) {
            if (startStopButton.getText().equals("start")) {
                boolean started = false;
                // do motions
                if (mBluetoothTurretConnect.isConnected()) {
                    timerTurret = new Timer();
                    timerTurret.scheduleAtFixedRate(new TurretMovementTimerTask(), 1000, 1000);
                    started = true;
                }
//                if (mBluetoothMobileConnect.isConnected()) {
//                    timerMobile = new Timer();
//                    timerMobile.scheduleAtFixedRate(new MobileMovementTimerTask(), 1000, 1000);
//                    started = true;
//                }


                if (started) {
                    startStopButton.setText("stop");
                }
            } else {
                startStopButton.setEnabled(false);
                Thread th = new Thread() {
                    public void run() {
                        // stop motions
                        if (timerMobile != null) {
                            timerMobile.cancel();
                            timerMobile = null;
                        }
                        if (timerTurret != null) {
                            timerTurret.cancel();
                            timerTurret = null;
                        }
                        turret.halt();
                        mobile.halt();

                        runOnUiThread(new Thread() {
                            public void run() {
                                startStopButton.setText("start");
                                startStopButton.setEnabled(true);
                            }
                        });
                    }
                };
                th.start();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (requestCode == REQUEST_BT_GET_DEVICE) {
            if (resultCode == RESULT_OK) {
                final BluetoothDevice mBluetoothDevice;
                mBluetoothDevice = resultIntent.getParcelableExtra(BluetoothDevListActivity.EXTRA_KEY_DEVICE);
                if (mBluetoothDevice == null) {
                    //Toast.makeText(this, getString(R.string.msg_bt_not_found), Toast.LENGTH_LONG).show();
                    return;
                }
                //
                if (mPressedBluetoothScanButtonId == R.id.buttonMobileBluetooth) {
                    if (mBluetoothMobileConnect.isConnected()) {
                        mBluetoothMobileConnect.disconnectBluetooth();
                    }
                    mBluetoothMobileConnect.connectBluetooth(mBluetoothDevice);
                } else if (mPressedBluetoothScanButtonId == R.id.buttonTurretBluetooth) {
                    if (mBluetoothTurretConnect.isConnected()) {
                        mBluetoothTurretConnect.disconnectBluetooth();
                    }
                    mBluetoothTurretConnect.connectBluetooth(mBluetoothDevice);
                }
                //
                runOnUiThread(new Thread() {
                    public void run() {
                        if (mPressedBluetoothScanButtonId == R.id.buttonMobileBluetooth) {
                            mobileBluetoothAddr.setText(mBluetoothDevice.getAddress());
                        } else if (mPressedBluetoothScanButtonId == R.id.buttonTurretBluetooth) {
                            turretBluetoothAddr.setText(mBluetoothDevice.getAddress());
                        }
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
                //mBluetoothGatt = null;
            }
        }
    }

    // connection handler
    public void onConnect() {
        Toast.makeText(this,
                getString(R.string.msg_bt_connecting),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuccess() {
        final Activity activity = this;
        final String msg = getString(R.string.msg_bt_connected);

        runOnUiThread(new Thread() {
            public void run() {
//                if (mBluetoothTurretConnect.isConnected()
//                        && mBluetoothMobileConnect.isConnected()) {
//                    startStopButton.setEnabled(true);
//                    turretBluetoothButton.setEnabled(true);
//                }
                startStopButton.setEnabled(true);
                turretBluetoothButton.setEnabled(true);
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onConnectionFail() {
        final Activity activity = this;
        final String msg = getString(R.string.msg_bt_not_connected);
        runOnUiThread(new Thread() {
            public void run() {
                if (!mBluetoothTurretConnect.isConnected()
                        || !mBluetoothMobileConnect.isConnected()) {
                    startStopButton.setEnabled(false);
                    turretBluetoothButton.setEnabled(true);
                }
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
                if (mPressedBluetoothScanButtonId == R.id.buttonMobileBluetooth) {
                    mobileBluetoothAddr.setText("ff:ff:ff:ff:ff:ff");
                } else if (mPressedBluetoothScanButtonId == R.id.buttonTurretBluetooth) {
                    turretBluetoothAddr.setText("ff:ff:ff:ff:ff:ff");
                }
            }
        });
    }

    //
    class TurretMovementTimerTask extends TimerTask {
        final int actions[] = {
                Turret.LEFT, Turret.LEFT, Turret.HALT,
                Turret.RIGHT, Turret.RIGHT, Turret.HALT,
                Turret.RIGHT, Turret.RIGHT, Turret.HALT,
                Turret.LEFT, Turret.LEFT, Turret.HALT,
                Turret.UP, Turret.HALT,
                Turret.DOWN, Turret.HALT,
                Turret.DOWN, Turret.HALT,
                Turret.UP, Turret.HALT,
                Turret.HOME, Turret.HALT};
        int action;
        int index = 0;

        @Override
        public void run() {
            action = actions[index];
            switch (action) {
                case Turret.LEFT:
                    turret.panLeft();
                    break;
                case Turret.RIGHT:
                    turret.panRight();
                    break;
                case Turret.UP:
                    turret.tiltUp();
                    break;
                case Turret.DOWN:
                    turret.tiltDown();
                    break;
                case Turret.HOME:
                    turret.home();
                    break;
                default:
                    turret.halt();
            }
            Log.e("TurretMovementTimerTask", "action " + String.valueOf(actions[index]));
            index = (index + 1) % actions.length;
        }
    }

    class MobileMovementTimerTask extends TimerTask {
        final int actions[] = {
                Mobile.SIDEWAY_UP, Mobile.HALT, Mobile.SIDEWAY_DOWN, Mobile.HALT,
                Mobile.SIDEWAY_LEFT, Mobile.HALT, Mobile.SIDEWAY_RIGHT, Mobile.HALT,
                Mobile.TURN_LEFT, Mobile.HALT, Mobile.TURN_RIGHT, Mobile.HALT,
                Mobile.DIAG_UP_RIGHT, Mobile.HALT, Mobile.DIAG_DOWN_LEFT, Mobile.HALT,
                Mobile.DIAG_UP_LEFT, Mobile.HALT, Mobile.DIAG_DOWN_RIGHT, Mobile.HALT,
        };
        int action = 0, index = 0;

        @Override
        public void run() {
//            if (index >= actions.length) {
//                mobile.halt();
//                return;
//            }
            action = actions[index];
            switch (action) {
                case Mobile.SIDEWAY_UP:
                    mobile.sidewayUp();
                    break;
                case Mobile.SIDEWAY_DOWN:
                    mobile.sidewayDown();
                    break;
                case Mobile.SIDEWAY_LEFT:
                    mobile.sidewayLeft();
                    break;
                case Mobile.SIDEWAY_RIGHT:
                    mobile.sidewayRight();
                    break;
                case Mobile.TURN_LEFT:
                    mobile.turnLeft();
                    break;
                case Mobile.TURN_RIGHT:
                    mobile.turnRight();
                    break;
                case Mobile.DIAG_UP_RIGHT:
                    mobile.diagonalUpRight();
                    break;
                case Mobile.DIAG_DOWN_RIGHT:
                    mobile.diagonalDownRight();
                    break;
                case Mobile.DIAG_UP_LEFT:
                    mobile.diagonalUpRight();
                    break;
                case Mobile.DIAG_DOWN_LEFT:
                    mobile.diagonalDownRight();
                    break;
                default:
                    mobile.halt();
            }
            Log.e("MobileMovementTimerTask", "action " + String.valueOf(actions[index]));
            //index++;
            index = (index + 1) % actions.length;
        }
    }

}
