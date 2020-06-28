package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nick;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

           // new Thread(() -> {
            /*Я думаю, что применение  ExecutorService в нашем чате будет оправдано, когда в нем будет достаточно клиентов. Я использовала
            CachedThreadPool потому,что на данном этапе пользователей маленькое количество, а этот сервис позволяет работать как с одним потоком,
            так и с несколькими, сервис сам добавляет их по мере необходимости, и удаляет их за ненадобностью после их работы. Перегрузка здесь не грозит,
             т.к. мало клиенов. В дальнейшем (при расширении клиентской базы) я бы использовала FixedThreadPool, т.к. в нем можно расчитать
              примерно сколько потоков понадобится и как- то избежать "падения железа". В любом случае можно корректироватьЯ, добавляя потоки. */
            ExecutorService service = Executors.newCachedThreadPool();
            service.execute(() -> {
            try {
                //Если в течении 12 секунд не будет сообщений по сокету то вызовится исключение
                socket.setSoTimeout(12000);

                //цикл аутентификации
                while (true) {
                    String str = in.readUTF();

                    if (str.startsWith("/reg ")) {
                        String[] token = str.split(" ");

                        if (token.length < 4) {
                            continue;
                        }

                        boolean succeed = server
                                .getAuthService()
                                .registration(token[1], token[2], token[3]);
                        if (succeed) {
                            sendMsg("Регистрация прошла успешно");
                        } else {
                            sendMsg("Регистрация  не удалась. \n" +
                                    "Возможно логин уже занят, или данные содержат пробел");
                        }
                    }

                    if (str.startsWith("/auth ")) {
                        String[] token = str.split(" ");

                        if (token.length < 3) {
                            continue;
                        }

                        String newNick = server.getAuthService().getNicknameByLoginAndPassword(token[1], token[2]);

                        login = token[1];

                        if (newNick != null) {
                            if (!server.isLoginAuthorized(login)) {
                                sendMsg("/authok " + newNick);
                                nick = newNick;
                                server.subscribe(ClientHandler.this);
                                System.out.println("Клиент: " + nick + " подключился"+ socket.getRemoteSocketAddress());
                                socket.setSoTimeout(0);
                                sendMsg(DataBase.getMessageFromNick(nick));
                                break;
                            } else {
                                sendMsg("С этим логином уже прошли аутентификацию");
                            }
                        } else {
                            sendMsg("Неверный логин / пароль");
                        }
                    }
                }

                //цикл работы
                while (true) {
                    String str = in.readUTF();

                    if (str.startsWith("/")) {
                        if (str.equals("/end")) {
                            sendMsg("/end");
                            break;
                        }
                        if (str.startsWith("/w ")) {
                            String[] token = str.split(" ", 3);

                            if (token.length < 3) {
                                continue;
                            }

                            server.privateMsg(ClientHandler.this, token[1], token[2]);
                        }
                        if (str.startsWith("/chnick")){
                            String [] token = str.split(" ",2);
                            if (token.length < 2){
                                continue;
                            }
                            if (token[1].contains(" ")){
                                sendMsg("Ник не может содержать пробелов.");
                                continue;
                            }
                            if (server.getAuthService().changeNick(ClientHandler.this.nick, token[1])){
                                sendMsg("/yournick: " + token[1]);
                                server.broadcastClientList();
                            }else {
                                sendMsg("Не удалось сменить ник.");
                            }
                        }
                    } else {
                        server.broadcastMsg(nick, str);
                    }
                }
            }catch (SocketTimeoutException e){
                sendMsg("/end");
            }

            catch (IOException e) {
                e.printStackTrace();
            } finally {
                server.unsubscribe(ClientHandler.this);
                System.out.println("Клиент отключился");
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            });
            service.shutdown();
            //}).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public  String getLogin() {
        return login;
    }
}
