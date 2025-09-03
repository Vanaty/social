package iranga.mg.social.messaging;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.dto.chat.ReadStatus;
import iranga.mg.social.dto.chat.TypingStatus;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.InstantChatMessage;
import iranga.mg.social.model.Media;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.Participant;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.ChatRepository;
import iranga.mg.social.repository.MessageRepository;
import iranga.mg.social.repository.UserRepository;
import iranga.mg.social.service.UserService;
import iranga.mg.social.type.TypeMessage;
import jakarta.transaction.Transactional;

@Component
public class MessageConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserService userService;

    @RabbitListener(queues = RabbitConfig.CHAT_MESSAGING_QUEUE)
    public void handleChatMessage(InstantChatMessage instantMessage) {
        try {
            logger.info("Processing message from sender ID {} for chat ID {}", 
                       instantMessage.getSender(), instantMessage.getReceiver());
            
            // Save message to database
            Message message = saveMessageToDatabase(instantMessage);
            sendMessagePushNotification(message);
            messagingTemplate.convertAndSend(
                "/topic/chat/" + instantMessage.getReceiver(), 
                message
            );
            
            logger.info("Message processed and distributed successfully with ID: {}", message.getId());
            
        } catch (Exception e) {
            logger.error("Failed to process message: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.TYPING_STATUS_QUEUE)
    public void handleTypingStatus(TypingStatus status, @Header("amqp_receivedRoutingKey") String routingKey) {
        try {
            String chatId = routingKey.substring("chat.typing.".length());
            logger.info("Forwarding typing status for chat {}: {}", chatId, status.getUsername());
            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/typing", status);
        } catch (Exception e) {
            logger.error("Failed to process typing status: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.READ_STATUS_QUEUE)
    public void handleReadStatus(ReadStatus status, @Header("amqp_receivedRoutingKey") String routingKey) {
        try {
            String chatId = routingKey.substring("chat.read.".length());
            logger.info("Forwarding read status for chat {}: message {}", chatId, status.getMessageId());
            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/read", status);
        } catch (Exception e) {
            logger.error("Failed to process read status: {}", e.getMessage(), e);
        }
    }
    
    private Message saveMessageToDatabase(InstantChatMessage instantMessage) {
        try {
            // Find sender
            User sender = userRepository.findById(Long.parseLong(instantMessage.getSender()))
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            
            // Find chat
            Chat chat = chatRepository.findById(Long.parseLong(instantMessage.getReceiver()))
                    .orElseThrow(() -> new RuntimeException("Chat not found"));
            
            // Create and save message
            Message message = new Message();
            message.setContentText(instantMessage.getContent());
            message.setSender(sender);
            message.setChat(chat);
            message.setTimestamp(LocalDateTime.now());
            message.setType(instantMessage.getType());
            
            if (instantMessage.getType() == TypeMessage.IMAGE || instantMessage.getType() == TypeMessage.FILE) {
                Media media = new Media();
                media.setFileName(instantMessage.getContent());
                media.setThumbnailUrl(instantMessage.getThumbnailUrl());
                media.setFileUrl(instantMessage.getFileUrl());
                media.setMediaType(instantMessage.getType().name());
                message.setMedia(media);
            }
            
            return messageRepository.save(message);
            
        } catch (Exception e) {
            logger.error("Failed to save message to database: {}", e.getMessage(), e);
            throw new RuntimeException("Database save failed", e);
        }
    }

    @Transactional
    private void sendMessagePushNotification(Message message) {
        Chat chat = message.getChat();
        List<Participant> participants = chat.getParticipants();
        for (Participant user : participants) {
            String content = message.getContentText();
            String token = userService.getExpoPushToken(user.getUser().getId());
            if (token == null || token.isEmpty()) {
                logger.warn("No Expo token found for user ID {}", user.getUser().getId());
                continue;
            }
            String url = "https://exp.host/--/api/v2/push/send";

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> payload = new HashMap<>();
            payload.put("to", token);
            payload.put("sound", "default");
            payload.put("title", "New message from " + message.getSender().getUsername());
            payload.put("body", content.length() > 50 ? content.substring(0, 50) + "..." : content);
            payload.put("data", Map.of("screen", "Chat", "sender", user.getUser().getUsername(),"chat", message.getChat().getId()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("Expo response: {}", response.getBody());
        }
    }
}
