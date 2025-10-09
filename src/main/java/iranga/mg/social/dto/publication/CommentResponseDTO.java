package iranga.mg.social.dto.publication;

import java.time.LocalDateTime;
import java.util.Map;

import iranga.mg.social.model.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class CommentResponseDTO {
    private Long id;

    private String content;

    private LocalDateTime createdAt;

    private User author;
    private Map<String, Object> publication;
}
