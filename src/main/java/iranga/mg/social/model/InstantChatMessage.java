package iranga.mg.social.model;

import java.io.Serializable;

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
	private String receiver;
	private String content;
	private String timestamp = String.valueOf(System.currentTimeMillis());
	private String type = "CHAT";
}
