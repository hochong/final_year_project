package com.example.fyp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class fyp001_otherPhonesHandler extends Thread{
    private Socket socket;
    private InputStream in;
    private byte[] buffer;
    private fyp001_BluetoothConnectionRobotApp bcra;
    private int terminateflag = 0;
    public fyp001_otherPhonesHandler(Socket socket, fyp001_BluetoothConnectionRobotApp bcra) {
        this.socket = socket;
        this.bcra = bcra;
    }
    public void setterminateflag(int i) {
        this.terminateflag = i;
    }
    public void run() {
        try{
            in = socket.getInputStream();

            while (true) {
                if (this.terminateflag == 1){
                    interrupt();
                }
                in.read(buffer);
                switch(buffer[0]){
                    case 0: bcra.blewriteCharacteristic_byte(bcra.get_forward_byte_array());
                        break;
                    case 3: bcra.blewriteCharacteristic_byte(bcra.get_right_byte_array());
                        break;
                    case 6: bcra.blewriteCharacteristic_byte(bcra.get_backward_byte_array());
                        break;
                    case 9: bcra.blewriteCharacteristic_byte(bcra.get_left_byte_array());
                        break;
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
