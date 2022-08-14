package ru.home.project.java;

public abstract class Messages {
    public static final String DELEMITOR = "•";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_ERROR = "/auth_error";
    public static final String TYPE_BROADCAST = "/broadcast";
    public static final String TYPE_CLIENT_BCAST = "/client_bcast";
    public static final String MSG_FORMAT_ERROR = "/msg_format_error";
    public static final String USER_LIST = "/user_list";

    public static String getAUTH_REQUEST(String login, String password) {
        return AUTH_REQUEST + DELEMITOR + login + DELEMITOR + password;
    }

    public static String getAUTH_ACCEPT(String nickname) {
        return AUTH_ACCEPT + DELEMITOR + nickname;
    }

    public static String getAuthError() {
        return AUTH_ERROR;
    }

    public static String getTypeBroadcast(String src, String msg) {
        return TYPE_BROADCAST + DELEMITOR + System.currentTimeMillis() + DELEMITOR + src + DELEMITOR + msg;
    }

    public static String getMsgFormatError(String msg) {
        return MSG_FORMAT_ERROR + DELEMITOR + msg;
    }

    public static String getUserList(String user){
        return USER_LIST + DELEMITOR + user;
    }

    public static String getTypeClientBcast(String msg){
        return TYPE_CLIENT_BCAST + DELEMITOR + msg;
    }
}
