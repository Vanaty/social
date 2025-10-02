package iranga.mg.social.dto.webrtc;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallOfferDto {
    private String callId;
    private String callerName;
    private Long callerId;
    private Long receiverId;
    private String type; // "video" or "audio"
    private Object offer; // SDP offer
    private LocalDateTime timestamp;
}
