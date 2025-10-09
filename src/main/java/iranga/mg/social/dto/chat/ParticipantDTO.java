package iranga.mg.social.dto.chat;

import iranga.mg.social.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantDTO {
    public ParticipantDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFirstName() + " " + user.getLastName();
    }
    private Long id;
    private String username;
    private String fullName;
}
