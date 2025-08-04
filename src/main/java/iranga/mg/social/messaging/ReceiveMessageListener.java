package iranga.mg.social.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.controller.MessagingController;
import iranga.mg.social.model.Chat;
import iranga.mg.social.model.InstantChatMessage;
import iranga.mg.social.model.Message;
import iranga.mg.social.model.User;
import iranga.mg.social.repository.MessageRepository;
import iranga.mg.social.repository.OnlineUserRepository;

@Service
public class ReceiveMessageListener {

	private final static String LISTENER_CONTAINER = "queue_listener_container";
	private Logger logger = LoggerFactory.getLogger(MessagingController.class);

	@Autowired
	AmqpAdmin amqpAdmin;

	@Autowired
	RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

	@Autowired
	MessageRepository messageRepository;

	@Autowired
	RabbitMessagingTemplate rabbitMessagingTemplate;

	@Autowired
	SimpMessagingTemplate simpMessagingTemplate;

	@Autowired
	OnlineUserRepository onlineUserRepo;

	@RabbitListener(queues = { RabbitConfig.CHAT_MESSAGING_QUEUE })
	public void listenToChatMessagingQueue(InstantChatMessage payload) {
		String chatId = payload.getReceiver();
		String sender = payload.getSender();
		logger.info("---->>>" + sender);
		// create queue
		createMessageSenderQueue(sender, chatId);

		// save message
		Message msg = new Message();
		msg.setContentText(payload.getContent());

		Chat chat = new Chat();
		chat.setId(Long.parseLong(payload.getReceiver()));
		msg.setChat(chat);

		User senderUser = new User();
		senderUser.setId(Long.parseLong(sender));
		msg.setSender(senderUser);
		msg.setTimestamp(java.time.LocalDateTime.now(java.time.ZoneId.of("UTC")));
		messageRepository.save(msg);
		// send message
		rabbitMessagingTemplate.convertAndSend(RabbitConfig.INCOMING_EXCHANGE, chatId + "." + sender, payload);
	}

	public void createMessageSenderQueue(String sender, String receiver) {
		Queue queue = new Queue(sender + "_queue", true, false, false);
		amqpAdmin.declareQueue(queue);
		SimpleMessageListenerContainer container = (SimpleMessageListenerContainer) rabbitListenerEndpointRegistry
				.getListenerContainer(LISTENER_CONTAINER);
		container.addQueueNames(queue.getName());
		Binding binding = new Binding(queue.getName(), DestinationType.QUEUE, receiver + "_exchange",
				receiver + "." + sender, null);
		amqpAdmin.declareBinding(binding);
	}

	@RabbitListener(id = LISTENER_CONTAINER)
	public void senderQueueListener(InstantChatMessage payload) {
		simpMessagingTemplate.convertAndSend("/topic/" + payload.getReceiver() + "/reply", payload);
	}

	public void disconnectListener(String sessionId) {
		onlineUserRepo.deleteById(sessionId);
		logger.warn("disconnected............." + sessionId);
	}
}
