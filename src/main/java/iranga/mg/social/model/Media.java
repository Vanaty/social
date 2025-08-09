package iranga.mg.social.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Data
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1024)
    private String thumbnailUrl;

    @Column(nullable = false, length = 1024)
    private String fileUrl;

    private String mediaType; // e.g., 'image/png'

    private Long fileSize;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "media")
    @JsonIgnore
    private Message message;
}
