package iranga.mg.social.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
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
