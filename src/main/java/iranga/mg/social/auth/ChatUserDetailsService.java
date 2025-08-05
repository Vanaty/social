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
    private UserRepository userRepository;

    @Override

    
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return new UserPrincipale(user);
    }
}
