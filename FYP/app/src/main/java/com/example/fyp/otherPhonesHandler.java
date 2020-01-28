package com.example.fyp;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class otherPhonesHandler extends Thread{
    private Socket socket;
    private InputStream in;
    private byte[] buffer;
    private BluetoothConnectionRobotApp bcra;
    public otherPhonesHandler (Socket socket, BluetoothConnectionRobotApp bcra) {
        this.socket = socket;
        this.bcra = bcra;
    }

    public void run() {
        try{
            in = socket.getInputStream();

            while (true) {
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
