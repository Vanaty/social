package iranga.mg.social.model;

import java.io.Serializable;

import iranga.mg.social.type.TypeMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstantChatMessage implements Serializable {
	private String sender;
	private String receiver; // Chat ID
	private String content;
	private String thumbnailUrl;
	private String fileUrl;
	private TypeMessage messageType = TypeMessage.TEXT;
	private String timestamp = String.valueOf(System.currentTimeMillis());
	private String type = "CHAT";
}
