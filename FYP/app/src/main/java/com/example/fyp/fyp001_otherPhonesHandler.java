package com.example.fyp;

import android.os.HandlerThread;
import android.os.StrictMode;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class fyp001_otherPhonesHandler extends Thread {
    private ServerSocket socket;                        /*thread*/
    private InputStream in;                             /*thread*/

    private final byte MOBILE = 0;                      /*flag to determine the control type*/
    private final byte TURRET = 1;                      /*flag to determine the control type*/

    private fyp001_BluetoothConnectionRobotApp bcra;    /*helper class*/

    private int terminate_flag = 0;                     /*terminate flag*/
    private Socket s;                                   /*thread*/
    private int port;                                   /*thread*/
    private HandlerThread ht;                           /*thread*/

    /*
    Public function definitions

    Function Name: void fyp001_otherPhonesHandler
                        socket socket
                        fyp001_BluetoothConnectionRobotApp bcra

    Description:
        Constructor of the class

    Import:
        socket, Socket, socket of the client
        bcra, fyp001_BluetoothConnectionRobotApp, application long helper class, used to access different utlilty functions

    Export:
        no export

    Return:
        no return value
     */
    public fyp001_otherPhonesHandler(int p, final fyp001_BluetoothConnectionRobotApp bcra) {

        this.port = p;
        this.bcra = bcra;

        ht = new HandlerThread("otherPhoneHandler"){
            private byte[] buffer = {0, 0, 0, 0};                              /*1, byte_mobile_or_turret, byte_instruction, 1*/
            /*
            Public function definitions

            Function Name: void run

            Description:
                accept the byte coming from client and communicate with the mobile/turret

            Import:

            Export:
                no export

            Return:
                no return value
             */
            public void run() {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

                StrictMode.setThreadPolicy(policy);
                try {
                    socket = new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try{
                    s = socket.accept();
                    in = s.getInputStream();
                    while (in != null) {
                        if (terminate_flag == 1){
                            interrupt();
                        }
                        in.read(buffer, 0 , buffer.length);
                        Log.e("otherphoneMsg", "msg: "+buffer[1]+ buffer[2]);
                        if (buffer != null){
                            switch(buffer[1]){
                                case MOBILE:
                                    bcra.set_mobile_movement(buffer[2]);
                                    break;
                                case TURRET:
                                    bcra.set_turret_movement(buffer[2]);
                                    break;
                                default:
                                    break;
                            }
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        ht.start();
    }
    /*
    Public function definitions

    Function Name: void run

    Description:
        close the thread when terminate

    Import:

    Export:
        no export

    Return:
        no return value
     */
    public void closethread(){
        ht.quitSafely();
    }

}
