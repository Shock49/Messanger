package ru.home.project.java.server.core;

import ru.home.project.java.Messages;
import ru.home.project.java.networking.SocketThread;
import ru.home.project.java.networking.SocketThreadListner;

import java.net.Socket;

public class ClientThread extends SocketThread {

    private String nicname;
    private boolean isAuthorized;
    private boolean isReconnecting;

    public ClientThread(SocketThreadListner listner, String name, Socket socket) {
        super(listner, name, socket);
    }

    public String getNicname() {
        return nicname;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }
    public boolean isReconnecting(){return isReconnecting;}

    public void reconnect(){
        isReconnecting = true;
        close();
    }

    void authorizeAccept(String nicname){
        isAuthorized = true;
        this.nicname = nicname;
        sendMsg(Messages.getAUTH_ACCEPT(nicname));
    }

    void authorizeError(){
        sendMsg(Messages.getAuthError());
        close();
    }

    void msgFormatError(String msg){
        sendMsg(Messages.getMsgFormatError(msg));
        close();
    }
}
