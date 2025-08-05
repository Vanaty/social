package iranga.mg.social.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import iranga.mg.social.messaging.MessageProducer;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.InstantChatMessage;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.OnlineUser;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.ChatRepository;
import iranga.mg.social.repository.MessageRepository;
import iranga.mg.social.repository.OnlineUserRepository;
import iranga.mg.social.repository.UserRepository;
import iranga.mg.social.service.MessageService;

@RestController
public class MessagingController {

    private static final Logger logger = LoggerFactory.getLogger(MessagingController.class);

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private OnlineUserRepository onlineUserRepository;

    @MessageMapping("/chat.subscribe")
    public void subscribe(@Payload String chatId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails != null) {
                String username = userDetails.getUsername();
                logger.info("User {} subscribed to chat {}", username, chatId);
                
                // Add user to online users
                OnlineUser onlineUser = onlineUserRepository.findByUsername(username);
                if (onlineUser == null) {
                    onlineUser = new OnlineUser();
                    onlineUser.setUsername(username);
                    onlineUserRepository.save(onlineUser);
                }

                // Send confirmation back to user
                messagingTemplate.convertAndSendToUser(
                    username, 
                    "/topic/" + chatId + "/reply", 
                    "Subscribed to chat " + chatId
                );
            }
        } catch (Exception e) {
            logger.error("Error in chat subscription: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.message")
    public void sendMessage(@Payload InstantChatMessage payload, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                logger.warn("Unauthenticated user attempted to send message");
                return;
            }

            String senderUsername = userDetails.getUsername();
            User sender = userRepository.findUserByUsername(senderUsername)
                    .orElseThrow(() -> new RuntimeException("Sender not found: " + senderUsername));

            // Validate chat exists and user has access
            Long chatId = Long.parseLong(payload.getReceiver());
            Chat chat = chatRepository.findByIdAndParticipant(chatId, sender)
                    .orElseThrow(() -> new RuntimeException("Chat not found or access denied"));

            // Set authenticated sender ID
            payload.setSender(sender.getId().toString());
            payload.setTimestamp(LocalDateTime.now().toString());

            logger.info("Sending message from {} to chat {}", senderUsername, chatId);

            // Save message to database immediately for consistency
            Message message = new Message();
            message.setContentText(payload.getContent());
            message.setSender(sender);
            message.setChat(chat);
            message.setTimestamp(LocalDateTime.now());
            Message savedMessage = messageRepository.save(message);

            // Send to RabbitMQ for distribution
            messageProducer.sendChatMessage(payload);

            // Send immediate confirmation to sender
            messagingTemplate.convertAndSendToUser(
                senderUsername,
                "/topic/" + chatId + "/confirmation",
                "Message sent successfully"
            );

            logger.info("Message saved with ID: {} and queued for distribution", savedMessage.getId());

        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
            if (userDetails != null) {
                messagingTemplate.convertAndSendToUser(
                    userDetails.getUsername(),
                    "/topic/error",
                    "Failed to send message: " + e.getMessage()
                );
            }
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicator typingIndicator, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails != null) {
                String username = userDetails.getUsername();
                typingIndicator.setUsername(username);
                
                // Broadcast typing indicator to chat participants
                messagingTemplate.convertAndSend(
                    "/topic/" + typingIndicator.getChatId() + "/typing",
                    typingIndicator
                );
            }
        } catch (Exception e) {
            logger.error("Error handling typing indicator: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.disconnect")
    public void handleDisconnect(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails != null) {
                String username = userDetails.getUsername();
                logger.info("User {} disconnecting", username);
                
                // Remove from online users
                OnlineUser onlineUser = onlineUserRepository.findByUsername(username);
                if (onlineUser != null) {
                    onlineUserRepository.delete(onlineUser);
                }
            }
        } catch (Exception e) {
            logger.error("Error handling disconnect: {}", e.getMessage(), e);
        }
    }

	public static class TypingIndicator {
        private String chatId;
        private String username;
        private boolean isTyping;

        // Getters and setters
        public String getChatId() { return chatId; }
        public void setChatId(String chatId) { this.chatId = chatId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public boolean isTyping() { return isTyping; }
        public void setTyping(boolean typing) { isTyping = typing; }
    }
}