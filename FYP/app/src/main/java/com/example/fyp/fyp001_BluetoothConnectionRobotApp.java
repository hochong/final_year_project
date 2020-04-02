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
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

public class fyp001_BluetoothConnectionRobotApp extends Application {
    private BluetoothAdapter mBluetooth;
    BluetoothDevice device;
    BluetoothGatt mBluetoothGatt;
    UUID selectedserviceuuid;
    UUID selectedcharuuid;
    private String TAG = "BCRA";
    private Net net = null;
    String serviceUuid[] = {
            "0000ffe0-0000-1000-8000-00805f9b34fb",
            "0000dfb0-0000-1000-8000-00805f9b34fb"
    };

    String characteristicsUuid[] = {
            "0000ffe1-0000-1000-8000-00805f9b34fb",
            "0000dfb1-0000-1000-8000-00805f9b34fb"
    };
    private static final String[] classNames = {"background",
            "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};
    private static final byte[] forward_byte_array = {
            0x55, (byte)0xAA, 0x11, 0x01/*1 button pressed*/, 0x02,
            0x01, // it is button 1
            0x5E, // joystick y
            0x00, // joystick x
            0x00, // unuse
            0x00, // unuse
            0x7B // checksum
    };
    private static final byte[] backward_byte_array = {
            0x55, (byte)0xAA, 0x11, 0x01/*1 button pressed*/, 0x02,
            0x01, // it is button 1
            0x00, // joystick y
            0x00, // joystick x
            0x00, // unuse
            0x00, // unuse
            0x1C // checksum
    };
    private static final byte[] left_byte_array = {
            0x55, (byte)0xAA, 0x11, 0x01/*1 button pressed*/, 0x02,
            0x01, // it is button 1
            0x00, // joystick y
            0x00, // joystick x
            0x00, // unuse
            0x00, // unuse
            0x1C // checksum
    };
    private static final byte[] right_byte_array = {
            0x55, (byte)0xAA, 0x11, 0x01/*1 button pressed*/, 0x02,
            0x01, // it is button 1
            0x00, // joystick y
            0x5E, // joystick x
            0x00, // unuse
            0x00, // unuse
            0x7B // checksum
    };
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("BCRA", "OpenCV failed to load");
        }
        else {
            Log.i("BCRA", "OpenCV loaded successfully");
        }
    }
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
    public byte[] get_forward_byte_array() {return forward_byte_array;}
    public byte[] get_backward_byte_array() {return backward_byte_array;}
    public byte[] get_left_byte_array() {return left_byte_array;}
    public byte[] get_right_byte_array() {return right_byte_array;}


    public Boolean loadOpenCVNet(){

            String proto = getPath("MobileNetSSD_deploy.prototxt", this);
            String weights = getPath("MobileNetSSD_deploy.caffemodel", this);
            if (proto == "" || weights == "") {
                return false;
            }
            net = Dnn.readNetFromCaffe(proto, weights);
            Log.i(TAG, "Network loaded");
            return true;

    }

    public Mat processFrame (CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Boolean loadNet = false;
        if (net == null) {
            loadNet = loadOpenCVNet();
        } else{
            loadNet = true;
        }
        final int IN_WIDTH = 300;
        final int IN_HEIGHT = 300;
        //final float WH_RATIO = (float) IN_WIDTH/IN_HEIGHT;
        final double IN_SCALE_FACTOR = 0.007843;
        final double MEAN_VAL = 127.5;
        final double THRESHOLD = 0.1;

        //new frame
        Mat frame = inputFrame.rgba();
        Imgproc.cvtColor(frame,frame, Imgproc.COLOR_RGBA2RGB);
        if (!loadNet){
            return frame;
        }

        //forward image to network
        Mat blob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR,
                new Size(IN_WIDTH,IN_HEIGHT),
                new Scalar(MEAN_VAL,MEAN_VAL, MEAN_VAL),
                false, false);
        net.setInput(blob);
        Mat detections = net.forward();
        int cols = frame.cols();
        int rows = frame.rows();
        detections = detections.reshape(1, (int)detections.total()/7);
        for (int i = 0; i < detections.rows(); i++){
            double confidence = detections.get (i, 2)[0];
            if (confidence > THRESHOLD) {
                int classId = (int) detections.get(i, 1)[0];
                int left = (int)(detections.get (i,3)[0] * cols);
                int right = (int) (detections.get(i,4)[0] * rows);
                int top = (int) (detections.get(i,5)[0] * cols);
                int bottom = (int)(detections.get(i,6)[0] * rows);
                String label = classNames[classId] + ": "+confidence;
                /*
                //draw rect around dtected obj
                Imgproc.rectangle(frame,
                        new Point(left,top),
                        new Point(right,bottom),
                        new Scalar(0,255,0));
                int[] baseLine = new int[1];
                Size labelSize = Imgproc.getTextSize(label, Core.FONT_HERSHEY_SIMPLEX,0.5,1,baseLine);

                //draw background for label
                Imgproc.rectangle(frame, new Point(left, top - labelSize.height), new Point(left + labelSize.width, top+baseLine[0]),new Scalar(255,255,255), 1);

                //write class name and conf
                Imgproc.putText(frame,label, new Point(left,top), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0,0,0));
                */
                //output log
                Log.i(TAG,"Obj found! with conf: " + confidence + " item is: " + label);
                //TODO: avoid obj if it is in the center with conf > 0.5
                //TODO: rotate if person found with conf > 0.5
            }
        }

        return frame;
    }
    /*public String create_protocol_message(int button, int rockerformat, int rocker1, int rocker2, int rocker3, int rocker4) {
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
    }*/

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

    private static String getPath(String f, Context c){
        AssetManager am = c.getAssets();
        BufferedInputStream inputStream = null;
        try{
            inputStream = new BufferedInputStream(am.open(f));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();

            File outFile = new File(c.getFilesDir(),f);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();

            return outFile.getAbsolutePath();
        }catch (Exception e) {
            Log.i("BCRA", "Failed to getpath, read and write " + e);
        }
        return "";
    }
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
