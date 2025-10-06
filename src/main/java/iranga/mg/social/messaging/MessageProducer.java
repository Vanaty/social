package iranga.mg.social.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.dto.chat.ReadStatus;
import iranga.mg.social.dto.chat.TypingStatus;
import iranga.mg.social.dto.notif.NotificationDto;
import iranga.mg.social.model.Chat;
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

    public void sendChatCreated(Chat chat) {
        try {
            logger.info("Sending chat created event {}", chat.getChatName());
            rabbitTemplate.convertAndSend(
                RabbitConfig.OUTGOING_EXCHANGE,
                "chat.created",
                chat
            );
        } catch (Exception e) {
            logger.error("Failed to send chat created event: {}", e.getMessage(), e);
        }
    }

    public void sendTypingStatus(Long userId, TypingStatus status) {
        try {
            logger.info("Sending typing status for user {} in chat {}", userId, status.getChatId());
            rabbitTemplate.convertAndSend(
                RabbitConfig.OUTGOING_EXCHANGE,
                "chat.typing." + userId,
                status
            );
        } catch (Exception e) {
            logger.error("Failed to send typing status: {}", e.getMessage(), e);
        }
    }

    public void sendReadStatus(Long userId, ReadStatus status) {
        try {
            logger.info("Sending read status for user {} for message {}", userId, status.getMessageId());
            rabbitTemplate.convertAndSend(
                RabbitConfig.OUTGOING_EXCHANGE,
                "chat.read." + userId,
                status
            );
        } catch (Exception e) {
            logger.error("Failed to send read status: {}", e.getMessage(), e);
        }
    }

    public void sendCallOffer(iranga.mg.social.dto.webrtc.CallOfferDto callOffer) {
        try {
            logger.info("Sending call offer from {} to {}", callOffer.getCallerId(), callOffer.getReceiverId());
            rabbitTemplate.convertAndSend(
                RabbitConfig.WEBRTC_CALL_EXCHANGE,
                "webrtc.call.offer",
                callOffer
            );
        } catch (Exception e) {
            logger.error("Failed to send call offer: {}", e.getMessage(), e);
        }
    }

    public void sendIceCandidate(iranga.mg.social.dto.webrtc.IceCandidateDto iceCandidate) {
        try {
            logger.info("Sending ICE candidate for call {}", iceCandidate.getCallId());
            rabbitTemplate.convertAndSend(
                RabbitConfig.WEBRTC_CALL_EXCHANGE,
                "webrtc.call.candidate",
                iceCandidate
            );
        } catch (Exception e) {
            logger.error("Failed to send ICE candidate: {}", e.getMessage(), e);
        }
    }

    public void sendCallAnswer(iranga.mg.social.dto.webrtc.CallAnswerDto callAnswer) {
        try {
            logger.info("Sending call answer for call {}", callAnswer.getCallId());
            rabbitTemplate.convertAndSend(
                RabbitConfig.WEBRTC_CALL_EXCHANGE,
                "webrtc.call.answer",
                callAnswer
            );
        } catch (Exception e) {
            logger.error("Failed to send call answer: {}", e.getMessage(), e);
        }
    }

    public void sendCallEnd(iranga.mg.social.dto.webrtc.CallEndDto callEnd) {
        try {
            logger.info("Sending call end for call {}", callEnd.getCallId());
            rabbitTemplate.convertAndSend(
                RabbitConfig.WEBRTC_CALL_EXCHANGE,
                "webrtc.call.end",
                callEnd
            );
        } catch (Exception e) {
            logger.error("Failed to send call end: {}", e.getMessage(), e);
        }
    }

    public void sendNotification(Long userId, NotificationDto notificationDto) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitConfig.OUTGOING_EXCHANGE,
                "notification." + userId,
                notificationDto
            );
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage(), e);
        }
    }
}
