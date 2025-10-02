package iranga.mg.social.model;

import java.time.LocalDateTime;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
public class CallSession {
    @Id
    private String callId;

    @ManyToOne(fetch = FetchType.EAGER)
    private User caller;

    @ManyToOne(fetch = FetchType.EAGER)
    private User receiver;

    private CallType type; // AUDIO, VIDEO
    private CallStatus status; // INITIATING, RINGING, CONNECTED, ENDED
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public enum CallType {
        AUDIO,
        VIDEO
    }

    public enum CallStatus {
        INITIATING,
        RINGING,
        CONNECTED,
        ENDED
    }

}
