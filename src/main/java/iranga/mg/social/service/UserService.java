package iranga.mg.social.service;

import java.util.List;

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

    public String getExpoPushToken(String username) {
        Long userId = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        return getExpoPushToken(userId);
    }

    public List<String> getAllExpoPushTokenExcept(Long excludeUserId) {
        return expoTokenRepository.findAll().stream()
                .filter(token -> !token.getUser().getId().equals(excludeUserId))
                .map(ExpoToken::getToken)
                .toList();
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

    @Transactional
    public void setUserOnlineStatus(String username, boolean isOnline) {
        if (isOnline) {
            if (!onlineUserRepository.existsByUsername(username)) {
                onlineUserRepository.save(new iranga.mg.social.model.OnlineUser(null, username));
            }
        } else {
            onlineUserRepository.deleteByUsername(username);
        }
    }
}
