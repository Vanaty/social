package iranga.mg.social.dto.notif;

import java.util.Map;

import lombok.Data;

@Data
public class NotificationDto {
    private String title;
    private String body;
    private boolean showNotification = true;
    private Map<String, Object> data;
}
