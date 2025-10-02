package iranga.mg.social.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "coturn")
@Data
public class CoturnConfig {
    private boolean enabled = false;
    private Stun stun = new Stun();
    private Turn turn = new Turn();

    @Data
    public static class Stun {
        private String host = "localhost";
        private int port = 3478;
    }

    @Data
    public static class Turn {
        private String host = "localhost";
        private int port = 3478;
        private String username = "";
        private String password = "";
        private String secret = "";
        private long ttl = 3600;
    }
}
