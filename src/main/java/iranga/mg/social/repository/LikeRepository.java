package iranga.mg.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.Like;
import iranga.mg.social.model.Publication;
import iranga.mg.social.model.User;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    Optional<Like> findByUserAndPublication(User user, Publication publication);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.publication.id = :publicationId")
    long countByPublicationId(@Param("publicationId") Long publicationId);
    
    void deleteByUserAndPublication(User user, Publication publication);
}
