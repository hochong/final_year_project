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


import com.makerlab.bt.BluetoothConnect;
import com.makerlab.protocol.Mobile;
import com.makerlab.protocol.Turret;

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
    public static Turret turret = null;                                   /*turret*/
    public static Mobile mobile = null;                                   /*mobile*/
    public static BluetoothConnect mBluetoothTurretConnect;               /*bluetooth connection*/
    public static BluetoothConnect mBluetoothMobileConnect;               /*bluetooth connection*/

    private String TAG = "BCRA";                                          /*tag for log purpose*/
    private Net net = null;                                               /*openCv network*/

    private static final String[] classNames = {"background",             /*class names for the network*/
            "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair",
            "cow", "diningtable", "dog", "horse",
            "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"};

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.i("BCRA", "OpenCV failed to load");
        }
        else {
            Log.i("BCRA", "OpenCV loaded successfully");
        }
    }

    /*
    Public function definitions

    Function Name: void create_new_mobile
                    BluetoothConnect bluetoothConnect

    Description:
        create new mobile

    Import:
        bluetoothConnect, BluetoothConnect, connection that is already connected to a device

    Export:
        no export

    Return:
        no return
    */
    public synchronized static void create_new_mobile(BluetoothConnect  bluetoothConnect){
        mobile = new Mobile(bluetoothConnect);
    }
    /*
    Public function definitions

    Function Name: void create_new_turret
                    BluetoothConnect bluetoothConnect

    Description:
        create new turret

    Import:
        bluetoothConnect, BluetoothConnect, connection that is already connected to a device

    Export:
        no export

    Return:
        no return
    */
    public synchronized static void create_new_turret(BluetoothConnect  bluetoothConnect){
        turret = new Turret(bluetoothConnect);
    }
    /*
    Public function definitions

    Function Name: void get_mobile

    Description:
        get mobile from helper class

    Import:
        no import

    Export:
        no export

    Return:
        return mobile
    */
    public synchronized static Mobile get_mobile(){
        return mobile;
    }
    /*
    Public function definitions

    Function Name: void get_turret

    Description:
        get turret from helper class

    Import:
        no import

    Export:
        no export

    Return:
        return turret
    */
    public synchronized static Turret get_turret(){
        return turret;
    }

    /*
    Public function definitions

    Function Name: void loadOpenCVNet

    Description:
        load proto and weights for openCV
        construct the net for the network

    Import:
        no import

    Export:
        no export

    Return:
        return turret
    */
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

    /*
    Public function definitions

    Function Name: mat processFrame
                    CvCameraViewFrame inputFrame

    Description:
        modify function from https://docs.opencv.org/3.4/d0/d6c/tutorial_dnn_android.html
        analyse the frame
        identify the person and obstacles

    Import:
        input Frame, CvCameraViewFrame, the frame captured by the camera in each sec

    Export:
        no export

    Return:
        return frame
    */
    public Mat processFrame (CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Boolean loadNet = false;
        if (net == null) {
            loadNet = loadOpenCVNet();
        } else{
            loadNet = true;
        }
        final int IN_WIDTH = 300;
        final int IN_HEIGHT = 300;
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

        //create a box for axis aligned bounding box
        int centerbox_min_x = (int) (cols * 0.4);
        int centerbox_max_x = (int) (cols * 0.6);
        int centerbox_min_y = (int) (rows);
        int centerbox_max_y = (int) (rows * 0.3);
        detections = detections.reshape(1, (int)detections.total()/7);
        Boolean obs_at_center = false;
        Boolean person = false;
        for (int i = 0; i < detections.rows(); i++){
            double confidence = detections.get (i, 2)[0];

            if (confidence > THRESHOLD) {
                int classId = (int) detections.get(i, 1)[0];
                int min_x = (int)(detections.get (i,3)[0] * cols);
                int max_y = (int) (detections.get(i,4)[0] * rows);
                int max_x = (int) (detections.get(i,5)[0] * cols);
                int min_y = (int)(detections.get(i,6)[0] * rows);
                int left = min_x;
                int top = max_y;
                int right = max_x;
                int bottom = min_y;
                String label = classNames[classId] + ": "+confidence;
                /*
                //draw rect around detected obj
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
                //if collide and conf > 0.6, assume obstacle at center
                if (!(centerbox_max_x<min_x || max_x<centerbox_min_x || centerbox_max_y<min_y || max_y<centerbox_min_y) &&
                        confidence > 0.6){
                    obs_at_center = true;
                }
                if (classId == 15 && confidence > 0.6){
                    person = true;
                }

            }
        }
        //stops the robot if it is at the center
        if (obs_at_center){
            mobile.halt();
        }
        //look up to see the face if there is a person
        if (person) {
            turret.tiltUp();
        }
        return frame;
    }


    /*
    Public function definitions

    Function Name: void onTerminate

    Description:
        close connections

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mBluetoothMobileConnect != null){
            mBluetoothMobileConnect.disconnectBluetooth();
        }
        if (mBluetoothTurretConnect != null){
            mBluetoothTurretConnect.disconnectBluetooth();
        }
    }

    /*
    Public function definitions

    Function Name: String getPath
                    String f
                    Context c

    Description:
        get model and config files downloaded from https://github.com/chuanqi305/MobileNet-SSD
        and store in assets folder
        get the file from the string name
        and return the full path

    Import:
        f, string, the naem of the file
        c, Context, object

    Export:
        no export

    Return:
        return the full path of the file
    */
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

}
