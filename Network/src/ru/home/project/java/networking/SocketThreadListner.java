package ru.home.project.java.networking;

import java.net.Socket;

public interface SocketThreadListner {
    void socketThreadStart(SocketThread thread, Socket socket);
    void socketThreadStop(SocketThread thread);

    void socketThreadIsReady(SocketThread thread, Socket socket);
    void socketThreadException(SocketThread thread,Exception e);

    void socketPullMsg(SocketThread thread,Socket socket,String msg);
}
