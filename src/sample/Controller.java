package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.awt.*;

public class Controller {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;


    public void onClickBtnSend (ActionEvent actionEvent) {
        textArea.appendText(textField.getText() + "\n");
        textField.requestFocus();
        textField.clear();
    }

    public void handleEnterPressed (KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER){
            textArea.appendText(textField.getText()+"\n");
            textField.requestFocus();
            textField.clear();
        }
    }
}
