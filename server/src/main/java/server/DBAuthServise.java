package server;

public class DBAuthServise implements AuthService {
    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        return DataBase.getNickByLoginAndPassword(login, password);
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        return DataBase.registration(login, password, nickname);
    }

    @Override
    public boolean changeNick(String oldNick, String newNick) {
        return DataBase.changeNick(oldNick, newNick);
    }
}
