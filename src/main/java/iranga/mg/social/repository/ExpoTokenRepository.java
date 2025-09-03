package iranga.mg.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import iranga.mg.social.model.ExpoToken;

public interface ExpoTokenRepository extends JpaRepository<ExpoToken, Long> {
    @Query("SELECT e FROM ExpoToken e WHERE e.user.id = ?1 ORDER BY e.createdAt DESC")
    public Optional<ExpoToken> findByUserId(Long userId);
}
