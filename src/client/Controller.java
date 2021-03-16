package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
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
    @FXML
    public ListView<String> clientsList;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private String serverMsg;
    private Socket socket;
    DataInputStream in;
    DataOutputStream out;
    private boolean isAuthentication;
    private String nickName;
    private final String TITLE = "GeekBrains";
    private Stage stage;
    private Stage regStage; // ссылка на стейдж окна регистрации
    private RegController regController; //создаем ссылку на окно регистрации для организации взаимодействия между ними

    //настройки экрана
    public void setAuthentication(boolean isAuthentication){
        this.isAuthentication = isAuthentication;

        msgPanel.setVisible(isAuthentication);
        msgPanel.setManaged(isAuthentication);

        authPanel.setVisible(!isAuthentication);
        authPanel.setManaged(!isAuthentication);

        clientsList.setVisible(isAuthentication);
        clientsList.setManaged(isAuthentication);

        if(!isAuthentication){
            nickName = "";
        }

        setTitle(nickName);
    }

    @Override
    //инициализация графических объектов
    public void initialize (URL location, ResourceBundle resources) {
        setAuthentication(false);
        createRegWindow(); //инициализируем окно регистрации, но оно скрыто до тех пор, пока не нажмут на кнопку регистрации
        Platform.runLater (() -> { //закрываем соединение клиента, если закрыли окно
            stage = (Stage)textField.getScene ().getWindow ();
            stage.setOnCloseRequest (new EventHandler<WindowEvent> () {
                @Override
                public void handle (WindowEvent event) {
                    if(socket != null && !socket.isClosed ()) {
                        try {
                            out.writeUTF ("/end");
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                    }
                }
            });
        });
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

    public void tryToAction (ActionEvent actionEvent) { //обработка нажатия на кнопку авторизоваться
        if(socket == null || socket.isClosed()){ //если сокет закрыт или соединение оборвалось,
            connect(); //тогда создаем новое соединение
        }
        try {
            out.writeUTF(String.format("/auth %s %s", loginField.getText().trim().toLowerCase(), passwordField.getText().trim()));//передаем на сервер сообщение
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
                        serverMsg = in.readUTF ();
                        if(serverMsg.startsWith("/authok")){
                            nickName = serverMsg.split(" ", 2)[1];
                            setAuthentication(true);
                            break;
                        }
                        if(serverMsg.startsWith("/regok")){
                            regController.addMsgToTextArea ("Регистрация прошла успешно");
                        }
                        if(serverMsg.startsWith("/regno")){
                            regController.addMsgToTextArea ("Регистрация не прошла \n возможно логин или nick заняты");
                        }
                        textArea.appendText(serverMsg + "\n"); //добавляем в текстовое поле сообщение от сервера
                    }
                    //цикл работы
                    while (true) {
                        serverMsg = in.readUTF ();
                        if(serverMsg.startsWith ("/")) {
                            if (serverMsg.equalsIgnoreCase ("/end")) { //заканчиваем цикл работы, если сервер закрыл соединение
                                System.out.println ("Сервер закрыл соединение");
                                break;
                            }
                            if(serverMsg.startsWith ("/clientsList ")){ //если получили список
                                String[] token = serverMsg.split ("\\s+"); //то разбиваем строку на массив
                                Platform.runLater (()->{ //укладываем полученный массив в элемент ListView, который является ArrayList
                                    clientsList.getItems().clear(); //очистим список на случай, если там уже были значения
                                    for(int i = 1; i < token.length; i++){
                                        clientsList.getItems().add(token[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText (serverMsg + "\n"); //добавляем в текстовое поле сообщение от сервера
                        }
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


    //установка заголовка окна
    private void setTitle(String nickName){
        Platform.runLater(()-> {
            ((Stage)textField.getScene().getWindow()).setTitle(TITLE + " " + nickName);
        });

    }

    public void clickClientList (MouseEvent mouseEvent) {
        String receiver = clientsList.getSelectionModel().getSelectedItem();
        textField.setText ("/w " + receiver + " ");
    }
    private void createRegWindow(){ //метод будет инициализировать окно регистрации
        try {
            FXMLLoader fxmlLoader = new FXMLLoader (getClass().getResource ("reg.fxml")); //создали новый объект загрузчик, в который передали наименование ресурса, для которого мы создали загрузчик
            Parent  root = fxmlLoader.load();//создали новый объект root в который записали объект загрузчика

            regStage = new Stage(); //создаем новый экземпляр окна (сцены)
            regStage.setTitle ("Reg windew"); //установили заголовок окна
            regStage.setScene (new Scene (root, 400,250)); //устанавливаем параметры текущей сцены

            regController = fxmlLoader.getController(); //получили ссылку на контроллер, который будет обрабатывать окно регистрации
            regController.setController (this); //установили связь между контроллером основного окна и окна регистрации

            regStage.initModality(Modality.APPLICATION_MODAL); //заблокирует другие окна, будет активно только окно регистрации

        } catch (IOException e) {
            e.printStackTrace ();
        }

    }

    public void registration (ActionEvent actionEvent) { //по нажатию на кнопку reg
        regStage.show (); //отображается окно регистрации, которое создается через метод createRegWindow()
    }
    public void tryToReg(String login, String password, String nickName){ //будет вызываться по нажатию на кнопку на форме регистрации
        String regMsg = String.format ("/reg %s %s %s", login, password, nickName); //формируем строку в заданном формате
        if(socket == null || socket.isClosed ()){ //проверяем есть ли соединение или нет
            connect(); //если соединения нет или сокет закрыт, тогда создадим новое соединение
        }
        try {
            out.writeUTF (regMsg);//отправляем эту строку серверу
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}
