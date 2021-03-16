package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Server {
    SimpleDateFormat formater = new SimpleDateFormat ("HH: mm: ss");
    List<ClientHandler> clientsList;
    private AuthService authService;
    private static int PORT = 8189;
    ServerSocket server = null;
    Socket clientSocket = null;

    public AuthService getAuthService () {
        return authService;
    }
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

    public void broadcast(ClientHandler sender, String msg) { //транслируем сообщение в чат
        String message = String.format("[%s] : %s : %s", formater.format (new Date ()), sender.getNickName(), msg);
        for (ClientHandler client: clientsList) {
            client.sendMsg(message);
        }
    }

    public void sendPrivateMsg(ClientHandler sender, String msg, String receiver){ //транслируем приватное сообщение
        String message = String.format("[%s] : %s private for %s : %s", formater.format (new Date ()), sender.getNickName(), receiver, msg);
        for (ClientHandler client: clientsList) {
            if(client.getNickName ().equalsIgnoreCase (receiver) || //берем только тех клиентов у которых nickName совпадает с получателем
            client.getNickName ().equalsIgnoreCase (sender.getNickName ())){  //+ добавляем клиента у которого nickName совпадает с отправителем,
                client.sendMsg(message);    //отправляем только тем, у кого совпал nickName
            }
        }
    }
    public void subscribe(ClientHandler clientHandler){//добавляет пользователя в список
        clientsList.add(clientHandler);
        broadcastClientList();//отсылаем список
    }
    public void unsubscribe(ClientHandler clientHandler){ //исключает пользователя из списка
        clientsList.remove(clientHandler);
        broadcastClientList();//отсылаем список
    }
    public boolean isLoginAuthentication(String login){ //проверяет, есть ли в списке пользователь с таким же логином
        for(ClientHandler client :clientsList){
            if(client.getLogin().equalsIgnoreCase(login)){
                return true;
            }
        }
        return false;
    }
    private void broadcastClientList(){
        StringBuilder sb = new StringBuilder ("/clientsList "); //Создаем перезаписываемую строку,
        for (ClientHandler c : clientsList){ // проходимся по списку подклчюенных клиентов
            sb.append (c.getNickName ()).append(" "); // добавляем в строку никнейм
        }
        String msg = sb.toString (); //преобразуем структуру в строку
        for(ClientHandler c: clientsList){ //всем клиентам
            c.sendMsg (msg); //отсылаем сообщение
        }
    }
}
