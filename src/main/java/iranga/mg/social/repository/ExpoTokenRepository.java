package iranga.mg.social.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import iranga.mg.social.model.ExpoToken;

public interface ExpoTokenRepository extends JpaRepository<ExpoToken, Long> {
    @Query("SELECT e FROM ExpoToken e WHERE e.user.id = ?1 ORDER BY e.createdAt DESC")
    public Optional<List<ExpoToken>> findByUserId(Long userId);

    @Query("SELECT e FROM ExpoToken e WHERE e.token = ?1")
    public Optional<ExpoToken> findByToken(String token);

    @Query("SELECT e FROM ExpoToken e WHERE e.user.id = ?1 AND e.createdAt < :oneMonthAgo")
    Optional<List<ExpoToken>> findOldTokenByUserId(@Param("userId") Long userId, @Param("oneMonthAgo") Instant oneMonthAgo);

}
