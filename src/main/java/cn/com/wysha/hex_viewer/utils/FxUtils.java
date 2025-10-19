package cn.com.wysha.hex_viewer.utils;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class FxUtils {
    public static void show(Scene scene, Stage stage, int w, int h, String title) {
        stage.setScene(scene);

        stage.setMinWidth(w);
        stage.setMinHeight(h);
        stage.setTitle(title);

        //居中并显示
        Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
        stage.setX((rectangle2D.getWidth() - w)/2);
        stage.setY((rectangle2D.getHeight() - h)/2);
        stage.show();
    }
}
