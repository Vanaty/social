package iranga.mg.social.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
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
    public final static String TYPING_STATUS_QUEUE = "typing_status_queue";
    public final static String READ_STATUS_QUEUE = "read_status_queue";
    public final static String WEBRTC_CALL_QUEUE = "webrtc_call_queue";
    public final static String WEBRTC_CALL_EXCHANGE = "webrtc_call_exchange";
    public final static String WEBRTC_CALL_OFFER_QUEUE = "webrtc_call_offer_queue";
    public final static String WEBRTC_CALL_ANSWER_QUEUE = "webrtc_call_answer_queue";
    public final static String WEBRTC_CALL_END_QUEUE = "webrtc_call_end_queue";
    public final static String WEBRTC_CALL_CANDIDATE_QUEUE = "webrtc_call_candidate_queue";

	@Bean
	public Queue createChatMessagingQueue() {
		return QueueBuilder.durable(CHAT_MESSAGING_QUEUE).build();
	}

	@Bean
	public TopicExchange createIncomingExchange() {
		return ExchangeBuilder.topicExchange(INCOMING_EXCHANGE).build();
	}

	@Bean
	public TopicExchange createOutgoingExchange() {
		return ExchangeBuilder.topicExchange(OUTGOING_EXCHANGE).build();
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
                .to(createOutgoingExchange())
                .with("chat.message");
    }

    @Bean
    public Queue createTypingStatusQueue() {
        return QueueBuilder.durable(TYPING_STATUS_QUEUE).build();
    }

    @Bean
    public Binding createTypingStatusBinding() {
        return BindingBuilder.bind(createTypingStatusQueue())
                .to(createOutgoingExchange())
                .with("chat.typing.*");
    }

    @Bean
    public Queue createReadStatusQueue() {
        return QueueBuilder.durable(READ_STATUS_QUEUE).build();
    }

    @Bean
    public Binding createReadStatusBinding() {
        return BindingBuilder.bind(createReadStatusQueue())
                .to(createOutgoingExchange())
                .with("chat.read.*");
    }

    @Bean
    public Queue createWebRTCCallQueue() {
        return QueueBuilder.durable(WEBRTC_CALL_QUEUE).build();
    }

    @Bean
    public TopicExchange createWebRTCCallExchange() {
        return ExchangeBuilder.topicExchange(WEBRTC_CALL_EXCHANGE).build();
    }

    @Bean
    public Queue createWebRTCCallOfferQueue() {
        return QueueBuilder.durable(WEBRTC_CALL_OFFER_QUEUE).build();
    }

    @Bean
    public Queue createWebRTCCallAnswerQueue() {
        return QueueBuilder.durable(WEBRTC_CALL_ANSWER_QUEUE).build();
    }

    @Bean
    public Queue createWebRTCCallEndQueue() {
        return QueueBuilder.durable(WEBRTC_CALL_END_QUEUE).build();
    }

    @Bean
    public Queue createWebRTCCallCandidateQueue() {
        return QueueBuilder.durable(WEBRTC_CALL_CANDIDATE_QUEUE).build();
    }

    @Bean
    public Binding createWebRTCCallCandidateBinding() {
        return BindingBuilder.bind(createWebRTCCallCandidateQueue())
                .to(createWebRTCCallExchange())
                .with("webrtc.call.candidate");
    }

    @Bean
    public Binding createWebRTCCallOfferBinding() {
        return BindingBuilder.bind(createWebRTCCallOfferQueue())
                .to(createWebRTCCallExchange())
                .with("webrtc.call.offer");
    }

    @Bean
    public Binding createWebRTCCallAnswerBinding() {
        return BindingBuilder.bind(createWebRTCCallAnswerQueue())
                .to(createWebRTCCallExchange())
                .with("webrtc.call.answer");
    }

    @Bean
    public Binding createWebRTCCallEndBinding() {
        return BindingBuilder.bind(createWebRTCCallEndQueue())
                .to(createWebRTCCallExchange())
                .with("webrtc.call.end");
    }

	@Bean
	public MessageConverter jsonMessageConverter() {
	    Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
	    DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
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
