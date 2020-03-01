package com.example.fyp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class otherPhonesHandler extends Thread{
    private Socket socket;
    private InputStream in;
    private byte[] buffer;
    private BluetoothConnectionRobotApp bcra;
    private int terminateflag = 0;
    public otherPhonesHandler (Socket socket, BluetoothConnectionRobotApp bcra) {
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
                if (buffer == null) {
                    return;
                }
                bcra.blewriteCharacteristic_byte(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
