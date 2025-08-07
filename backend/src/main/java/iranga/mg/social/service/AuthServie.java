package iranga.mg.social.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import iranga.mg.social.dto.auth.RegisterRequest;
import iranga.mg.social.model.Role;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.RoleRepository;
import iranga.mg.social.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthServie {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    public User registreUser(RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setAddress(registerRequest.getAddress());
        user.setProfilePictureUrl(registerRequest.getProfilePictureUrl());
        Role role = roleRepository.findById(registerRequest.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found with id: " + registerRequest.getRoleId()));
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return user;
        
    }
}
