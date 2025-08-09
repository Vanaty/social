package iranga.mg.social.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
@Table(name = "online_users")
public class OnlineUser {
	@Id
	private String id;
	private String username;
}
