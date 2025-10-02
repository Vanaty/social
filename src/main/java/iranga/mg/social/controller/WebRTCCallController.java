package iranga.mg.social.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import iranga.mg.social.dto.webrtc.CallAnswerDto;
import iranga.mg.social.dto.webrtc.CallEndDto;
import iranga.mg.social.dto.webrtc.CallOfferDto;
import iranga.mg.social.dto.webrtc.IceCandidateDto;
import iranga.mg.social.messaging.MessageProducer;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.UserRepository;

@RestController
public class WebRTCCallController {

    private static final Logger logger = LoggerFactory.getLogger(WebRTCCallController.class);

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/call/offer")
    public void handleCallOffer(@Payload CallOfferDto callOffer, Principal principal) {
        try {
            UserDetails userDetails = getUserDetails(principal);
            if (userDetails == null) return;

            User caller = userRepository.findUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Caller not found"));

            User receiver = userRepository.findById(callOffer.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));

            // Generate call ID if not provided
            if (callOffer.getCallId() == null || callOffer.getCallId().isEmpty()) {
                callOffer.setCallId(UUID.randomUUID().toString());
            }

            callOffer.setCallerId(caller.getId());
            callOffer.setTimestamp(LocalDateTime.now());

            logger.info("Call offer from {} to {} with call ID {}", 
                       caller.getUsername(), receiver.getUsername(), callOffer.getCallId());

            // Also queue for potential push notification
            messageProducer.sendCallOffer(callOffer);

        } catch (Exception e) {
            logger.error("Error handling call offer: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/call/answer")
    public void handleCallAnswer(@Payload CallAnswerDto callAnswer, Principal principal) {
        try {
            UserDetails userDetails = getUserDetails(principal);
            if (userDetails == null) return;
            callAnswer.setTimestamp(LocalDateTime.now());

            logger.info("Call answer from {} for call ID {}", 
                       userDetails.getUsername(), callAnswer.getCallId());

            messageProducer.sendCallAnswer(callAnswer);

        } catch (Exception e) {
            logger.error("Error handling call answer: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/call/candidate")
    public void handleIceCandidate(@Payload IceCandidateDto iceCandidate, Principal principal) {
        try {
            UserDetails userDetails = getUserDetails(principal);
            if (userDetails == null) return;

            User user = userRepository.findUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            iceCandidate.setUserId(user.getId());
            iceCandidate.setTimestamp(LocalDateTime.now());

            logger.debug("ICE candidate from {} for call ID {}", 
                        user.getUsername(), iceCandidate.getCallId());

            messageProducer.sendIceCandidate(iceCandidate);

        } catch (Exception e) {
            logger.error("Error handling ICE candidate: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/call/end")
    public void handleCallEnd(@Payload CallEndDto callEnd, Principal principal) {
        try {
            UserDetails userDetails = getUserDetails(principal);
            if (userDetails == null) return;

            User user = userRepository.findUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            callEnd.setUserId(user.getId());
            callEnd.setTimestamp(LocalDateTime.now());

            logger.info("Call end from {} for call ID {}", 
                       user.getUsername(), callEnd.getCallId());

            messageProducer.sendCallEnd(callEnd);

        } catch (Exception e) {
            logger.error("Error handling call end: {}", e.getMessage(), e);
        }
    }

    private UserDetails getUserDetails(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            return (UserDetails) auth.getPrincipal();
        } else {
            logger.warn("Unauthenticated user attempted WebRTC operation");
            return null;
        }
    }
}
