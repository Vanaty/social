package iranga.mg.social.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import iranga.mg.social.messaging.SendMessageHandler;
import iranga.mg.social.model.InstantChatMessage;

@RestController
public class MessagingController {

	@Autowired
	SendMessageHandler sender;

	Logger logger = LoggerFactory.getLogger(MessagingController.class);

	@MessageMapping("/chat.subscribe")
	@SendTo("/topic/reply")
	public void subscribe(@Payload String name) {
		sender.createExchange(name);
		logger.info("subscribed............" + name);
	}

	@MessageMapping("/chat.message")
	@SendTo("/topic/reply")
	public void sendMessage(@Payload InstantChatMessage payload) {
		logger.info("send message from " + payload.getSender() + " to " + payload.getReceiver());
		sender.sendMessageToOutgoingExchange(payload);
	}


	// @Autowired
	// private SimpMessagingTemplate messagingTemplate;
	// @Autowired
	// private MessageRepository messageRepository;
	// @Autowired
	// private UserRepository userRepository;
	// @Autowired
	// private ChatRepository chatRepository;


	// Logger logger = LoggerFactory.getLogger(MessagingController.class);

	// @MessageMapping("/chat.sendMessage")
	// public void sendMessage(@Payload Message chatMessage) {
	// 	logger.info("Received message: " + chatMessage.getContent());
	// 	User sender = userRepository.findUserByUsername(chatMessage.getSender().getUsername()).orElseThrow();
	// 	chatMessage.setSender(sender);
	// 	chatMessage.setTimestamp(LocalDateTime.now());
	// 	Message savedMessage = messageRepository.save(chatMessage);

	// 	// Populate chat for the response
	// 	savedMessage.setChat(chatRepository.findById(chatMessage.getChat().getId()).orElse(null));

	// 	messagingTemplate.convertAndSend("/topic/chat/" + savedMessage.getChat().getId(), savedMessage);
	// }

}