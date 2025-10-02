package iranga.mg.social.service;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import iranga.mg.social.dto.notif.ExpoNotification;
import iranga.mg.social.dto.webrtc.CallOfferDto;
import iranga.mg.social.model.CallSession;
import iranga.mg.social.model.CallSession.CallType;
import iranga.mg.social.model.OnlineUser;
import iranga.mg.social.repository.CallSessionRepository;
import iranga.mg.social.repository.OnlineUserRepository;
import iranga.mg.social.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CallService {
    private CallSessionRepository callSessionRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;
    private OnlineUserRepository onlineUserRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CallService.class);

    @Transactional
    public CallSession createCallSession(CallOfferDto callSession) {
        CallSession session = new CallSession();
        session.setCaller(userRepository.findById(callSession.getCallerId()).orElseThrow());
        session.setReceiver(userRepository.findById(callSession.getReceiverId()).orElseThrow());
        session.setStartTime(java.time.LocalDateTime.now());
        session.setType(CallType.valueOf(callSession.getType().toUpperCase()));
        session.setStatus(CallSession.CallStatus.INITIATING);
        session.setCallId(callSession.getCallId());
        session.setEndTime(null);
        return callSessionRepository.save(session);
    }
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String canCall(CallOfferDto callOfferDto) {
        OnlineUser receiver = onlineUserRepository.findByUsername(
            userRepository.findById(callOfferDto.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"))
                .getUsername()
        ).orElse(null);

        if (receiver == null) {
            return "OFFLINE";
        }
        List<CallSession> ongoingCalls = callSessionRepository.findByReceiverIdAndStatusIn(
            receiver.getId(),List.of(CallSession.CallStatus.INITIATING, CallSession.CallStatus.CONNECTED)
        );

        if (!ongoingCalls.isEmpty()) {
            return "BUSY";
        }

        return "OK";
    }

    public void updateStatus(String callId, CallSession.CallStatus status) {
        CallSession session = callSessionRepository.findById(callId).orElseThrow();
        session.setStatus(status);
        if (status == CallSession.CallStatus.ENDED) {
            session.setEndTime(java.time.LocalDateTime.now());
        }
        callSessionRepository.save(session);
    }

    public CallSession getCallSession(String callId) {
        return callSessionRepository.findById(callId).orElseThrow();
    }

    public Long getOtherParticipantId(String callId, Long userId) {
        CallSession session = getCallSession(callId);
        if (session.getCaller().getId().equals(userId)) {
            return session.getReceiver().getId();
        } else if (session.getReceiver().getId().equals(userId)) {
            return session.getCaller().getId();
        } else {
            throw new IllegalArgumentException("User is not a participant in this call");
        }
    }

    public void handleCallOfferNotification(iranga.mg.social.dto.webrtc.CallOfferDto callOffer) {
        try {
            ExpoNotification payload = new ExpoNotification();
            payload.setTitle("Appel entrant");
            payload.setBody("Vous avez un appel de " + callOffer.getCallerName());
            payload.setData(Map.of(
                "type", "call",
                "callData", callOffer
            ));
            payload.setAndroid(Map.of(
                "channelId", "calls",
                "actions", List.of(
                    Map.of("identifier", "ANSWER", "title", "Répondre"),
                    Map.of("identifier", "DECLINE", "title", "Refuser")
                ),
                "fullScreenIntent", true
            ));

            notificationService.sendNotification(
                userRepository.findById(callOffer.getReceiverId())
                    .orElseThrow(() -> new IllegalArgumentException("Receiver not found"))
                    .getUsername(),
                payload
            );
            
        } catch (Exception e) {
            logger.error("Failed to send call notification: {}", e.getMessage(), e);
        }
    }
}
