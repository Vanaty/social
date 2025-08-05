package iranga.mg.social.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iranga.mg.social.dto.chat.ChatDTO;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.ChatRepository;
import iranga.mg.social.repository.MessageRepository;
import iranga.mg.social.repository.UserRepository;
import iranga.mg.social.service.ChatService;
import jakarta.validation.Valid;

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

    @Autowired
    private MessageRepository messageRepository;
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
        try {
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create group chat: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "Get all chats for the authenticated user")
    public ResponseEntity<List<Chat>> getUserChats(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername()).orElse(null);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(chatRepository.findByUser(currentUser));
    }

    @GetMapping("/{chatId}/messages")
    @Operation(summary = "Get all messages in a chat")
    public ResponseEntity<?> getChatMessages(
            @PathVariable Long chatId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findUserByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify user is participant in this chat
            Chat chat = chatRepository.findByIdAndParticipant(chatId, currentUser)
                    .orElseThrow(() -> new RuntimeException("Chat not found or access denied"));

            List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chatId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: " + e.getMessage());
        }
    }
}
