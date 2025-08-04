package iranga.mg.social.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstantChatMessage implements Serializable {
	private String sender;
	private String receiver;
	private String content;
}
