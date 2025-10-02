package iranga.mg.social.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iranga.mg.social.dto.CreatePublicationRequest;
import iranga.mg.social.dto.CreateCommentRequest;
import iranga.mg.social.dto.PublicationDTO;
import iranga.mg.social.dto.UpdatePublicationRequest;
import iranga.mg.social.model.Comment;
import iranga.mg.social.model.Like;
import iranga.mg.social.model.Publication;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.UserRepository;
import iranga.mg.social.service.PublicationService;

@RestController
@RequestMapping("/api/publications")
@Tag(name = "Publication Management", description = "APIs for managing publications")
public class PublicationController {
    
    @Autowired
    private PublicationService publicationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    @Operation(summary = "Get all publications")
    public ResponseEntity<Page<PublicationDTO>> getAllPublications(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername()).orElse(null);
        return ResponseEntity.ok(publicationService.getAllPublications(pageable, currentUser));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get publication by ID")
    public ResponseEntity<PublicationDTO> getPublicationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername()).orElse(null);
        return ResponseEntity.ok(publicationService.getPublicationById(id, currentUser));
    }
    
    @PostMapping
    @Operation(summary = "Create a new publication")
    public ResponseEntity<Publication> createPublication(
            @RequestBody CreatePublicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User author = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Publication publication = publicationService.createPublication(
            request.getTitle(), 
            request.getContent(), 
            request.getImageUrl(), 
            author
        );
        return ResponseEntity.ok(publication);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a publication")
    public ResponseEntity<Publication> updatePublication(
            @PathVariable Long id,
            @RequestBody UpdatePublicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User author = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Publication publication = publicationService.updatePublication(
            id, 
            request.getTitle(), 
            request.getContent(), 
            request.getImageUrl(), 
            author
        );
        return ResponseEntity.ok(publication);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a publication")
    public ResponseEntity<Void> deletePublication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User author = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        publicationService.deletePublication(id, author);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/like")
    @Operation(summary = "Toggle like on a publication")
    public ResponseEntity<String> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Like result = publicationService.toggleLike(id, user);
        return ResponseEntity.ok(result != null ? "liked" : "unliked");
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Unlike a publication")
    public ResponseEntity<String> unLike(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Like result = publicationService.toggleLike(id, user);
        return ResponseEntity.ok(result != null ? "liked" : "unliked");
    }
    
    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to a publication")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long id,
            @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User author = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment comment = publicationService.addComment(id, request.getContent(), author);
        return ResponseEntity.ok(comment);
    }
    
    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comments for a publication")
    public ResponseEntity<Page<Comment>> getPublicationComments(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(publicationService.getPublicationComments(id, pageable));
    }
    
    @GetMapping("/user/{username}")
    @Operation(summary = "Get publications by user")
    public ResponseEntity<Page<PublicationDTO>> getUserPublications(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername()).orElse(null);
        
        return ResponseEntity.ok(publicationService.getUserPublications(user, pageable, currentUser));
    }
}
