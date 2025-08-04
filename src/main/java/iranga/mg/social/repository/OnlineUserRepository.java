package iranga.mg.social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.OnlineUser;


@Repository
public interface OnlineUserRepository extends JpaRepository<OnlineUser, String> {
	OnlineUser findByUsername(String username);
}