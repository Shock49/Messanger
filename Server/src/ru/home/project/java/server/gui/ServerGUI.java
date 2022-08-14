package ru.home.project.java.server.gui;

import ru.home.project.java.server.core.ChatServer;
import ru.home.project.java.server.core.ChatServerListner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, ChatServerListner {
    private static final int WIDTH = 300;
    private static final int HEIGTH = 400;
    private static final int POS_X = 800;
    private static final int POS_Y = 100;

    private final ChatServer chatServer = new ChatServer(this);
    private final JButton btnStart = new JButton("Start");
    private final JButton btnStop = new JButton("Stop");
    private final JTextArea log = new JTextArea();
    private final JPanel panel = new JPanel(new GridLayout(1,2));

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerGUI();
            }
        });
    }

    ServerGUI(){
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGTH);
        setLocation(POS_X,POS_Y);
        setTitle("Chat Server");
        setResizable(false);
        setAlwaysOnTop(true);


        btnStart.addActionListener(this);
        btnStop.addActionListener(this);
        panel.add(btnStart);
        panel.add(btnStop);

        log.setLineWrap(true);
        log.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(log);

        add(panel,BorderLayout.NORTH);
        add(scrollPane,BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src == btnStart){
            chatServer.start(1900);
        }else if(src == btnStop){
            chatServer.stop();
        }else throw new RuntimeException("Uncnow source: " + src);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0) {
            msg = "Empty stack trace";
        } else {
            msg = e.getClass().getCanonicalName() + ": " +
                    e.getMessage() + "\n" + "\t at " + ste[0];
        }
        JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    @Override
    public void onChatServerMsg(ChatServer chatServer, String msg) {
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());

        });
    }
}
