package iranga.mg.social.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iranga.mg.social.dto.notif.ExpoNotification;
import iranga.mg.social.messaging.MessageProducer;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.InstantChatMessage;
import iranga.mg.social.model.Media;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.Participant;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.ChatRepository;
import iranga.mg.social.repository.MessageRepository;
import iranga.mg.social.repository.UserRepository;
import iranga.mg.social.type.TypeMessage;

@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    public void sendMessage(String senderId, String chatId, String content) {
        // Create instant message for real-time delivery
        InstantChatMessage instantMessage = new InstantChatMessage();
        instantMessage.setSender(senderId);
        instantMessage.setReceiver(chatId);
        instantMessage.setContent(content);
        instantMessage.setTimestamp(LocalDateTime.now().toString());
        
        // Send via RabbitMQ for processing
        messageProducer.sendChatMessage(instantMessage);
    }

    @Transactional
    public Message saveMessageToDatabase(InstantChatMessage instantMessage) {
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

    @Transactional(readOnly = true)
    public void sendMessagePushNotification(Message message) {
        Chat chat = message.getChat();
        List<Participant> participants = chat.getParticipants();
        for (Participant user : participants) {
            if (user.getUser().getId().equals(message.getSender().getId())) {
                continue; // Skip sender
            }
            String content = message.getContentText();

            ExpoNotification payload = new ExpoNotification();
            payload.setTitle("Nouveau message de " + message.getSender().getUsername().substring(0, 1).toUpperCase() + message.getSender().getUsername().substring(1));
            payload.setBody(content.length() > 50 ? content.substring(0, 50) + "..." : content);
            payload.setData(Map.of("type", "message", "sender", user.getUser().getUsername(),"chatId", message.getChat().getId()));
            notificationService.sendNotification(user.getUser().getUsername(), payload);
        }
    }

    public void sendFileMessage(InstantChatMessage message) {
        message.setTimestamp(LocalDateTime.now().toString());
        messageProducer.sendChatMessage(message);
    }
    
    public Page<Message> getChatMessages(Long chatId, Pageable pageable) {
        return messageRepository.findByChatIdOrderByTimestampDesc(chatId, pageable);
    }
    
    public Message saveMessage(User sender, Chat chat, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        message.setContentText(content);
        message.setTimestamp(LocalDateTime.now());
        
        return messageRepository.save(message);
    }

    public Message saveMessage(User sender, Chat chat, String content, String fileUrl, TypeMessage type) {
        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        message.setContentText(content);
        message.setType(type);
        message.setTimestamp(LocalDateTime.now());

        if (type == TypeMessage.IMAGE || type == TypeMessage.FILE) {
            Media media = new Media();
            media.setFileName(content); // content is the original filename
            media.setFileUrl(fileUrl);
            media.setMediaType(type.toString()); // Or more specific if available
            message.setMedia(media);
        }

        return messageRepository.save(message);
    }
    
    public void markMessageAsRead(Long messageId, User user) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Verify user has access to this message
        Chat chat = message.getChat();
        boolean hasAccess = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(user.getId()));
        
        if (!hasAccess) {
            throw new RuntimeException("Access denied");
        }
        
        message.setRead(true);
        messageRepository.save(message);
    }

    public List<Participant> getChatParticipants(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        return chat.getParticipants();
    }


    public Message getLastMessageInChat(Long chatId) {
        return messageRepository.findTopByChatIdOrderByTimestampDesc(chatId).orElse(null);
    }

    public int countUnreadMessages(Long chatId, Long userId) {
        return messageRepository.countByChatIdAndSenderIdNotAndIsReadFalse(chatId, userId);
    }
}
