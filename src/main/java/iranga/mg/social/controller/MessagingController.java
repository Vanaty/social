package iranga.mg.social.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import iranga.mg.social.dto.chat.ReadStatus;
import iranga.mg.social.dto.chat.TypingStatus;
import iranga.mg.social.dto.notif.NotificationDto;
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
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

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
    
    @MessageMapping("/chat/{userId}/typing")
    public void sendTypingStatus(@DestinationVariable Long userId,
                                       TypingStatus status,
                                       Principal principal) {
        status.setUsername(principal.getName());
        messageProducer.sendTypingStatus(userId, status);
    }

    @MessageMapping("/chat/{userId}/read")
    public void markMessageAsRead(@DestinationVariable Long userId,
                                 ReadStatus readStatus,
                                 Principal principal) {
        User u  = userRepository.findUserByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
        messageService.markMessageAsRead(readStatus.getMessageId(), u);

        messageProducer.sendReadStatus(userId, readStatus);
    }

    @MessageMapping("/notification/{senderId}")
    public void sendNotification(@DestinationVariable Long senderId,
                                  NotificationDto notificationDto,
                                  Principal principal) {
        User u  = userRepository.findUserByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));
        messageProducer.sendNotification(senderId, notificationDto);
    }
}