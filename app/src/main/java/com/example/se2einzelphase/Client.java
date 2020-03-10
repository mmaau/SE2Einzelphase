package com.example.se2einzelphase;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    private Socket socket;
    private String receivedMsg;

    public void initClient(String ip, int port){
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip,port),10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(String msg){
        try {

            PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            pw.println(msg);
            pw.flush();
            // get message
            socket.setSoTimeout(10000);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            receivedMsg = br.readLine();
            // close connection
            pw.close();
            br.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getReceivedMsg() {
        return receivedMsg;
    }

}
