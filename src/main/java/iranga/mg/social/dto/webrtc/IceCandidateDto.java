package iranga.mg.social.dto.webrtc;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IceCandidateDto {
    private String callId;
    private Long userId;
    private Object candidate; // ICE candidate
    private LocalDateTime timestamp;
}
