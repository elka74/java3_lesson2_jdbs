package server;

import java.sql.*;

public class DataBase {
    private static Connection connection;
    private static PreparedStatement stmGetNick;
    private static PreparedStatement stmRegistration;
    private static PreparedStatement stmChange;
    private static PreparedStatement stmAdd;
    private static PreparedStatement stmMesNick;


    public static boolean connect() throws ClassNotFoundException, SQLException {
        try {

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:maind.db");
            prepareAllStatement();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void prepareAllStatement() throws SQLException {

        stmGetNick = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND password = ?");
        stmRegistration = connection.prepareStatement("INSERT INTO users (login, password, nick) VALUE (?, ?, ?);");
        stmChange = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?;");
        stmAdd = connection.prepareStatement("INSERT INTO messags (sender, recipient, message) VALUES ((SELECT id FROM users WHERE nick), (SELECT id FROM users WHERE nick), ?);");
        stmMesNick = connection.prepareStatement("SELECT (SELECT nick FROM users WHERE id = sender), (SELECT nick FROM users WHERE id = recipient), message\n" +
                "FROM messags WHERE sender = (SELECT id FROM users WHERE nick = ?) OR recipient =(SELECT id FROM users WHERE nick = ?) OR recipient =(SELECT id FROM users WHERE nick = 'null');");
    }

    public static boolean addMessage(String sender, String recipient, String message) {
        try {
            stmAdd.setString(1, sender);
            stmAdd.setString(2, recipient);
            stmAdd.setString(3, message);
            stmAdd.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getMessageFromNick(String nick) {
        StringBuilder bil = new StringBuilder();
        try {
            stmMesNick.setString(1, nick);
            stmMesNick.setString(2, nick);
            ResultSet rs = stmMesNick.executeQuery();

            while (rs.next()) {
                String sender = rs.getString(1);
                String recipient = rs.getString(2);
                String message = rs.getString(3);
                if (recipient.equals("null")) {
                    bil.append(String.format("%s : %s\n", sender, message));
                } else {
                    bil.append(String.format("[%s] private [%s] : %s\n", sender, recipient, message));
                }
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bil.toString();
    }

    public static String getNickByLoginAndPassword(String login, String password){
        String nick = null;
        try {
            stmGetNick.setString(1, login);
            stmGetNick.setString(2, password);
            ResultSet rs = stmGetNick.executeQuery();
            if (rs.next()){
                nick = rs.getString(1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
    }


    public static boolean registration(String login, String password, String nick){
        try {
            stmRegistration.setString(1, login);
            stmRegistration.setString(2, password);
            stmRegistration.setString(3, nick);
            stmRegistration.executeUpdate();
            return  true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean changeNick(String oldNick, String newNick){
        try {
            stmChange.setString(1, newNick);
            stmChange.setString(2, oldNick);
            stmChange.executeUpdate();
            return  true;
        } catch (SQLException e) {
            return false;
        }
    }


    public static void disconnect() {
        try {
            stmRegistration.close();
            stmGetNick.close();
            stmChange.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

