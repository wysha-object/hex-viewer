package cn.com.wysha.hex_viewer;

import cn.com.wysha.hex_viewer.controller.IndexController;
import cn.com.wysha.hex_viewer.utils.BeanUtils;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class Launcher extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        IndexController.show(stage);
    }

    @Override
    public void stop() {
        ConfigurableApplicationContext context = BeanUtils.getApplicationContext();
        if (context != null) {
            context.close();
        }
    }
}
