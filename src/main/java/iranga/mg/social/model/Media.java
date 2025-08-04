package iranga.mg.social.model;

import jakarta.persistence.*;
import iranga.mg.social.type.TypeMedia;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "media")
@Data
@Setter
@Getter
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Ensure correct strategy
    private Long id;
    private String url;
    private String name; // e.g., "photo.jpg", "video.mp4"
    @Enumerated(EnumType.STRING)
    private TypeMedia type = TypeMedia.FILE; // e.g., "image", "video", "audio"
    private String thumbnailUrl; // Optional, for images or videos
    private Long size; // Size in bytes
    private String format; // e.g., "jpg", "mp4", etc.
}
