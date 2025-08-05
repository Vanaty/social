package iranga.mg.social.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

	public final static String CHAT_MESSAGING_QUEUE = "chat_messaging_queue";
	public final static String INCOMING_EXCHANGE = "incoming_exchange";
	public final static String OUTGOING_EXCHANGE = "outgoing_exchange";
    public final static String NOTIFICATION_QUEUE = "notification_queue";
    public final static String DEAD_LETTER_QUEUE = "dead_letter_queue";
    public final static String NOTIFICATION_EXCHANGE = "notification_exchange";

	@Bean
	public Queue createChatMessagingQueue() {
		return QueueBuilder.durable(CHAT_MESSAGING_QUEUE).build();
	}

	@Bean
	public TopicExchange createIncomingExchange() {
		return ExchangeBuilder.topicExchange(INCOMING_EXCHANGE).build();
	}

	@Bean
	public FanoutExchange createOutgoingExchange() {
		return ExchangeBuilder.fanoutExchange(OUTGOING_EXCHANGE).build();
	}

	@Bean
	public Queue createNotificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_QUEUE)
                .withArgument("x-message-ttl", 60000) // 1 minute TTL
                .build();
    }

    @Bean
    public Queue createDeadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public TopicExchange createNotificationExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICATION_EXCHANGE).build();
    }

	@Bean
	public Binding createNotificationBinding() {
        return BindingBuilder.bind(createNotificationQueue())
                .to(createNotificationExchange())
                .with("notification.*");
    }

	@Bean
	public Binding createChatMessageBinding() {
        return BindingBuilder.bind(createChatMessagingQueue())
                .to(createOutgoingExchange());
    }

	@Bean
	public MessageConverter jsonMessageConverter() {
	    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
	    DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
	    typeMapper.setTrustedPackages("iranga.mg.social.model");
	    converter.setJavaTypeMapper(typeMapper);
	    return converter;
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
	    RabbitTemplate template = new RabbitTemplate(connectionFactory);
	    template.setMessageConverter(jsonMessageConverter());
	    return template;
	}

}
