package ru.home.project.java.server.core;

import java.sql.*;

public abstract class SqlClient {

    private static Connection connection;
    private static Statement statement;

    synchronized static void  connect(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost/users_messenger","root","150692");
            statement = connection.createStatement();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static String getNickName(String login,String password) {
        String request = "SELECT nicname FROM users_messenger.users WHERE login = \"" + login +
                "\" AND password = " + password + " ;";
        try (ResultSet set = statement.executeQuery(request)) {
            if (set.next()){
                return set.getString(1);
            }else
                return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
