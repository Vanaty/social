package iranga.mg.social.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import iranga.mg.social.messaging.MessageProducer;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.InstantChatMessage;
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

    @MessageMapping("/chat.message")
    public void sendMessage(@Payload InstantChatMessage payload, Principal principal) {
        UserDetails userDetails = null;
        try {
            if(principal instanceof UsernamePasswordAuthenticationToken auth) {
                userDetails = (UserDetails) auth.getPrincipal();
            } else {
                logger.warn("Unauthenticated user attempted to send message");
                return;
            }
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

            // Set authenticated sender ID and timestamp
            payload.setSender(sender.getId().toString());
            payload.setTimestamp(LocalDateTime.now().toString());

            logger.info("Sending message from {} to chat {}", senderUsername, chatId);

            // Send to RabbitMQ for distribution
            messageProducer.sendChatMessage(payload);

            logger.info("Message from user {} for chat {} queued for distribution", sender.getId(), chat.getId());

        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.file")
    public void sendFile(@Payload InstantChatMessage payload, Principal principal) {
        try {
            if (!(principal instanceof UsernamePasswordAuthenticationToken auth)) {
                logger.warn("Unauthenticated user attempted to send a file");
                return;
            }
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            if (userDetails == null) {
                logger.warn("Unauthenticated user attempted to send a file");
                return;
            }

            String senderUsername = userDetails.getUsername();
            User sender = userRepository.findUserByUsername(senderUsername)
                    .orElseThrow(() -> new RuntimeException("Sender not found: " + senderUsername));

            Long chatId = Long.parseLong(payload.getReceiver());
            chatRepository.findByIdAndParticipant(chatId, sender)
                    .orElseThrow(() -> new RuntimeException("Chat not found or access denied"));

            payload.setSender(sender.getId().toString());
            payload.setTimestamp(LocalDateTime.now().toString());

            logger.info("Sending file from {} to chat {}. File URL: {}", senderUsername, chatId, payload.getFileUrl());

            messageService.sendFileMessage(
                payload
            );

        } catch (Exception e) {
            logger.error("Error sending file message: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingIndicator typingIndicator, Principal principal) {
        try {
            UserDetails userDetails = null;
            if(principal instanceof UsernamePasswordAuthenticationToken auth) {
                userDetails = (UserDetails) auth.getPrincipal();
            }
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
    public void handleDisconnect(Principal principal) {
        try {
            UserDetails userDetails = null;
            if(principal instanceof UsernamePasswordAuthenticationToken auth) {
                userDetails = (UserDetails) auth.getPrincipal();
            } else {
                logger.warn("Unauthenticated user attempted to disconnect");
                return;
            }
            if (userDetails != null) {
                String username = userDetails.getUsername();
                logger.info("User {} disconnecting", username);

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