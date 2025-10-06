package iranga.mg.social.messaging;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.dto.chat.ReadStatus;
import iranga.mg.social.dto.chat.TypingStatus;
import iranga.mg.social.dto.notif.NotificationDto;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.InstantChatMessage;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.Participant;
import iranga.mg.social.service.CallService;
import iranga.mg.social.service.MessageService;
import iranga.mg.social.service.NotificationService;

@Component
public class MessageConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CallService callService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private NotificationService notificationService;
    

    @RabbitListener(queues = RabbitConfig.CHAT_MESSAGING_QUEUE)
    public void handleChatMessage(InstantChatMessage instantMessage) {
        try {
            logger.info("Processing message from sender ID {} for chat ID {}", 
                       instantMessage.getSender(), instantMessage.getReceiver());
            
            // Save message to database
            Message message = messageService.saveMessageToDatabase(instantMessage);
            messageService.sendMessagePushNotification(message);
            // Distribute message to chat participants
            for (var participant : messageService.getChatParticipants(Long.parseLong(instantMessage.getReceiver()))) {
                messagingTemplate.convertAndSend(
                    "/topic/chat/" + participant.getUser().getId(), 
                    message
                );
            }
            logger.info("Message processed and distributed successfully with ID: {}", message.getId());
            
        } catch (Exception e) {
            logger.error("Failed to process message: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.CHAT_CREATED_QUEUE)
    public void handleChatCreated(Chat chat) {
        try {
            logger.info("Processing chat created event for chat ID {}", chat.getId());
            // Notify all participants about the new chat
            for (Participant participant : chat.getParticipants()) {
                messagingTemplate.convertAndSend(
                    "/topic/chat/" + participant.getUser().getId() + "/newchat",
                    chat
                );
            }
            logger.info("Chat created event processed successfully for chat ID: {}", chat.getId());
        } catch (Exception e) {
            logger.error("Failed to process chat created event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.TYPING_STATUS_QUEUE)
    public void handleTypingStatus(TypingStatus status, @Header("amqp_receivedRoutingKey") String routingKey) {
        try {
            String userId = routingKey.substring("chat.typing.".length());
            logger.info("Forwarding typing status for user {}: {}", userId, status.getUsername());

            //Send to participants in the chat
            for (var participant : messageService.getChatParticipants(Long.parseLong(userId))) {
                messagingTemplate.convertAndSend("/topic/chat/" + participant.getUser().getId() + "/typing", status);
            }

        } catch (Exception e) {
            logger.error("Failed to process typing status: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.READ_STATUS_QUEUE)
    public void handleReadStatus(ReadStatus status, @Header("amqp_receivedRoutingKey") String routingKey) {
        try {
            String chatId = routingKey.substring("chat.read.".length());
            logger.info("Forwarding read status for chat {}: message {}", chatId, status.getMessageId());
            for (var participant : messageService.getChatParticipants(Long.parseLong(chatId))) {
                messagingTemplate.convertAndSend("/topic/chat/" + participant.getUser().getId() + "/read", status);
            }
        } catch (Exception e) {
            logger.error("Failed to process read status: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.WEBRTC_CALL_OFFER_QUEUE)
    public void handleCallOffer(iranga.mg.social.dto.webrtc.CallOfferDto callOffer) {
        try {
            logger.info("Processing WebRTC call offer from {} to {}", 
                       callOffer.getCallerId(), callOffer.getReceiverId());
            callService.handleCallOfferNotification(callOffer);
            callService.createCallSession(callOffer);
            String canCallResult = callService.canCall(callOffer);
            if (!canCallResult.equals("OK")) {
                logger.info("Call cannot be placed: {}", canCallResult);
                handleCallEnd(new iranga.mg.social.dto.webrtc.CallEndDto(
                    callOffer.getCallId(),  
                    callOffer.getReceiverId(), 
                    canCallResult,
                    LocalDateTime.now()
                ));
            }
            messagingTemplate.convertAndSend("/topic/call/offer/" + callOffer.getReceiverId(), callOffer);
        } catch (Exception e) {
            logger.error("Failed to process call offer: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.WEBRTC_CALL_CANDIDATE_QUEUE)
    public void handleCallCandidate(iranga.mg.social.dto.webrtc.IceCandidateDto iceCandidate) {
        try {
            logger.info("Processing WebRTC ICE candidate for call {}", iceCandidate.getCallId());
            Long otherParticipantId = callService.getOtherParticipantId(iceCandidate.getCallId(), iceCandidate.getUserId());
            messagingTemplate.convertAndSend("/topic/call/candidate/" + otherParticipantId, iceCandidate);
        } catch (Exception e) {
            logger.error("Failed to process ICE candidate: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.WEBRTC_CALL_ANSWER_QUEUE)
    public void handleCallAnswer(iranga.mg.social.dto.webrtc.CallAnswerDto callAnswer) {
        try {
            logger.info("Processing WebRTC call answer for call {}",  callAnswer.getCallId());

            callService.updateStatus(callAnswer.getCallId(), iranga.mg.social.model.CallSession.CallStatus.CONNECTED);
            Long otherParticipantId = callService.getOtherParticipantId(callAnswer.getCallId(), callAnswer.getUserId());
            messagingTemplate.convertAndSend("/topic/call/answer/" + otherParticipantId, callAnswer);
        } catch (Exception e) {
            logger.error("Failed to process call answer: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.WEBRTC_CALL_END_QUEUE)
    public void handleCallEnd(iranga.mg.social.dto.webrtc.CallEndDto callEnd) {
        try {
            logger.info("Processing WebRTC call end for call {}", callEnd.getCallId());
            callService.updateStatus(callEnd.getCallId(), iranga.mg.social.model.CallSession.CallStatus.ENDED);
            Long otherParticipantId = callService.getOtherParticipantId(callEnd.getCallId(), callEnd.getUserId());
            messagingTemplate.convertAndSend("/topic/call/end/" + otherParticipantId, callEnd);
            // messagingTemplate.convertAndSend("/topic/call/end/" + callEnd.getCallerId(), callEnd);
        } catch (Exception e) {
            logger.error("Failed to process call end: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationDto notification, @Header("amqp_receivedRoutingKey") String routingKey) {
        try {
            logger.info("Processing notification for all users: {}", notification.getBody());
            Long userId = Long.parseLong(routingKey.substring("notification.".length()));
            notificationService.sendNotification(userId, notification);
        } catch (Exception e) {
            logger.error("Failed to process notification: {}", e.getMessage(), e);
        }
    }
}
