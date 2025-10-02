package iranga.mg.social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.CallSession;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, String> {   
    java.util.List<CallSession> findByStatusIn(java.util.List<CallSession.CallStatus> statuses);
    java.util.List<CallSession> findByStatusInAndEndTimeIsNull(java.util.List<CallSession.CallStatus> statuses); 
    java.util.List<CallSession> findByReceiverIdAndStatusIn(Long receiverId, java.util.List<CallSession.CallStatus> statuses);
}
