package iranga.mg.social.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iranga.mg.social.dto.PublicationDTO;
import iranga.mg.social.dto.notif.NotificationDto;
import iranga.mg.social.messaging.PublicationProducer;
import iranga.mg.social.model.Comment;
import iranga.mg.social.model.Like;
import iranga.mg.social.model.Publication;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.CommentRepository;
import iranga.mg.social.repository.LikeRepository;
import iranga.mg.social.repository.PublicationRepository;

@Service
@Transactional
public class PublicationService {
    
    @Autowired
    private PublicationRepository publicationRepository;
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PublicationProducer publicationProducer;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PublicationService.class);
    
    public Publication createPublication(String title, String content, String imageUrl, User author) {
        Publication publication = new Publication();
        publication.setTitle(title);
        publication.setContent(content);
        publication.setImageUrl(imageUrl);
        publication.setAuthor(author);
        publication.setCreatedAt(LocalDateTime.now());
        publication.setUpdatedAt(LocalDateTime.now());

        publicationProducer.sendPublication(convertToDTO(publication, null));
        sendNotification(publication);
        return publicationRepository.save(publication);
    }

    @Async
    public void sendNotification(Publication publication) {
        logger.info("Sending notification for new publication by user {}", publication.getAuthor().getUsername());
        NotificationDto notification = new NotificationDto();

        notification.setTitle("Nouvelle publication de " + publication.getAuthor().getUsername());
        notification.setBody(publication.getTitle().length() > 50 ? publication.getTitle().substring(0, 50) + "..." : publication.getTitle());
        notification.setData(Map.of("type", "publication", "sender", publication.getAuthor().getUsername()));
        notificationService.sendNotification(publication.getAuthor().getId(), notification);
        logger.info("Notification sent for publication id {}", publication.getId());
    }
    
    public Publication updatePublication(Long id, String title, String content, String imageUrl, User author) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        
        if (!publication.getAuthor().getId().equals(author.getId())) {
            throw new RuntimeException("You can only edit your own publications");
        }
        
        publication.setTitle(title);
        publication.setContent(content);
        publication.setImageUrl(imageUrl);
        publication.setUpdatedAt(LocalDateTime.now());
        
        return publicationRepository.save(publication);
    }
    
    public void deletePublication(Long id, User author) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        
        if (!publication.getAuthor().getId().equals(author.getId())) {
            throw new RuntimeException("You can only delete your own publications");
        }
        
        publicationRepository.delete(publication);
    }
    
    @Transactional(readOnly = true)
    public Page<PublicationDTO> getAllPublications(Pageable pageable, User currentUser) {
        Page<Publication> publications = publicationRepository.findAllOrderByCreatedAtDesc(pageable);
        return publications.map(pub -> convertToDTO(pub, currentUser));
    }
    
    @Transactional(readOnly = true)
    public Page<PublicationDTO> getUserPublications(User user, Pageable pageable, User currentUser) {
        Page<Publication> publications = publicationRepository.findByAuthorOrderByCreatedAtDesc(user, pageable);
        return publications.map(pub -> convertToDTO(pub, currentUser));
    }
    
    @Transactional(readOnly = true)
    public PublicationDTO getPublicationById(Long id, User currentUser) {
        Publication publication = publicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        return convertToDTO(publication, currentUser);
    }
    
    public Like toggleLike(Long publicationId, User user) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        
        return likeRepository.findByUserAndPublication(user, publication)
                .map(existingLike -> {
                    likeRepository.delete(existingLike);
                    return (Like) null;
                })
                .orElseGet(() -> {
                    Like newLike = new Like();
                    newLike.setUser(user);
                    newLike.setPublication(publication);
                    return likeRepository.save(newLike);
                });
    }
    
    public Comment addComment(Long publicationId, String content, User author) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(author);
        comment.setPublication(publication);

        comment = commentRepository.save(comment);
        publicationProducer.sendComment(comment);
        return comment;
    }
    
    @Transactional(readOnly = true)
    public Page<Comment> getPublicationComments(Long publicationId, Pageable pageable) {
        Publication publication = publicationRepository.findById(publicationId)
                .orElseThrow(() -> new RuntimeException("Publication not found"));
        
        return commentRepository.findByPublicationOrderByCreatedAtDesc(publication, pageable);
    }
    
    private PublicationDTO convertToDTO(Publication publication, User currentUser) {
        PublicationDTO dto = new PublicationDTO();
        dto.setId(publication.getId());
        dto.setTitle(publication.getTitle());
        dto.setContent(publication.getContent());
        dto.setImageUrl(publication.getImageUrl());
        dto.setCreatedAt(publication.getCreatedAt());
        dto.setUpdatedAt(publication.getUpdatedAt());
        dto.setAuthor(publication.getAuthor());
        dto.setLikesCount(publication.getLikesCount());
        dto.setCommentsCount(publication.getCommentsCount());
        
        if (currentUser != null) {
            dto.setLiked(publicationRepository.existsLikeByPublicationAndUser(publication.getId(), currentUser.getId()));
        }
        
        return dto;
    }
}
