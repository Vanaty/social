package iranga.mg.social.dto.chat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TypingStatus {
    private String username;
    private boolean isTyping;
    // getters/setters
}
