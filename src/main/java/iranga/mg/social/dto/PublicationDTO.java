package iranga.mg.social.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonProperty;

import iranga.mg.social.model.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PublicationDTO {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private User author;
    private int likesCount;
    private int commentsCount;
    @JsonProperty("isLiked")
    private boolean isLiked;
}
