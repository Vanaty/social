package iranga.mg.social.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iranga.mg.social.messaging.MessageProducer;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.InstantChatMessage;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.ChatRepository;
import iranga.mg.social.repository.MessageRepository;

@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private ChatRepository chatRepository;
    
    @Autowired
    private MessageProducer messageProducer;
    
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
    
    public List<Message> getChatMessages(Long chatId) {
        return messageRepository.findByChatIdOrderByTimestampAsc(chatId);
    }
    
    public Message saveMessage(User sender, Chat chat, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        message.setContentText(content);
        message.setTimestamp(LocalDateTime.now());
        
        return messageRepository.save(message);
    }
    
    @Transactional(readOnly = true)
    public List<Message> getUnreadMessages(User user) {
        // Implement logic to get unread messages for a user
        return messageRepository.findByChatIdOrderByTimestampAsc(null); // Placeholder
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
}
