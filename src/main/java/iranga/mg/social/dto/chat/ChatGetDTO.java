package iranga.mg.social.dto.chat;

import java.time.LocalDateTime;
import java.util.List;

import iranga.mg.social.dto.MessageDTO;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.Participant;
import iranga.mg.social.model.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ChatGetDTO {
    public ChatGetDTO(Chat chat, int unreadMessagesCount, Message latestMessage) {
        this.id = chat.getId();
        this.chatName = chat.getChatName();
        this.isGroupChat = chat.getIsGroupChat();
        this.createdAt = chat.getCreatedAt();
        this.participants = chat.getParticipants();
        this.unreadMessagesCount = unreadMessagesCount;
        this.latestMessage = latestMessage;
    }
    private Long id;
    private String chatName;
    private boolean isGroupChat;
    private LocalDateTime createdAt;
    private List<Participant> participants;
    private Message latestMessage;
    private int unreadMessagesCount;
}
