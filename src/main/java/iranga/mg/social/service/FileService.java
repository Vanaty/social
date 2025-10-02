package iranga.mg.social.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iranga.mg.social.config.FileStorageConfig;

@Service
public class FileService {
    @Autowired
    FileStorageConfig fileStorageConfig;
    
    public String storeFile(Path fileStorageLocation, String originalFileName, byte[] fileBytes) {
        try {
            Path targetLocation = fileStorageLocation.resolve(originalFileName);
            Files.write(targetLocation, fileBytes);
            return targetLocation.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", e);
        }
    }

    public String getFileContentType(Path filePath) {
        try {
            return Files.probeContentType(filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return "application/octet-stream";
        }
    }

    public String getStorage(String  contentType) {
        contentType = contentType.toLowerCase();
        if (contentType.contains("image")) {
            return fileStorageConfig.getImagesDir();
        }  else {
            return fileStorageConfig.getFilesDir();
        }
    }

    public Path getPath(String fileName) {
        return  Path.of(this.fileStorageConfig.getFilesDir(), fileName);
    }

    private Path generateImageThumbnail(Path path) throws IOException {
        BufferedImage originalImage = ImageIO.read(path.toFile());

        int thumbnailWidth = 200;
        int thumbnailHeight = (originalImage.getHeight() * thumbnailWidth) / originalImage.getWidth();

        Image scaledImage = originalImage.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
        BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = thumbnail.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        String thumbFileName = path.getFileName().toString();
        Path thumbPath = Path.of(fileStorageConfig.getThumbnailDir(), thumbFileName);
        ImageIO.write(thumbnail, "jpg", thumbPath.toFile());
        return thumbPath;
    }


    public Path getThumbnail(Path path) throws IOException {
        String type = this.getFileContentType(path);
        if (type.contains("image")) {
            return this.generateImageThumbnail(path);
        } else {
            return Path.of("icons/file.png");
        }
        //TODO Ajouter les different thumb
    }

    public static void  main(String[] args) throws IOException {
        System.out.println(URI.create(Paths.get("data/google").toString()));
    }
}
