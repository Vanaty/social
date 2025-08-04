package iranga.mg.social.repository;

import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.Chat;
import iranga.mg.social.model.Participant;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
