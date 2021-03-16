package server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

//будет отвечать за работу с конкретным клиентом
public class ClientHandler {
    private Server server;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickName;
    private String receiver;
    private String login;
    private String clientMsg;

    public String getNickName () {
        return nickName;
    }

    public String getLogin () {
        return login;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.clientSocket = socket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());

            new Thread(() -> {
                try {
                //цикл аутентификации
                   while (true){
                       try {
                           socket.setSoTimeout (120_000); //включили счетчик ожидания авторизации
                           clientMsg = in.readUTF ();
                           if (clientMsg.startsWith ("/auth")) {
                               String[] token = clientMsg.split ("\\s");
                               if (token.length < 3) { //проверяем, если при аутентификации передали больше слов разделенных пробелом, чем надо (а надо 3), то выходим из цикла
                                   continue;
                               }
                               String newNick = server
                                       .getAuthService ()
                                       .getNickByLoginAndPassword (token[1], token[2]);//получаем из строки клиента nickName
                               login = token[1]; //получаем из строки клиента логин
                               if (newNick != null) { //проверяем получили ли мы никнейм
                                   if (!server.isLoginAuthentication (login)) { //проверяем, залогинился ли пользователь, если нет, то логиним
                                       nickName = newNick;
                                       sendMsg ("/authok " + nickName);
                                       server.subscribe (this); //заносим в список подключенных пользователей
                                       socket.setSoTimeout (0); //отключили счетчик ожидания авторизации
                                       System.out.println ("Клиент " + nickName + " подключился");
                                       break;
                                   } else {
                                       sendMsg ("С данной учетной записью уже зашли"); //если залогинился, то не логиним снова
                                   }
                               } else {
                                   sendMsg ("Неверный логин/пароль");
                               }
                           }
                           if (clientMsg.startsWith ("/reg")) {
                               String[] token = clientMsg.split ("\\s");
                               if (token.length < 4) { //если в полученном пакете меньше параметров, чем нам нужно, то ошибка (регистрация не прошла)
                                   continue;
                               }
                               boolean isRegistration = server
                                       .getAuthService ()
                                       .registration (token[1], token[2], token[3]); //проверяем, прошла ли регистрация нового клиента
                               if (isRegistration) { //если регистрация прошла успешно
                                   sendMsg ("/regok");//то сообщаем клиенту об успехе
                               } else {
                                   sendMsg ("/regno");//иначе сообщаем, что регистрация не прошла
                               }
                           }
                       }catch (SocketTimeoutException e){//если время ожидания истекло
                           sendMsg ("Время истекло"); //сообщаем об этом
                           socket.close (); //закрываем сокет
                       }
                   }
                    //цикл работы
                    while (true) {
                        String clientMsg = in.readUTF(); //получили сообщение
                        if (clientMsg.equalsIgnoreCase("/end")) { //проверили на завершение сеанса
                            break;
                        }
                        if(clientMsg.startsWith ("/w")){
                            receiver = clientMsg.split (" ", 3)[1]; //получили имя получателя сообщения
                            server.sendPrivateMsg (this, clientMsg.substring(clientMsg.indexOf (receiver) + receiver.length()), receiver); //отправили сообщение получателю
                        } else {
                            server.broadcast(this, clientMsg); //отправили сообщение
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    } finally {
                        System.out.println("Клиент отключился");
                        server.unsubscribe(this);//удалить клиента из списка на сервере
                        try {
                            clientSocket.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void sendMsg(String message){
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
