package iranga.mg.social.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.dto.chat.ReadStatus;
import iranga.mg.social.dto.chat.TypingStatus;
import iranga.mg.social.model.InstantChatMessage;

@Component
public class MessageProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageProducer.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendChatMessage(InstantChatMessage message) {
        try {
            logger.info("Sending message from {} to {}", message.getSender(), message.getReceiver());
            rabbitTemplate.convertAndSend(
                RabbitConfig.OUTGOING_EXCHANGE, 
                "chat.message", 
                message
            );
        } catch (Exception e) {
            logger.error("Failed to send message: {}", e.getMessage(), e);
        }
    }
    
    public void sendTypingStatus(Long chatId, TypingStatus status) {
        try {
            logger.info("Sending typing status for chat {} by user {}", chatId, status.getUsername());
            rabbitTemplate.convertAndSend(
                RabbitConfig.OUTGOING_EXCHANGE,
                "chat.typing." + chatId,
                status
            );
        } catch (Exception e) {
            logger.error("Failed to send typing status: {}", e.getMessage(), e);
        }
    }

    public void sendReadStatus(Long chatId, ReadStatus status) {
        try {
            logger.info("Sending read status for chat {} for message {}", chatId, status.getMessageId());
            rabbitTemplate.convertAndSend(
                RabbitConfig.OUTGOING_EXCHANGE,
                "chat.read." + chatId,
                status
            );
        } catch (Exception e) {
            logger.error("Failed to send read status: {}", e.getMessage(), e);
        }
    }

    public void sendNotification(String userId, String notification) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitConfig.OUTGOING_EXCHANGE,
                "notification." + userId,
                notification
            );
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage(), e);
        }
    }
}
