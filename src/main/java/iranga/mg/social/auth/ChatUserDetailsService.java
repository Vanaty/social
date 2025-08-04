package iranga.mg.social.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import iranga.mg.social.model.User;
import iranga.mg.social.repository.UserRepository;

@Service
public class ChatUserDetailsService implements UserDetailsService {

	@Autowired
	UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findUserByUsername(username).orElse(null);
		if(user == null) {
			throw new UsernameNotFoundException("username not found!!!");
		}
		return new UserPrincipale(user);
	}
}
