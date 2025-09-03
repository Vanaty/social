package iranga.mg.social.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateCommentRequest {
    private String content;
}
