package ru.home.project.java.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketThread extends  Thread{

    private SocketThreadListner listner;
    private Socket socket;
    private DataOutputStream out;

    public SocketThread(SocketThreadListner listner,String name, Socket socket){
        super(name);
        this.socket = socket;
        this.listner = listner;
        start();
    }

    @Override
    public void run() {
        listner.socketThreadStart(this,socket);
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            listner.socketThreadIsReady(this,socket);
            while (!isInterrupted()){
                String msg = in.readUTF();
                listner.socketPullMsg(this,socket,msg);
            }
        } catch (IOException e) {
            listner.socketThreadException(this,e);
        }finally {
            try {
                socket.close();
            } catch (IOException e) {
                listner.socketThreadException(this,e);
            }
            listner.socketThreadStop(this);
        }
    }

    public synchronized boolean sendMsg(String msg){
        try {
            out.writeUTF(msg);
            return true;
        }catch (IOException e){
            listner.socketThreadException(this,e);
            close();
            return false;
        }
    }

    public void close(){
        interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            listner.socketThreadException(this,e);
        }
    }
}
