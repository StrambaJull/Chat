package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable { //Initializable гарантирует что отработает после того как графические объекты инициализируются
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public HBox authPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox msgPanel;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private Socket socket;
    DataInputStream in;
    DataOutputStream out;

    private boolean isAuthentication;
    private String nickName;
    private final String TITLE = "GeekBrains";
    private String receiver;

    //настройки экрана
    public void setAuthentication(boolean isAuthentication){
        this.isAuthentication = isAuthentication;

        msgPanel.setVisible(isAuthentication);
        msgPanel.setManaged(isAuthentication);

        authPanel.setVisible(!isAuthentication);
        authPanel.setManaged(!isAuthentication);

        if(!isAuthentication){
            nickName = "";
        }

        setTitle(nickName);
    }

    @Override
    //инициализация графических объектов
    public void initialize (URL location, ResourceBundle resources) {
        setAuthentication(false);
    }
    //отправить сообщение
    public void sendMsg (ActionEvent actionEvent) {
        try {
            out.writeUTF (textField.getText ()); //для исходящего потока получить данные из textField
            textField.clear (); //очистить textField
            textField.requestFocus (); //вернуть фокус на textField
        } catch (IOException e) {
            e.printStackTrace ();
        }

    }
    //обработка нажатия на кнопку авторизоваться
    public void tryToAction (ActionEvent actionEvent) {
        if(socket == null || socket.isClosed()){ //если сокет закрыт или соединение оборвалось,
            connect(); //тогда создаем новое соединение
        }
        //передаем на сервер сообщение
        try {
            out.writeUTF(String.format("/auth %s %s", loginField.getText().trim().toLowerCase(), passwordField.getText().trim()));
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //создание соединения с сервером
    private void connect () {
        try {
            socket = new Socket(IP_ADDRESS, PORT); //сокет, через который клиент открывает подключение
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true){
                        String serverMsg = in.readUTF();
                        if(serverMsg.startsWith("/authok")){
                            nickName = serverMsg.split(" ", 2)[1];
                            setAuthentication(true);
                            break;
                        }
                        textArea.appendText(serverMsg + "\n"); //добавляем в текстовое поле сообщение от сервера
                    }
                    //цикл работы
                    while (true) {
                        String serverMsg = in.readUTF();
                        //заканчиваем цикл работы, если сервер закрыл соединение
                        if (serverMsg.equalsIgnoreCase("/end")){
                            System.out.println("Сервер закрыл соединение");
                            break;
                        }
                        textArea.appendText(serverMsg + "\n"); //добавляем в текстовое поле сообщение от сервера
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        System.out.println("Отключились от сервера");
                        setAuthentication(false); //разлогиниваемся
                        socket.close(); //закрываем сокет
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendReceiverMsg (String receiverMsg) {
    }

    //установка заголовка окна
    private void setTitle(String nickName){
        Platform.runLater(()-> {
            ((Stage)textField.getScene().getWindow()).setTitle(TITLE + " " + nickName);
        });

    }
}
