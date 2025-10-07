package iranga.mg.social.service;

import java.time.LocalDateTime;
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

    public List<String> getExpoPushToken(Long userId) {
        return expoTokenRepository.findByUserId(userId)
                .orElse(List.of())
                .stream()
                .map(ExpoToken::getToken)
                .toList();
    }

    public List<String> getExpoPushToken(String username) {
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

    @Transactional
    public void saveExpoPushToken(String username, String token) {
        // First, find the user by username to get the userId
        Long userId = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        
        //Delete old tokens for the user
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        expoTokenRepository.findOldTokenByUserId(userId, oneMonthAgo).ifPresent(oldTokens -> {
            expoTokenRepository.deleteAll(oldTokens);
        });
        // Check if the token already exists for the user
        ExpoToken existingToken = expoTokenRepository.findByToken(token)
                .orElse(new ExpoToken());
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
