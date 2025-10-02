package iranga.mg.social.dto.webrtc;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallEndDto {
    private String callId;
    private Long userId; // User who ended the call
    private String reason; // "hangup", "timeout", "rejected", etc.
    private LocalDateTime timestamp;
}
