package ru.home.project.java.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ServerSocketThread extends Thread{
    private final int port;
    private final int timeout;
    private ServerSocketThreadListner serverSocketThreadListner;

    public ServerSocketThread(ServerSocketThreadListner listner,String name, int port, int timeout) {
        super(name);
        this.serverSocketThreadListner = listner;
        this.port = port;
        this.timeout = timeout;
        start();
    }

    @Override
    public void run() {
        serverSocketThreadListner.serverThreadStart(this);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocketThreadListner.serverSocketCreated(this, serverSocket);
            serverSocket.setSoTimeout(timeout);
            while (!isInterrupted()) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                }catch (SocketTimeoutException e){
                    serverSocketThreadListner.socketTimeout(this,serverSocket);
                    continue;
                }
                serverSocketThreadListner.socketAccept(this,socket);
            }
        }catch (IOException e){
            serverSocketThreadListner.serverThreadException(this,e);
        }

        serverSocketThreadListner.serverThreadStop(this);
    }
}
