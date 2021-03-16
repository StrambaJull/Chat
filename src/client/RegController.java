package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    @FXML
    public TextField loginField;
    @FXML
    public TextField nicknameField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextArea textArea;

    private Controller controller; //создаем ссылку на основное окно чата для организации взаимодействия между ними


    public void tryToReg (ActionEvent actionEvent) {
        controller.tryToReg (loginField.getText ().trim (), passwordField.getText ().trim (), nicknameField.getText ().trim ());
    }

    private void tryToReg (String trim, String trim1, String trim2) {
    }

    public void setController (Controller controller) {
        this.controller = controller;
    }
    public void addMsgToTextArea(String regRezultMsg){
        textArea.appendText (regRezultMsg + "\n");
    }
}
