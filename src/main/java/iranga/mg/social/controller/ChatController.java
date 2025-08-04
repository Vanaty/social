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
    @Operation(summary = "Create a private chat with another user")
    public ResponseEntity<Chat> createPrivateChat(@Valid @RequestBody ChatDTO chatReq, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername()).orElse(null);
        User otherUser = userRepository.findById(chatReq.getParticipants().get(0)).orElse(null);

        if (currentUser == null || otherUser == null) {
            return ResponseEntity.badRequest().build();
        }
        Set<User> participants = new HashSet<>();
        participants.add(currentUser);
        participants.add(otherUser);
        Chat chat = chatService.createPrivateChat(participants.stream().toList());
        return ResponseEntity.ok(chat);
    }

    @PostMapping("/group")
    @Operation(summary = "Create a group chat with multiple users")
    public ResponseEntity<Chat> createGroupChat(@Valid @RequestBody ChatDTO chatDto, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findUserByUsername(userDetails.getUsername()).orElse(null);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Set<User> participants = new HashSet<>();
        userRepository.findAllByIdIn(chatDto.getParticipants()).forEach(participants::add);

        return ResponseEntity.ok(chatService.createGroupChat(chatDto.getChatName(), currentUser, participants.stream().toList()));
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
    public ResponseEntity<List<Message>> getChatMessages(@PathVariable Long chatId) {
        return ResponseEntity.ok(messageRepository.findByChatIdOrderByTimestampAsc(chatId));
    }
}
