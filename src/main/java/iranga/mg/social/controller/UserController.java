package iranga.mg.social.controller;

import iranga.mg.social.model.OnlineUser;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.OnlineUserRepository;
import iranga.mg.social.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OnlineUserRepository onlineUserRepository;

    @GetMapping
    public List<String> getAllUsers() {
        return userRepository.findAll().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
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
