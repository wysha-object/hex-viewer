package cn.com.wysha.hex_viewer.controller;

import cn.com.wysha.hex_viewer.constant.HexConstant;
import cn.com.wysha.hex_viewer.constant.IndexConstant;
import cn.com.wysha.hex_viewer.utils.FxUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class IndexController implements Initializable {
    public IndexController(Stage stage) {
        Objects.requireNonNull(stage);
        this.stage = stage;
    }

    private final Stage stage;

    public static void show(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(c -> new IndexController(stage));
        fxmlLoader.setLocation(IndexController.class.getResource("index.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        FxUtils.show(scene, stage, IndexConstant.MIN_WIDTH, IndexConstant.MIN_HEIGHT, "Hex viewer");
    }

    @FXML
    public Button chooseFile;
    @FXML
    public Label errorLabel;

    private void showErrorLabel(String text) {
        errorLabel.setText(text);
        errorLabel.setVisible(true);
    }

    private void hideErrorLabel () {
        errorLabel.setVisible(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hideErrorLabel();
    }

    @FXML
    public void chooseFile () throws IOException {
        hideErrorLabel();

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            showErrorLabel(HexConstant.FILE_NOT_SELECTED);
            return;
        }
        if (!file.canRead()) {
            showErrorLabel(HexConstant.FILE_NOT_READABLE);
            return;
        }

        HexController.show(new Stage(), file);
    }
}
