package iranga.mg.social.messaging;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.InstantChatMessage;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.ChatRepository;
import iranga.mg.social.repository.MessageRepository;
import iranga.mg.social.repository.UserRepository;

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

    @RabbitListener(queues = RabbitConfig.CHAT_MESSAGING_QUEUE)
    public void handleChatMessage(InstantChatMessage instantMessage) {
        try {
            logger.info("Processing message from {} to {}", 
                       instantMessage.getSender(), instantMessage.getReceiver());
            
            // Save message to database
            Message message = saveMessageToDatabase(instantMessage);
            
            // Send to WebSocket subscribers
            messagingTemplate.convertAndSend(
                "/topic/" + instantMessage.getReceiver() + "/reply", 
                instantMessage
            );
            
            logger.info("Message processed successfully with ID: {}", message.getId());
            
        } catch (Exception e) {
            logger.error("Failed to process message: {}", e.getMessage(), e);
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
            
            return messageRepository.save(message);
            
        } catch (Exception e) {
            logger.error("Failed to save message to database: {}", e.getMessage(), e);
            throw new RuntimeException("Database save failed", e);
        }
    }
}
