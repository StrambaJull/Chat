package server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

//будет отвечать за работу с конкретным клиентом
public class ClientHandler {
    private Server server;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickName;
    private String receiver;

    public String getNickName () {
        return nickName;
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
                       String clientMsg = null;
                       clientMsg = in.readUTF();
                       if(clientMsg.startsWith("/auth")){
                           String[] token = clientMsg.split("\\s");
                           String newNick = server.getAuthService().getNickByLoginAndPassword(token[1], token[2]);
                           if(newNick != null){
                               nickName = newNick;
                               sendMsg("/authok " + nickName);
                               server.subscribe(this);
                               System.out.println("Клиент " +nickName+ " подключился");
                               break;
                           } else {
                               sendMsg("Неверный логин/пароль");
                           }
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
                            server.sendPrivateMsg (this, clientMsg,receiver); //отправили сообщение получателю
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
