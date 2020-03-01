package com.example.fyp;


import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import android.widget.Toast;


import java.util.UUID;

public class BluetoothConnectionRobotApp extends Application {
    private BluetoothAdapter mBluetooth;
    BluetoothDevice device;
    BluetoothGatt mBluetoothGatt;
    UUID selectedserviceuuid;
    UUID selectedcharuuid;
    private String TAG = "BluetoothConnectionRobotApp";

    String serviceUuid[] = {
            "0000ffe0-0000-1000-8000-00805f9b34fb",
            "0000dfb0-0000-1000-8000-00805f9b34fb"
    };

    String characteristicsUuid[] = {
            "0000ffe1-0000-1000-8000-00805f9b34fb",
            "0000dfb1-0000-1000-8000-00805f9b34fb"
    };

    public synchronized void setBTConnectiondevice(BluetoothDevice d) {
        device = d;
    }
    public synchronized BluetoothDevice getBTConnection(){
        return device;
    }
    public synchronized void setBTConnection(BluetoothGatt gatt){
        mBluetoothGatt = gatt;
    }
    public synchronized void setServiceuuid(UUID serviceuuid){
        selectedserviceuuid = serviceuuid;
    }
    public synchronized void setCharuuid(UUID charuuid){
        selectedcharuuid = charuuid;
    }
    public BluetoothAdapter getmBluetooth() {
        return mBluetooth;
    }



    public String create_protocol_message(int button, int rockerformat, int rocker1, int rocker2, int rocker3, int rocker4) {
        String header = hextostring(0x55);
        String headerempty = hextostring(0xAA);
        String address = hextostring(0x11);
        String buttonquantity = hextostring(0x00);
        String rockerposition = hextostring(rockerformat);
        String b = hextostring(button);
        String r1 = hextostring(rocker1);
        String r2 = hextostring(rocker2);
        String r3 = hextostring(rocker3);
        String r4 = hextostring(rocker4);

        //add checksum
        int sum = 0x55 + 0xAA + 0x11 + 0x00 + button + rockerformat + rocker1 + rocker2 + rocker3 + rocker4;
        int checksum = (sum/256) + (sum % 256);


        String returnString = header + headerempty + address + buttonquantity + rockerposition + b + r1 + r2 + r3 + r4+ checksum;
        return returnString;
    }
    public String hextostring(int value) {
        String twohex = Integer.toHexString(value);
       if (value < 0x0f){
           return "0" + twohex;
       }

        return twohex;

    }
    public void sendtorobot(String message){
        boolean success = blewriteCharacteristic(message);
        Log.i(TAG, "message sent: "+ message);
        if (success){
            Log.i(TAG, "message successfully sent!");
            return;
        }
        Log.i(TAG, "message failed!");
    }


    public boolean blewriteCharacteristic(String msg){

        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        //BluetoothGattService Service = mBluetoothGatt.getService(selectedserviceuuid);
        BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString(serviceUuid[1]));
        if (Service == null) {
            Log.e(TAG, "service not found! Wtih service uuid = " + selectedserviceuuid.toString());
            return false;
        }
        //BluetoothGattCharacteristic charac = Service.getCharacteristic(selectedcharuuid);
        BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID.fromString(characteristicsUuid[1]));
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }

        //byte[] value = msg.getBytes();
        byte[] value = hexStringToByteArray(msg);
        Log.d(TAG, "value" + value);
        charac.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(charac);
        return status;
    }

    public boolean blewriteCharacteristic_byte(byte[] msg){

        //check mBluetoothGatt is available
        if (mBluetoothGatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        int i = 1;
        //BluetoothGattService Service = mBluetoothGatt.getService(selectedserviceuuid);
        BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString(serviceUuid[i]));
        if (Service == null){
            i = 2;
        }
        Service = mBluetoothGatt.getService(UUID.fromString(serviceUuid[i]));
        if (Service == null) {
            Log.e(TAG, "service not found! Wtih service uuid = " + selectedserviceuuid.toString());
            return false;
        }
        //BluetoothGattCharacteristic charac = Service.getCharacteristic(selectedcharuuid);
        BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID.fromString(characteristicsUuid[i]));
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }


        Log.d(TAG, "msg" + msg);
        charac.setValue(msg);
        boolean status = mBluetoothGatt.writeCharacteristic(charac);
        return status;
    }
//from stackoverflow
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public void connectToGattServer(BluetoothDevice device, UUID serviceuuid, UUID charuuid) {
        this.mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        this.selectedserviceuuid = serviceuuid;
        this.selectedcharuuid = charuuid;
        this.device = device;
        /*setBTConnectiondevice(device);
        setBTConnection(mBluetoothGatt);
        setServiceuuid(serviceuuid);
        setCharuuid(charuuid);*/

        setNotification(serviceuuid,charuuid);
        Log.d(TAG, "Service: " + serviceuuid);
        Log.d(TAG, "Char: " + charuuid);

    }
    private void setNotification(UUID sid,UUID cid){

        BluetoothGattCharacteristic characteristic;
        characteristic = mBluetoothGatt.getService(sid).getCharacteristic(cid);
        boolean enabled = true;
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        try{//BLE
            mBluetoothGatt.close();
        }catch (Exception exception){

        }
    }

    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt,
                                                    int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        mBluetoothGatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d(TAG, "Disconnected from GATT server.");
                    }
                }
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    for (BluetoothGattService service: gatt.getServices()) {
                        Log.d(TAG, "Service: " + service.getUuid());

                        for (BluetoothGattCharacteristic characteristic :
                                service.getCharacteristics()) {
                            Log.d(TAG, "Value: " + characteristic.getValue());
                            for (BluetoothGattDescriptor descriptor :
                                    characteristic.getDescriptors()) {
                                try{
                                    Log.d(TAG, descriptor.getValue().toString());
                                }catch (Exception e){ }

                            }
                        }
                    }

                }
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);

                }
            };
    /* only send msg to robot but not listening to its response
    private boolean mListening = false;
    private String listenForMessages(BluetoothSocket socket,
                                     StringBuilder incoming) {
        String result = "";
        mListening = true;

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        try {
            InputStream instream = socket.getInputStream();
            int bytesRead = -1;

            while (mListening) {
                bytesRead = instream.read(buffer);
                if (bytesRead != -1) {
                    while ((bytesRead == bufferSize) &&
                            (buffer[bufferSize-1] != 0)) {
                        result = result + new String(buffer, 0, bytesRead - 1);
                        bytesRead = instream.read(buffer);
                    }
                    result = result + new String(buffer, 0, bytesRead - 1);
                    incoming.append(result);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Message receive failed.", e);
        }
        return result;
    }*/
}
