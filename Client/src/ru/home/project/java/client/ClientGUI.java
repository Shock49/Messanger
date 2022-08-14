package ru.home.project.java.client;

import ru.home.project.java.Messages;
import ru.home.project.java.networking.SocketThread;
import ru.home.project.java.networking.SocketThreadListner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListner {

    private final int WIDTH = 800;
    private final int HEIGHT = 600;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final String[] EMPTY = new String[0];


    private final JTextArea log = new JTextArea();

    private final JPanel panelTop = new JPanel(new GridLayout(2,3));
    private final JTextField tfIPAdress = new JTextField("192.168.43.232");
    private final JTextField tfPort = new JTextField("1900");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("");
    private final JPasswordField tfPasswoed = new JPasswordField("");
    private final JButton btnLogin = new JButton("Login");

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");
    private final JButton btnDisconnect = new JButton("Disconnect");

    private final JList<String> userList = new JList<>();

    private SocketThread socketThread;
    private Socket socket;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI());
    }

    ClientGUI(){
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH,HEIGHT);
        setLocationRelativeTo(null);
        setTitle("Messenger");

        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);
        tfIPAdress.addActionListener(this);
        tfLogin.addActionListener(this);
        tfPasswoed.addActionListener(this);
        tfPort.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);

        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);

        JScrollPane scrollPaneUserList = new JScrollPane(userList);
        scrollPaneUserList.setPreferredSize(new Dimension(100,0));



        add(panelTop,BorderLayout.NORTH);
        panelTop.add(tfIPAdress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPasswoed);
        panelTop.add(btnLogin);

        add(log,BorderLayout.CENTER);

        add(panelBottom,BorderLayout.SOUTH);
        panelBottom.setVisible(false);
        panelBottom.add(tfMessage,BorderLayout.CENTER);
        panelBottom.add(btnSend,BorderLayout.EAST);
        panelBottom.add(btnDisconnect,BorderLayout.WEST);

        add(scrollPaneUserList,BorderLayout.EAST);
        setVisible(true);
    }
    private void sendMsg() {
        String txt = tfMessage.getText();
        if("".equals(txt)) return;
        socketThread.sendMsg(Messages.getTypeClientBcast(txt));
        tfMessage.setText("");
        tfMessage.requestFocus(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if(src == cbAlwaysOnTop){
           setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        }else if(src == btnSend || src == tfMessage){
            sendMsg();
        }else if(src == tfIPAdress || src == tfLogin || src == tfPasswoed || src == tfPort || src == btnLogin){
            connect();
        }else if(src == btnDisconnect){
            try {
               socket.close();
            } catch (IOException ex) {
                socketThreadException(socketThread,ex);
            }
        }
        else throw new RuntimeException("Uncnow source: " + e.getSource());

    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if(ste.length == 0){
            msg = "Empty Stac Trace !";
        }else {
            msg = e.getClass().getCanonicalName() + ": " +
                    e.getMessage() + "\n" + "\t at " + ste[0];
        }
        JOptionPane.showMessageDialog(this,msg,"Error",JOptionPane.ERROR_MESSAGE);
    }



    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });

    }

    private void handleMsg(String msg){
        String[] value = msg.split(Messages.DELEMITOR);
        String msgType = value[0];
        switch (msgType){
            case Messages.AUTH_ACCEPT:
                //
                break;
            case Messages.AUTH_ERROR:
                putLog(msg);
                break;
            case Messages.TYPE_BROADCAST:
                putLog(dateFormat.format(Long.parseLong(value[1])) + value[2] + ": " + value[3]);
                break;
            case Messages.MSG_FORMAT_ERROR:
                putLog(msg);
                socketThread.close();
                break;
            case Messages.USER_LIST:
                String newMsg = msg.substring(Messages.USER_LIST.length() + Messages.DELEMITOR.length());
                String[] arrUser = newMsg.split(Messages.DELEMITOR);
                Arrays.sort(arrUser);
                userList.setListData(arrUser);
                break;
            default:
                throw new RuntimeException("Unknow type msg: " + msg);
        }
    }


    private void connect() {
        socket = null;
        try {
            socket = new Socket(tfIPAdress.getText(), Integer.parseInt(tfPort.getText()));

        } catch (IOException e) {
            log.append("Exception: " + e.getMessage());
        }
        socketThread = new SocketThread(this, "Client thread", socket);
    }

    @Override
    public void socketThreadStart(SocketThread thread, Socket socket) {
        putLog("Connect complicte");

    }

    @Override
    public void socketThreadStop(SocketThread thread) {
        putLog("Disconnect");
        panelTop.setVisible(true);
        panelBottom.setVisible(false);
        userList.setListData(EMPTY);
    }

    @Override
    public void socketThreadIsReady(SocketThread thread, Socket socket) {
        putLog("socket thread ready");
        panelTop.setVisible(false);
        panelBottom.setVisible(true);
        String login = tfLogin.getText();
        String password = new String(tfPasswoed.getPassword());
        thread.sendMsg(Messages.getAUTH_REQUEST(login,password));
    }

    @Override
    public void socketThreadException(SocketThread thread, Exception e) {
        //putLog("exception in socket");
    }

    @Override
    public void socketPullMsg(SocketThread thread, Socket socket, String msg) {
        SwingUtilities.invokeLater(() -> handleMsg(msg));
    }
}
