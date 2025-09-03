package iranga.mg.social.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.Publication;
import iranga.mg.social.model.User;

@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {
    
    @Query("SELECT p FROM Publication p ORDER BY p.createdAt DESC")
    Page<Publication> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Publication p WHERE p.author = :author ORDER BY p.createdAt DESC")
    Page<Publication> findByAuthorOrderByCreatedAtDesc(@Param("author") User author, Pageable pageable);
    
    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Like l WHERE l.publication.id = :publicationId AND l.user.id = :userId")
    boolean existsLikeByPublicationAndUser(@Param("publicationId") Long publicationId, @Param("userId") Long userId);
}
