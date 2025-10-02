package iranga.mg.social.dto.webrtc;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallAnswerDto {
    private String callId;
    private Long userId; // User who answered the call
    private Object answer; // SDP answer
    private LocalDateTime timestamp;
    private boolean accepted; // true if call accepted, false if rejected
}
