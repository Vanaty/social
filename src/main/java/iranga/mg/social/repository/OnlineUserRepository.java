package iranga.mg.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.OnlineUser;


@Repository
public interface OnlineUserRepository extends JpaRepository<OnlineUser, Long> {
	Optional<OnlineUser> findByUsername(String username);
	boolean existsByUsername(String username);
	void deleteByUsername(String username);
}