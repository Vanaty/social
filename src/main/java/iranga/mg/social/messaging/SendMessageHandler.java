package iranga.mg.social.messaging;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.model.InstantChatMessage;

@Component
public class SendMessageHandler {

	@Autowired
	RabbitMessagingTemplate rabbitMessagingTemplate;

	@Autowired
	AmqpAdmin amqpAdmin;

	public void createExchange(String name) {
		DirectExchange exchange = new DirectExchange(name + "_exchange");
		amqpAdmin.declareExchange(exchange);
		Binding binding = new Binding(exchange.getName(), DestinationType.EXCHANGE, RabbitConfig.INCOMING_EXCHANGE,
				name + ".#", null);
		amqpAdmin.declareBinding(binding);
	}

	public void sendMessageToOutgoingExchange(InstantChatMessage payload) {
		rabbitMessagingTemplate.convertAndSend(RabbitConfig.OUTGOING_EXCHANGE, "x", payload);
	}

}
