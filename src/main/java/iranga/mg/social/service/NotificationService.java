package iranga.mg.social.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import iranga.mg.social.dto.notif.ExpoNotification;
import iranga.mg.social.dto.notif.NotificationDto;

@Service
public class NotificationService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NotificationService.class);
    @Autowired
    private UserService userService;

    public void sendNotification(String userName, ExpoNotification notification) {
        List<String> expoPushToken = userService.getExpoPushToken(userName);
        if (expoPushToken == null || expoPushToken.isEmpty()) {
            logger.warn("No Expo push token found for user: {}", userName);
            return;
        }
        for (String token : expoPushToken) {
            notification.setTo(token);

            String url = "https://exp.host/--/api/v2/push/send";
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ExpoNotification> request = new HttpEntity<>(notification, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Notification sent successfully: {}", response.getBody());
            } else {
                logger.error("Failed to send notification: {}", response.getBody());
            }
        }
    }

    public void sendNotification(Long senderId,NotificationDto notificationDto) {
        String url = "https://exp.host/--/api/v2/push/send";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<String> allExpoTokens = userService.getAllExpoPushTokenExcept(senderId);
        if (allExpoTokens.isEmpty()) {
            logger.warn("No Expo push tokens found for any user.");
            return;
        }

        for (String expoPushToken : allExpoTokens) {
            ExpoNotification notification = new ExpoNotification();
            notification.setTo(expoPushToken);
            notification.setTitle(notificationDto.getTitle());
            notification.setBody(notificationDto.getBody());
            notification.setData(notificationDto.getData());

            HttpEntity<ExpoNotification> request = new HttpEntity<>(notification, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Notification sent successfully to {}: {}", expoPushToken, response.getBody());
            } else {
                logger.error("Failed to send notification to {}: {}", expoPushToken, response.getBody());
            }
        }
    }
}
