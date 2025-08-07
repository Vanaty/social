package iranga.mg.social.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Getter
@Setter
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String username;


	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	private Role role;

	@JsonIgnore
	@Column(nullable = false, length = 100) // Ensure password is not null
	private String password;

	@Column(unique = true, length = 100)
	private String email;

	@Column(length = 50)
	private String firstName;

	@Column(length = 100)
	private String lastName;

	@Column(length = 15)
	private String phoneNumber;

	@Column(length = 255)
	private String address;

	@Column(length = 255)
	private String profilePictureUrl="/icons/default-profile.png";
	
}
