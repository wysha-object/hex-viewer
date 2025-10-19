package cn.com.wysha.hex_viewer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hex-viewer")
@Data @NoArgsConstructor @AllArgsConstructor
public class HexViewerProperties {
    private boolean dev;
}
