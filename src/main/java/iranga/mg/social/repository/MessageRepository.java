package iranga.mg.social.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);

    java.util.Optional<Message> findTopByChatIdOrderByTimestampDesc(Long chatId);
    int countByChatIdAndSenderIdNotAndIsReadFalse(Long chatId, Long senderId);
}
