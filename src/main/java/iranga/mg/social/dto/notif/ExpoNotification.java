package iranga.mg.social.dto.notif;

import java.util.Map;

import lombok.Data;

@Data
public class ExpoNotification {
    private String to;
    private String title;
    private String sound = "default";
    private boolean showNotification = true;
    private String body;
    private Map<String, Object> data;
    private Map<String, Object> android;
    private Map<String, Object> ios;
}

