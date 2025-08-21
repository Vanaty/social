package iranga.mg.social.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import iranga.mg.social.config.FileStorageConfig;
import iranga.mg.social.dto.FileResponse;
import iranga.mg.social.service.FileService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private  FileStorageConfig storageConf;

    @Autowired
    private FileService fileService;

    @PostConstruct
    public void init() {
        try {
            storageConf.createDirectories();
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storage = fileService.getStorage(file.getContentType());
        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            fileExtension = "";
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetLocation = Path.of(storage,newFileName).normalize();
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        String fileDownloadUri = URI.create("/api/files/download/"+ URI.create(targetLocation.toString().replace("\\", "/")).toString()).toString();
        String fileThumbPath = Path.of("/api/files/download/", fileService.getThumbnail(targetLocation).toString()).toString().replace("\\", "/");
        return ResponseEntity.ok(new FileResponse(fileDownloadUri, fileThumbPath));
    }

    @GetMapping("/download/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        try {
            // 1️⃣ Récupérer le chemin complet après "/download/"
            String requestPath = request.getRequestURI();
            String contextPath = request.getContextPath();
            String relativePath = requestPath.split("/download/")[1];
            System.out.println("Relative Path: " + relativePath);
            Path file = Path.of(relativePath).normalize();

            // 5️⃣ Vérification d'existence du fichier
            if (!Files.exists(file) || !Files.isReadable(file)) {
                return ResponseEntity.notFound().build();
            }

            // 6️⃣ Chargement de la ressource
            Resource resource = new UrlResource(file.toUri());

            // 7️⃣ Détection du type MIME
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
