package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class Server {
    List<ClientHandler> clientsList;

    public AuthService getAuthService () {
        return authService;
    }

    private AuthService authService;

    private static int PORT = 8189;
    ServerSocket server = null;
    Socket clientSocket = null;

    public  Server() {
        clientsList = new Vector<>();
        authService = new SimpleAuthService();
        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен");

            while (true){
                clientSocket = server.accept(); //ждем подключения
                System.out.println("Клиент подключился");
                new ClientHandler(this, clientSocket); //обрабатываем каждого клиента в отдельном потоке
            }
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //транслируем сообщение в чат
    public void broadcast(ClientHandler sender, String msg) {
        String message = String.format("%s : %s", sender.getNickName(), msg);
        for (ClientHandler client: clientsList) {
            client.sendMsg(message);
        }
    }
    //транслируем приватное сообщение
    public void sendPrivateMsg(ClientHandler sender, String msg, String receiver){
        String message = String.format("%s : %s", sender.getNickName(), msg);
        for (ClientHandler client: clientsList) {
            if(client.getNickName ().equalsIgnoreCase (receiver) || //берем только тех клиентов у которых nickName совпадает с получателем
            client.getNickName ().equalsIgnoreCase (sender.getNickName ())){  //+ добавляем клиента у которого nickName совпадает с отправителем,
                client.sendMsg(message);    //отправляем только тем, у кого совпал nickName
            }
        }
    }
    public void subscribe(ClientHandler clientHandler){
        clientsList.add(clientHandler);
    }
    public void unsubscribe(ClientHandler clientHandler){
        clientsList.remove(clientHandler);
    }
}
