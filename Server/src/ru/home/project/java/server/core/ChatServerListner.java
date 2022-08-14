package ru.home.project.java.server.core;

public interface ChatServerListner {
    void onChatServerMsg(ChatServer chatServer, String msg);
}
