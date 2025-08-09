package iranga.mg.social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.Participant;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
