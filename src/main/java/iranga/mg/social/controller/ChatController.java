package iranga.mg.social.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.ChatRepository;
import iranga.mg.social.repository.MessageRepository;
import iranga.mg.social.repository.UserRepository;
import iranga.mg.social.service.ChatService;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Chat Management", description = "APIs for managing chats")
public class ChatController {

    @Autowired
    private ChatService chatService;
    
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/private")
    @Operation(summary = "Create a private chat by username")
    public ResponseEntity<?> createPrivateChatByUsername(
            @RequestParam String otherUsername,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            
            User otherUser = userRepository.findUserByUsername(otherUsername)
                    .orElseThrow(() -> new RuntimeException("User not found: " + otherUsername));

            if (currentUser.getId().equals(otherUser.getId())) {
                return ResponseEntity.badRequest().body("Cannot create chat with yourself");
            }

            List<User> participants = List.of(currentUser, otherUser);
            Chat chat = chatService.createPrivateChat(participants);
            return ResponseEntity.ok(chat);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create chat: " + e.getMessage());
        }
    }

    @PostMapping("/group")
    @Operation(summary = "Create a group chat")
    public ResponseEntity<?> createGroupChatByUsernames(
            @RequestParam String chatName,
            @RequestParam List<String> usernames,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<User> participants = usernames.stream()
                .map(username -> userRepository.findUserByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username)))
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .toList();

        if (participants.isEmpty()) {
            return ResponseEntity.badRequest().body("At least one other user is required");
        }

        Chat chat = chatService.createGroupChat(chatName, currentUser, participants);
        return ResponseEntity.ok(chat);
    }

    @GetMapping
    @Operation(summary = "Get all chats for the authenticated user")
    public ResponseEntity<Page<Chat>> getUserChats(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername()).orElse(null);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(chatService.getUserChats(currentUser, pageable));
    }

    @GetMapping("/{chatId}/messages")
    @Operation(summary = "Get all messages in a chat")
    public ResponseEntity<Page<Message>> getChatMessages(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<Message> messages = chatService.getChatMessages(chatId, currentUser, pageable);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{chatId}")
    @Operation(summary = "Get chat details")
    public ResponseEntity<Chat> getChatDetails(@PathVariable Long chatId, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chat chat = chatService.getChatDetails(chatId, currentUser);
        return ResponseEntity.ok(chat);
    }
}