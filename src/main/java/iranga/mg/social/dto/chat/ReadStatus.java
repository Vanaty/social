package iranga.mg.social.dto.chat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ReadStatus {
    private Long messageId;
    private String username;
    // getters/setters
}
