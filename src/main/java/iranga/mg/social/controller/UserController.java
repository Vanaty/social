package iranga.mg.social.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iranga.mg.social.dto.notif.ExpoTokenUpdate;
import iranga.mg.social.model.OnlineUser;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.OnlineUserRepository;
import iranga.mg.social.repository.UserRepository;
import iranga.mg.social.service.UserService;

@RequestMapping("/api/users")
@RestController
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OnlineUserRepository onlineUserRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/expo/token")
    @Operation(summary = "Update token for push notifications")
    public void updateExpoToken(@AuthenticationPrincipal UserDetails userDetails, @RequestBody ExpoTokenUpdate request) {
        logger.info("Updating Expo token for user {}: {}", userDetails.getUsername(), request);
        userService.saveExpoPushToken(userDetails.getUsername(), request.getToken());
    }

    @GetMapping("/info")
    @Operation(summary = "Get current user's information")
    public User getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/online")
    @Operation(summary = "Get list of online users")
    public List<String> getOnlineUsers() {
        return onlineUserRepository.findAll().stream()
                .map(OnlineUser::getUsername)
                .collect(Collectors.toList());
    }
}
