package ru.home.project.java.server.core;

import ru.home.project.java.Messages;
import ru.home.project.java.networking.ServerSocketThread;
import ru.home.project.java.networking.ServerSocketThreadListner;
import ru.home.project.java.networking.SocketThread;
import ru.home.project.java.networking.SocketThreadListner;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListner, SocketThreadListner {
    private ServerSocketThread serverSocketThread;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private Vector<SocketThread> client = new Vector<>();
    private final ChatServerListner listner;

    public ChatServer(ChatServerListner listner) {
        this.listner = listner;
    }

    public void start(int port) {
        System.out.println("Server try started on port: " + port);
        if (serverSocketThread != null && serverSocketThread.isAlive()) {
            System.out.println("Server is started.");
        } else
            serverSocketThread = new ServerSocketThread(this,"Server Soket Thread",port,2000);
    }

    public void stop() {
        System.out.println("Server try stoped.");
        if (serverSocketThread == null || !serverSocketThread.isAlive()) {
            System.out.println("Server is stopd.");
        } else
            serverSocketThread.interrupt();
    }

    public void putLog(String msg){
        msg = dateFormat.format(System.currentTimeMillis()) + Thread.currentThread().getName() + ": " + msg;
        listner.onChatServerMsg(this,msg);
    }

    private String getUsers(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < client.size(); i++) {
            ClientThread clientThread = (ClientThread) client.get(i);
            if(clientThread.isAuthorized())
                sb.append(clientThread.getNicname()).append(Messages.DELEMITOR);
        }
        return sb.toString();
    }

    /**
     *SSTListner method
     */

    @Override
    public void serverThreadStart(ServerSocketThread thread) {
        SqlClient.connect();
        putLog("started " + SqlClient.getNickName("Denis", "12345"));
    }

    @Override
    public void serverSocketCreated(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("server socket created");
    }

    @Override
    public void socketAccept(ServerSocketThread thread, Socket socket) {
        putLog("client connected: " + socket);
        String threadName = "SocketThread: " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, threadName, socket);
    }

    @Override
    public void socketTimeout(ServerSocketThread thread, ServerSocket serverSocket) {
        //putLog("server timeout");
    }

    @Override
    public void serverThreadException(ServerSocketThread thread, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }

    @Override
    public void serverThreadStop(ServerSocketThread thread) {
        SqlClient.disconnect();
        for (int i = 0; i < client.size(); i++) {
            client.get(i).close();
        }
        putLog("server is stoped");
    }

    /**
     * Socket Thread methods
     */

    @Override
    public synchronized void socketThreadStart(SocketThread thread, Socket socket) {
        putLog("socket thread start");
    }

    @Override
    public synchronized void socketThreadStop(SocketThread thread) {
        ClientThread clientThread = (ClientThread) thread;
        client.remove(thread);
        if(clientThread.isAuthorized() && !clientThread.isReconnecting()) {
            sendToAuthrized(Messages.getTypeBroadcast("Server", clientThread.getNicname() + " disconnect"));
            sendToAuthrized(Messages.getUserList(getUsers()));
        }

    }

    @Override
    public synchronized void socketThreadIsReady(SocketThread thread, Socket socket) {
        putLog("socket thread is ready");
        client.add(thread);
    }

    @Override
    public synchronized void socketThreadException(SocketThread thread, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }

    @Override
    public synchronized void socketPullMsg(SocketThread thread, Socket socket, String msg) {
        ClientThread clients = (ClientThread) thread;
        if(clients.isAuthorized()){
            handleAuthMsg(clients,msg);
        }else {
            handleNoAuthMsg(clients,msg);
        }
    }

    private void handleNoAuthMsg(ClientThread newClients, String msg) {
        String[] arr = msg.split(Messages.DELEMITOR);
        if(arr.length != 3 || !arr[0].equals(Messages.AUTH_REQUEST) ){
            newClients.sendMsg(Messages.getMsgFormatError(msg));
            return;
        }else {
            String login = arr[1];
            String password = arr[2];
            String nickname = SqlClient.getNickName(login,password);
            if(nickname == null){
                putLog("Invalid log/passw : login = " + login + " password = " + password);
                newClients.authorizeError();
            }else {
                ClientThread clients = findeClientByNick(nickname);
                newClients.authorizeAccept(nickname);
                if(clients == null){
                    sendToAuthrized(Messages.getTypeBroadcast("Server",nickname + " connected"));

                }else {
                   client.remove(clients);
                   clients.reconnect();
                }
                sendToAuthrized(Messages.getUserList(getUsers()));

            }
        }
    }

    private synchronized ClientThread findeClientByNick(String nickname){
        for (int i = 0; i < client.size(); i++) {
            ClientThread clients = (ClientThread) client.get(i);
            if(!clients.isAuthorized())continue;
            if (clients.getNicname().equals(nickname))
                return clients;
        }
        return null;
    }

    private void handleAuthMsg(ClientThread clients, String msg) {
        String[] arr = msg.split(Messages.DELEMITOR);
        String typeMsg = arr[0];
        switch (typeMsg){
            case Messages.TYPE_CLIENT_BCAST :
                sendToAuthrized(Messages.getTypeBroadcast(clients.getNicname(),arr[1]));
                break;
            default:
                clients.msgFormatError(msg);
        }
    }

    private void sendToAuthrized(String msg){
        for (int i = 0; i < client.size(); i++) {
            ClientThread clients = (ClientThread) client.get(i);
            if(!clients.isAuthorized()) continue;
            clients.sendMsg(msg);
        }
    }
}
