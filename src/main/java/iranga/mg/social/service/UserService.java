package iranga.mg.social.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iranga.mg.social.model.ExpoToken;
import iranga.mg.social.repository.ExpoTokenRepository;
import iranga.mg.social.repository.OnlineUserRepository;
import iranga.mg.social.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Service
public class UserService {
    private UserRepository userRepository;
    private OnlineUserRepository onlineUserRepository;
    private ExpoTokenRepository expoTokenRepository;

    public String getExpoPushToken(Long userId) {
        return expoTokenRepository.findByUserId(userId).orElse(new ExpoToken()).getToken();
    }

    public void saveExpoPushToken(String username, String token) {
        Long userId = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        ExpoToken existingToken = expoTokenRepository.findByUserId(userId).orElse(new ExpoToken());
        if(token != null && !token.isBlank()) {
            existingToken.setToken(token);
            existingToken.setUser(userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found")));
            expoTokenRepository.save(existingToken);
        }
    }
}
