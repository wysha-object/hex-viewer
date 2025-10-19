package cn.com.wysha.hex_viewer.controller;

import cn.com.wysha.hex_viewer.constant.NoticeConstant;
import cn.com.wysha.hex_viewer.utils.FxUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class NoticeController implements Initializable {
    public static NoticeController show(Stage stage, String notice) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(_ -> new NoticeController(stage, notice));
        fxmlLoader.setLocation(IndexController.class.getResource("notice.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        FxUtils.show(scene, stage, NoticeConstant.WIDTH, NoticeConstant.HEIGHT, notice);

        return fxmlLoader.getController();
    }

    private final Stage stage;

    private final String notice;

    public NoticeController(Stage stage, String notice) {
        this.stage = stage;
        this.notice = notice;
    }

    @FXML
    private Label noticeLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        noticeLabel.setText(notice);
        stage.setOnCloseRequest(_ -> {
            if (ensure == null) ensure = false;
        });
    }

    private Boolean ensure = null;

    @FXML
    private void ensure() {
        stage.close();
        ensure = true;
    }

    @FXML
    private void cancel() {
        stage.close();
        ensure = false;
    }

    public void setOnEnsure(Consumer<Boolean> consumer) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (ensure == null) {
                    Platform.runLater(this);
                } else {
                    consumer.accept(ensure);
                }
            }
        });
    }
}
