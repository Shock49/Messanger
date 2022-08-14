package ru.home.project.java.networking;

import java.net.ServerSocket;
import java.net.Socket;

public interface ServerSocketThreadListner {
    void serverThreadStart(ServerSocketThread thread);
    void serverSocketCreated(ServerSocketThread thread, ServerSocket serverSocket);

    void socketAccept(ServerSocketThread thread, Socket socket);
    void socketTimeout(ServerSocketThread thread, ServerSocket serverSocket);

    void serverThreadException(ServerSocketThread thread, Exception e);
    void serverThreadStop(ServerSocketThread thread);

}
