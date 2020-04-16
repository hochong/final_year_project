package com.makerlab.bt;

import android.app.Activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import com.makerlab.protocol.R;

public class BluetoothConnect {
    private BluetoothDevice mBluetoothDevice;
    // classic Bluetooth SPP
    private BluetoothSocket mBluetoothSocket = null;
    private OutputStream mOutputStream;

    private Activity mActivity;
    //
    private int mPrevChecksum = -1;
    private ConnectionHandler mConnectionHandler;
    private boolean mIsConnected = false;

    public BluetoothConnect(Activity activity) {
        mActivity = activity;
    }

    public void setConnectionHandler(ConnectionHandler mConnectionHandler) {
        this.mConnectionHandler = mConnectionHandler;
    }

    public void connectBluetooth(BluetoothDevice bluetoothDevice) {
        this.mBluetoothDevice = bluetoothDevice;
        BtSocketConnectAsyncTask btSocketConnectAsyncTask = new BtSocketConnectAsyncTask(mActivity, mBluetoothDevice);
        btSocketConnectAsyncTask.execute();
    }

    public void disconnectBluetooth() {
        //
        try {
            if (mBluetoothSocket != null) {
                if (mOutputStream != null) {
                    mOutputStream.close();
                }
                mBluetoothSocket.close();
            }
        } catch (IOException e) {
            Log.e("disconnectBluetooth", e.toString());
        } finally {
            mBluetoothSocket = null;
            mOutputStream = null;
        }
        //
        mPrevChecksum = -1;
        mIsConnected = false;
    }

    public String getDeviceAddress() {
        String addr = null;
        if (mBluetoothDevice != null) {
            addr = mBluetoothDevice.getAddress();
        }
        return addr;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public boolean send(byte[] payload) {
        if (payload == null || payload.length == 0) return false;

        if (mOutputStream != null) {
            try {
                mOutputStream.write(payload);
                mOutputStream.flush();
                return true;
            } catch (IOException e) {
                Log.e("send", e.toString());
                disconnectBluetooth();
            }
        } else {
/*            Toast.makeText(this,
                    getString(R.string.msg_sending_packet_fail),
                    Toast.LENGTH_LONG).show();*/
        }
        return false;
    }


    //  AsyncTask
    class BtSocketConnectAsyncTask extends AsyncTask<Void, Void, String> {
        private Activity activity;
        private BluetoothDevice bluetoothDevice;

        public BtSocketConnectAsyncTask(Activity activity, BluetoothDevice bluetoothDevice) {
            super();
            this.activity = activity;
            this.bluetoothDevice = bluetoothDevice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(activity,
                    activity.getString(R.string.msg_bt_connecting),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            mIsConnected = connectClassicBlueTooth(bluetoothDevice);
            if (mIsConnected) {
                if (mConnectionHandler != null) {
                    mConnectionHandler.onConnectionSuccess();
                }
            } else {
                if (mConnectionHandler != null) {
                    mConnectionHandler.onConnectionFail();
                }
            }
            return null;
        }

        protected void onPostExecute(String result) {
            // result is parameter passed from doInBackground()
        }

        private boolean connectClassicBlueTooth(BluetoothDevice bluetoothDevice) {
            boolean rc = true;
            try {
                ParcelUuid[] uuids = null;
                UUID SPPuuid;
                if (uuids == null || uuids.length == 0) {
                    SPPuuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                } else {
                    SPPuuid = uuids[0].getUuid();
                }
                mBluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(SPPuuid);
                mBluetoothSocket.connect();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e("connectClassicBlueTooth", e.toString());
                //e.printStackTrace();
                mOutputStream = null;
                mBluetoothSocket = null;
                rc = false;
            }
            return rc;
        }

    }//  AsyncTask

    public interface ConnectionHandler {
        void onConnectionSuccess();

        void onConnectionFail();
    }
}
