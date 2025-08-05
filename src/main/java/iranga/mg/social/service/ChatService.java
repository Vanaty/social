package iranga.mg.social.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import iranga.mg.social.exception.AccessGranted;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.Participant;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.ChatRepository;

@Service
@Transactional
public class ChatService {
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageService messageService;

    public Chat createPrivateChat(List<User> users) {
        if (users.size() != 2) {
            throw new IllegalArgumentException("Private chat must have exactly 2 users");
        }

        // Check if private chat already exists
        Optional<Chat> existingChat = chatRepository.findPrivateChatByUsers(users.get(0), users.get(1));
        if (existingChat.isPresent()) {
            return existingChat.get();
        }

        Chat chat = new Chat();
        chat.setIsGroupChat(false);
        chat.setChatName("Private Chat");
        chat.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        
        // Create participants and set bidirectional relationship
        for (User user : users) {
            Participant participant = new Participant();
            participant.setUser(user);
            participant.setChat(chat);
            participant.setJoinedAt(LocalDateTime.now(ZoneId.of("UTC")));
            participant.setIsNotifActive(true);
            chat.getParticipants().add(participant);
        }
        
        return chatRepository.save(chat);
    }

    public Chat createGroupChat(String chatName, User admin, List<User> participants) {
        Chat chat = new Chat();
        chat.setChatName(chatName);
        chat.setIsGroupChat(true);
        chat.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC")));
        
        // Add admin as participant
        Participant adminParticipant = new Participant();
        adminParticipant.setUser(admin);
        adminParticipant.setChat(chat);
        adminParticipant.setJoinedAt(LocalDateTime.now(ZoneId.of("UTC")));
        adminParticipant.setIsNotifActive(true);
        adminParticipant.setIsAdmin(true);
        chat.getParticipants().add(adminParticipant);
        
        // Add other participants
        for (User user : participants) {
            if (!user.getId().equals(admin.getId())) { // Avoid duplicate admin
                Participant participant = new Participant();
                participant.setUser(user);
                participant.setChat(chat);
                participant.setJoinedAt(LocalDateTime.now(ZoneId.of("UTC")));
                participant.setIsNotifActive(true);
                chat.getParticipants().add(participant);
            }
        }
        
        return chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    public Page<Message> getChatMessages(Long chatId,User u, Pageable pageable) {
        Optional<Chat> chatOpt = chatRepository.findById(chatId);
        if (chatOpt.isEmpty()) {
            throw new RuntimeException("Chat not found");
        }
        Chat chat = chatOpt.get();
        boolean hasAccess = chat.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(u.getId()));
        if (!hasAccess) {
            throw new AccessGranted("Access denied to chat messages");
        }
        return messageService.getChatMessages(chatId, pageable);
    }

}
