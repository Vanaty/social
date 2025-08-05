package iranga.mg.social.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import iranga.mg.social.config.RabbitConfig;
import iranga.mg.social.model.InstantChatMessage;

@Component
public class SendMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(SendMessageHandler.class);

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;

    @Autowired
    public SendMessageHandler(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    @Retryable(value = {AmqpException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void createExchange(String name) {
        try {
            DirectExchange exchange = new DirectExchange(name + "_exchange");
            amqpAdmin.declareExchange(exchange);
            
            Binding binding = new Binding(
                exchange.getName(), 
                DestinationType.EXCHANGE, 
                RabbitConfig.INCOMING_EXCHANGE,
                name + ".#", 
                null
            );
            amqpAdmin.declareBinding(binding);
            
            logger.info("Successfully created exchange: {}", exchange.getName());
        } catch (Exception e) {
            logger.error("Failed to create exchange for: {}", name, e);
            throw e;
        }
    }

    @Retryable(value = {AmqpException.class}, maxAttempts = 3, backoff = @Backoff(delay = 500))
    public void sendMessageToOutgoingExchange(InstantChatMessage message) {
        try {
            if (message == null) {
                logger.warn("Attempted to send null message");
                return;
            }
            
            rabbitTemplate.convertAndSend(RabbitConfig.OUTGOING_EXCHANGE, "", message);
            logger.debug("Message sent successfully from {} to {}", 
                        message.getSender(), message.getReceiver());
        } catch (Exception e) {
            logger.error("Failed to send message to outgoing exchange: {}", e.getMessage(), e);
            throw e;
        }
    }
}
