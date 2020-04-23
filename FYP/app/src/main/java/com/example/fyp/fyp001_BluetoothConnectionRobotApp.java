package com.example.fyp;


import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


import com.makerlab.bt.BluetoothConnect;
import com.makerlab.protocol.Mobile;
import com.makerlab.protocol.Turret;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class fyp001_BluetoothConnectionRobotApp extends Application {
    public static Turret turret = null;                                   /*turret/robot arm*/
    public static Mobile mobile = null;                                   /*mobile*/
    public static BluetoothConnect mBluetoothTurretConnect;               /*bluetooth connection*/
    public static BluetoothConnect mBluetoothMobileConnect;               /*bluetooth connection*/
    public static BluetoothDevice mBluetoothTurretDevice = null;          /*bluetooth connection*/
    public static BluetoothDevice mBluetoothMobileDevice = null;          /*bluetooth connection*/

    private Timer mobile_timer = null;                                    /*timer to regularly send instruction to robot*/
    private Timer turret_timer = null;                                    /*timer to regularly send instruction to robot arm*/
    static byte MOBILE_MOVEMENT = 0;                                      /*byte to hold the next instruction going to be sent to robot*/
    static byte TURRET_MOVEMENT = 0;                                      /*byte to hold the next instruction going to be sent to robot arm*/
    int MOBILE_DELAY = 1000;                                              /*delay for robot timer*/
    int MOBILE_INTERVAL = 1000;                                           /*interval for robot timer*/
    int TURRET_DELAY = 1000;                                              /*delay for robot arm*/
    int TURRET_INTERVAL = 1000;                                           /*interval for robot arm*/

    private static boolean obs_at_center = false;                         /*check if obstacle at center*/

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

    Function Name: void get_obs_at_center

    Description:
        access the boolean obs_at_center

    Import:
        no import

    Export:
        no export

    Return:
        obs_at_center, if there are obstacle at center determine by the process frame function
    */
    public static boolean get_obs_at_center() {
        return obs_at_center;
    }

    /*
    Public function definitions

    Function Name: void set_turret_device

    Description:
        bind the BLE device to the a application long variable

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    public synchronized static void set_turret_device(BluetoothDevice device){
        mBluetoothTurretDevice = device;
    }

    /*
    Public function definitions

    Function Name: void set_mobile_device

    Description:
        bind the BLE device to the a application long variable

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    public synchronized static void set_mobile_device(BluetoothDevice device){
        mBluetoothMobileDevice = device;
    }

    /*
    Public function definitions

    Function Name: void set_mobile_movement

    Description:
        set the next instruction for the mobile timer to send
        use as set_mobile_movement(Mobile.HALT);

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    public synchronized static void set_mobile_movement(byte i){
        MOBILE_MOVEMENT = i;
    }

    /*
    Public function definitions

    Function Name: void set_turret_movement

    Description:
        set the next instruction for the turret timer to send
        use as set_turret_movement(Turret.HALT);

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    public synchronized static void set_turret_movement(byte i){
        TURRET_MOVEMENT = i;
    }

    /*
    Public function definitions

    Function Name: void start_mobile_timer_task

    Description:
        start running the timer task for robot
        will not start running if there is already a existing one

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    public synchronized void start_mobile_timer_task(){
        if (mobile_timer != null){
            return;
        }
        mobile_timer = new Timer();
        mobile_timer.scheduleAtFixedRate(new fyp001_mobileHandler(), MOBILE_DELAY, MOBILE_INTERVAL);
    }

    /*
    Public function definitions

    Function Name: void start_turret_timer_task

    Description:
        start running the timer task for robot arm
        will not start running if there is already a existing one

    Import:
        no import

    Export:
        no export

    Return:
        no return
    */
    public synchronized void start_turret_timer_task(){
        if (turret_timer != null){
            return;
        }
        turret_timer = new Timer();
        turret_timer.scheduleAtFixedRate(new fyp001_turretHandler(), TURRET_DELAY, TURRET_INTERVAL);

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

    Function Name: Boolean loadOpenCVNet

    Description:
        load proto and weights for openCV
        construct the net for the network

    Import:
        no import

    Export:
        no export

    Return:
        return true if successful and else false
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
        boolean obs_at_center_local = false;
        for (int i = 0; i < detections.rows(); i++){
            double confidence = detections.get (i, 2)[0];

            if (confidence > THRESHOLD) {
                int classId = (int) detections.get(i, 1)[0];
                int min_x = (int)(detections.get (i,3)[0] * cols);
                int max_y = (int) (detections.get(i,4)[0] * rows);
                int max_x = (int) (detections.get(i,5)[0] * cols);
                int min_y = (int)(detections.get(i,6)[0] * rows);

                String label = classNames[classId] + ": "+confidence;
                /*
                int left = min_x;
                int top = max_y;
                int right = max_x;
                int bottom = min_y;

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
                Log.i(TAG,"Obj found! with conf: " + confidence + " item is: " + label+"x x y y: " + min_x +" "+ max_x +" "+ min_y +" "+ max_y);
                //if collide and conf > 0.6, assume obstacle at center
                if ((!(centerbox_max_x < min_x) || !(max_x < centerbox_min_x) || !(centerbox_max_y < min_y) || !(max_y < centerbox_min_y)) &&
                        confidence > 0.6){
                    Log.i(TAG,"object at center!");
                    obs_at_center_local = true;
                }
            }
        }
        obs_at_center = obs_at_center_local;
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
            Log.i("BCRA", "Failed to get path" + e);
        }
        return "";
    }

    public class fyp001_mobileHandler extends TimerTask {
        /*
        Public function definitions

        Function Name: void run

        Description:
            instruct the robot at regular time interval

        Import:
            no import

        Export:
            no export

        Return:
            no return
        */
        @Override
        public void run(){
            if (mBluetoothMobileDevice != null) {
                Log.e("Mobile", String.valueOf(MOBILE_MOVEMENT));
                switch (MOBILE_MOVEMENT){
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
                    case Mobile.DIAG_UP_LEFT:
                        mobile.diagonalUpLeft();
                        break;
                    case Mobile.DIAG_UP_RIGHT:
                        mobile.diagonalUpRight();
                        break;
                    case Mobile.DIAG_DOWN_LEFT:
                        mobile.diagonalDownLeft();
                        break;
                    case Mobile.DIAG_DOWN_RIGHT:
                        mobile.diagonalDownRight();
                        break;
                    default:
                        mobile.halt();
                        break;
                }
            }
        }
    }

    public class fyp001_turretHandler extends TimerTask {
        /*
        Public function definitions

        Function Name: void run

        Description:
            instruct the robot arm at regular time interval

        Import:
            no import

        Export:
            no export

        Return:
            no return
        */
        @Override
        public void run(){
            if (mBluetoothTurretDevice != null) {
                Log.e("Turret", String.valueOf(TURRET_MOVEMENT));
                switch(TURRET_MOVEMENT){
                    case Turret.UP:
                        turret.tiltUp();
                        break;
                    case Turret.LEFT:
                        turret.panLeft();
                        break;
                    case Turret.RIGHT:
                        turret.panRight();
                        break;
                    case Turret.DOWN:
                        turret.tiltDown();
                        break;
                    case Turret.HOME:
                        turret.home();
                        break;
                    default:
                        turret.halt();
                        break;
                }
            }
        }
    }

    /*
    Public function definitions

    Function Name: String getIPAddress
                    boolean useIPv4

    Description:
        function from https://stackoverflow.com/questions/17355365/convert-socket-to-string-and-vice-versa
        return ipv4 or ipv6 address under request

    Import:
        useIPv4, boolean, return IPv4 or return Ipv6

    Export:
        no export

    Return:
        IP address or empty string if not connected
    */
    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }
}
