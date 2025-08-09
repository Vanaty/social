package iranga.mg.social.repository;
import iranga.mg.social.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface  RoleRepository  extends JpaRepository<Role, Long> {
}
