package cn.com.wysha.hex_viewer;

import cn.com.wysha.hex_viewer.utils.BeanUtils;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class HexViewerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(HexViewerApplication.class, args);
        BeanUtils.setApplicationContext(applicationContext);
        Application.launch(Launcher.class, args);
    }
}
