package iranga.mg.social.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    private String generateThumbnail(Path path) throws IOException {
        BufferedImage originalImage = ImageIO.read(path.toFile());

        int thumbnailWidth = 200;
        int thumbnailHeight = (originalImage.getHeight() * thumbnailWidth) / originalImage.getWidth();

        Image scaledImage = originalImage.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
        BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = thumbnail.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        String thumbFileName = "thumb_" + path.getFileName();
        Path thumbPath = path.getParent().resolve(thumbFileName);
        ImageIO.write(thumbnail, "jpg", thumbPath.toFile());

        return thumbFileName;
    }
}
