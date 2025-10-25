package cn.com.wysha.hex_viewer;

import cn.com.wysha.hex_viewer.service.FileService;
import cn.com.wysha.hex_viewer.utils.BeanUtils;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class HexViewerApplicationRunner implements ApplicationRunner {
    private final ConfigurableApplicationContext applicationContext;
    private final FileService fileService;

    @Autowired
    public HexViewerApplicationRunner(ConfigurableApplicationContext applicationContext, FileService fileService) {
        this.applicationContext = applicationContext;
        this.fileService = fileService;
    }

    @Override
    public void run(ApplicationArguments args) {
        String[] sourceArgs = args.getSourceArgs();
        if (sourceArgs.length > 0) {
            try {
                File src = new File(getSingleOptionValues(args, "src"));
                File dst = new File(getSingleOptionValues(args, "dst"));

                long from;
                long to;
                try {
                    from = Long.parseLong(getSingleOptionValues(args, "from"), 16);
                    to = Long.parseLong(getSingleOptionValues(args, "to"), 16);
                } catch (NumberFormatException e) {
                    log.error("from或to无效");
                    throw new RuntimeException(e);
                }

                try {
                    int length = (int) (to - from);
                    byte[] tmp = Files.readAllBytes(src.toPath());
                    byte[] bytes = Arrays.copyOf(tmp, length);

                    fileService.open(dst);
                    fileService.saveBytes(from, bytes);
                } catch (Exception e) {
                    log.error("src或dst无效");
                    throw new RuntimeException(e);
                }
            } catch (IllegalArgumentException e) {
                log.error("无效参数");
            } catch (Exception e) {
                log.error("发生错误,程序中止");
            }
        } else {
            BeanUtils.setApplicationContext(applicationContext);
            Application.launch(Launcher.class, args.getSourceArgs());
        }
    }

    private String getSingleOptionValues(ApplicationArguments applicationArguments, String optionName) {
        List<String> values = applicationArguments.getOptionValues(optionName);
        if (values == null || values.size() != 1) throw new IllegalArgumentException();
        return values.getFirst();
    }
}
