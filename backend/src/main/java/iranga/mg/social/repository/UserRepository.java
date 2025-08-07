package iranga.mg.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import iranga.mg.social.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	Optional<User> findUserByUsername(String username);
	List<User> findAllByIdIn(List<Long> ids);
}
