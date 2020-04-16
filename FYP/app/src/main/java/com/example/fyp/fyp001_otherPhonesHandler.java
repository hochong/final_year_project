package com.example.fyp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static com.example.fyp.fyp001_BluetoothConnectionRobotApp.mobile;
import static com.example.fyp.fyp001_BluetoothConnectionRobotApp.turret;

public class fyp001_otherPhonesHandler extends Thread{
    private Socket socket;                              /*thread*/
    private InputStream in;                             /*thread*/
    private byte[] buffer;                              /*thread*/

    private fyp001_BluetoothConnectionRobotApp bcra;    /*helper class*/

    private int terminate_flag = 0;                      /*terminate flag*/
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
    public fyp001_otherPhonesHandler(Socket socket, fyp001_BluetoothConnectionRobotApp bcra) {
        this.socket = socket;
        this.bcra = bcra;
    }
    /*
    Public function definitions

    Function Name: void setterminateflag
                        int i

    Description:
        set the terminate flag for the thread to check and terminate automatically

    Import:
        i, int, integer 1 = terminate other = alive

    Export:
        no export

    Return:
        no return value
     */
    public void setterminateflag(int i) {
        this.terminate_flag = i;
    }
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
        try{
            in = socket.getInputStream();

            while (true) {
                if (this.terminate_flag == 1){
                    interrupt();
                }
                in.read(buffer);
                switch(buffer[0]){
                    case 0:
                        if (mobile != null) {
                            mobile.sidewayUp();
                            if (turret != null){
                                turret.home();
                            }
                        }
                        break;
                    case 3:
                        if (mobile != null) {
                            mobile.sidewayLeft();
                            if (turret != null){
                                turret.panLeft();
                            }
                        }
                        break;
                    case 6:
                        if (mobile != null) {
                            mobile.sidewayRight();
                            if (turret != null){
                                turret.panRight();
                            }
                        }
                        break;
                    case 9:
                        if (mobile != null) {
                            mobile.sidewayDown();
                        }
                        break;
                    default:
                        if (mobile != null){
                            mobile.halt();
                            if (turret != null){
                                turret.home();
                            }
                        }
                        break;
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
