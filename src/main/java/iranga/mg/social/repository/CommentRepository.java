package iranga.mg.social.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.Comment;
import iranga.mg.social.model.Publication;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @Query("SELECT c FROM Comment c WHERE c.publication = :publication ORDER BY c.createdAt DESC")
    Page<Comment> findByPublicationOrderByCreatedAtDesc(@Param("publication") Publication publication, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.publication.id = :publicationId")
    long countByPublicationId(@Param("publicationId") Long publicationId);
}
