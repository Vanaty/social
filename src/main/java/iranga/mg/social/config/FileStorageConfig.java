package iranga.mg.social.config;

import java.net.URI;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "file")
@Getter
@Setter
@Data
public class FileStorageConfig {
    private String uploadDir = "./uploads";
    private String thumbnailDir =  Paths.get(uploadDir, "thumbnails").toString();
    private String imagesDir = Paths.get(uploadDir, "images").toString();
    private String videosDir = Paths.get(uploadDir, "videos").toString();
    private String filesDir = Paths.get(uploadDir, "files").toString();
    private String audioDir = Paths.get(uploadDir, "audio").toString();

    public void createDirectories() {
        try {
            java.nio.file.Files.createDirectories(Paths.get(uploadDir));
            java.nio.file.Files.createDirectories(Paths.get(thumbnailDir));
            java.nio.file.Files.createDirectories(Paths.get(imagesDir));
            java.nio.file.Files.createDirectories(Paths.get(videosDir));
            java.nio.file.Files.createDirectories(Paths.get(filesDir));
            java.nio.file.Files.createDirectories(Paths.get(audioDir));
        } catch (Exception e) {
            throw new RuntimeException("Could not create upload directories", e);
        }
    }

    public static void main(String[] args) {
        // This main method is just a placeholder to allow the class to be run independently.
        // In a real application, this class would be used by the Spring context.
        System.out.println(URI.create("/HH/test.png"));
    }
}
