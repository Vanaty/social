package iranga.mg.social.repository;

import iranga.mg.social.model.Chat;
import iranga.mg.social.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT DISTINCT c FROM Chat c JOIN c.participants p WHERE p.user = :user")
    Page<Chat> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Chat c JOIN c.participants p WHERE p.user = :user AND c.isGroupChat = true")
    List<Chat> findGroupChatsByUser(@Param("user") User user);

    @Query("SELECT DISTINCT c FROM Chat c JOIN c.participants p WHERE p.user = :user AND c.isGroupChat = false")
    List<Chat> findPrivateChatsByUser(@Param("user") User user);

    @Query("SELECT DISTINCT c FROM Chat c JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE p1.user = :user1 AND p2.user = :user2 AND c.isGroupChat = false")
    Optional<Chat> findPrivateChatByUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Chat c JOIN c.participants p WHERE c.id = :chatId AND p.user = :user")
    Optional<Chat> findByIdAndParticipant(@Param("chatId") Long chatId, @Param("user") User user);
}
