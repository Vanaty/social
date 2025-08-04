package iranga.mg.social.dto.chat;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatDTO {
    private Long id;
    private String chatName;
    private String chatType;

    @NotEmpty(message = "At least one participant is required")
    private List<Long> participants;
}
